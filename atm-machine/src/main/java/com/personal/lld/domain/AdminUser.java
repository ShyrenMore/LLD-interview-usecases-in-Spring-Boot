package com.personal.lld.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUser {

    private String id;
    private String name;
    private String pinHash;
    private boolean active;

    public AdminUser(String id, String name, String pinHash) {
        this.id = id;
        this.name = name;
        this.pinHash = pinHash;
        this.active = true;
    }
}
