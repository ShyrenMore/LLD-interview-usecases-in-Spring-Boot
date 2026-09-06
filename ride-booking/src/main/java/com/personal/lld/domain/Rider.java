package com.personal.lld.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rider {
    private String id;
    private String name;
    private String email;
    private String phone;
    private long createdAt;
}
