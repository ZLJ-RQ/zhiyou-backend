package com.rq.zhiyou.model.request.team;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author 若倾
 * @version 1.0
 * @description TODO
 * @date 2023/5/21 22:34
 */
@Data
public class TeamUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * id
     */
    private Long id;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 队伍头像
     */
    private String teamAvatarUrl;

    /**
     * 描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 队伍公告
     */
    private String announce;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;

    /**
     * 密码
     */
    private String password;


}
