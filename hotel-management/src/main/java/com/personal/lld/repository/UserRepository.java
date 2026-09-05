package com.personal.lld.repository;

import com.personal.lld.domain.User;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class UserRepository {
    private final Map<String, User> users = new ConcurrentHashMap<>();

    public User save(User user) {
        users.put(user.getId(), user);
        return user;
    }

    public Optional<User> findById(String userId) {
        return Optional.ofNullable(users.get(userId));
    }
}
