package com.campusbridge.backend.dto;

import com.campusbridge.backend.enums.Role;

public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private String id;
    private String name;
    private String email;
    private Role role;
    private UserDto user;

    public AuthResponse() {
    }

    public AuthResponse(String token, UserDto user) {
        this.token = token;
        this.type = "Bearer";
        this.user = user;
        if (user != null) {
            this.id = user.getId();
            this.name = user.getName();
            this.email = user.getEmail();
            this.role = user.getRole();
        }
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public UserDto getUser() {
        return user;
    }

    public void setUser(UserDto user) {
        this.user = user;
        if (user != null) {
            this.id = user.getId();
            this.name = user.getName();
            this.email = user.getEmail();
            this.role = user.getRole();
        }
    }
}
