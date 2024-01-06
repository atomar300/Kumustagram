package com.ashish.kumustagram.service;


import com.ashish.kumustagram.exception.PostNotFoundException;
import com.ashish.kumustagram.exception.UserNotFoundException;
import com.ashish.kumustagram.model.post.Image;
import com.ashish.kumustagram.model.post.Post;
import com.ashish.kumustagram.model.user.Avatar;
import com.ashish.kumustagram.model.user.User;
import com.ashish.kumustagram.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.bind.DatatypeConverter;
import java.util.Map;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;


    @Autowired
    private CloudinaryImageService cloudinaryImageService;


    public Image processImage(String base64) {
        byte[] data = DatatypeConverter.parseBase64Binary(base64.split(",")[1]);
        Map<String, String> imageData = this.cloudinaryImageService.upload(data, "Kumustagram/posts");
        return new Image(imageData.get("public_id"), imageData.get("secure_url"));
    }


    public void deleteImage(String public_id){
        this.cloudinaryImageService.delete(public_id, "Kumustagram/posts");
    }

    public void save(Post post) {
        postRepository.save(post);
    }


    public void delete(Post post) {
        postRepository.delete(post);
    }


    public Post findById(String id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException("No Post found with the given ID: " + id));
        return post;
    }


}
