package com.rq.zhiyou.model.request.team;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 若倾
 * @version 1.0
 * @description TODO
 * @date 2023/5/22 15:18
 */
@Data
public class TeamQuitRequest implements Serializable {
    /**
     * teamId
     */
    private Long teamId;


    private static final long serialVersionUID = 1L;
}
