package com.rq.zhiyou.model.dto.comment;

import lombok.Data;

import java.io.Serializable;

/**
 * 创建请求
 *

 */
@Data
public class CommentAddRequest implements Serializable {

    /**
     * 帖子id
     */
    private Long postId;

    /**
     * 评论内容
     */
    private String content;


    private static final long serialVersionUID = 1L;
}