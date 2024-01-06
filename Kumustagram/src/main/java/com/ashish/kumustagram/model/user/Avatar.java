package com.ashish.kumustagram.model.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class Avatar {

    @NotBlank
    private String public_id;

    @NotBlank
    private String url;

    public Avatar(String public_id, String url) {
        this.public_id = public_id;
        this.url = url;
    }
}
