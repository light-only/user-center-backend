package com.example.usercenter.model.domain.request.team;

import lombok.Data;

import java.io.Serializable;

@Data
public class TeamQuitRequest implements Serializable {
    private static final long serialVersionUID = 6010197493235628180L;

    /**
     * 队伍id
     */
    private long teamId;
}
