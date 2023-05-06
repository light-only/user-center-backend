package com.example.usercenter.model.domain.dto;

import com.example.usercenter.common.PageRequest;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.List;

@Data
public class TeamQuery extends PageRequest {
    /**
     * id
     */

    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 用户id
     */

    @JsonSerialize(using= ToStringSerializer.class)
    private Long userId;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;


    /**
     * id列表
     */
    @JsonSerialize(using= ToStringSerializer.class)
    private List<Long> idList;

    /**
     * 搜索关键词（同时对队伍名称和描述搜索）
     */
    private String searchText;
}
