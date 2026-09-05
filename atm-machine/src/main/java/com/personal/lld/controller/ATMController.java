package com.personal.lld.controller;

import com.personal.lld.domain.ATM;
import com.personal.lld.domain.CashDrawer;
import com.personal.lld.service.ATMService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/atms")
@RequiredArgsConstructor
@Slf4j
public class ATMController {
    private final ATMService atmService;

    @PostMapping("/{atmId}/offline")
    public void takeOffline(@PathVariable String atmId) {
        atmService.takeOffline(atmId);
    }

    @PostMapping("/{atmId}/online")
    public void bringOnline(@PathVariable String atmId) {
        atmService.bringOnline(atmId);
    }

    @GetMapping("/{atmId}/cash")
    public CashDrawer auditCash(@PathVariable String atmId) {
        return atmService.auditCash(atmId);
    }

    @GetMapping("/{atmId}")
    public ATM getATM(@PathVariable String atmId) {
        return atmService.getATM(atmId);
    }
}
