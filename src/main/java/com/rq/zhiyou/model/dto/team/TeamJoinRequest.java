package com.rq.zhiyou.model.dto.team;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

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
