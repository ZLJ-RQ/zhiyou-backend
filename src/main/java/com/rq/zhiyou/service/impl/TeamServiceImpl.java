package com.rq.zhiyou.service.impl;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rq.zhiyou.common.StatusCode;
import com.rq.zhiyou.exception.BusinessException;
import com.rq.zhiyou.mapper.TeamMapper;
import com.rq.zhiyou.model.domain.Team;
import com.rq.zhiyou.model.domain.User;
import com.rq.zhiyou.model.domain.UserTeam;
import com.rq.zhiyou.model.request.team.*;
import com.rq.zhiyou.model.enums.TeamStatusEnum;
import com.rq.zhiyou.model.vo.UserTeamVO;
import com.rq.zhiyou.model.vo.UserVO;
import com.rq.zhiyou.service.TeamService;
import com.rq.zhiyou.service.UserService;
import com.rq.zhiyou.service.UserTeamService;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.similarities.LambdaDF;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;




/**
* @author 若倾
* @description 针对表【team(队伍)】的数据库操作Service实现
* @createDate 2023-05-21 14:44:40
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService {

    @Resource
    private UserTeamService userTeamService;

    @Resource
    private UserService userService;

    @Resource
    private RedissonClient redissonClient;

    @Override
    @Transactional
    public long addTeam(Team team, User loginUser) {
//        1. 请求参数是否为空？
        if (team==null){
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        }
//        2. 是否登录，未登录不允许创建
        if (loginUser==null){
            throw new BusinessException(StatusCode.NO_LOGIN);
        }
        long userId = loginUser.getId();
//        3. 校验信息
//        a. 队伍人数 > 1 且 <= 20
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if (maxNum<=1||maxNum>20){
            throw new BusinessException(StatusCode.PARAMS_ERROR,"队伍人数不满足要求");
        }
        checkTeam(team);
//        g. 校验用户最多创建 5 个队伍,可能会出现用户多次点击,创建多个队伍,需要加锁
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        long hasTeamNum = count(queryWrapper);
        if (hasTeamNum>=5){
            throw new BusinessException(StatusCode.PARAMS_ERROR,"最多创建 5 个队伍");
        }
//        4. 插入队伍信息到队伍表
        team.setUserId(userId);
        boolean save = save(team);
        Long teamId = team.getId();
        if (!save||teamId==null){
            throw new BusinessException(StatusCode.SYSTEM_ERROR,"创建队伍失败");
        }
//        5. 插入用户 => 队伍关系到关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        boolean result = userTeamService.save(userTeam);
        if (!result){
            throw new BusinessException(StatusCode.SYSTEM_ERROR,"创建队伍失败");
        }
        return teamId;
    }

    private void checkTeam(Team team) {
        //        b. 队伍标题 <= 20
        String name = team.getName();
        if (StringUtils.isBlank(name)||name.length()>20){
            throw new BusinessException(StatusCode.PARAMS_ERROR,"队伍标题不满足要求");
        }
//        c. 描述 <= 50
        String description = team.getDescription();
        if (StringUtils.isNotBlank(description)&&description.length()>50){
            throw new BusinessException(StatusCode.PARAMS_ERROR,"队伍描述过长");
        }
        //       公告 <= 50
        String announce = team.getAnnounce();
        if (StringUtils.isNotBlank(announce)&&announce.length()>50){
            throw new BusinessException(StatusCode.PARAMS_ERROR,"队伍公告过长");
        }
//        d. status 是否公开（int）不传默认为 0（公开）
        int status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
        if (statusEnum==null){
            throw new BusinessException(StatusCode.PARAMS_ERROR,"队伍状态不满足要求");
        }
//        e. 如果 status 是加密状态，一定要有密码，且密码 <= 32
        String password = team.getPassword();
        if (TeamStatusEnum.SECRET.equals(statusEnum)){
            if (StringUtils.isBlank(password)||password.length()>32){
                throw new BusinessException(StatusCode.PARAMS_ERROR,"密码设置不正确");
            }
        }
//        f. 超时时间 > 当前时间
        Date expireTime = team.getExpireTime();
        if (new Date().after(expireTime)){
            throw new BusinessException(StatusCode.PARAMS_ERROR,"超时时间 > 当前时间");
        }
    }

    @Override
    public List<UserTeamVO> listTeams(TeamQueryRequest teamQueryRequest, boolean isAdmin,int state) {
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        if (teamQueryRequest !=null){
            Long id = teamQueryRequest.getId();
            if (id!=null&&id>0){
                queryWrapper.eq("id",id);
            }
            List<Long> idList = teamQueryRequest.getIdList();
            String idsStr = StringUtils.join(idList, ",");
            if (CollectionUtils.isNotEmpty(idList)){
                queryWrapper.in("id",idList).last("ORDER BY FIELD(id," + idsStr + ")");
            }
            String searchText = teamQueryRequest.getSearchText();
            if (StringUtils.isNotBlank(searchText)){
                queryWrapper.and(qw->qw.like("name",searchText).or().like("description",searchText));
            }
            String name = teamQueryRequest.getName();
            if (StringUtils.isNotBlank(name)){
                queryWrapper.like("name",name);
            }
            String description = teamQueryRequest.getDescription();
            if (StringUtils.isNotBlank(description)){
                queryWrapper.like("description",description);
            }
            Integer maxNum = teamQueryRequest.getMaxNum();
            if (maxNum!=null&&maxNum>0){
                queryWrapper.eq("max_num",maxNum);
            }
            Long userId = teamQueryRequest.getUserId();
            if (userId!=null&&userId>0){
                queryWrapper.eq("user_id",userId);
            }
            Integer status = teamQueryRequest.getStatus();
            if (state==1){
                queryWrapper.in("status",TeamStatusEnum.PUBLIC.getValue(),
                        TeamStatusEnum.PRIVATE.getValue(),TeamStatusEnum.SECRET.getValue());
            }else{
                TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
                if (statusEnum==null){
                    statusEnum=TeamStatusEnum.PUBLIC;
                }
                if (!isAdmin&&statusEnum.equals(TeamStatusEnum.PRIVATE)){
                    throw new BusinessException(StatusCode.NO_AUTH);
                }
                queryWrapper.eq("status",statusEnum.getValue());
            }

        }
        //查询未过期的和expire_time为空的
        queryWrapper.and(qw->qw.gt("expire_time",new Date()).or().isNull("expire_time"));
        List<Team> teamList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(teamList)){
            return new ArrayList<>();
        }
        List<UserTeamVO> userTeamVOList = new ArrayList<>();
        for (Team team : teamList) {
            Long userId = team.getUserId();
            if (userId==null){
                continue;
            }
            User user = userService.getById(userId);
            UserTeamVO userTeamVO = new UserTeamVO();
            BeanUtils.copyProperties(team,userTeamVO);
            //用户脱敏
            if (user!=null){
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(user,userVO);
                userTeamVO.setCreateUser(userVO);
            }
            userTeamVOList.add(userTeamVO);
        }
        return userTeamVOList;
    }

    @Override
    @Transactional
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest,User loginUser) {
        if (teamUpdateRequest==null){
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        }
        Team updateTeam = new Team();
        BeanUtils.copyProperties(teamUpdateRequest,updateTeam);
        checkTeam(updateTeam);
        //        3. 校验信息
//        a. 队伍人数 要大于当前队伍中人员数 并且不过大
        Integer maxNum = teamUpdateRequest.getMaxNum();
        LambdaQueryWrapper<UserTeam> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserTeam::getTeamId,teamUpdateRequest.getId());
        long count = userTeamService.count(queryWrapper);
        if (maxNum<count){
            throw new BusinessException(StatusCode.PARAMS_ERROR,"队伍人数不能小于已在队伍人数");
        }
        if (maxNum<=1||maxNum>20){
            throw new BusinessException(StatusCode.PARAMS_ERROR,"队伍人数不满足要求");
        }
        Long id = teamUpdateRequest.getId();
        Team oldTeam = getTeamById(id);
        //只有管理员和本人可以修改
        if (loginUser.getId()!=oldTeam.getUserId()&&!userService.isAdmin(loginUser)){
            throw new BusinessException(StatusCode.NO_AUTH);
        }
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(teamUpdateRequest.getStatus());
        if (statusEnum.equals(TeamStatusEnum.SECRET)){
            if (StringUtils.isBlank(teamUpdateRequest.getPassword())){
                throw new BusinessException(StatusCode.PARAMS_ERROR,"加密房间必须要设置密码");
            }
        }
        return this.updateById(updateTeam);
    }

    @Override
    public boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {
        if (teamJoinRequest==null){
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        }
        Long teamId = teamJoinRequest.getTeamId();
        Team team = getTeamById(teamId);
        Date expireTime = team.getExpireTime();
        if (expireTime !=null&& expireTime.before(new Date())){
            throw new BusinessException(StatusCode.PARAMS_ERROR,"队伍已过期");
        }
        Integer status = team.getStatus();
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
        if (TeamStatusEnum.PRIVATE.equals(statusEnum)){
            throw new BusinessException(StatusCode.PARAMS_ERROR,"禁止加入私有的队伍");
        }
        String password = teamJoinRequest.getPassword();
        if (TeamStatusEnum.SECRET.equals(statusEnum)) {
            if (StringUtils.isBlank(password)||!password.equals(team.getPassword())){
                throw new BusinessException(StatusCode.PARAMS_ERROR,"密码错误");
            }
        }
        //该用户已加入的队伍数量
        long userId = loginUser.getId();
        RLock lock = redissonClient.getLock("zhiyou:team:join:"+userId);
        try {
            while (true){
                boolean isLock = lock.tryLock(0, 30000L, TimeUnit.MICROSECONDS);
                if (isLock){
                    QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("user_id",userId);
                    long hasJoinNum = userTeamService.count(queryWrapper);
                    if (hasJoinNum>5){
                        throw new BusinessException(StatusCode.PARAMS_ERROR,"最多创建和加入五个队伍");
                    }
                    //不重复加入队伍
                    queryWrapper=new QueryWrapper<>();
                    queryWrapper.eq("user_id",userId);
                    queryWrapper.eq("team_id",teamId);
                    long hasUserJoinTeam = userTeamService.count(queryWrapper);
                    if (hasUserJoinTeam>0){
                        throw new BusinessException(StatusCode.PARAMS_ERROR,"用户已加入该队伍");
                    }
                    //队伍人数是否已满
                    queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("team_id",teamId);
                    long teamHasJoinNum = userTeamService.count(queryWrapper);
                    if (teamHasJoinNum>=team.getMaxNum()){
                        throw new BusinessException(StatusCode.NULL_ERROR,"队伍已满");
                    }
                    UserTeam userTeam = new UserTeam();
                    userTeam.setUserId(userId);
                    userTeam.setTeamId(teamId);
                    userTeam.setJoinTime(new Date());
                    return userTeamService.save(userTeam);
                }
            }
        } catch (InterruptedException e) {
            log.error("unlock error",e);
            return false;
        } finally {
            //释放自己的锁
            if (lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }
    }

    //根据id获取队伍
    private Team getTeamById(Long teamId) {
        if (teamId==null||teamId<0){
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        }
        Team team = this.getById(teamId);
        if (team==null){
            throw new BusinessException(StatusCode.NULL_ERROR,"队伍不存在");
        }
        return team;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
        if (teamQuitRequest==null){
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        }
        Long teamId = teamQuitRequest.getTeamId();
        Team team = getTeamById(teamId);
        //判断我是否在这个队伍或者是否是管理员
        long userId = loginUser.getId();
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        queryWrapper.eq("team_id",teamId);
        UserTeam userTeam = userTeamService.getOne(queryWrapper);
        if (userTeam==null&&!userService.isAdmin(loginUser)){
            throw new BusinessException(StatusCode.NULL_ERROR,"没有权限");
        }
        //如果队伍只剩一人,解散队伍
        QueryWrapper<UserTeam> qw=new QueryWrapper<>();
        qw.eq("team_id",teamId);
        qw.last("order by id asc limit 2");
        List<UserTeam> userTeamList = userTeamService.list(qw);
        if (CollectionUtils.isEmpty(userTeamList)){
            throw new BusinessException(StatusCode.SYSTEM_ERROR);
        }
        if (userTeamList.size()==1){
            //移除队伍
            boolean result = this.removeById(teamId);
            if (!result){
                throw new BusinessException(StatusCode.PARAMS_ERROR,"退出队伍失败");
            }
        }else {
            Long createUserId = team.getUserId();
            if (createUserId==userId){
                //移交队长给第二个人
                UserTeam userTeam1 = userTeamList.get(1);
                team.setUserId(userTeam1.getUserId());
                boolean result = this.updateById(team);
                if (!result){
                    throw new BusinessException(StatusCode.PARAMS_ERROR,"退出队伍失败");
                }
            }
        }
        //移除队伍关系
        return userTeamService.remove(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeam(Long id,User loginUser) {
        //1.判断队伍是否存在
        Team team=getTeamById(id);
        //2.校验是否是队长
        if (team.getUserId()!=loginUser.getId()){
            throw new BusinessException(StatusCode.NO_AUTH,"无权限");
        }
        //3.移除所有关联信息
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("team_id",id);
        boolean result = userTeamService.remove(queryWrapper);
        if (!result){
            throw new BusinessException(StatusCode.SYSTEM_ERROR,"删除失败");
        }
        //4.删除队伍
        return this.removeById(id);
    }

    @Override
    public UserTeamVO getTeamById(long teamId,User loginUser) {
        Team team = getById(teamId);
        UserTeamVO userTeamVO = BeanUtil.copyProperties(team, UserTeamVO.class);
        User userInfo = userService.getById(team.getUserId());
        UserVO userInfoVO = BeanUtil.copyProperties(userInfo, UserVO.class);
        userTeamVO.setCreateUser(userInfoVO);
        LambdaQueryWrapper<UserTeam> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserTeam::getTeamId,teamId);
        List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
        List<UserVO> userVOList = userTeamList.stream().map(userTeam -> {
            Long userId = userTeam.getUserId();
            if (userId==loginUser.getId()){
                userTeamVO.setHasJoin(true);
            }
            User user = userService.getById(userId);
            UserVO userVO = BeanUtil.copyProperties(user, UserVO.class);
            return userVO;
        }).collect(Collectors.toList());
        userTeamVO.setHasJoinNum(userVOList.size());
        userTeamVO.setTeamUserList(userVOList);
        return userTeamVO;
    }

    @Override
    public boolean transferTeam(TeamTransferRequest teamTransferRequest, User loginUser) {
        Long teamId = teamTransferRequest.getId();
        if (teamId==null||teamId<=0){
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        }
        Long userId = teamTransferRequest.getUserId();
        if (userId==null||userId<=0){
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        }
        //判断是否在转移队伍时候离队
        LambdaQueryWrapper<UserTeam> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserTeam::getTeamId,teamId);
        queryWrapper.eq(UserTeam::getUserId,userId);
        UserTeam userTeam = userTeamService.getOne(queryWrapper);
        if (userTeam==null){
            throw new BusinessException(StatusCode.PARAMS_ERROR,"该用户已不在队伍内");
        }
        Team team = getById(teamId);
        team.setUserId(userId);
        return updateById(team);
    }

//    @Override
//    public List<UserVO> getTeamMemberById(Long teamId, User loginUser) {
//        Team team = getById(teamId);
//        if (team.getUserId()!=loginUser.getId()){
//            throw new BusinessException(StatusCode.PARAMS_ERROR,"只有队长能操作");
//        }
//        LambdaQueryWrapper<UserTeam> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(UserTeam::getTeamId,teamId);
//        queryWrapper.ne(UserTeam::getUserId,loginUser.getId());
//        List<UserTeam> list = userTeamService.list(queryWrapper);
//        if (list.size()==0){
//            return new ArrayList<>();
//        }
//        return list.stream().map(userTeam -> {
//            User user = userService.getById(userTeam.getUserId());
//            UserVO userVO = BeanUtil.copyProperties(user, UserVO.class);
//            userVO.setTags("");
//            return userVO;
//        }).collect(Collectors.toList());
//    }

    @Override
    @Transactional
    public boolean removeTeam(TeamRemoveRequest teamRemoveRequest, User loginUser) {
        Long teamId = teamRemoveRequest.getId();
        if (teamId==null||teamId<=0){
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        }
        Long userId = teamRemoveRequest.getUserId();
        if (userId==null||userId<=0){
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        }
        Team team = getById(teamId);
        if (loginUser.getId()!=team.getUserId()){
            throw new BusinessException(StatusCode.PARAMS_ERROR,"只有队长能操作");
        }
        LambdaQueryWrapper<UserTeam> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserTeam::getTeamId,teamId);
        queryWrapper.eq(UserTeam::getUserId,userId);
        UserTeam userTeam = userTeamService.getOne(queryWrapper);
        if (userTeam==null){
            throw new BusinessException(StatusCode.PARAMS_ERROR,"该用户已不在队伍内");
        }
        return userTeamService.removeById(userTeam);
    }
}




