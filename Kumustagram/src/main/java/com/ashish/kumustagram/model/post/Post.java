package com.ashish.kumustagram.model.post;

import com.ashish.kumustagram.model.user.User;
import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "posts")
public class Post {

    @Id
    private String id;

    private String caption;

    @Valid
    private Image image;

    private String owner;

    private LocalDateTime createdAt = LocalDateTime.now();

    private List<String> likes = new ArrayList<>();

    @Valid
    private List<Comment> comments = new ArrayList<>();


    public Post(String caption, Image image, String owner) {
        this.caption = caption;
        this.image = image;
        this.owner = owner;
    }
}

