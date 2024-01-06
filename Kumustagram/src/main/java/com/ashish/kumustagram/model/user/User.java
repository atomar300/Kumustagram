package com.ashish.kumustagram.model.user;

import com.ashish.kumustagram.model.post.Post;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "users")
public class User {

    @Id
    private String id;

    private String name;

    @Valid
    private Avatar avatar;

    private String email;

    @JsonIgnore
    private String password;

    @DBRef
    private List<Post> posts = new ArrayList<>();

    private List<String> followers = new ArrayList<>();

    private List<String> following = new ArrayList<>();

    private String resetPasswordToken;

    private LocalDateTime resetPasswordExpire;

    public User(String name, String email, String password, Avatar avatar) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.avatar = avatar;
    }
}
