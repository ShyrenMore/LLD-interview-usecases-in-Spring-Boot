package com.personal.lld.repository;

import com.personal.lld.domain.Wallet;

import java.util.Optional;

public interface WalletRepository {
    Wallet save(Wallet wallet);
    Optional<Wallet> findById(String walletId);
    Optional<Wallet> findByAccountNumber(String accountNumber);
}
