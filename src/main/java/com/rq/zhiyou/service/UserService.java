package com.rq.zhiyou.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.rq.zhiyou.model.domain.User;
import com.rq.zhiyou.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 若倾
* @description 针对表【user(用户表)】的数据库操作Service
* @createDate 2023-01-11 21:45:20
*/
public interface UserService extends IService<User> {

     User getSafetyUser(User user);
    /**
     * 用户注册
     * @param account 用户账号
     * @param password 用户密码
     * @param checkPassword 检查密码
     * @return 用户id
     */
    Long userRegister(String account,String password,String checkPassword);

    User userLogin(String account, String password, HttpServletRequest request);

    Integer userLogout(HttpServletRequest request);

    /**
     * 获取脱敏的用户信息
     *
     * @param user
     * @return
     */
    UserVO getUserVO(User user);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 获取当前登录用户（允许未登录）
     *
     * @param request
     * @return
     */
    User getLoginUserPermitNull(HttpServletRequest request);
    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param user
     * @return
     */
    boolean isAdmin(User user);

    List<User> searchUserByTags(List<String> tagNameList);

    int updateUser(User user,User loginUser);

    Page<User> recommendUsers(long pageSize, long pageNum, HttpServletRequest request);
}
