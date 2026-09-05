package com.personal.lld.repository;

import com.personal.lld.domain.Account;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class AccountRepository {
    private final Map<String, Account> accountStore = new ConcurrentHashMap<>();

    public Account save(Account account) {
        accountStore.put(account.getId(), account);
        return account;
    }

    public Optional<Account> findById(String accountId) {
        return Optional.ofNullable(accountStore.get(accountId));
    }

    public void updateBalance(String accountId, long newBalance) {
        findById(accountId).ifPresent(account -> account.setBalanceMinorUnits(newBalance));
    }

    public void updateDailyWithdrawalUsed(String accountId, long amountUsed) {
        findById(accountId).ifPresent(account -> account.setDailyWithdrawalUsedMinor(amountUsed));
    }
}
