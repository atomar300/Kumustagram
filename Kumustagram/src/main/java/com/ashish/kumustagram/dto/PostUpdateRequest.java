package com.ashish.kumustagram.dto;

import com.ashish.kumustagram.model.post.Comment;
import lombok.Data;

@Data
public class PostUpdateRequest {

    private String caption;
    private String comment;
    private String commentId;
}
