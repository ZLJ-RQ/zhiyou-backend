package com.rq.zhiyou.model.request.team;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 若倾
 * @description TODO
 */
@Data
public class TeamTransferRequest implements Serializable {

    private Long id;

    private Long userId;

}
