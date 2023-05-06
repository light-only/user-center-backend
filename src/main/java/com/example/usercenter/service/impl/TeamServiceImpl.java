package com.example.usercenter.service.impl;

import com.alibaba.excel.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.usercenter.Mapper.TeamMapper;
import com.example.usercenter.common.ErrorCode;
import com.example.usercenter.exception.BusinessException;
import com.example.usercenter.model.domain.Team;
import com.example.usercenter.model.domain.User;
import com.example.usercenter.model.domain.UserTeam;
import com.example.usercenter.model.domain.dto.TeamQuery;
import com.example.usercenter.model.domain.enums.TeamStatusEnum;
import com.example.usercenter.model.domain.request.team.TeamJoinRequest;
import com.example.usercenter.model.domain.request.team.TeamQuitRequest;
import com.example.usercenter.model.domain.request.team.TeamUpdateRequest;
import com.example.usercenter.model.domain.vo.TeamUserVo;
import com.example.usercenter.model.domain.vo.UserVo;
import com.example.usercenter.service.TeamService;
import com.example.usercenter.service.UserService;
import com.example.usercenter.service.UserTeamService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
* @author Z
* @description 针对表【team(队伍表)】的数据库操作Service实现
* @createDate 2023-05-04 15:23:10
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService {

    @Resource
    private UserTeamService userTeamService;

    @Resource
    private UserService userService;

    /**
     * 创建队伍
     * @param team 队伍信息
     * @param loginUser 当前登录用户信息
     * @return teamId
     */
    @Override
    public long addTeam(Team team, User loginUser) {
        //请求参数是否为空
        if(team == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        //是否登录，未登录不允许创建
        if(loginUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        final long userId = loginUser.getId();
        //校验信息
        //队伍人数>1并且<=20
        Integer maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if(maxNum<1 || maxNum >20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍人数不符合要求");
        }
        //队伍标题<=20
        String name = team.getName();
        if(StringUtils.isBlank(name) || name.length()>20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍标题不满足要求");
        }
        //描述<=512
        String description = team.getDescription();
        if(StringUtils.isNotBlank(description) && description.length()>512){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍描述过长");
        }
        //status是否公开，不传默认是0
        int status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
        if(statusEnum == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍状态不满足要求");
        }


        //如何status是加密状态，一定要有密码，且密码小于等于32
        String password = team.getPassword();
        if(TeamStatusEnum.SECRET.equals(statusEnum)){
            if(StringUtils.isBlank(password) || password.length()>32){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码设置不正确");
            }
        }
        //超时时间>当前时间
        Date expireTime = team.getExpireTime();
        if(new Date().after(expireTime)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"超时时间>当前时间");
        }
        //检验用户最多创建5个队伍
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId",userId);
        long hasTeamNum = this.count(queryWrapper);
        if(hasTeamNum>5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户最多创建5个队伍");
        }

        //插入队伍信息到队伍表
        team.setId(null);
        team.setUserId(userId);
        boolean result = this.save(team);
        Long teamId = team.getId();
        if(!result || teamId == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"创建队伍失败");
        }
        //插入用户=》队伍关系到关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setTeamId(teamId);
        userTeam.setUserId(userId);
        userTeam.setJoinTime(new Date());
        result = userTeamService.save(userTeam);
        if(!result){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"加入队伍失败");
        }
        return teamId;
    }

    /**
     * 查询队伍列表
     * @param teamQuery 队伍查询条件包装类
     * @param isAdmin  当前登录是否是管理员
     * @return 返回队伍数组
     */
    @Override
    public List<TeamUserVo> listTeams(TeamQuery teamQuery, boolean isAdmin) {
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        //组合查询条件
        if(teamQuery != null){
            Long id = teamQuery.getId();
            if(id!=null && id>0){
                queryWrapper.eq("id",id);
            }
            List<Long> idList = teamQuery.getIdList();
            if(CollectionUtils.isNotEmpty(idList)){
                queryWrapper.eq("id",idList);
            }

            String searchText = teamQuery.getSearchText();
            if(StringUtils.isNotBlank(searchText)){
                queryWrapper.and(qw->qw.like("name",searchText).or().like("description",searchText));
            }
            String name = teamQuery.getName();
            if(StringUtils.isNotBlank(name)){
                queryWrapper.like("name",name);
            }
            String description = teamQuery.getDescription();
            if (StringUtils.isNotBlank(description)) {
                queryWrapper.like("description", description);
            }
            Integer maxNum = teamQuery.getMaxNum();
            if(maxNum!=null && maxNum>0){
                queryWrapper.eq("maxNum",maxNum);
            }

            Long userId = teamQuery.getUserId();
            // 根据创建人来查询
            if (userId != null && userId > 0) {
                queryWrapper.eq("userId", userId);
            }

            //根据状态来查询
            Integer status = teamQuery.getStatus();
            TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
            if(statusEnum == null){
                statusEnum = TeamStatusEnum.PUBLIC;
            }
            if(!isAdmin && statusEnum.equals(TeamStatusEnum.PRIVATE)){
                throw new BusinessException(ErrorCode.NO_AUTH);
            }
            queryWrapper.eq("status",statusEnum.getValue());
        }
        //不展示已过期的队伍
        queryWrapper.and(qw->qw.gt("expireTime",new Date())).or().isNull("expireTime");
        List<Team> teamList = this.list(queryWrapper);
        if(CollectionUtils.isEmpty(teamList)){
            return new ArrayList<>();
        }

        List<TeamUserVo> teamUserVoList = new ArrayList<>();
        //关联查询创建人信息
        for(Team team :teamList){
            Long userId = team.getUserId();
            if(userId == null){
                continue;
            }
            User user = userService.getById(userId);
            TeamUserVo teamUserVo = new TeamUserVo();
            BeanUtils.copyProperties(team,teamUserVo);
            //脱敏用户信息
            if(user != null){
                UserVo userVo = new UserVo();
                BeanUtils.copyProperties(team,userVo);
                teamUserVo.setCreateUser(userVo);
            }
            teamUserVoList.add(teamUserVo);
        }
        return teamUserVoList;
    }

    @Override
    public boolean updateTeam(TeamUpdateRequest team, User loginUser) {
        if(team == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = team.getId();
        if(id<0 || id == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team oldTeam = this.getById(id);
        if(oldTeam == null){
            throw new BusinessException(ErrorCode.NULL_ERROR,"队伍不存在");
        }
        //只有队伍的创建者或者管理员可以修改
        if(oldTeam.getUserId() != loginUser.getId() && !userService.isAdmin(loginUser)){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        //队伍如果是加密状态，必须要设置密码
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(team.getStatus());
        if(statusEnum.equals(TeamStatusEnum.SECRET)){
            if(StringUtils.isBlank(team.getPassword())){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍加密状态下，必须设置密码");
            }
        }
        Team updateTeam = new Team();
        BeanUtils.copyProperties(team,updateTeam);
        return this.updateById(updateTeam);
    }

    @Override
    public boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {
       if(teamJoinRequest == null){
           throw new BusinessException(ErrorCode.NULL_ERROR);
       }
       Long teamId = teamJoinRequest.getTeamId();
       if(teamId == null || teamId <0){
           throw new BusinessException(ErrorCode.PARAMS_ERROR);
       }
       Team team = this.getById(teamId);
       if(team == null){
           throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍不存在");
       }
       Date expireTime = team.getExpireTime();
       if(expireTime == null || expireTime.before(new Date())){
           throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍已过期");
       }
       TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(team.getStatus());
       if(statusEnum.equals(TeamStatusEnum.PRIVATE)){
           throw new BusinessException(ErrorCode.PARAMS_ERROR,"禁止加入私有队伍");
       }
       String password = teamJoinRequest.getPassword();
       if(TeamStatusEnum.SECRET.equals(statusEnum)){
           if(StringUtils.isBlank(password) || password!=team.getPassword()){
               throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍密码错误");
           }
       }
       //该用户已加入队伍数量
        long userId = loginUser.getId();
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("userId",userId);
        long hasJoinTeam = userTeamService.count(userTeamQueryWrapper);
        if(hasJoinTeam>5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"一个用户最多创建和加入5个队伍");
        }
        //不能重复加入已加入的队伍
        userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("userId",userId);
        userTeamQueryWrapper.eq("teamId",teamId);
        long hasJoinNum = userTeamService.count(userTeamQueryWrapper);
        if(hasJoinNum>0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户已加入该队伍");
        }

        //已加入队伍人数
        userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId",teamId);
        long teamHasJoinNum = userTeamService.count(userTeamQueryWrapper);
        if(teamHasJoinNum >= team.getMaxNum()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍人数已满");
        }
        //加入，修改队伍信息
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        return userTeamService.save(userTeam);
    }

    @Override
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
        if(teamQuitRequest == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        Long teamId = teamQuitRequest.getTeamId();
        if(teamId == null || teamId<0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = this.getById(teamId);
        if(team == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍不存在");
        }
        long userId = loginUser.getId();
        UserTeam queryTeam = new UserTeam();
        queryTeam.setTeamId(teamId);
        queryTeam.setUserId(userId);
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>(queryTeam);
        long count = userTeamService.count(queryWrapper);
        if(count == 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"未加入队伍");
        }

        //判断加入队伍的人数
        long teamHasJoinNum = this.countTeamByTeamId(teamId);
        if(teamHasJoinNum == 1){
            //删除队伍
            this.removeById(teamId);
        }else {
            //队伍至少还剩两个人
            //队长
            if(team.getUserId() == userId){
                //把队伍转交给最早加入队伍的用户
                //查询加入队伍的所有用户和加入时间
                QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
                userTeamQueryWrapper.eq("teamId",teamId);
                userTeamQueryWrapper.last("order by id asc limit 2");
                List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
                if(CollectionUtils.isEmpty(userTeamList) || userTeamList.size()<=1){
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }
                UserTeam nextUserTeam = userTeamList.get(1);
                Long nextUserId = nextUserTeam.getUserId();

                //更新当前队伍的队长
                Team updateTeam = new Team();
                updateTeam.setUserId(nextUserId);
                updateTeam.setId(teamId);
                boolean result = this.updateById(updateTeam);
                if(!result){
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新队伍队长失败");
                }
            }
        }
        //移除原来队长跟队伍的关系
        return userTeamService.remove(queryWrapper);
    }

    @Override
    public boolean deleteTeam(long id, User loginUser) {
       if(id<0){
           throw new BusinessException(ErrorCode.PARAMS_ERROR);
       }
       Team team = this.getById(id);
       if(team == null){
           throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍不存在");
       }
       long userId = loginUser.getId();
       if(team.getUserId() != userId){
           throw new BusinessException(ErrorCode.NO_AUTH);
       }
       //移除所有队伍的关联信息
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId",id);
        boolean result = userTeamService.remove(userTeamQueryWrapper);
        if(!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"删除队伍关联信息失败");
        }
        //删除队伍
        return this.removeById(id);

    }

    /**
     * 根据队伍id获取队伍人数。
     * @param teamId
     * @return
     */
    private long countTeamByTeamId(long teamId) {
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId",teamId);
        return userTeamService.count(userTeamQueryWrapper);
    }
}






