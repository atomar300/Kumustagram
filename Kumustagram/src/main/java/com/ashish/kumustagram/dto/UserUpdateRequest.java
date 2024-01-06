package com.ashish.kumustagram.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateRequest {

    private String name;

    @Email
    private String email;

    private String avatar;

    @Size(min = 6, max = 20)
    private String oldPassword;

    @Size(min = 6, max = 20)
    private String newPassword;

}
