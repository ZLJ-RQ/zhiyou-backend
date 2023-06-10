package com.rq.zhiyou.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rq.zhiyou.common.DeleteRequest;
import com.rq.zhiyou.common.StatusCode;
import com.rq.zhiyou.exception.BusinessException;
import com.rq.zhiyou.model.domain.Team;
import com.rq.zhiyou.model.domain.User;
import com.rq.zhiyou.model.domain.UserTeam;
import com.rq.zhiyou.model.request.team.*;
import com.rq.zhiyou.model.vo.UserTeamVO;
import com.rq.zhiyou.service.TeamService;
import com.rq.zhiyou.service.UserService;
import com.rq.zhiyou.service.UserTeamService;
import com.rq.zhiyou.utils.ResultData;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/team")
public class TeamController {

    @Resource
    private UserService userService;

    @Resource
    private TeamService teamService;

    @Resource
    private UserTeamService userTeamService;

    @PostMapping("/add")
    public ResultData<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request){
        if (teamAddRequest==null){
            throw new BusinessException(StatusCode.NULL_ERROR);
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest,team);
        User loginUser = userService.getLoginUser(request);
        long teamId = teamService.addTeam(team, loginUser);
        return ResultData.success(teamId);
    }
    //删除/解散队伍
    @PostMapping("/del")
    public ResultData<Boolean> deleteTeam(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request){
        Long id = deleteRequest.getId();
        if (id==null||id<=0){
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.deleteTeam(id,loginUser);
        if (!result){
            throw new BusinessException(StatusCode.SYSTEM_ERROR,"删除失败");
        }
        return ResultData.success(true);
    }

    @PostMapping("/update")
    public ResultData<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest,HttpServletRequest request){
        if (teamUpdateRequest==null){
            throw new BusinessException(StatusCode.NULL_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.updateTeam(teamUpdateRequest,loginUser);
        if (!result){
            throw new BusinessException(StatusCode.SYSTEM_ERROR,"修改失败");
        }
        return ResultData.success(true);
    }

    @GetMapping("/get")
    public ResultData<UserTeamVO> getTeamById(long teamId,HttpServletRequest request){
        if (teamId<=0){
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        UserTeamVO userTeamVO= teamService.getTeamById(teamId,loginUser);
        if (userTeamVO==null){
            throw new BusinessException(StatusCode.SYSTEM_ERROR,"查询队伍失败");
        }
        return ResultData.success(userTeamVO);
    }


    @GetMapping("/list")
    public ResultData<List<UserTeamVO>> listTeams(TeamQueryRequest teamQueryRequest, HttpServletRequest request){
        if (teamQueryRequest ==null){
            throw new BusinessException(StatusCode.NULL_ERROR);
        }
        boolean isAdmin = userService.isAdmin(request);
        List<UserTeamVO> teamList = teamService.listTeams(teamQueryRequest,isAdmin,0);
        List<Long> teamIdList = teamList.stream().map(UserTeamVO::getId).collect(Collectors.toList());
        if (CollectionUtil.isEmpty(teamIdList)){
            return ResultData.success(new ArrayList<UserTeamVO>());
        }
        QueryWrapper<UserTeam> userTeamQueryWrapper=new QueryWrapper<>();
        //判断当前用户是否加入队伍
        try {
            User loginUser = userService.getLoginUser(request);
            userTeamQueryWrapper.eq("user_id",loginUser.getId());
            userTeamQueryWrapper.in("team_id",teamIdList);
            List<UserTeam> list = userTeamService.list(userTeamQueryWrapper);
            Set<Long> hasJoinTeamIdList = list.stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
            teamList.forEach(team->{
                team.setHasJoin(hasJoinTeamIdList.contains(team.getId()));
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        //查询加入的用户数量

        QueryWrapper<UserTeam> userTeamJoinQueryWrapper=new QueryWrapper<>();
        userTeamJoinQueryWrapper.in("team_id",teamIdList);
        List<UserTeam> userTeamList = userTeamService.list(userTeamJoinQueryWrapper);
        //队伍id=>加入这个队伍的用户列表
        Map<Long, List<UserTeam>> teamIdUserTeamList = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        teamList.forEach(team->{
            team.setHasJoinNum(teamIdUserTeamList.getOrDefault(team.getId(),new ArrayList<>()).size());
        });
        return ResultData.success(teamList);
    }

    @GetMapping("/list/page")
    public ResultData<Page<Team>> listTeamsByPage(TeamQueryRequest teamQueryRequest){
        if (teamQueryRequest ==null){
            throw new BusinessException(StatusCode.NULL_ERROR);
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamQueryRequest,team);
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
        Page<Team> page = new Page<>(teamQueryRequest.getCurrent(), teamQueryRequest.getPageSize());
        Page<Team> resultPage = teamService.page(page, queryWrapper);
        return ResultData.success(resultPage);
    }

    @PostMapping("/join")
    public ResultData<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest,HttpServletRequest request){
        if (teamJoinRequest==null){
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result= teamService.joinTeam(teamJoinRequest,loginUser);
        return ResultData.success(result);
    }

    @PostMapping("/quit")
    public ResultData<Boolean> quitTeam(@RequestBody TeamQuitRequest teamQuitRequest, HttpServletRequest request){
        if (teamQuitRequest==null){
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result= teamService.quitTeam(teamQuitRequest,loginUser);
        return ResultData.success(result);
    }

    /***
     * @description 获取我创建的队伍
     * @param teamQueryRequest
     * @param request
     * @return com.rq.zhiyou.utils.ResultData<java.util.List<com.rq.zhiyou.model.vo.UserTeamVO>>
    */
    @GetMapping("/list/my/create")
    public ResultData<List<UserTeamVO>> listMyCreateTeams(TeamQueryRequest teamQueryRequest, HttpServletRequest request){
        if (teamQueryRequest ==null){
            throw new BusinessException(StatusCode.NULL_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        teamQueryRequest.setUserId(loginUser.getId());
        List<UserTeamVO> teamList = teamService.listTeams(teamQueryRequest,true,0);
        return ResultData.success(teamList);
    }

    /***
     * @description 获取我加入的队伍
     * @param teamQueryRequest
     * @param request
     * @return com.rq.zhiyou.utils.ResultData<java.util.List<com.rq.zhiyou.model.vo.UserTeamVO>>
     */
    @GetMapping("/list/my/join")
    public ResultData<List<UserTeamVO>> listMyJoinTeams(TeamQueryRequest teamQueryRequest, HttpServletRequest request){
        if (teamQueryRequest ==null){
            throw new BusinessException(StatusCode.NULL_ERROR);
        }
        boolean isAdmin = userService.isAdmin(request);
        User loginUser = userService.getLoginUser(request);
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("user_id",loginUser.getId());
        List<UserTeam> list = userTeamService.list(queryWrapper);
        //取出不重复的队伍id
        Map<Long, List<UserTeam>> listMap = list.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        ArrayList<Long> idList = new ArrayList<>(listMap.keySet());
        teamQueryRequest.setIdList(idList);
        List<UserTeamVO> teamList = teamService.listTeams(teamQueryRequest,isAdmin,1);
        teamList.forEach(team->{
            team.setHasJoin(true);
            team.setHasJoinNum(listMap.get(team.getId()).size());
        });
        return ResultData.success(teamList);
    }
}
