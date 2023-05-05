package com.example.usercenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.usercenter.model.domain.Team;
import com.example.usercenter.model.domain.User;
import com.example.usercenter.model.domain.dto.TeamQuery;
import com.example.usercenter.model.domain.request.team.TeamJoinRequest;
import com.example.usercenter.model.domain.request.team.TeamQuitRequest;
import com.example.usercenter.model.domain.request.team.TeamUpdateRequest;
import com.example.usercenter.model.domain.vo.TeamUserVo;

import java.util.List;

/**
* @author Z
* @description 针对表【team(队伍表)】的数据库操作Service
* @createDate 2023-05-04 15:23:10
*/
public interface TeamService extends IService<Team> {

    /**
     * 新增队伍
     * @param team
     * @param loginUser
     * @return
     */
    long addTeam(Team team, User loginUser);

    /**
     * 查询队伍
     * @param teamQuery
     * @param isAdmin
     * @return
     */
    List<TeamUserVo> listTeams(TeamQuery teamQuery,boolean isAdmin);

    /**
     * 更新队伍信息
     * @param team
     * @param loginUser
     * @return
     */
    boolean updateTeam(TeamUpdateRequest team, User loginUser);

    /**
     * 加入队伍
     * @param teamJoinRequest
     * @param loginUser
     * @return
     */
    boolean joinTeam(TeamJoinRequest teamJoinRequest,User loginUser);

    /**
     * 退出队伍
     * @param teamQuitRequest
     * @param loginUser
     * @return
     */
    boolean quitTeam(TeamQuitRequest teamQuitRequest,User loginUser);

    /**
     * 删除队伍
     * @param id
     * @param loginUser
     * @return
     */
    boolean deleteTeam(long id, User loginUser);
}
