package com.example.usercenter.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用的删除请求
 */
@Data
public class DeleteRequest implements Serializable {

    private static final long serialVersionUID = 4599826603200646629L;


    private long id;
}
