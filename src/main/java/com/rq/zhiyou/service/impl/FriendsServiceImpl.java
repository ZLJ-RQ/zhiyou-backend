package com.rq.zhiyou.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rq.zhiyou.common.StatusCode;
import com.rq.zhiyou.constant.FriendsConstant;
import com.rq.zhiyou.exception.BusinessException;
import com.rq.zhiyou.mapper.FriendsMapper;
import com.rq.zhiyou.model.domain.Friends;
import com.rq.zhiyou.model.domain.User;
import com.rq.zhiyou.model.dto.friends.FriendsAddRequest;
import com.rq.zhiyou.model.vo.FriendsVO;
import com.rq.zhiyou.model.vo.UserVO;
import com.rq.zhiyou.service.FriendsService;
import com.rq.zhiyou.service.UserService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author 若倾
* @description 针对表【friends(好友申请管理表)】的数据库操作Service实现
* @createDate 2023-06-08 14:07:04
*/
@Service
public class FriendsServiceImpl extends ServiceImpl<FriendsMapper, Friends>
    implements FriendsService {

    @Resource
    private UserService userService;

    @Override
    public boolean addFriend(FriendsAddRequest friendsAddRequest, User loginUser) {
        Long receiveId = friendsAddRequest.getReceiveId();
        long userId = loginUser.getId();
        if (ObjectUtils.anyNull(receiveId,userId)){
            throw new BusinessException(StatusCode.PARAMS_ERROR,"添加失败");
        }
        if (receiveId==userId){
            throw new BusinessException(StatusCode.PARAMS_ERROR,"不能添加自己");
        }
        if (friendsAddRequest.getRemark().length()>50){
            throw new BusinessException(StatusCode.PARAMS_ERROR,"备注不能超过50字");
        }
        LambdaQueryWrapper<Friends> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Friends::getFromId,userId);
        queryWrapper.eq(Friends::getReceiveId,receiveId);
        long count = count(queryWrapper);
        if (count>0){
            throw new BusinessException(StatusCode.PARAMS_ERROR,"不能重复申请");
        }
        Friends friends = new Friends();
        friends.setFromId(userId);
        friends.setReceiveId(receiveId);
        if (StringUtils.isBlank(friendsAddRequest.getRemark())){
            friends.setRemark("我是"+loginUser.getUsername());
        }else {
            friends.setRemark(friendsAddRequest.getRemark());
        }
        return save(friends);
    }

    @Transactional
    @Override
    public boolean applyForFriends(Long formId, User loginUser, boolean status) {
        LambdaQueryWrapper<Friends> queryWrapper = new LambdaQueryWrapper<>();
        long userId = loginUser.getId();
        queryWrapper.eq(Friends::getReceiveId, userId);
        queryWrapper.eq(Friends::getFromId,formId);
        Friends friends = getOne(queryWrapper);
        if (friends==null){
            throw new BusinessException(StatusCode.PARAMS_ERROR,"该申请不存在");
        }
        if (friends.getStatus()!=FriendsConstant.DEFAULT_STATUS){
            throw new BusinessException(StatusCode.PARAMS_ERROR,"该申请已处理");
        }
        if (status){
            friends.setStatus(FriendsConstant.AGREE_STATUS);
            addFriends(formId, userId);
            addFriends(userId, formId);
        }else {
            friends.setStatus(FriendsConstant.NO_AGREE_STATUS);
        }
        return updateById(friends);
    }

    private void addFriends(Long formId, long userId) {
        User user = userService.getById(userId);
        String friendIds = user.getFriendIds();
        Gson gson = new Gson();
        Set<Long> idsSet = gson.fromJson(friendIds, new TypeToken<Set<String>>() {
        }.getType());
        idsSet.add(formId);
        user.setFriendIds(gson.toJson(idsSet));
        userService.updateById(user);
    }

    @Transactional
    @Override
    public List<UserVO> friendsList(String username, User loginUser) {
        long userId = loginUser.getId();
        LambdaQueryWrapper<Friends> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Friends::getReceiveId,userId).or().eq(Friends::getFromId,userId);
        queryWrapper.eq(Friends::getStatus,FriendsConstant.AGREE_STATUS);
        List<Friends> friendsList = list(queryWrapper);
        Set<Long> idsFromList = friendsList.stream().map(Friends::getFromId).collect(Collectors.toSet());
        Set<Long> idsReceiveList = friendsList.stream().map(Friends::getReceiveId).collect(Collectors.toSet());
        idsFromList.addAll(idsReceiveList);
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.ne("id",userId);
        userQueryWrapper.in("id",idsFromList);
        userQueryWrapper.like(username!=null,"username",username);
        return userService.list(userQueryWrapper).stream().map(user -> {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            return userVO;
        }).collect(Collectors.toList());
    }

    @Override
    public List<FriendsVO> friendsRecords(User loginUser, int status) {
        LambdaQueryWrapper<Friends> queryWrapper = new LambdaQueryWrapper<>();
        long userId = loginUser.getId();
        List<FriendsVO> friendsVOList = null;
        if (status==FriendsConstant.APPLY){
            queryWrapper.eq(Friends::getFromId,userId);
            List<Friends> friendsList = list(queryWrapper);
             friendsVOList = friendsList.stream().map(friends -> {
                 FriendsVO friendsVO = getFriendsVOById(friends,friends.getReceiveId());
                 return friendsVO;
            }).collect(Collectors.toList());
        }else if (status==FriendsConstant.RECEIVE){
            queryWrapper.eq(Friends::getReceiveId,userId);
            List<Friends> friendsList = list(queryWrapper);
             friendsVOList = friendsList.stream().map(friends -> {
                 FriendsVO friendsVO = getFriendsVOById(friends,friends.getFromId());
                return friendsVO;
            }).collect(Collectors.toList());
        }
        return friendsVOList;
    }

    //根据friends和id获取所需要的friendsVO
    private FriendsVO getFriendsVOById(Friends friends,Long id) {
        FriendsVO friendsVO = new FriendsVO();
        BeanUtils.copyProperties(friends, friendsVO);
        User user = userService.getById(id);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        friendsVO.setApplyUser(userVO);
        return friendsVO;
    }

    @Override
    public boolean toRead(User loginUser, Set<Long> ids) {
        LambdaQueryWrapper<Friends> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Friends::getReceiveId,loginUser.getId());
        queryWrapper.eq(Friends::getIsRead,0);
        queryWrapper.in(Friends::getFromId,ids);
        list(queryWrapper).forEach(friends -> {
            friends.setIsRead(1);
            updateById(friends);
        });
        return true;
    }

    @Override
    public long getRecordCount(User loginUser) {
        LambdaQueryWrapper<Friends> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Friends::getReceiveId,loginUser.getId());
        queryWrapper.eq(Friends::getIsRead,0);
        return count(queryWrapper);
    }


}




