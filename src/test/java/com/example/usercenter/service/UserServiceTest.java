package com.example.usercenter.service;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.example.usercenter.model.domain.User;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UserServiceTest {

    @Resource
    private UserService userService;

    @Test
    public void testAddUser(){
        User user = new User();
        user.setUserName("dogyupi");
        user.setUserAccount("123");
        user.setAvatarUrl("https://cdn.nlark.com/yuque/0/2022/png/25430380/1647165604019-25aec295-f5ab-4d32-be18-bb3f50240e8c.png");
        user.setGender(0);
        user.setUserPassword("123456");
        user.setEmail("123");
        user.setUserStatus(0);
        user.setPhone("456");

        boolean result = userService.save(user);
        System.out.println(user.getId());
        Assertions.assertTrue(result);
    }

    @Test
    void userRegister() {
        String userAccount = "yupi";
        String userPassword = "";
        String checkPassword = "123456";
        String planetCode = "1";
        long result = userService.userRegister(userAccount,userPassword,checkPassword,planetCode);
        Assertions.assertEquals(-1,result);

        userAccount = "yu";
        result = userService.userRegister(userAccount,userPassword,checkPassword,planetCode);
        Assertions.assertEquals(-1,result);

        userAccount = "yupi";
        userPassword = "123456";
        result = userService.userRegister(userAccount,userPassword,checkPassword,planetCode);
        Assertions.assertEquals(-1,result);

        userAccount = "yu pi";
        userPassword = "12345678";
        result = userService.userRegister(userAccount,userPassword,checkPassword,planetCode);
        Assertions.assertEquals(-1,result);

        checkPassword = "123456789";
        result = userService.userRegister(userAccount,userPassword,checkPassword,planetCode);
        Assertions.assertEquals(-1,result);

        userAccount = "dogyupi";
        checkPassword = "12345678";
        result = userService.userRegister(userAccount,userPassword,checkPassword,planetCode);
        Assertions.assertEquals(-1,result);

        userAccount = "yupi";
        result = userService.userRegister(userAccount,userPassword,checkPassword,planetCode);
        Assertions.assertTrue(result > 0);
    }

    @Test
    void searchUserTyTags() {
        List<String> tagNameList = Arrays.asList("java","python");
        List<User> userList = userService.searchUserTyTags(tagNameList);
        Assert.assertNotNull(userList);
    }
}