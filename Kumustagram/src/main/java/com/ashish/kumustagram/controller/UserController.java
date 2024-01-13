package com.ashish.kumustagram.controller;

import com.ashish.kumustagram.dto.LoginRequest;
import com.ashish.kumustagram.dto.SignupRequest;
import com.ashish.kumustagram.dto.UserResponse;
import com.ashish.kumustagram.dto.UserUpdateRequest;
import com.ashish.kumustagram.exception.UserNotFoundException;
import com.ashish.kumustagram.model.user.Avatar;
import com.ashish.kumustagram.model.user.User;
import com.ashish.kumustagram.security.jwt.JwtUtils;
import com.ashish.kumustagram.service.EmailService;
import com.ashish.kumustagram.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/v1")
@EnableAsync
public class UserController {
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;
    private final UserService userService;
    private final EmailService emailService;


    @Autowired
    UserController(PasswordEncoder encoder, JwtUtils jwtUtils, UserService userService, EmailService emailService) {
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
        this.userService = userService;
        this.emailService = emailService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest loginRequest) throws BadCredentialsException {
        if (!userService.existsByEmail(loginRequest.getEmail())){
            throw new UserNotFoundException("No User found with the given Email: " + loginRequest.getEmail());
        }

        ResponseCookie jwtCookie = userService.loginUser(loginRequest.getEmail(), loginRequest.getPassword());
        User user = userService.getUserFromCookie(jwtCookie.getValue());

        UserResponse response = new UserResponse();
        response.setUser(user);
        response.setToken(jwtCookie.getValue().toString());
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(response);
    }


    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) throws Exception {
        if (userService.existsByEmail(signUpRequest.getEmail())) {
            throw new Exception("Email is already taken!");
        }

        // Receiving a base64 string of image from the front-end using FormData with json data.
        String base64 = signUpRequest.getAvatar();
        Avatar avatar = userService.processAvatar(base64);

        // Create new user's account
        User user = new User(signUpRequest.getName(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()),
                avatar
        );

        userService.save(user);

        // Sending jwt token without having to login
        ResponseCookie jwtCookie = userService.loginUser(signUpRequest.getEmail(), signUpRequest.getPassword());

        UserResponse response = new UserResponse();
        response.setUser(user);
        response.setToken(jwtCookie.getValue().toString());
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(response);
    }


    @GetMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        // Setting the security context to null
        SecurityContextHolder.getContext().setAuthentication(null);

        // Setting the value of cookie to empty String
        ResponseCookie cookie = jwtUtils.getCleanJwtCookie();

        UserResponse response = new UserResponse();
        response.setMessage("Logged Out!");
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }



    @GetMapping("/follow/{id}")
    public ResponseEntity<?> followUser(@PathVariable("id") String id, @CookieValue("kumustagram") String kumustagram) {
        User userToFollow = userService.findById(id);
        User loggedInUser = userService.getUserFromCookie(kumustagram);

        UserResponse response = new UserResponse();

        if (loggedInUser.getFollowing().contains(id)){
            //loggedInUser.setFollowing(loggedInUser.getFollowing().stream().filter(e -> !e.equals(id)).collect(Collectors.toList()));
            //userToFollow.setFollowers(userToFollow.getFollowers().stream().filter(e -> !e.equals(loggedInUser.getId())).collect(Collectors.toList()));
            loggedInUser.getFollowing().remove(id);
            userToFollow.getFollowers().remove(loggedInUser.getId());

            response.setMessage("User Unfollowed");
        } else {
            loggedInUser.getFollowing().add(id);
            userToFollow.getFollowers().add(loggedInUser.getId());

            response.setMessage("User Followed");
        }

        userService.save(userToFollow);
        userService.save(loggedInUser);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PutMapping("/update/password")
    public ResponseEntity<?> updatePassword(@RequestBody @Valid UserUpdateRequest userUpdateRequest, @CookieValue("kumustagram") String kumustagram) throws Exception {

        User user = userService.getUserFromCookie(kumustagram);

        // Encoder is using bCryptPasswordEncoder in the background.
        // Comparing the passwords using matches() method in bCryptPasswordEncoder.
        boolean isPasswordMatched = encoder.matches(userUpdateRequest.getOldPassword(), user.getPassword());

        if (!isPasswordMatched) {
            throw new Exception("Old password is incorrect!");
        }

        user.setPassword(encoder.encode(userUpdateRequest.getNewPassword()));
        userService.save(user);

        UserResponse response = new UserResponse();
        response.setMessage("Password Updated");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }



    @PutMapping("/update/profile")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UserUpdateRequest userUpdateRequest, @CookieValue("kumustagram") String kumustagram) {

        // Cookie name is ashish and value of this cookie is the JwtToken. i.e. ashish = value of token
        User user = userService.getUserFromCookie(kumustagram);

        if (userUpdateRequest.getName() != null){
            user.setName(userUpdateRequest.getName());
        }

        if (userUpdateRequest.getEmail() != null){
            user.setEmail(userUpdateRequest.getEmail());
        }

        if (userUpdateRequest.getAvatar() != null) {
            String imageId = user.getAvatar().getPublic_id();
            userService.deleteAvatar(imageId);

            String base64 = userUpdateRequest.getAvatar();
            Avatar avatar = userService.processAvatar(base64);

            user.setAvatar(avatar);
        }

        userService.save(user);

        UserResponse response = new UserResponse();
        response.setMessage("Profile Updated");

        return ResponseEntity.ok().body(response);
    }


    @DeleteMapping("/delete/me")
    public ResponseEntity<?> deleteMyProfile(@CookieValue("kumustagram") String kumustagram){
        User user = userService.getUserFromCookie(kumustagram);

        List<Post> posts = user.getPosts();
        List<String> followers = user.getFollowers();
        List<String> followings = user.getFollowing();
        String userId = user.getId();

        userService.deleteAvatar(user.getAvatar().getPublic_id());
        userService.delete(user);

        // Logging out the user
        // Setting the security context to null
        SecurityContextHolder.getContext().setAuthentication(null);

        // Setting the value of cookie to empty String
        ResponseCookie cookie = jwtUtils.getCleanJwtCookie();


        // Delete all posts of the user
        posts.stream().forEach(p -> {
            postService.deleteImage(p.getImage().getPublic_id());
            postService.delete(p);
        });

        // Removing User from Followers Following
        followers.stream().forEach(f  -> {
            User follower = userService.findById(f);
            follower.getFollowing().remove(userId);
            //follower.getFollowing().removeIf(u -> u.equals(userId));
            userService.save(follower);
        });

        // Removing User from Following's Followers
        followings.stream().forEach(f  -> {
            User follows = userService.findById(f);
            follows.getFollowers().remove(userId);
            //follows.getFollowers().removeIf(u -> u.equals(userId));
            userService.save(follows);
        });

        List<Post> allPosts = postService.findAll();

        // removing all comments of the user from all posts
        allPosts.stream().forEach(p -> {
            p.getComments().removeAll(p.getComments().stream().filter(c -> c.getUser().equals(userId)).collect(Collectors.toList()));
            //p.getComments().removeIf(c -> c.getUser().equals(userId));
            postService.save(p);
        });

        // removing all likes of the user from all posts
        allPosts.stream().forEach(p -> {
            p.getLikes().removeAll(p.getLikes().stream().filter(l -> l.equals(userId)).collect(Collectors.toList()));
            //p.getLikes().removeIf(l -> l.equals(userId));
            postService.save(p);
        });

        UserResponse response = new UserResponse();
        response.setMessage("Profile Deleted");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @GetMapping("/me")
    public ResponseEntity<?> myProfile(@CookieValue("kumustagram") String kumustagram){

        User user = userService.getUserFromCookie(kumustagram);

        UserResponse response = new UserResponse();
        response.setUser(user);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @GetMapping("/user/{id}")
    public ResponseEntity<?> getUserProfile(@PathVariable("id") String id){
        User user = userService.findById(id);

        UserResponse response = new UserResponse();
        response.setUser(user);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(@RequestParam("name") String name){

        List<User> users = userService.findUsersByName(name);

        UserResponse response = new UserResponse();
        response.setUsers(users);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }



    @PostMapping("/forgot/password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> payload, HttpServletRequest request) throws UserNotFoundException {

        // Lookup user in database by e-mail
        User user = userService.findByEmail(payload.get("email"));

        // Generate random 36-character string token for reset password
        String resetToken = UUID.randomUUID().toString();
        user.setResetPasswordToken(DigestUtils.sha256Hex(resetToken));
        user.setResetPasswordExpire(LocalDateTime.now().plusMinutes(10));

        // Save token to database
        userService.save(user);

        String appUrl = request.getScheme() + "://" + request.getServerName();
        String resetPasswordUrl = appUrl + "/api/v1/password/reset/" + resetToken;
//        String appUrl = "http://localhost:3000";
//        String resetPasswordUrl = appUrl + "/password/reset/" + resetToken;

        String to = user.getEmail();
        String subject = "Kumustagram Reset Password";
        String message = "Your password reset token is: \n\n" + resetPasswordUrl
                + "\n\nPlease click on the link to reset password.\nIf you have not requested this email then, please ignore it.";

        try {

            emailService.sendSimpleMail(to, subject, message);

        } catch (Exception ex) {

            user.setResetPasswordToken(null);
            user.setResetPasswordExpire(null);
            userService.save(user);
        }

        UserResponse response = new UserResponse();
        response.setMessage("Email sent to " + payload.get("email") + " successfully!");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PutMapping("/password/reset/{token}")
    public ResponseEntity<?> resetPassword(@PathVariable("token") String token, @RequestBody @Valid UserUpdateRequest userUpdateRequest) {

        String resetPasswordToken = DigestUtils.sha256Hex(token);

        User user = userService.findByResetPasswordToken(resetPasswordToken);

        if (!(user.getResetPasswordExpire().isAfter(LocalDateTime.now()))) {
            throw new BadCredentialsException("Reset Password Token is expired");
        }

        user.setPassword(encoder.encode(userUpdateRequest.getNewPassword()));
        user.setResetPasswordToken(null);
        user.setResetPasswordExpire(null);

        userService.save(user);

        UserResponse response = new UserResponse();
        response.setMessage("Password Updated");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @GetMapping("/my/posts")
    public ResponseEntity<?> getMyPosts(@CookieValue("kumustagram") String kumustagram){
        User user = userService.getUserFromCookie(kumustagram);
        PostResponse response = new PostResponse();
        response.setPosts(user.getPosts());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @GetMapping("/userposts/{id}")
    public ResponseEntity<?> getUserPosts(@PathVariable("id") String id){
        User user = userService.findById(id);
        PostResponse response = new PostResponse();
        response.setPosts(user.getPosts());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
