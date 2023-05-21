package com.rq.zhiyou.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rq.zhiyou.common.StatusCode;
import com.rq.zhiyou.exception.BusinessException;
import com.rq.zhiyou.mapper.TeamMapper;
import com.rq.zhiyou.model.domain.Team;
import com.rq.zhiyou.model.domain.User;
import com.rq.zhiyou.model.domain.UserTeam;
import com.rq.zhiyou.model.enums.TeamStatusEnum;
import com.rq.zhiyou.service.TeamService;
import com.rq.zhiyou.service.UserTeamService;
import java.util.Date;
import java.util.Optional;
import javax.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
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

    @Override
    @Transactional
    public synchronized long addTeam(Team team, User loginUser) {
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
//        b. 队伍标题 <= 20
        String name = team.getName();
        if (StringUtils.isBlank(name)||name.length()>20){
            throw new BusinessException(StatusCode.PARAMS_ERROR,"队伍标题不满足要求");
        }
//        c. 描述 <= 512
        String description = team.getDescription();
        if (StringUtils.isNotBlank(description)&&description.length()>512){
            throw new BusinessException(StatusCode.PARAMS_ERROR,"队伍描述过长");
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
            if (StringUtils.isBlank(password)||password.length()<=32){
                throw new BusinessException(StatusCode.PARAMS_ERROR,"密码设置不正确");
            }
        }
//        f. 超时时间 > 当前时间
        Date expireTime = team.getExpireTime();
        if (new Date().after(expireTime)){
            throw new BusinessException(StatusCode.PARAMS_ERROR,"超时时间 > 当前时间");
        }
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
        if (true){
            throw new BusinessException(StatusCode.SYSTEM_ERROR,"创建队伍失败");
        }
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
}




