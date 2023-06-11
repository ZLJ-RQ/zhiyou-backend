package com.rq.zhiyou.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.rq.zhiyou.model.domain.User;
import com.rq.zhiyou.model.request.user.UserUpdatePasswordRequest;
import com.rq.zhiyou.model.vo.UserInfoVO;
import com.rq.zhiyou.model.vo.UserVO;
import com.rq.zhiyou.utils.ResultData;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 若倾
* @description 针对表【user(用户表)】的数据库操作Service
* @createDate 2023-01-11 21:45:20
*/
public interface UserService extends IService<User> {

    /**
     *  用户脱敏
     */
     User getSafetyUser(User user);
    /**
     * 用户注册
     * @param account 用户账号
     * @param password 用户密码
     * @param checkPassword 检查密码
     * @return 用户id
     */
    Long userRegister(String account,String password,String checkPassword);
    /**
     *  用户登录
     */
    User userLogin(String account, String password, HttpServletRequest request);
    /**
     *  用户登出
     */
    Integer userLogout(HttpServletRequest request);

    /**
     * 获取脱敏的用户信息
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

    /**
     *  根据标签查询用户信息
     */
    List<User> searchUserByTags(List<String> tagNameList);
    /**
     *  修改用户信息
     */
    boolean updateUser(User user,User loginUser);
    /**
     *  主页推荐用户列表
     */
    Page<User> recommendUsers(String username,long pageSize, long pageNum, HttpServletRequest request);

    /**
     *  (心动模式)匹配标签相似度高的用户
     */
    List<User> matchUsers(long num, User loginUser);
    /**
     *  查询好友列表
     */
    List<UserVO> searchFriends(String searchText, User loginUser);
    /**
     *  删除好友
     */
    boolean deleteFriends(Long id, User loginUser);
    /**
     *  获取用户详细信息
     */
    UserInfoVO getUserInfoById(long id,User loginUser);

    boolean updateUserPassword(UserUpdatePasswordRequest userUpdatePasswordRequest, User loginUser);
}
