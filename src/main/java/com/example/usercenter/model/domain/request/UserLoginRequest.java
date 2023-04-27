package com.example.usercenter.model.domain.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserLoginRequest implements Serializable {
    private static final long serialVersionUID = -543286974259314538L;
    private String userAccount;

    private String userPassword;

}
