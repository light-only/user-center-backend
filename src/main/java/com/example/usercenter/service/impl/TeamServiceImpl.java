package com.example.usercenter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.usercenter.Mapper.TeamMapper;
import com.example.usercenter.model.domain.Team;
import com.example.usercenter.service.TeamService;
import org.springframework.stereotype.Service;

/**
* @author Z
* @description 针对表【team(队伍表)】的数据库操作Service实现
* @createDate 2023-05-04 15:23:10
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService {

}




