package com.personal.lld.repository;

import com.personal.lld.domain.User;
import com.personal.lld.domain.UserRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
@Slf4j
public class UserRepository {

    private final Map<Integer, User> users = new HashMap<>();

    public UserRepository() {
        users.put(
            1,
            new User(
                1,
                "john_doe",
                "john@example.com",
                UserRole.USER
            )
        );

        users.put(
            2,
            new User(
                2,
                "jane_smith",
                "jane@example.com",
                UserRole.ADMIN
            )
        );
    }

    public User findById(int userId) {
        return users.get(userId);
    }
}
