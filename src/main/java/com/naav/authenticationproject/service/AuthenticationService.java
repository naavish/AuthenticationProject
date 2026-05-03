package com.naav.authenticationproject.service;

import com.naav.authenticationproject.dto.LoginUserDto;
import com.naav.authenticationproject.dto.RegisterUserDto;
import com.naav.authenticationproject.dto.VerifyUserDto;
import com.naav.authenticationproject.model.UserEntity;
import com.naav.authenticationproject.repository.UserRepository;
import jakarta.mail.MessagingException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    public AuthenticationService(UserRepository userRepository , PasswordEncoder passwordEncoder , AuthenticationManager authenticationManager , EmailService emailService
    ){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.emailService = emailService;
    }

    public UserEntity signup(RegisterUserDto registerUserDto){
        UserEntity user = new UserEntity(registerUserDto.getUsername(), registerUserDto.getEmail(), passwordEncoder.encode(registerUserDto.getPassword()));
        user.setVerificationCode(generateVerificationCode());
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
        user.setEnabled(false);
        sendVerificationEmail(user);
        return  userRepository.save(user);
    }
    public UserEntity authenticate(LoginUserDto loginUserDto){
        UserEntity userEntity = userRepository.findByEmail(loginUserDto.getEmail()).orElseThrow(() -> new RuntimeException("user not found"));

        if (!userEntity.isEnabled()){
            throw new RuntimeException("Account not verified , please verify your account");
        }
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginUserDto.getEmail(), loginUserDto.getPassword()
                )
        );

        return userEntity;
    }

    public void verifyUser(VerifyUserDto loginUserDto){
        Optional<UserEntity> optionalUserEntity = userRepository.findByEmail(loginUserDto.getEmail());
        if (optionalUserEntity.isPresent()){
            UserEntity userEntity = optionalUserEntity.get();
            if (userEntity.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())){
                throw new RuntimeException("Verification code has expired");
            }
            if (userEntity.getVerificationCode().equals(loginUserDto.getVerificationCode())){
                userEntity.setEnabled(true);
                userEntity.setVerificationCode(null);
                userEntity.setVerificationCode(null);
                userRepository.save(userEntity);
            }else{
                throw new RuntimeException("Invalid verification code");
            }
        }else {
            throw new RuntimeException("User not found");
        }
    }

    public void resendVerificationCode(String email){
        Optional<UserEntity> optionalUserEntity = userRepository.findByEmail(email);
        if (optionalUserEntity.isPresent()){
            UserEntity userEntity = optionalUserEntity.get();
            if (userEntity.isEnabled()){
                throw new RuntimeException("Account is Already Verified");
            }
            userEntity.setVerificationCode(generateVerificationCode());
            userEntity.setVerificationCodeExpiresAt(LocalDateTime.now().plusHours(1));
            sendVerificationEmail(userEntity);
            userRepository.save(userEntity);
        }else {
            throw new RuntimeException("user not found");
        }
    }
    public void sendVerificationEmail(UserEntity userEntity){
        String subject = "Account verification";
        String verificationCode = userEntity.getVerificationCode();
        String htmlMessage = "<html>"
                + "<body style=\"font-family: Arial, sans-serif;\">"
                + "<div style=\"background-color: #f5f5f5; padding: 20px;\">"
                + "<h2 style=\"color: #333;\">Welcome to our app!</h2>"
                + "<p style=\"font-size: 16px;\">Please enter the verification code below to continue:</p>"
                + "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
                + "<h3 style=\"color: #333;\">Verification Code:</h3>"
                + "<p style=\"font-size: 18px; font-weight: bold; color: #007bff;\">" + verificationCode + "</p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";
        try {
            emailService.sendVerificationEmail(userEntity.getEmail(),subject,htmlMessage);

        }catch (MessagingException e){
            e.printStackTrace();
        }
    }

    private String generateVerificationCode(){
        Random random = new Random();
        int code = random.nextInt(900000)+100000;
        return String.valueOf(code);
    }

}
