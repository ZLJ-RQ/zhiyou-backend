package com.rq.zhiyou.model.vo;

import com.rq.zhiyou.model.domain.User;
import lombok.Data;

import java.io.Serializable;

/**
 * @author 若倾
 * @description TODO
 */
@Data
public class FriendsVO implements Serializable {

    private static final long serialVersionUID = 1928465648232335L;

    private Long id;

    /**
     * 申请状态 默认0 （0-未通过 1-已同意 2-不同意）
     */
    private Integer status;

    /**
     * 好友申请备注信息
     */
    private String remark;

    /**
     * 申请用户
     */
    private UserVO applyUser;
}
