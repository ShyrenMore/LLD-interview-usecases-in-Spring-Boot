package com.personal.lld.repository;

import com.personal.lld.domain.User;

import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(String userId);
}
