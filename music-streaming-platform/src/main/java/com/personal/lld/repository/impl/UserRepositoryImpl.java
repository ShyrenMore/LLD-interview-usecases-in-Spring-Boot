package com.personal.lld.repository.impl;

import com.personal.lld.domain.User;
import com.personal.lld.repository.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class UserRepositoryImpl implements UserRepository {
    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final Map<String, String> idByEmail = new ConcurrentHashMap<>();

    public User save(User u) {
        users.put(u.getId(), u);
        idByEmail.put(u.getEmail(), u.getId());
        return u;
    }

    public Optional<User> findById(String id) {
        return Optional.ofNullable(users.get(id));
    }

    public Optional<User> findByEmail(String e) {
        String id = idByEmail.get(e);
        return id == null ? Optional.empty() : Optional.ofNullable(users.get(id));
    }
}
