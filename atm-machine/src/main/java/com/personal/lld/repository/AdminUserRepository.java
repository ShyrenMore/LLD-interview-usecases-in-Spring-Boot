package com.personal.lld.repository;

import com.personal.lld.domain.AdminUser;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class AdminUserRepository {
    private final Map<String, AdminUser> adminStore = new ConcurrentHashMap<>();

    public AdminUser save(AdminUser adminUser) {
        adminStore.put(adminUser.getId(), adminUser);
        return adminUser;
    }

    public Optional<AdminUser> findById(String adminId) {
        return Optional.ofNullable(adminStore.get(adminId));
    }

    public boolean validateAdminCredentials(String adminId, String pinHash) {
        return findById(adminId)
                .map(admin -> admin.isActive() && admin.getPinHash().equals(pinHash))
                .orElse(false);
    }
}
