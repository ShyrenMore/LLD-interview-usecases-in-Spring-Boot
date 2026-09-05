package com.personal.lld.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User {

    private int id;
    private String username;
    private String email;
    private UserRole role;

    public User(
        int id,
        String username,
        String email,
        UserRole role
    ) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
    }
}
