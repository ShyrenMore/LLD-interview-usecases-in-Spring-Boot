package com.personal.lld.controller;

import com.personal.lld.domain.CashDrawer;
import com.personal.lld.domain.Denomination;
import com.personal.lld.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {
    private final AdminService adminService;

    @PostMapping("/login")
    public boolean loginAdmin(@RequestParam String adminId, @RequestParam String pin) {
        return adminService.loginAdmin(adminId, pin);
    }

    @PostMapping("/atms/{atmId}/cash/refill")
    public void refillCash(
            @PathVariable String atmId,
            @RequestBody Map<Denomination, Integer> notes) {
        adminService.refillCash(atmId, notes);
    }

    @GetMapping("/atms/{atmId}/cash/audit")
    public CashDrawer auditCash(@PathVariable String atmId) {
        return adminService.auditCash(atmId);
    }
}
