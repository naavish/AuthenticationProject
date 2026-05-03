package com.naav.authenticationproject.service;

import com.naav.authenticationproject.model.UserEntity;
import com.naav.authenticationproject.repository.UserRepository;
import org.apache.catalina.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository , EmailService emailService){
        this.userRepository = userRepository;
    }

    public List<UserEntity> allUsers(){
        List<UserEntity> users = new ArrayList<>();
        for (UserEntity userEntity : userRepository.findAll()){
            users.add(userEntity);
        }
//        userRepository.findAll().forEach(users::add);
        return users;
    }


}
