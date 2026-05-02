package com.naav.authenticationproject.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "users" , schema = "authentication")
public class UserEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(unique = true , nullable = false , name = "username")
    private String username;

    @Column(unique = true,nullable = false , name = "email")
    private String email;

    @Column(nullable = false, name = "password")
    private String password;

    private boolean enabled;

    @Column(name="verification_code")
    private String verificationCode;

    @Column(name = "verification_expiration")
    private LocalDateTime verificationCodeExpiresAt;


// Constructor with all fields used when we manually create users.
    public UserEntity(String username , String email , String password ){
        this.username = username;
        this.email = email;
        this.password = password;
    }
    // used when referencing to an entity
    public UserEntity(Long id){
        this.id = id;
    }

    //empty constructor required by spring JPA  , when hibernate JPA loads data from DB it does : 1. Create empty object , 2. Fill fields using reflections
    //without the empty constructor APP will crash
    public UserEntity(){

    }

//because class implements UserDetails , spring Security requires us to define Collection<? extends GrantedAuthority> getAuthorities();
    // authorities = roles/permissions e.g ROLE_USER , ROLE_ADMIN
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }
    @Override
    public boolean isAccountNonExpired(){
        return true;
    }

    @Override
    public boolean isAccountNonLocked(){
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired(){
        return true;
    }

    @Override
    public boolean isEnabled(){
        return enabled;
    }



}
