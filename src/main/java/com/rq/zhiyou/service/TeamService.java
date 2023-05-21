package com.rq.zhiyou.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rq.zhiyou.model.domain.Team;
import com.rq.zhiyou.model.domain.User;
import com.rq.zhiyou.model.dto.team.TeamJoinRequest;
import com.rq.zhiyou.model.dto.team.TeamQueryDTO;
import com.rq.zhiyou.model.dto.team.TeamUpdateRequest;
import com.rq.zhiyou.model.vo.UserTeamVO;

import java.util.List;


/**
* @author 若倾
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2023-05-21 14:44:40
*/
public interface TeamService extends IService<Team> {

    long addTeam(Team team, User loginUser);

    List<UserTeamVO> listTeams(TeamQueryDTO teamQueryDto, boolean isAdmin);

    boolean updateTeam(TeamUpdateRequest teamUpdateRequest,User loginUser);


    boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);
}
