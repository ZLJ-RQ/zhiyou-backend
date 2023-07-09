package com.rq.zhiyou.discover;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.rq.zhiyou.utils.FutureUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author 若倾
 * @description TODO
 */
@Slf4j
@Component
public class PrioritizedUrlTitleDiscover extends AbstractUrlTitleDiscover{

    private final List<UrlTitleDiscover> urlTitleDiscovers=new ArrayList<>(2);
    private static final String PATTERN= "(https?://)?(www\\.[\\w-]+(\\.[\\w-]+)*)(:\\d+)?(/[^\\s]*)?";

    public PrioritizedUrlTitleDiscover() {
        urlTitleDiscovers.add(new CommonUrlTitleDiscover());
        urlTitleDiscovers.add(new WxUrlTitleDiscover());
    }

    @Override
    public String getDocTitle(Document document) {
        for (UrlTitleDiscover urlTitleDiscover : urlTitleDiscovers) {
            String urlTitle = urlTitleDiscover.getDocTitle(document);
            if (StrUtil.isNotBlank(urlTitle)){
                return urlTitle;
            }
        }
        return null;
    }

    @Override
    public Map<String, String> getContentTitleMap(String content) {
        if (StrUtil.isBlank(content)){
            return new HashMap<>();
        }
        List<String> matchList = ReUtil.findAll(PATTERN, content, 0);
        //并行请求
        //supplyAsync() 方法接受一个 Supplier 函数式接口作为参数，该接口定义了一个不接受参数但返回结果的方法。supplyAsync() 方法将该方法包装成一个 CompletableFuture 对象，并在后台线程上异步执行。
        List<CompletableFuture<Pair<String, String>>> futures = matchList.stream().map(match -> CompletableFuture.supplyAsync(() -> {
            String title = getUrlTitle(match);
            return StrUtil.isNotEmpty(title) ? Pair.of(match, title) : null;
        })).collect(Collectors.toList());
        CompletableFuture<List<Pair<String, String>>> future = FutureUtils.sequenceNonNull(futures);
        //结果组装,通过调用future.join()，您将阻塞当前线程直到future完成，并同步地获取最终结果
        return future.join().stream().collect(Collectors.toMap(Pair::getKey,Pair::getValue));
    }

    @Nullable
    @Override
    public String getUrlTitle(String url) {
        Document document = getUrlDocument(assemble(url));
        if (Objects.isNull(document)){
            return null;
        }
        return getDocTitle(document);
    }

    private String assemble(String url){
        if (!StrUtil.startWith(url,"http")){
            return "http://"+url;
        }
        return url;
    }

    protected Document getUrlDocument(String matchUrl){
        try {
            Connection connect = Jsoup.connect(matchUrl);
            //超时将抛出异常 熔断
            connect.timeout(1000);
            return connect.get();
        } catch (IOException e) {
            log.error("find title error:url{}",matchUrl,e);
        }
        return null;
    }
}
