package com.ashish.kumustagram.model.post;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

@Data
public class Comment {

    @Id
    private String id;

    @NotNull
    private String user;

    @NotNull
    private String comment;

    public Comment(String user, String comment){
        this.id = ObjectId.get().toHexString();
        this.user= user;
        this.comment = comment;
    }
}
