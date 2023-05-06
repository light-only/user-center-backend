package com.example.usercenter.model.domain.request.team;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

@Data
public class TeamQuitRequest implements Serializable {
    private static final long serialVersionUID = 6010197493235628180L;

    /**
     * 队伍id
     */
    @JsonSerialize(using= ToStringSerializer.class)
    private long teamId;
}
