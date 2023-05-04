package com.example.usercenter.common;

import lombok.Data;

import java.io.Serializable;

@Data
public class PageRequest implements Serializable {
    private static final long serialVersionUID = 1395844225639844641L;

    /**
     * 页面大小
     */
    private int pageSize;

    /**
     * 当前是第几页
     */
    private int pageNum;
}
