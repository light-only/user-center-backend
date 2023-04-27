package com.example.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.usercenter.Mapper.UserMapper;
import com.example.usercenter.common.ErrorCode;
import com.example.usercenter.exception.BusinessException;
import com.example.usercenter.model.domain.User;
import com.example.usercenter.service.UserService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.example.usercenter.constant.UserConstant.ADMIN_ROLE;
import static com.example.usercenter.constant.UserConstant.USER_LOGIN_STATE;

/**
* @author Z
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createDate 2023-04-11 16:36:09
*/
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

    @Resource
    private UserMapper userMapper;

    private static final String SALT = "yupi";


    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword,String planetCode) {

        //1.校验
        if(StringUtils.isAnyBlank(userAccount,userPassword,checkPassword,planetCode)){
           throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        if(userAccount.length() <4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户账号长度小于4");
        }
        if(userPassword.length() <8 || checkPassword.length() <8 ){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户密码长度小于8");
        }

        if(planetCode.length() >5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"星球编码长度超过5");
        }

        //账户不能包含特殊字符
        String validPattern = "[\\n`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。， 、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if(matcher.find()){
            return -1;
        }



        //密码和校验密码相同
        if(!userPassword.equals(checkPassword)){
            return -1;
        }

        //账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount",userAccount);
        long count  = userMapper.selectCount(queryWrapper);
        if(count>0){
            return -1;
        }

        //星球编码不能重复
        queryWrapper = new QueryWrapper();
        queryWrapper.eq("planetCode",planetCode);
        count  = userMapper.selectCount(queryWrapper);
        if(count>0){
            return -1;
        }

        //加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        //插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setPlanetCode(planetCode);
        boolean saveResult =  this.save(user);
        if(!saveResult){
            return -1;
        }
        return user.getId();

    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //1.校验
        if(StringUtils.isAnyBlank(userAccount,userPassword)){
            return null;
        }
        if(userAccount.length() <4){
            return null;
        }
        if(userPassword.length() <8){
            return null;
        }

        //账户不能包含特殊字符
        String validPattern = "[\\n`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。， 、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if(matcher.find()){
            return null;
        }

        //加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        //查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount",userAccount);
        queryWrapper.eq("userPassword",encryptPassword);
        User user  = userMapper.selectOne(queryWrapper);

        //用户不存在
        if(user  == null){
            log.info("user login failed  userAccount Cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户不存在！");
        }

        //用户脱敏
        User safetyUser = getSatetyUser(user);



        //记录用户的登录状态
        request.getSession().setAttribute(USER_LOGIN_STATE,safetyUser);
        return safetyUser;


    }

    @Override
    public int userLogout(HttpServletRequest request) {
        //移除登录状态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    /**
     * 根据内存方式通过标签查找用户
     * @param tagNameList  用户拥有的标签
     * @return
     */
    @Override
    public List<User> searchUserTyTags(List<String> tagNameList){
        if(CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        //1.先查询所有的用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> userList = userMapper.selectList(queryWrapper);
        Gson gson = new Gson();

        //2.在内存中判断是否有符合要求的标签
        return userList.stream().filter(user->{
            String tagsStr = user.getTags();
            if(StringUtils.isBlank(tagsStr)){
                return false;
            }
            Set<String> tempTagNameSet = gson.fromJson(tagsStr,new TypeToken<Set<String>>(){}.getType());
//            tempTagNameSet = Optional.ofNullable(tempTagNameSet).orElse(new HashSet<>());
            for(String tagName:tagNameList){
                if(!tempTagNameSet.contains(tagName)){
                    return false;
                }
            }
            return true;

        }).map(this::getSatetyUser).collect(Collectors.toList());
    }

    /**
     * 根据SQL方法通过标签查找用户
      * @param tagNameList
     * @return
     */
    @Deprecated
    private List<User> searchUserTySQL(List<String> tagNameList){
        if(CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        //拼接and 查询
        //like  '%Java%' and like '%Python%'

        for(String tagName : tagNameList){
            queryWrapper = queryWrapper.like("tags",tagName);
        }
        List<User> userList = userMapper.selectList(queryWrapper);
        return userList.stream().map(this::getSatetyUser).collect(Collectors.toList());
    }

    /**
     * 用户脱敏
     * @param originUser
     * @return
     */
    @Override
    public User getSatetyUser(User originUser) {
        if(originUser == null){
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUserName(originUser.getUserName());
        safetyUser.setPlanetCode(originUser.getPlanetCode());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setTags(originUser.getTags());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setProfile(originUser.getProfile());
        return safetyUser;
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        if(request == null){
            return null;
        }
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if(userObj == null){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        return (User) userObj;
    }

    @Override
    public int updateUser(User user, User loginUser) {
        //获取要修改的用户id
        long userId = user.getId();
        if(userId <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //如何是管理员，允许更新任意用户
        //如果不是管理员，只允许更新自己的信息
        if(!isAdmin(loginUser) && userId != loginUser.getId()){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        User userRole = userMapper.selectById(userId);
        if(userRole == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return userMapper.updateById(user);
    }

    @Override
    public boolean isAdmin(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

    /**
     * 判断当前用户是否是管理员
     * @param loginUser
     * @return
     */
    private boolean isAdmin(User loginUser) {
        return loginUser != null && loginUser.getUserRole() == ADMIN_ROLE;
    }


}




