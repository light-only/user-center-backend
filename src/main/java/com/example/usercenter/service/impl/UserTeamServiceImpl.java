package com.example.usercenter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.usercenter.Mapper.UserTeamMapper;
import com.example.usercenter.model.domain.UserTeam;

import com.example.usercenter.service.UserTeamService;
import org.springframework.stereotype.Service;

/**
* @author Z
* @description 针对表【user_team(用户-队伍表)】的数据库操作Service实现
* @createDate 2023-05-04 15:18:50
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService {

}




