package com.personal.lld.repository.impl;

import com.personal.lld.domain.Wallet;
import com.personal.lld.repository.WalletRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class WalletRepositoryImpl implements WalletRepository {

    private final Map<String, Wallet> walletsById = new ConcurrentHashMap<>();
    private final Map<String, String> walletIdByAccountNumber = new ConcurrentHashMap<>();

    @Override
    public Wallet save(Wallet wallet) {
        walletsById.put(wallet.getId(), wallet);
        walletIdByAccountNumber.put(wallet.getAccountNumber(), wallet.getId());
        return wallet;
    }

    @Override
    public Optional<Wallet> findById(String walletId) {
        return Optional.ofNullable(walletsById.get(walletId));
    }

    @Override
    public Optional<Wallet> findByAccountNumber(String accountNumber) {
        String id = walletIdByAccountNumber.get(accountNumber);

        if (id == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(walletsById.get(id));
    }
}
