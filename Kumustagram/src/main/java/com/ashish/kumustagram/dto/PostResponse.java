package com.ashish.kumustagram.dto;

import com.ashish.kumustagram.model.post.Post;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PostResponse {

    private boolean success = true;
    private String message;
    private List<Post> posts = new ArrayList<>();

}
