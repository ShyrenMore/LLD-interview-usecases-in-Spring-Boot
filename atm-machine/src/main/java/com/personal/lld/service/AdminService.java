package com.personal.lld.service;

import com.personal.lld.domain.AdminUser;
import com.personal.lld.domain.CashDrawer;
import com.personal.lld.domain.Denomination;
import com.personal.lld.repository.AdminUserRepository;
import com.personal.lld.repository.CashDrawerRepository;
import com.personal.lld.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {
    private final AdminUserRepository adminUserRepository;
    private final CashDrawerRepository cashDrawerRepository;
    private final TransactionRepository transactionRepository; // kept for future audit logs

    public boolean loginAdmin(String adminId, String pin) {
        // TODO: Hash the PIN and compare with stored hash
        Optional<AdminUser> adminOpt = adminUserRepository.findById(adminId);
        if (adminOpt.isPresent()) {
            AdminUser admin = adminOpt.get();
            return admin.isActive() && admin.getPinHash().equals(pin);
        }
        return false;
    }

    public void refillCash(String atmId, Map<Denomination, Integer> notes) {
        Optional<CashDrawer> drawerOpt = cashDrawerRepository.findByATMId(atmId);
        if (drawerOpt.isPresent()) {
            CashDrawer drawer = drawerOpt.get();
            for (Map.Entry<Denomination, Integer> entry : notes.entrySet()) {
                drawer.addNotes(entry.getKey(), entry.getValue());
            }
            cashDrawerRepository.save(drawer);
            log.info("Cash refilled for ATM: {}", atmId);
        }
    }

    public CashDrawer auditCash(String atmId) {
        return cashDrawerRepository.findByATMId(atmId).orElse(null);
    }
}
