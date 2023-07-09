package com.rq.zhiyou.discover;

import org.jsoup.nodes.Document;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Map;

/**
 * @author 若倾
 * @description TODO
 */
public interface UrlTitleDiscover {

    Map<String,String> getContentTitleMap(String content);

    @Nullable
    String getUrlTitle(String url) throws IOException;

    @Nullable
    String getDocTitle(Document document);
}
