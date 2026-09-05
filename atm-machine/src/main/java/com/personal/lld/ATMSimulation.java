package com.personal.lld;

import com.personal.lld.controller.AdminController;
import com.personal.lld.controller.CardController;
import com.personal.lld.controller.SessionController;
import com.personal.lld.controller.TransactionController;
import com.personal.lld.domain.ATM;
import com.personal.lld.domain.Account;
import com.personal.lld.domain.AdminUser;
import com.personal.lld.domain.Card;
import com.personal.lld.domain.CashDrawer;
import com.personal.lld.domain.Denomination;
import com.personal.lld.domain.Session;
import com.personal.lld.domain.Transaction;
import com.personal.lld.repository.ATMRepository;
import com.personal.lld.repository.AccountRepository;
import com.personal.lld.repository.AdminUserRepository;
import com.personal.lld.repository.CardRepository;
import com.personal.lld.repository.CashDrawerRepository;
import com.personal.lld.repository.SessionRepository;
import com.personal.lld.repository.TransactionRepository;
import com.personal.lld.service.ATMService;
import com.personal.lld.service.AdminService;
import com.personal.lld.service.CardService;
import com.personal.lld.service.SessionService;
import com.personal.lld.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@RequiredArgsConstructor
@Slf4j
public class ATMSimulation implements CommandLineRunner {

    private final ATMRepository atmRepository;
    private final CardRepository cardRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final SessionRepository sessionRepository;
    private final CashDrawerRepository cashDrawerRepository;
    private final AdminUserRepository adminUserRepository;

    private final ATMService atmService;
    private final CardService cardService;
    private final SessionService sessionService;
    private final TransactionService transactionService;
    private final AdminService adminService;

    private final CardController cardController;
    private final SessionController sessionController;
    private final TransactionController transactionController;
    private final AdminController adminController;

    public static void main(String[] args) {
        SpringApplication.run(ATMSimulation.class, args);
    }

    @Override
    public void run(String... args) {
        setupTestData();

        ATM demoAtm = atmService.getATM("ATM_001");
        if (demoAtm != null) {
            demoAtm.attachServices(cardService, sessionService, transactionService);
        }

        log.info("=== ATM Machine Simulation ===");

        String atmId = "ATM_001";
        String cardId = "CARD_001";

        log.info("1. Card Operations:");
        boolean cardInserted = cardController.insertCard(atmId, cardId);
        log.info("Card inserted: {}", cardInserted);

        log.info("2. Session Management:");
        Session session = sessionController.startSession(atmId, cardId);
        log.info("Session started: {}", session != null ? session.getId() : "N/A");

        if (session == null) {
            return;
        }

        log.info("3. Authentication:");
        boolean authenticated = cardController.authenticateCard(atmId, cardId, "1234");
        log.info("Authentication: {}", authenticated);

        log.info("4. Transaction Operations:");

        Transaction balanceTxn = transactionController.showBalance(session.getId());
        log.info("Balance inquiry: {}", balanceTxn != null ? balanceTxn.getStatus() : "Failed");

        long withdrawAmount = 10000;
        Transaction withdrawTxn = transactionController.withdrawCash(session.getId(), withdrawAmount);
        log.info("Withdrawal: {}", withdrawTxn != null ? withdrawTxn.getStatus() : "Failed");

        Map<Denomination, Integer> depositNotes = new HashMap<>();
        // Adjust denomination values to match the Denomination enum used by the domain model.
        Denomination[] denominations = Denomination.values();
        if (denominations.length > 0) {
            depositNotes.put(denominations[0], 2);
        }

        Transaction depositTxn = transactionController.depositCash(session.getId(), depositNotes);
        log.info("Deposit: {}", depositTxn != null ? depositTxn.getStatus() : "Failed");

        log.info("5. Admin Operations:");
        boolean adminLoggedIn = adminController.loginAdmin("ADMIN_001", "1234");
        log.info("Admin login: {}", adminLoggedIn);

        if (adminLoggedIn) {
            CashDrawer cashDrawer = adminController.auditCash(atmId);
            if (cashDrawer != null) {
                log.info("Cash audit - Total minor units: {}", cashDrawer.getTotalCash());
            } else {
                log.info("Cash audit - No cash drawer found for ATM: {}", atmId);
            }
        }

        sessionController.endSession(session.getId());
        cardController.ejectCard(atmId);
        log.info("Session ended and card ejected");
    }

    private void setupTestData() {
        ATM atm = new ATM("ATM_001", "Main Street Branch");
        atmRepository.save(atm);

        CashDrawer cashDrawer = new CashDrawer("ATM_001");
        for (Denomination denomination : Denomination.values()) {
            cashDrawer.addNotes(denomination, 10);
        }
        cashDrawerRepository.save(cashDrawer);

        Card card = new Card("CARD_001", "ACC_001", "12/25");
        cardRepository.save(card);

        Account account = new Account("ACC_001", "John Doe", 100000);
        accountRepository.save(account);

        AdminUser admin = new AdminUser("ADMIN_001", "Admin User", "1234");
        adminUserRepository.save(admin);
    }
}
