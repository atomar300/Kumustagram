package com.ashish.kumustagram.dto;

import com.ashish.kumustagram.model.user.User;
import lombok.*;

import java.util.List;

@Data
public class UserResponse {

    // default value is true
    private boolean success = true;

    private String message;

    private User user;

    private List<User> users;

    private String token;


}
