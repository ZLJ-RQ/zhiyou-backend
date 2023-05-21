package com.rq.zhiyou.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rq.zhiyou.mapper.UserTeamMapper;
import com.rq.zhiyou.model.domain.UserTeam;
import com.rq.zhiyou.service.UserTeamService;
import org.springframework.stereotype.Service;

/**
* @author 若倾
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2023-05-21 14:44:54
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService {

}




