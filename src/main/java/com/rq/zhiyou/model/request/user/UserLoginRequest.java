package com.rq.zhiyou.model.request.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = 6547149604805212199L;

    private String account;

    private String password;

}
