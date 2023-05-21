package com.rq.zhiyou.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rq.zhiyou.common.StatusCode;
import com.rq.zhiyou.exception.BusinessException;
import com.rq.zhiyou.model.domain.User;
import com.rq.zhiyou.model.domain.request.UserLoginRequest;
import com.rq.zhiyou.model.domain.request.UserRegisterRequest;
import com.rq.zhiyou.service.UserService;
import com.rq.zhiyou.utils.ResultData;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static com.rq.zhiyou.constant.UserConstant.USER_LOGIN_STATE;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;


    /**
     * 注册
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/register")
    public ResultData<Long> UserRegister(@RequestBody UserRegisterRequest userRegisterRequest){
        if (userRegisterRequest==null)
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        String account = userRegisterRequest.getAccount();
        String password = userRegisterRequest.getPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(account, password, checkPassword))
            return null;
        Long result = userService.userRegister(account, password, checkPassword);
        return ResultData.success(result);

    }

    /**
     * 登录
     * @param userLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    public ResultData<User> UserLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request){
        if (userLoginRequest==null)
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        String account = userLoginRequest.getAccount();
        String password = userLoginRequest.getPassword();
        if (StringUtils.isAnyBlank(account, password))
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        User user = userService.userLogin(account, password, request);
        return ResultData.success(user);
    }

    /**
     * 登出
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public ResultData<Integer> UserLogout(HttpServletRequest request){
        if (request==null)
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        Integer i = userService.userLogout(request);
        return ResultData.success(i);
    }

    /**
     * 查询当前登录的用户信息
     * @param request
     * @return
     */
    @GetMapping("/current")
    public ResultData<User> getCurrentUser(HttpServletRequest request){
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser=(User) userObj;
        if (currentUser==null){
            throw new BusinessException(StatusCode.NO_LOGIN);
        }
        Long userId = currentUser.getId();
        User user = userService.getById(userId);
        User safetyUser = userService.getSafetyUser(user);
        return ResultData.success(safetyUser);
    }

    /**
     * 推荐用户列表
     * @param request
     * @return
     */
    @GetMapping("/recommend")
    public ResultData<Page<User>> recommendUsers(long pageSize,long pageNum,HttpServletRequest request){
        Page<User> userPage = userService.recommendUsers(pageSize, pageNum, request);
        return ResultData.success(userPage);
    }

    /**
     * 查询用户列表
     * @param username
     * @param request
     * @return
     */
    @GetMapping("/search")
    public ResultData<List<User>> searchUserList(String username, HttpServletRequest request){
        //鉴权,仅管理员可用
        if (!userService.isAdmin(request))
            throw new BusinessException(StatusCode.NO_AUTH);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.like(username!=null,"username",username);
        List<User> userList = userService.list(queryWrapper);
        List<User> list = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResultData.success(list);
    }

    /***
     * @description 根据标签查询用户
     * @param tagNameList
     * @return
    */
    @GetMapping("/search/tags")
    public ResultData<List<User>> searchUserByTags(@RequestParam(required = false) List<String> tagNameList){
        if (CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        }
        List<User> userList = userService.searchUserByTags(tagNameList);
        return ResultData.success(userList);
    }

    /**
     * 删除用户
     * @param id
     * @param request
     * @return
     */
    @PostMapping("/deleteUser")
    public ResultData<Boolean> deleteUser(long id, HttpServletRequest request){
        if (!userService.isAdmin(request))
            throw new BusinessException(StatusCode.NO_AUTH);
        if (id<=0)
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        boolean b = userService.removeById(id);
        return ResultData.success(b);
    }

    /**
     * 修改用户信息
     * @param user
     * @param request
     * @return
     */
    @PostMapping("/updateUser")
    public ResultData<Boolean> updateUser(@RequestBody User user, HttpServletRequest request){
        if (user==null)
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        int result = userService.updateUser(user,loginUser);
        return ResultData.success(result);
    }
}
