package com.example.usercenter.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * @TableName user
 */
@TableName(value ="user")
@Data
public class User implements Serializable {


    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;

    private String userName;

    private String userAccount;

    private String avatarUrl;

    private Integer gender;

    private String userPassword;

    private String email;

    private Integer userStatus;

    private String phone;

    private Date createTime;

    private Date updateTime;

    private Integer isDelete;

    private Integer userRole;

    private String planetCode;

    /**
     * 标签列表
     */
    private String tags;

    private String profile;

    private static final long serialVersionUID = 1L;
}