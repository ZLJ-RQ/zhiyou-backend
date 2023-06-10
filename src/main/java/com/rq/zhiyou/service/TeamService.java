package com.rq.zhiyou.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rq.zhiyou.model.domain.Team;
import com.rq.zhiyou.model.domain.User;
import com.rq.zhiyou.model.request.team.TeamJoinRequest;
import com.rq.zhiyou.model.request.team.TeamQueryRequest;
import com.rq.zhiyou.model.request.team.TeamQuitRequest;
import com.rq.zhiyou.model.request.team.TeamUpdateRequest;
import com.rq.zhiyou.model.vo.UserTeamVO;

import java.util.List;


/**
* @author 若倾
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2023-05-21 14:44:40
*/
public interface TeamService extends IService<Team> {

    long addTeam(Team team, User loginUser);

    List<UserTeamVO> listTeams(TeamQueryRequest teamQueryDto, boolean isAdmin,int state);

    boolean updateTeam(TeamUpdateRequest teamUpdateRequest,User loginUser);


    boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);

    boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

    boolean deleteTeam(Long id, User loginUser);

    UserTeamVO getTeamById(long teamId,User loginUser);
}
