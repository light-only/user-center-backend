package com.example.usercenter.model.domain;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @TableName team
 */
@TableName(value ="team")
@Data
public class Team implements Serializable {

    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;

    private String name;

    private String description;

    private Integer maxNum;

    private Date expireTime;

    @JsonSerialize(using= ToStringSerializer.class)
    private Long userId;

    private Integer status;

    private String password;

    private Date createTime;

    private Date updateTime;

    @TableLogic
    private Integer isDelete;

    private static final long serialVersionUID = 1L;
}