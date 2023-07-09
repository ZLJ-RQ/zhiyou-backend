package com.rq.zhiyou.model.request.tag;


import lombok.Data;

import java.io.Serializable;

/**
 * @author 若倾
 * @description TODO
 */
@Data
public class TagAddRequest implements Serializable {

    /**
     * 标签名称
     */
    private String tagName;

    /**
     * 父标签 id
     */
    private Long parentId;
}
