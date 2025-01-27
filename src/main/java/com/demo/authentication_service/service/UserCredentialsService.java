package com.demo.authentication_service.service;

import com.demo.authentication_service.dao.UserCredentialsDao;
import com.demo.authentication_service.dao.entity.UserCredentialsEntity;
import com.demo.authentication_service.dao.entity.Role;
import com.demo.authentication_service.dao.RoleDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Service
public class UserCredentialsService {
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserCredentialsDao authDao;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RoleDao roleDao;

    @Transactional
    public UserCredentialsEntity register(UserCredentialsEntity user) {
        // Add logging
        System.out.println("Registering user: " + user.getUsername());

        // Check if username already exists
        if (authDao.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        // Check if email already exists
        if (authDao.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        // Encode password
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        System.out.println("Password encoded successfully");
        user.setPassword(encodedPassword);

        // Assign roles
        Set<Role> roles = new HashSet<>();
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            System.out.println("Assigning default USER role");
            // Default to ROLE_USER if no roles are specified
            Role defaultRole = roleDao.findByName(Role.ERole.ROLE_USER)
                    .orElseGet(() -> {
                        Role newRole = new Role();
                        newRole.setName(Role.ERole.ROLE_USER);
                        return roleDao.save(newRole);
                    });
            roles.add(defaultRole);
        } else {
            // Fetch or create roles specified in the request
            for (Role role : user.getRoles()) {
                Role existingRole = roleDao.findByName(role.getName())
                        .orElseGet(() -> {
                            Role newRole = new Role();
                            newRole.setName(role.getName());
                            return roleDao.save(newRole);
                        });
                roles.add(existingRole);
            }
        }

        user.setRoles(roles);
        UserCredentialsEntity savedUser = authDao.save(user);
        System.out.println("User registered successfully with ID: " + savedUser.getId());
        return savedUser;
    }

    public String generateToken(String name) {
        return jwtService.generateToken(name);
    }

    public boolean verifyToken(String token) {
        jwtService.validateToken(token);
        return true;
    }

    public UserCredentialsEntity findByName(String name) {
        System.out.println("Looking up user by name: " + name);
        return authDao.findByUsername(name)
                .orElseThrow(() -> {
                    System.out.println("User not found: " + name);
                    return new UsernameNotFoundException("User not found");
                });
    }
}
