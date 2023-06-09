package com.rq.zhiyou.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.rq.zhiyou.model.domain.Friends;
import com.rq.zhiyou.model.domain.User;
import com.rq.zhiyou.model.dto.friends.FriendsAddRequest;
import com.rq.zhiyou.model.vo.FriendsVO;
import com.rq.zhiyou.model.vo.UserVO;

import java.util.List;
import java.util.Set;

/**
* @author 若倾
* @description 针对表【friends(好友申请管理表)】的数据库操作Service
* @createDate 2023-06-08 14:07:04
*/
public interface FriendsService extends IService<Friends> {

    /***
     * @description 加好友
     * @param friendsAddRequest
     * @param loginUser
     * @return boolean
    */
    boolean addFriend(FriendsAddRequest friendsAddRequest, User loginUser);

    /***
     * @description 审批好友
     * @param formId
     * @param loginUser
     * @param status true 同意 false 不同意
     * @return boolean
    */
    boolean applyForFriends(Long formId, User loginUser, boolean status);


    List<UserVO> friendsList(String userName, User loginUser);

    List<FriendsVO> friendsRecords(User loginUser, int status);

    boolean toRead(User loginUser, Set<Long> ids);

    long getRecordCount(User loginUser);
}
