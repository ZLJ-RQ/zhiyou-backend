package com.rq.zhiyou.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author 若倾
 * @version 1.0
 * @description TODO
 * @date 2023/4/3 20:45
 */
@Data
public class CommentVO  implements Serializable {

    /**
     * 评论id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 帖子id
     */
    private Long postId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 创建人信息
     */
    private UserVO user;

}
