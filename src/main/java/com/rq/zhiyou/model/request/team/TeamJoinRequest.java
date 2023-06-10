package com.rq.zhiyou.model.request.team;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 若倾
 * @version 1.0
 * @description TODO
 * @date 2023/5/21 23:02
 */
@Data
public class TeamJoinRequest implements Serializable {

    /**
     * teamId
     */
    private Long teamId;

    /**
     * 密码
     */
    private String password;

    private static final long serialVersionUID = 1L;
}
