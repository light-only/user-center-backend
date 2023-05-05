package com.example.usercenter.model.domain.request.team;

import lombok.Data;

import java.io.Serializable;

@Data
public class TeamJoinRequest implements Serializable {
    private static final long serialVersionUID = -4482662439037223087L;

    /**
     * 队伍id
     */
    private Long teamId;

    /**
     * 队伍密码
     */
    private String password;
}
