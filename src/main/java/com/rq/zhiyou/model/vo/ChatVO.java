package com.rq.zhiyou.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 若倾
 * @description TODO
 */
@Data
public class ChatVO implements Serializable {
    private static final long serialVersionUID = -4722378360550337925L;
    private WebSocketVO sendMsgUser;
    private Long teamId;
    private String text;
    private Boolean isMy = false;
    private Integer chatType;
    private Boolean isAdmin = false;
    private String createTime;
}
