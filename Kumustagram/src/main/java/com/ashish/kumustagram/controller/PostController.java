package com.ashish.kumustagram.controller;


import com.ashish.kumustagram.dto.CreatePostRequest;
import com.ashish.kumustagram.dto.PostResponse;
import com.ashish.kumustagram.dto.PostUpdateRequest;
import com.ashish.kumustagram.model.post.Comment;
import com.ashish.kumustagram.model.post.Image;
import com.ashish.kumustagram.model.post.Post;
import com.ashish.kumustagram.model.user.User;
import com.ashish.kumustagram.service.PostService;
import com.ashish.kumustagram.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;


import java.util.Optional;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/v1")
public class PostController {

    @Autowired
    private UserService userService;

    @Autowired
    private PostService postService;

    @PostMapping("/post/upload")
    public ResponseEntity<?> createPost(@CookieValue("kumustagram") String kumustagram, @RequestBody CreatePostRequest createPostRequest){

        User user = userService.getUserFromCookie(kumustagram);

        Image image = postService.processImage(createPostRequest.getImage());

        Post post = new Post(createPostRequest.getCaption(), image, user.getId());

        postService.save(post);

        // Adding the post to the beginning of the posts List
        user.getPosts().add(0, post);

        userService.save(user);

        PostResponse response = new PostResponse();
        response.setMessage("Post created");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @DeleteMapping("/post/{id}")
    public ResponseEntity<?> deletePost(@PathVariable("id") String id, @CookieValue("kumustagram") String kumustagram){

        Post post = postService.findById(id);

        User user = userService.getUserFromCookie(kumustagram);

        if (!post.getOwner().equals(user.getId())){
            throw new AccessDeniedException("You are Unauthorized to delete the post");
        }

        postService.deleteImage(post.getImage().getPublic_id());

        postService.delete(post);

        //user.setPosts(user.getPosts().stream().filter(e -> !e.getId().equals(id)).collect(Collectors.toList()));
        user.getPosts().remove(post);

        userService.save(user);

        PostResponse response = new PostResponse();
        response.setMessage("Post deleted");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @GetMapping("/post/{id}")
    public ResponseEntity<?> likeAndUnlikePost(@PathVariable("id") String id, @CookieValue("kumustagram") String kumustagram){
        User user = userService.getUserFromCookie(kumustagram);

        Post post = postService.findById(id);

        PostResponse response = new PostResponse();

        if (post.getLikes().contains(user.getId())){
            //post.setLikes(post.getLikes().stream().filter(e -> !e.equals(user.getId())).collect(Collectors.toList()));
            post.getLikes().remove(user.getId());
            response.setMessage("Post Unliked");
        } else {
            post.getLikes().add(user.getId());
            response.setMessage("Post Liked");
        }

        postService.save(post);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @GetMapping("/posts")
    public ResponseEntity<?> getPostOfFollowing( @CookieValue("kumustagram") String kumustagram){
        User user = userService.getUserFromCookie(kumustagram);

        PostResponse response = new PostResponse();

        user.getFollowing().stream().forEach(e -> response.getPosts().addAll(userService.findById(e).getPosts()));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PutMapping("/post/{id}")
    public ResponseEntity<?> updateCaption(@PathVariable("id") String id, @CookieValue("kumustagram") String kumustagram, @RequestBody PostUpdateRequest postUpdateRequest){
        Post post = postService.findById(id);

        User user = userService.getUserFromCookie(kumustagram);

        if (!post.getOwner().equals(user.getId())){
            throw new AccessDeniedException("You are Unauthorized to edit the post");
        }

        post.setCaption(postUpdateRequest.getCaption());

        postService.save(post);

        PostResponse response = new PostResponse();
        response.setMessage("Post Updated");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PutMapping("/post/comment/{id}")
    public ResponseEntity<?> commentOnPost(@PathVariable("id") String id, @CookieValue("kumustagram") String kumustagram, @RequestBody PostUpdateRequest postUpdateRequest){
        Post post = postService.findById(id);

        User user = userService.getUserFromCookie(kumustagram);

        PostResponse response = new PostResponse();

        Optional<Comment> existingComment = post.getComments().stream().filter(e -> e.getUser().equals(user.getId())).findFirst();

        if (existingComment.isPresent()) {
            existingComment.get().setComment(postUpdateRequest.getComment());
            response.setMessage("Comment Updated");
        }

//        if (post.getComments().stream().anyMatch(e -> e.getUser().equals(user.getId()))){
//            post.getComments().stream().filter(e -> e.getUser().equals(user.getId())).findFirst().get().setComment(postUpdateRequest.getComment());
//            response.setMessage("Comment Updated");
//        }

        else {
            Comment comment = new Comment(user.getId(), postUpdateRequest.getComment());
            post.getComments().add(comment);
            response.setMessage("Comment Added");
        }

        postService.save(post);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @DeleteMapping("/post/comment/{id}")
    public ResponseEntity<?> deleteComment(@PathVariable("id") String id, @CookieValue("kumustagram") String kumustagram, @RequestBody(required = false) PostUpdateRequest postUpdateRequest) throws Exception {
        Post post = postService.findById(id);

        User user = userService.getUserFromCookie(kumustagram);

        PostResponse response = new PostResponse();

        // Making sure if the post owner is loggedIn user. Owner should be able to delete any comments on his posts.
        if (post.getOwner().equals(user.getId())){
            if (postUpdateRequest.getCommentId().equals(null)){
                throw new Exception("Comment Id is required");
            }

            post.getComments().remove(post.getComments().stream().filter(e -> e.getId().equals(postUpdateRequest.getCommentId())).findFirst().get());

            response.setMessage("Selected Comment has been deleted");

        }

        // Other user who commented can only delete their own comment on the post.
        else {
            post.getComments().remove(post.getComments().stream().filter(e -> e.getUser().equals(user.getId())).findFirst().get());;

            response.setMessage("Your Comment has been deleted");
        }

        postService.save(post);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
