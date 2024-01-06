package com.ashish.kumustagram.model.post;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Image {

    @NotNull
    private String public_id;

    @NotNull
    private String url;

    public Image(String public_id, String url){
        this.public_id = public_id;
        this.url = url;
    }

}
