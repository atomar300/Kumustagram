package com.ashish.kumustagram.service;

import com.ashish.kumustagram.model.user.Avatar;
import com.ashish.kumustagram.model.user.User;
import com.ashish.kumustagram.security.jwt.JwtUtils;
import com.ashish.kumustagram.exception.UserNotFoundException;
import com.ashish.kumustagram.repository.UserRepository;
import com.ashish.kumustagram.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.xml.bind.DatatypeConverter;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserRepository userRepository;


    @Autowired
    private MongoTemplate mongoTemplate;


    @Autowired
    private CloudinaryImageService cloudinaryImageService;


    public User findByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("No User found with the given Email: " + email));

        return user;
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }


    public User findById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("No User found with the given ID: " + id));

        return user;
    }


    public User findByResetPasswordToken(String resetPasswordToken) {
        User user = userRepository.findByResetPasswordToken(resetPasswordToken)
                .orElseThrow(() -> new BadCredentialsException("Reset Password Token is invalid"));
        return user;
    }


    public List<User> findUsersByName(String name){
        Query query = new Query();

        Criteria searchName = Criteria.where("name").regex(name, "i");
        query.addCriteria(searchName);

        List<User> users = mongoTemplate.find(query, User.class);

        return users;
    }


    public void save(User user) {
        userRepository.save(user);
    }

    public void delete(User user) {
        userRepository.delete(user);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public ResponseCookie loginUser(String email, String password) throws BadCredentialsException {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);

        return jwtCookie;
    }



    public Avatar processAvatar(String base64) {
        byte[] data = DatatypeConverter.parseBase64Binary(base64.split(",")[1]);
        Map<String, String> imageData = this.cloudinaryImageService.upload(data, "Kumustagram/avatars");
        return new Avatar(imageData.get("public_id"), imageData.get("secure_url"));
    }

    public void deleteAvatar(String public_id){
        this.cloudinaryImageService.delete(public_id, "Kumustagram/avatars");
    }


    public User getUserFromCookie(String cookie) {
        String id = jwtUtils.getIdFromJwtToken(cookie);
        User user = userRepository.findById(id).get();
        return user;
    }

}

