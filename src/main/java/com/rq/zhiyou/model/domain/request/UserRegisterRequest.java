package com.rq.zhiyou.model.domain.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = 6547149604805212199L;

    private String account;

    private String password;

    private String checkPassword;
}
