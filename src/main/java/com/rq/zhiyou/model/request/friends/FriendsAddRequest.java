package com.rq.zhiyou.model.request.friends;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 若倾
 * @description TODO
 */
@Data
public class FriendsAddRequest implements Serializable {

    private Long id;

    /***
     接收申请人id
    */
    private Long receiveId;
    /***
     备注
     */
    private String remark;
}
