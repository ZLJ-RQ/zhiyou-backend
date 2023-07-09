package com.rq.zhiyou.discover;

import org.jsoup.nodes.Document;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Map;

/**
 * @author 若倾
 * @description TODO
 */
public abstract class AbstractUrlTitleDiscover implements UrlTitleDiscover{

     @Override
     public Map<String, String> getContentTitleMap(String content) {
          return null;
     }

     @Nullable
     @Override
     public String getUrlTitle(String url) throws IOException {
          return null;
     }

     @Nullable
     @Override
     public String getDocTitle(Document document) {
          return null;
     }
}
