package com.personal.lld.service;

import com.personal.lld.domain.AccountStatement;
import com.personal.lld.domain.Transaction;
import com.personal.lld.domain.TransactionStatus;
import com.personal.lld.domain.TransactionType;
import com.personal.lld.domain.Wallet;
import com.personal.lld.domain.WalletStatus;
import com.personal.lld.repository.TransactionRepository;
import com.personal.lld.repository.WalletRepository;
import com.personal.lld.service.gateway.PaymentGatewayProvider;
import com.personal.lld.service.gateway.PaymentGatewayRouter;
import com.personal.lld.service.notification.NotificationMessage;
import com.personal.lld.service.notification.NotificationRouter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final LockService lockService;
    private final PaymentGatewayRouter paymentGatewayRouter;
    private final NotificationRouter notificationRouter;

    public Transaction transfer(
            String fromAccountNumber,
            String toAccountNumber,
            long amountMinor,
            String description) {

        if (fromAccountNumber == null || toAccountNumber == null) {
            throw new IllegalArgumentException("Account numbers cannot be null");
        }

        if (fromAccountNumber.equals(toAccountNumber)) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }

        if (amountMinor <= 0) {
            throw new IllegalArgumentException("Amount must be positive (minor units)");
        }

        Wallet fromWallet = null;
        Wallet toWallet = null;
        String lockKey1 = null;
        String lockKey2 = null;
        List<String> sortedKeys = null;
        boolean firstAcquired = false;
        boolean secondAcquired = false;

        try {
            fromWallet = getActiveWalletOrThrow(fromAccountNumber);
            toWallet = getActiveWalletOrThrow(toAccountNumber);

            lockKey1 = "wallet_lock_" + fromWallet.getId();
            lockKey2 = "wallet_lock_" + toWallet.getId();

            sortedKeys = new ArrayList<>(List.of(lockKey1, lockKey2));
            Collections.sort(sortedKeys);

            if (lockService.acquire(sortedKeys.get(0), 5000)) {
                firstAcquired = true;
            } else {
                throw new IllegalStateException("Failed to acquire lock");
            }

            if (lockService.acquire(sortedKeys.get(1), 5000)) {
                secondAcquired = true;
            } else {
                lockService.release(sortedKeys.get(0));
                firstAcquired = false;
                throw new IllegalStateException("Failed to acquire lock");
            }

            fromWallet = walletRepository.findById(fromWallet.getId())
                    .orElseThrow(() ->
                            new IllegalStateException("Source wallet not found"));

            toWallet = walletRepository.findById(toWallet.getId())
                    .orElseThrow(() ->
                            new IllegalStateException("Destination wallet not found"));

            if (fromWallet.getStatus() != WalletStatus.ACTIVE
                    || toWallet.getStatus() != WalletStatus.ACTIVE) {
                throw new IllegalStateException("One or more wallets are not ACTIVE");
            }

            if (fromWallet.getBalanceMinor() < amountMinor) {
                throw new IllegalStateException("Insufficient balance");
            }

            fromWallet.setBalanceMinor(
                    fromWallet.getBalanceMinor() - amountMinor);
            toWallet.setBalanceMinor(
                    toWallet.getBalanceMinor() + amountMinor);

            long now = System.currentTimeMillis();
            fromWallet.setUpdatedAt(now);
            toWallet.setUpdatedAt(now);

            walletRepository.save(fromWallet);
            walletRepository.save(toWallet);

            Transaction tx = new Transaction(
                    UUID.randomUUID().toString(),
                    fromWallet.getId(),
                    toWallet.getId(),
                    amountMinor,
                    TransactionType.TRANSFER,
                    TransactionStatus.COMPLETED,
                    null,
                    description,
                    now);

            tx = transactionRepository.save(tx);

            notificationRouter.send(
                    "email",
                    new NotificationMessage(
                            "user@example.com",
                            "Transfer Completed",
                            "Transfer of " + amountMinor
                                    + " minor units from " + fromAccountNumber
                                    + " to " + toAccountNumber + " completed."));

            return tx;
        } finally {
            if (secondAcquired) {
                lockService.release(sortedKeys.get(1));
            }

            if (firstAcquired) {
                lockService.release(sortedKeys.get(0));
            }
        }
    }

    public Transaction initiateDeposit(
            String accountNumber,
            long amountMinor,
            String paymentMethod,
            String paymentGateway,
            Map<String, String> paymentDetails) {

        if (amountMinor <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be positive (minor units)");
        }

        Wallet wallet = getActiveWalletOrThrow(accountNumber);

        String selected = paymentGatewayRouter.selectProvider(
                paymentGateway, amountMinor, "TUF");

        PaymentGatewayProvider provider =
                paymentGatewayRouter.resolve(selected);

        String providerRef = provider.initiatePayment(
                accountNumber,
                amountMinor,
                paymentMethod,
                paymentDetails);

        Transaction tx = new Transaction(
                UUID.randomUUID().toString(),
                null,
                wallet.getId(),
                amountMinor,
                TransactionType.DEPOSIT,
                TransactionStatus.PENDING,
                providerRef,
                "Deposit via " + selected,
                System.currentTimeMillis());

        return transactionRepository.save(tx);
    }

    public void handleDepositCallback(
            String providerRef,
            TransactionStatus status) {

        Optional<Transaction> txOpt =
                transactionRepository.findByProviderRef(providerRef);

        if (txOpt.isEmpty()) {
            throw new IllegalArgumentException(
                    "Transaction not found for providerRef: " + providerRef);
        }

        Transaction tx = txOpt.get();

        if (tx.getStatus() == TransactionStatus.COMPLETED
                || tx.getStatus() == TransactionStatus.FAILED) {
            return;
        }

        String walletId = tx.getToWalletId();
        String lockKey = "wallet_lock_" + walletId;

        if (!lockService.acquire(lockKey, 5000)) {
            throw new IllegalStateException(
                    "Failed to acquire lock for wallet " + walletId);
        }

        try {
            Wallet wallet = walletRepository.findById(walletId)
                    .orElseThrow(() ->
                            new IllegalStateException(
                                    "Wallet not found for transaction"));

            if (status == TransactionStatus.COMPLETED) {
                wallet.setBalanceMinor(
                        wallet.getBalanceMinor() + tx.getAmountMinor());
                wallet.setUpdatedAt(System.currentTimeMillis());

                walletRepository.save(wallet);
                tx.setStatus(TransactionStatus.COMPLETED);

                notificationRouter.send(
                        "email",
                        new NotificationMessage(
                                "user@example.com",
                                "Deposit Completed",
                                "Deposit of " + tx.getAmountMinor()
                                        + " minor units to account credited."));
            } else {
                tx.setStatus(TransactionStatus.FAILED);
            }

            transactionRepository.save(tx);
        } finally {
            lockService.release(lockKey);
        }
    }

    public Transaction withdraw(
            String accountNumber,
            long amountMinor,
            String description) {

        if (amountMinor <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be positive (minor units)");
        }

        Wallet wallet = getActiveWalletOrThrow(accountNumber);

        if (wallet.getBalanceMinor() < amountMinor) {
            throw new IllegalStateException("Insufficient balance");
        }

        Transaction tx = new Transaction(
                UUID.randomUUID().toString(),
                wallet.getId(),
                null,
                amountMinor,
                TransactionType.WITHDRAWAL,
                TransactionStatus.PENDING,
                null,
                description,
                System.currentTimeMillis());

        return transactionRepository.save(tx);
    }

    public AccountStatement getStatement(
            String accountNumber,
            Long startUtc,
            Long endUtc) {

        Wallet wallet = getActiveOrClosedWallet(accountNumber);

        long start = startUtc == null ? 0L : startUtc;
        long end = endUtc == null ? Long.MAX_VALUE : endUtc;

        List<Transaction> txs =
                transactionRepository.findByWalletAndRange(
                        wallet.getId(), start, end);

        return new AccountStatement(
                wallet.getId(),
                wallet.getAccountNumber(),
                txs,
                startUtc,
                endUtc,
                wallet.getBalanceMinor());
    }

    private Wallet getActiveWalletOrThrow(String accountNumber) {
        Wallet wallet = walletRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Wallet not found for account: " + accountNumber));

        if (wallet.getStatus() != WalletStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Wallet is not ACTIVE: " + wallet.getStatus());
        }

        return wallet;
    }

    private Wallet getActiveOrClosedWallet(String accountNumber) {
        return walletRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Wallet not found for account: " + accountNumber));
    }
}
