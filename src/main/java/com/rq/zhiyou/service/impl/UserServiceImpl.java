package com.rq.zhiyou.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rq.zhiyou.common.StatusCode;
import com.rq.zhiyou.constant.UserConstant;
import com.rq.zhiyou.exception.BusinessException;
import com.rq.zhiyou.mapper.UserMapper;
import com.rq.zhiyou.model.domain.User;
import com.rq.zhiyou.model.enums.UserGenderEnum;
import com.rq.zhiyou.model.request.user.UserUpdatePasswordRequest;
import com.rq.zhiyou.model.vo.UserInfoVO;
import com.rq.zhiyou.model.vo.UserVO;
import com.rq.zhiyou.service.UserService;
import com.rq.zhiyou.utils.AlgorithmUtils;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.rq.zhiyou.constant.UserConstant.USER_LOGIN_STATE;


/**
* @author 若倾
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createDate 2023-01-11 21:45:20
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    //盐值
    private static final String SALT="rq";


    @Override
    public Long userRegister(String account, String password, String checkPassword) {
        //1.检验用户账户,密码,校验密码是否符合要求
        //非空判断
        if(StringUtils.isAllBlank(account,password,checkPassword))
            throw new BusinessException(StatusCode.PARAMS_ERROR,"参数为空");
        //账户长度不小于4位
        if (account.length()<4)
            throw new BusinessException(StatusCode.PARAMS_ERROR,"账号长度过短");
        //密码不小于8位
        if (password.length()<8||checkPassword.length()<8)
            throw new BusinessException(StatusCode.PARAMS_ERROR,"密码长度过短");
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(account);
        if (matcher.find())
            throw new BusinessException(StatusCode.PARAMS_ERROR,"账号包含特殊字符");
        //密码和校验密码要一致
        if (!password.equals(checkPassword))
            throw new BusinessException(StatusCode.PARAMS_ERROR,"密码和校验密码不一致");
        //查询数据库判断账户是否重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("account",account);
        long count = userMapper.selectCount(queryWrapper);
        if (count>0)
            throw new BusinessException(StatusCode.PARAMS_ERROR,"账号重复");
        //2.md5加盐加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes());
        //3.向数据库插入用户数据
        User user = new User();
        user.setAccount(account);
        user.setPassword(encryptPassword);
        user.setUsername(RandomUtil.randomString(8));
        Gson gson = new Gson();
        List<String> tagList = new ArrayList<>();
        tagList.add(UserGenderEnum.FEMALE.getGender());
        user.setTags(gson.toJson(tagList));
        boolean res = this.save(user);
        if (!res)
            throw new BusinessException(StatusCode.DATABASE_OPERATION_FAIL);
        return user.getId();
    }

    @Override
    public User userLogin(String account, String password, HttpServletRequest request) {

        //1.业务逻辑判断
        //非空判断
        if(StringUtils.isAllBlank(account,password))
            throw new BusinessException(StatusCode.PARAMS_ERROR,"参数为空");
        //账户长度不小于4位
        if (account.length()<4)
            throw new BusinessException(StatusCode.PARAMS_ERROR,"账号长度过短");
        //密码不小于8位
        if (password.length()<8)
            throw new BusinessException(StatusCode.PARAMS_ERROR,"密码长度过短");
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(account);
        if (matcher.find())
            throw new BusinessException(StatusCode.PARAMS_ERROR,"账号包含特殊字符");
        //2.查询用户是否存在
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes());
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("account",account);
        queryWrapper.eq("password",encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        if (user==null) {
            log.info("user login failed, userAccount Cannot match userPassword");
            throw new BusinessException(StatusCode.DATABASE_OPERATION_FAIL,"错误的账号或密码");
        }
        //3.用户脱敏
        User safetyUser = getSafetyUser(user);
        //4.记录用户的登录状态
        request.getSession().setAttribute(USER_LOGIN_STATE,safetyUser);
        return safetyUser;
    }

    @Override
    public Integer userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    @Override
    public User getSafetyUser(User user){
        if (user==null){
            throw new BusinessException(StatusCode.PARAMS_ERROR,"参数为空");
        }
        User safetyUser=new User();
        safetyUser.setId(user.getId());
        safetyUser.setUsername(user.getUsername());
        safetyUser.setAccount(user.getAccount());
        safetyUser.setAvatarUrl(user.getAvatarUrl());
        safetyUser.setGender(user.getGender());
        safetyUser.setEmail(user.getEmail());
        safetyUser.setStatus(user.getStatus());
        safetyUser.setPhone(user.getPhone());
        safetyUser.setCreateTime(user.getCreateTime());
        safetyUser.setUserRole(user.getUserRole());
        safetyUser.setTags(user.getTags());
        safetyUser.setProfile(user.getProfile());
        return safetyUser;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }
    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() <=0) {
            throw new BusinessException(StatusCode.NO_LOGIN);
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(StatusCode.NO_LOGIN);
        }
        return currentUser;
    }

    /**
     * 获取当前登录用户（允许未登录）
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUserPermitNull(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() <=0) {
            return null;
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        return this.getById(userId);
    }
    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return isAdmin(user);
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && UserConstant.ADMIN_ROLE==user.getUserRole();
    }

    /***
        根据标签搜索用户
    */
    @Override
    public List<User> searchUserByTags(List<String> tagNameList){
        //1.sql
//        if (CollectionUtils.isEmpty(tagNameList)){
//            throw  new BusinessException(StatusCode.PARAMS_ERROR);
//        }
//        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
//        for (String tagName : tagNameList) {
//            queryWrapper.like("tags",tagName);
//        }
//        List<User> list = userMapper.selectList(queryWrapper);
//        return list.stream().map(this::getSafetyUser).collect(Collectors.toList());
        //2.内存
        //2.1 先查询全部用户信息
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> userList = userMapper.selectList(queryWrapper);
        //2.2然后按照标签进行过滤
        return userList.stream().filter(user -> {
            String tagsStr = user.getTags();
            if (StringUtils.isBlank(tagsStr)){
                return false;
            }
            Gson gson = new Gson();
            Set<String> tempTagNameSet = gson.fromJson(tagsStr, new TypeToken<Set<String>>() {
            }.getType());
            tempTagNameSet=Optional.ofNullable(tempTagNameSet).orElse(new HashSet<>());
            for (String tagName : tagNameList) {
                if (!tempTagNameSet.contains(tagName)){
                    return false;
                }
            }
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());

    }

    @Override
    public boolean updateUser(User user,User loginUser) {
        long userId = user.getId();
        if (userId<=0){
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        }
        if (StringUtils.isNotBlank(user.getUsername())&&user.getUsername().length()>10){
            throw new BusinessException(StatusCode.PARAMS_ERROR,"昵称不能超过10个字");
        }
        String phoneValidPattern="^1(3[0-9]|5[0-3,5-9]|7[1-3,5-8]|8[0-9])\\d{8}$";
        if (StringUtils.isNotBlank(user.getPhone())&& !Pattern.compile(phoneValidPattern).matcher(user.getPhone()).find()){
            throw new BusinessException(StatusCode.PARAMS_ERROR,"手机号格式不符");
        }
        String emailValidPattern="^[A-Za-z0-9\\u4e00-\\u9fa5]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";
        if (StringUtils.isNotBlank(user.getEmail())&& !Pattern.compile(emailValidPattern).matcher(user.getEmail()).find()){
            throw new BusinessException(StatusCode.PARAMS_ERROR,"邮箱格式不符");
        }
        if (StringUtils.isNotBlank(user.getProfile())&&user.getProfile().length()>50){
            throw new BusinessException(StatusCode.PARAMS_ERROR,"描述不能超过50个字");
        }
        if (!isAdmin(loginUser)&& !(userId==loginUser.getId())){
            throw new BusinessException(StatusCode.NO_AUTH);
        }
        User oldUser = getById(userId);
        if (oldUser==null){
            throw new BusinessException(StatusCode.NULL_ERROR);
        }
        if (user.getGender()!=null){
            String tags = oldUser.getTags();
            Gson gson = new Gson();
            List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {
            }.getType());
            tagList.remove(UserGenderEnum.getEnumByValue(oldUser.getGender()).getGender());
            if (UserGenderEnum.FEMALE.getValue()==user.getGender()){
                tagList.add(UserGenderEnum.FEMALE.getGender());
            }else{
                tagList.add(UserGenderEnum.MALE.getGender());
            }
            user.setTags(gson.toJson(tagList));
        }
        return updateById(user);
    }

    @Override
    public Page<User> recommendUsers(String username,long pageSize, long pageNum, HttpServletRequest request) {
        User loginUser = getLoginUser(request);
        final String RECOMMEND_KEY="zhiyou:user:recommend:"+loginUser.getId();
        Page<User> userPage=null;
        if (StringUtils.isBlank(username)){
             userPage =(Page<User>) redisTemplate.opsForValue().get(RECOMMEND_KEY);
        }
        if (userPage!=null) {
            return userPage;
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(username),"username",username);
        userPage = page(new Page<>(pageNum,pageSize),queryWrapper);
        userPage.setRecords(userPage.getRecords().stream().map(user -> getSafetyUser(user)).collect(Collectors.toList()));
        try {
            if (StringUtils.isBlank(username)) {
                redisTemplate.opsForValue().set(RECOMMEND_KEY, userPage, 10, TimeUnit.MINUTES);
            }
        } catch (Exception e) {
            log.error("redis set key error",e);
        }
        return userPage;
    }

    @Override
    public List<User> matchUsers(long num, User loginUser) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id","tags");
        queryWrapper.isNotNull("tags");
        List<User> userList = this.list(queryWrapper);
        String tags = loginUser.getTags();
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {
        }.getType());
        //以下标为key,分数为值
        List<Pair<User,Long>> list=new ArrayList<>();
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            String userTags = user.getTags();
            if(StringUtils.isBlank(userTags)||user.getId()==loginUser.getId()){
                continue;
            }
            List<String> userTagList = gson.fromJson(userTags, new TypeToken<List<String>>() {
            }.getType());
            long distance = AlgorithmUtils.minDistance(tagList, userTagList);
            list.add(new Pair<>(user,distance));
        }
        List<Pair<User, Long>> pairList = list.stream().
                sorted((a, b) -> (int) (a.getValue() - b.getValue())).
                limit(num).collect(Collectors.toList());
        List<Long> userVOList = pairList.stream().
                map(pair->pair.getKey().getId()).collect(Collectors.toList());
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        String idStr = StringUtils.join(userVOList, ",");
        userQueryWrapper.in("id",userVOList).last("order by field(id,"+idStr+")");
        List<User> safetyUserList = this.list(userQueryWrapper).
                stream().map(this::getSafetyUser).collect(Collectors.toList());
        return safetyUserList;
    }

    @Override
    public List<UserVO> searchFriends(String searchText, User loginUser) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(searchText)){
            queryWrapper.like(User::getUsername,searchText);
        }
        String friendIds = loginUser.getFriendIds();
        if (StringUtils.isBlank(friendIds)){
            return new ArrayList<>();
        }
        Gson gson = new Gson();
        Set<Long> idsSet = gson.fromJson(friendIds, new TypeToken<Set<Long>>() {}.getType());
        if (CollectionUtil.isEmpty(idsSet)){
            return new ArrayList<>();
        }
        queryWrapper.in(User::getId,idsSet);
        List<User> list = list(queryWrapper);
        return list.stream().map(user -> {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user,userVO);
            return userVO;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean deleteFriends(Long id, User loginUser) {
        String friendIds = loginUser.getFriendIds();
        if (StringUtils.isBlank(friendIds)){
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        }
        Gson gson = new Gson();
        Set<Long> idsSet = gson.fromJson(friendIds, new TypeToken<Set<Long>>() {}.getType());
        idsSet.remove(id);
        loginUser.setFriendIds(gson.toJson(idsSet));
        //被删除的人也删除当前用户
        User user = getById(id);
        String userFriendIds = user.getFriendIds();
        if (StringUtils.isBlank(userFriendIds)){
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        }
        Set<Long> idsUserSet = gson.fromJson(userFriendIds, new TypeToken<Set<Long>>() {}.getType());
        idsUserSet.remove(loginUser.getId());
        user.setFriendIds(gson.toJson(idsUserSet));
        updateById(user);
        return updateById(loginUser);
    }

    @Override
    public UserInfoVO getUserInfoById(long id,User loginUser) {
        User user = getById(id);
        UserInfoVO userInfoVO = new UserInfoVO();
        BeanUtils.copyProperties(user,userInfoVO);
        String friendIds = loginUser.getFriendIds();
        if (StringUtils.isBlank(friendIds)){
            return userInfoVO;
        }
        Gson gson = new Gson();
        Set<Long> friendSet = gson.fromJson(friendIds, new TypeToken<Set<Long>>() {
        }.getType());
        if (friendSet.contains(id)){
            userInfoVO.setIsFriend(true);
        }else {
            userInfoVO.setIsFriend(false);
        }
        return userInfoVO;
    }

    @Override
    public boolean updateUserPassword(UserUpdatePasswordRequest userUpdatePasswordRequest, User loginUser) {
        //检验用户旧密码,新密码,校验密码是否符合要求
        String oldPassword = userUpdatePasswordRequest.getOldPassword();
        String newPassword = userUpdatePasswordRequest.getNewPassword();
        String checkPassword = userUpdatePasswordRequest.getCheckPassword();
        //非空判断
        if(StringUtils.isAllBlank(oldPassword,newPassword,checkPassword))
            throw new BusinessException(StatusCode.PARAMS_ERROR,"参数为空");
        //密码不小于8位
        if (newPassword.length()<8||oldPassword.length()<8||checkPassword.length()<8)
            throw new BusinessException(StatusCode.PARAMS_ERROR,"密码长度过短");
        //密码和校验密码要一致
        if (!newPassword.equals(checkPassword))
            throw new BusinessException(StatusCode.PARAMS_ERROR,"密码和校验密码不一致");
        User user = getById(userUpdatePasswordRequest.getId());
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + newPassword).getBytes());
        user.setPassword(encryptPassword);
        return updateById(user);
    }
}




