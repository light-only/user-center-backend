package com.example.usercenter.once;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 星球表格用户信息
 */
@Data
@EqualsAndHashCode
public class XingQiuTableUserInfo {
    /**
     * id
     */
    @ExcelProperty("成员编号")
    private String planetCode;
    /**
     * 用户昵称
     */
    @ExcelProperty("用户昵称")
    private String username;
}