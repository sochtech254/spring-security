package com.sochtech.jwtauthenticationwithdatabase.jwt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;

@RestController
@RequestMapping("/api")
public class UserSignupController {

    @Autowired
    DataSource dataSource;

    @Autowired
    PasswordEncoder passwordEncoder;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestParam String username,
                                         @RequestParam String password,
                                         @RequestParam String role) {

        JdbcUserDetailsManager userDetailsManager = new JdbcUserDetailsManager(dataSource);

        if (userDetailsManager.userExists(username)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User already exists");
        }

        UserDetails user = User.withUsername(username)
                .password(passwordEncoder.encode(password))
                .roles(role)
                .build();

        userDetailsManager.createUser(user);
        return ResponseEntity.ok().body("User created successfully");
    }
}
