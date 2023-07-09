package com.rq.zhiyou.discover;

import org.jsoup.nodes.Document;

/**
 * @author 若倾
 * @description TODO
 */
public class CommonUrlTitleDiscover extends AbstractUrlTitleDiscover{
    @Override
    public String getDocTitle(Document document) {
        return document.title();
    }
}
