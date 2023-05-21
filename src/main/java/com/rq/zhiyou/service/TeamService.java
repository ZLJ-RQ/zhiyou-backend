package com.rq.zhiyou.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rq.zhiyou.model.domain.Team;
import com.rq.zhiyou.model.domain.User;


/**
* @author 若倾
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2023-05-21 14:44:40
*/
public interface TeamService extends IService<Team> {

    long addTeam(Team team, User loginUser);

}
