package com.rq.zhiyou.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rq.zhiyou.model.domain.Team;
import com.rq.zhiyou.model.domain.User;
import com.rq.zhiyou.model.request.team.*;
import com.rq.zhiyou.model.vo.UserTeamVO;
import com.rq.zhiyou.model.vo.UserVO;

import java.util.List;


/**
* @author 若倾
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2023-05-21 14:44:40
*/
public interface TeamService extends IService<Team> {

    /**
     *  添加队伍
     */
    long addTeam(Team team, User loginUser);

    /**
     *  展示队伍列表
     */
    List<UserTeamVO> listTeams(TeamQueryRequest teamQueryDto, boolean isAdmin,int state);

    /**
     *  修改队伍信息
     */
    boolean updateTeam(TeamUpdateRequest teamUpdateRequest,User loginUser);

    /**
     *  加入队伍
     */
    boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);

    /**
     *  退出队伍
     */
    boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

    /**
     *  解散队伍
     */
    boolean deleteTeam(Long id, User loginUser);

    /**
     *  获取队伍详细信息(包括队伍成员)
     */
    UserTeamVO getTeamById(long teamId,User loginUser);

    /**
     *  转移队伍
    */
    boolean transferTeam(TeamTransferRequest teamTransferRequest, User loginUser);
    /**
     *  获取队伍成员信息(除队长)
     */
//    List<UserVO> getTeamMemberById(Long teamId, User loginUser);
    /**
     *  踢出队伍
     */
    boolean removeTeam(TeamRemoveRequest teamRemoveRequest, User loginUser);
}
