package com.rq.zhiyou.model.request.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 若倾
 * @description TODO
 */
@Data
public class UserUpdatePasswordRequest implements Serializable {
    private static final long serialVersionUID = -7620643864967860479L;
    private long id;
    private String oldPassword;
    private String newPassword;
    private String checkPassword;
}
