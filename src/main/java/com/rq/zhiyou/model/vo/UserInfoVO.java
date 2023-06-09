package com.rq.zhiyou.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author 若倾
 * @description TODO
 */
@Data
public class UserInfoVO implements Serializable {
    /**
     *
     */
    @TableId(type = IdType.AUTO)
    private long id;

    /**
     * 用户昵称
     */
    private String username;

    /**
     * 账号
     */
    private String account;

    /**
     * 用户头像
     */
    private String avatarUrl;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 用户简介
     */
    private String profile;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 电话
     */
    private String phone;

    /**
     * 标签
     */
    private String tags;

    /**
     * 是否是好友
     */
    private Boolean isFriend;

    /**
     * 用户角色(0-普通用户 1-管理员)
     */
    private Integer userRole;


    private static final long serialVersionUID = 1L;
}
