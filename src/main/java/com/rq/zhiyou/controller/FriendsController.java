package com.rq.zhiyou.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.rq.zhiyou.common.StatusCode;
import com.rq.zhiyou.constant.FriendsConstant;
import com.rq.zhiyou.exception.BusinessException;
import com.rq.zhiyou.model.domain.User;
import com.rq.zhiyou.model.request.friends.FriendsAddRequest;
import com.rq.zhiyou.model.vo.FriendsVO;
import com.rq.zhiyou.model.vo.UserVO;
import com.rq.zhiyou.service.FriendsService;
import com.rq.zhiyou.service.UserService;
import com.rq.zhiyou.utils.ResultData;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;

/**
 * @author 若倾
 * @description 朋友申请管理
 */
@RestController
@RequestMapping("/friends")
public class FriendsController {

    @Resource
    private FriendsService friendsService;

    @Resource
    private UserService userService;

    @PostMapping("/add")
    public ResultData<Boolean> addFriend(@RequestBody FriendsAddRequest friendsAddRequest, HttpServletRequest request){
        if (friendsAddRequest == null) {
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result= friendsService.addFriend(friendsAddRequest,loginUser);
        return ResultData.success(result);
    }

    @PostMapping("/agree/{formId}")
    public ResultData<Boolean> agreeToApply(@PathVariable Long formId,HttpServletRequest request){
        if (formId==null||formId<0){
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result= friendsService.applyForFriends(formId,loginUser,true);
        return ResultData.success(result);
    }

    @PostMapping("/noagree/{formId}")
    public ResultData<Boolean> noAgreeToApply(@PathVariable Long formId,HttpServletRequest request){
        if (formId==null||formId<0){
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result= friendsService.applyForFriends(formId,loginUser,false);
        return ResultData.success(result);
    }

    @GetMapping("/list")
    public ResultData<List<UserVO>> friendsList(String username,HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        List<UserVO> userVOList= friendsService.friendsList(username,loginUser);
        return ResultData.success(userVOList);
    }

    @GetMapping("/receive/records")
    public ResultData<List<FriendsVO>> friendsReceiveRecords(HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        List<FriendsVO> friendsVOList= friendsService.friendsRecords(loginUser,FriendsConstant.RECEIVE);
        return ResultData.success(friendsVOList);
    }

    @GetMapping("/apply/records")
    public ResultData<List<FriendsVO>> friendsApplyRecords(HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        List<FriendsVO> friendsVOList= friendsService.friendsRecords(loginUser, FriendsConstant.APPLY);
        return ResultData.success(friendsVOList);
    }

    @GetMapping("/read")
    public ResultData<Boolean> toRead(@RequestParam("ids") Set<Long> ids, HttpServletRequest request){
        if (CollectionUtil.isEmpty(ids)){
            return ResultData.success(false);
        }
        User loginUser = userService.getLoginUser(request);
        boolean isRead = friendsService.toRead(loginUser, ids);
        return ResultData.success(isRead);
    }

    @GetMapping("/getRecordCount")
    public ResultData<Long> getRecordCount(HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        long count = friendsService.getRecordCount(loginUser);
        return ResultData.success(count);
    }
}
