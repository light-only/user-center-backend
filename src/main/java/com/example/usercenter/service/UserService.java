package com.example.usercenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.usercenter.model.domain.User;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author Z
* @description 针对表【user(用户表)】的数据库操作Service
* @createDate 2023-04-11 16:36:09
*/
public interface UserService extends IService<User> {


    /**
     *用户注册
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param checkPassword 密码校验
     * @return  新用户id
     */
    long userRegister(String userAccount,String userPassword,String checkPassword,String planetCode);

    /**
     * 用户登录
     * @param userAccount  用户账号
     * @param userPassword 用户密码
     * @param request
     * @return 返回用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     *用户注销
     * @param request
     * @return
     */
    int userLogout(HttpServletRequest request);


    /**
     * 根据标签搜索用户
     * @param tagNameList
     * @return
     */
    List<User> searchUserTyTags(List<String> tagNameList);

    /**
     * 用户脱敏
     * @param originUser
     * @return
     */

    /**
     * 返回脱敏的用户信息方法
     * @param originUser
     * @return
     */
    User getSatetyUser(User originUser);

    /**
     * 返回登录的当前用户信息
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 更新用户信息
     * @param user
     * @param loginUser
     * @return
     */
    int updateUser(User user, User loginUser);

    /**
     * 是否是管理员角色
     * @param request 根据当前的请求
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 是否是管理员
     * @param loginUser 根据当前用户信息判断
     * @return
     */
    boolean isAdmin(User loginUser);
}
