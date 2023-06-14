package com.rq.zhiyou.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户视图（脱敏）
 *

 */
@Data
public class UserVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String username;

    /**
     * 用户头像
     */
    private String avatarUrl;

    /**
     * 用户标签
     */
    private String tags;

    /**
     * 用户简介
     */
    private String profile;

    /**
     * 用户角色：user/admin/ban
     */
    private Integer userRole;


    private static final long serialVersionUID = 1L;
}