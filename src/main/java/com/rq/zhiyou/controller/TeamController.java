package com.rq.zhiyou.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rq.zhiyou.common.DeleteRequest;
import com.rq.zhiyou.common.StatusCode;
import com.rq.zhiyou.exception.BusinessException;
import com.rq.zhiyou.model.domain.Team;
import com.rq.zhiyou.model.domain.User;
import com.rq.zhiyou.model.dto.team.TeamAddRequest;
import com.rq.zhiyou.model.dto.team.TeamQueryDto;
import com.rq.zhiyou.service.TeamService;
import com.rq.zhiyou.service.UserService;
import com.rq.zhiyou.utils.ResultData;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/team")
public class TeamController {

    @Resource
    private UserService userService;

    @Resource
    private TeamService teamService;

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

    @PostMapping("/del")
    public ResultData<Boolean> delTeam(@RequestBody DeleteRequest deleteRequest){
        Long id = deleteRequest.getId();
        if (id<=0){
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        }
        boolean result = teamService.removeById(id);
        if (!result){
            throw new BusinessException(StatusCode.SYSTEM_ERROR,"删除失败");
        }
        return ResultData.success(true);
    }

    @PostMapping("/update")
    public ResultData<Boolean> updateTeam(@RequestBody Team team){
        if (team==null){
            throw new BusinessException(StatusCode.NULL_ERROR);
        }
        boolean result = teamService.updateById(team);
        if (!result){
            throw new BusinessException(StatusCode.SYSTEM_ERROR,"修改失败");
        }
        return ResultData.success(true);
    }

    @GetMapping("/get")
    public ResultData<Team> getTeamById(long id){
        if (id<=0){
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        }
        Team team = teamService.getById(id);
        if (team==null){
            throw new BusinessException(StatusCode.SYSTEM_ERROR,"查询队伍失败");
        }
        return ResultData.success(team);
    }

    @GetMapping("/list")
    public ResultData<List<Team>> listTeams( TeamQueryDto teamQueryDto){
        if (teamQueryDto==null){
            throw new BusinessException(StatusCode.NULL_ERROR);
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamQueryDto,team);
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
        List<Team> teamList = teamService.list(queryWrapper);
        return ResultData.success(teamList);
    }

    @GetMapping("/list/page")
    public ResultData<Page<Team>> listTeamsByPage(TeamQueryDto teamQueryDto){
        if (teamQueryDto==null){
            throw new BusinessException(StatusCode.NULL_ERROR);
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamQueryDto,team);
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
        Page<Team> page = new Page<>(teamQueryDto.getCurrent(), teamQueryDto.getPageSize());
        Page<Team> resultPage = teamService.page(page, queryWrapper);
        return ResultData.success(resultPage);
    }
}
