
package com.personal.lld;

import com.personal.lld.controller.AdminController;
import com.personal.lld.controller.TransactionController;
import com.personal.lld.controller.WalletController;
import com.personal.lld.domain.Transaction;
import com.personal.lld.domain.TransactionStatus;
import com.personal.lld.domain.User;
import com.personal.lld.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
public class DigitalWalletApplication implements CommandLineRunner {

    private final UserRepository userRepository;
    private final WalletController walletController;
    private final TransactionController transactionController;
    private final AdminController adminController;

    public static void main(String[] args) {
        SpringApplication.run(DigitalWalletApplication.class, args);
    }

    @Override
    public void run(String... args) {

        log.info("=== DIGITAL WALLET SYSTEM SIMULATION STARTED ===");

        // ---------------------------------------------------------
        // 1. Create users
        // ---------------------------------------------------------

        log.info("Step 1: Creating users");

        User userA = new User(
                UUID.randomUUID().toString(),
                "alice",
                "alice@example.com",
                "Alice",
                System.currentTimeMillis()
        );

        userRepository.save(userA);

        User userB = new User(
                UUID.randomUUID().toString(),
                "bob",
                "bob@example.com",
                "Bob",
                System.currentTimeMillis()
        );

        userRepository.save(userB);

        log.info(
                "Users created successfully. userAId={}, userBId={}",
                userA.getId(),
                userB.getId()
        );

        // ---------------------------------------------------------
        // 2. Create wallets
        // ---------------------------------------------------------

        log.info("Step 2: Creating wallets");

        var walletA = walletController.createWallet(userA.getId());
        var walletB = walletController.createWallet(userB.getId());

        log.info(
                "Wallets created successfully. walletAAccount={}, walletBAccount={}",
                walletA.getAccountNumber(),
                walletB.getAccountNumber()
        );

        // ---------------------------------------------------------
        // 3. Initiate deposit
        // ---------------------------------------------------------

        log.info(
                "Step 3: Initiating deposit. accountNumber={}, amountMinor={}, paymentMethod={}",
                walletA.getAccountNumber(),
                50000,
                "CARD"
        );

        Map<String, String> paymentDetails = new HashMap<>();

        Transaction depositTransaction =
                transactionController.initiateDeposit(
                        walletA.getAccountNumber(),
                        50000,
                        "CARD",
                        "mock",
                        paymentDetails
                );

        log.info(
                "Deposit initiated successfully. transactionId={}, providerRef={}, status={}",
                depositTransaction.getId(),
                depositTransaction.getProviderRef(),
                depositTransaction.getStatus()
        );

        // ---------------------------------------------------------
        // 4. Simulate payment callback
        // ---------------------------------------------------------

        log.info(
                "Step 4: Simulating payment success callback. providerRef={}, status={}",
                depositTransaction.getProviderRef(),
                TransactionStatus.COMPLETED
        );

        transactionController.handlePaymentCallback(
                depositTransaction.getProviderRef(),
                TransactionStatus.COMPLETED
        );

        log.info(
                "Deposit completed successfully. accountNumber={}, balanceMinor={}",
                walletA.getAccountNumber(),
                walletController.getBalance(
                        walletA.getAccountNumber()
                )
        );

        // ---------------------------------------------------------
        // 5. Transfer
        // ---------------------------------------------------------

        log.info(
                "Step 5: Initiating transfer. fromAccount={}, toAccount={}, amountMinor={}",
                walletA.getAccountNumber(),
                walletB.getAccountNumber(),
                20000
        );

        Transaction transferTransaction =
                transactionController.transfer(
                        walletA.getAccountNumber(),
                        walletB.getAccountNumber(),
                        20000,
                        "Pay Bob"
                );

        log.info(
                "Transfer completed successfully. transactionId={}, status={}",
                transferTransaction.getId(),
                transferTransaction.getStatus()
        );

        log.info(
                "Balances after transfer. walletAAccount={}, walletABalanceMinor={}, " +
                        "walletBAccount={}, walletBBalanceMinor={}",
                walletA.getAccountNumber(),
                walletController.getBalance(
                        walletA.getAccountNumber()
                ),
                walletB.getAccountNumber(),
                walletController.getBalance(
                        walletB.getAccountNumber()
                )
        );

        // ---------------------------------------------------------
        // 6. Withdrawal
        // ---------------------------------------------------------

        log.info(
                "Step 6: Initiating withdrawal. accountNumber={}, amountMinor={}",
                walletB.getAccountNumber(),
                10000
        );

        Transaction withdrawalTransaction =
                transactionController.withdraw(
                        walletB.getAccountNumber(),
                        10000,
                        "Withdraw to bank"
                );

        log.info(
                "Withdrawal initiated successfully. transactionId={}, status={}",
                withdrawalTransaction.getId(),
                withdrawalTransaction.getStatus()
        );

        // ---------------------------------------------------------
        // 7. Account statement
        // ---------------------------------------------------------

        log.info(
                "Step 7: Fetching account statement. accountNumber={}",
                walletA.getAccountNumber()
        );

        var statementA =
                walletController.getStatement(
                        walletA.getAccountNumber(),
                        null,
                        null
                );

        log.info(
                "Account statement retrieved. accountNumber={}, transactionCount={}, balanceMinor={}",
                walletA.getAccountNumber(),
                statementA.getTransactions().size(),
                statementA.getCurrentBalanceMinor()
        );

        // ---------------------------------------------------------
        // 8. Suspend wallet and test transfer
        // ---------------------------------------------------------

        log.info(
                "Step 8: Suspending wallet. accountNumber={}",
                walletB.getAccountNumber()
        );

        adminController.suspendWallet(
                walletB.getAccountNumber()
        );

        log.info(
                "Wallet suspended. Attempting transfer to suspended wallet. " +
                        "fromAccount={}, toAccount={}, amountMinor={}",
                walletA.getAccountNumber(),
                walletB.getAccountNumber(),
                1000
        );

        try {

            transactionController.transfer(
                    walletA.getAccountNumber(),
                    walletB.getAccountNumber(),
                    1000,
                    "Test after suspend"
            );

            log.error(
                    "Unexpected result: transfer to suspended wallet succeeded"
            );

        } catch (Exception ex) {

            log.warn(
                    "Expected transfer failure received. fromAccount={}, toAccount={}, reason={}",
                    walletA.getAccountNumber(),
                    walletB.getAccountNumber(),
                    ex.getMessage()
            );
        }

        log.info("=== DIGITAL WALLET SYSTEM SIMULATION COMPLETED ===");
    }
}
