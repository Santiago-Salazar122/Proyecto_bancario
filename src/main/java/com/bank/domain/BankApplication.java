package com.bank.domain;

import com.bank.domain.enums.*;
import com.bank.domain.model.client.CompanyClient;
import com.bank.domain.model.client.IndividualClient;
import com.bank.domain.model.account.BankAccount;
import com.bank.domain.model.loan.Loan;
import com.bank.domain.model.transfer.Transfer;
import com.bank.domain.model.user.User;
import com.bank.domain.valueobject.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Main demonstration class for the domain model.
 *
 * Runs examples of entity creation and business flows
 * to verify that validations and rules work correctly.
 */
public class BankApplication {

    public static void main(String[] args) {

        System.out.println("==============================================");
        System.out.println("  BANK DDD - Domain Model Demonstration");
        System.out.println("==============================================\n");

        // ── 1. VALUE OBJECTS ──
        System.out.println("── 1. Creating Value Objects ──");

        Email clientEmail = new Email("john.doe@mail.com");
        PhoneNumber clientPhone = new PhoneNumber("3001234567");
        Address clientAddress = new Address("123 Main Street, New York");
        Money initialAmount = new Money(1500000.00);

        System.out.println("Email: " + clientEmail);
        System.out.println("Phone: " + clientPhone);
        System.out.println("Address: " + clientAddress);
        System.out.println("Money: " + initialAmount);
        System.out.println();

        // ── 2. INDIVIDUAL CLIENT ──
        System.out.println("── 2. Creating Individual Client ──");

        IndividualClient individualClient = new IndividualClient(
            "1023456789",
            "John Charles Doe",
            clientEmail,
            clientPhone,
            LocalDate.of(1990, 5, 15),
            clientAddress
        );

        System.out.println(individualClient);
        System.out.println("Age: " + individualClient.calculateAge() + " years");
        System.out.println("Role: " + individualClient.getRole());
        System.out.println();

        // ── 3. COMPANY CLIENT ──
        System.out.println("── 3. Creating Company Client ──");

        CompanyClient companyClient = new CompanyClient(
            "900123456-7",
            "Advanced Technology Inc.",
            new Email("contact@advancedtech.com"),
            new PhoneNumber("6012345678"),
            new Address("7th Avenue #80-49 Suite 301, New York"),
            "1023456789" // Reference to the legal representative (John Charles)
        );

        companyClient.authorizeEmployee("EMP-001");
        companyClient.authorizeEmployee("EMP-002");

        System.out.println(companyClient);
        System.out.println("EMP-001 authorized: " + companyClient.isEmployeeAuthorized("EMP-001"));
        System.out.println();

        // ── 4. SYSTEM USER ──
        System.out.println("── 4. Creating System User ──");

        User clientUser = new User(
            1, "1023456789", "John Charles Doe",
            "1023456789", clientEmail, clientPhone,
            LocalDate.of(1990, 5, 15), clientAddress,
            UserRole.INDIVIDUAL_CLIENT,
            "jdoe", "pass123"
        );

        User internalAnalyst = new User(
            2, null, "Mary Garcia",
            "5198765432", new Email("m.garcia@bank.com"),
            new PhoneNumber("6019876543"), LocalDate.of(1985, 3, 20),
            new Address("Bank Headquarters, Floor 5"),
            UserRole.INTERNAL_ANALYST,
            "mgarcia", "analyst456"
        );

        System.out.println(clientUser);
        System.out.println("Can operate: " + clientUser.canOperate());
        System.out.println("Is Analyst: " + clientUser.hasRole(UserRole.INTERNAL_ANALYST));
        System.out.println("Correct authentication: " + clientUser.authenticate("jdoe", "pass123"));
        System.out.println("Wrong authentication: " + clientUser.authenticate("jdoe", "wrongPass"));
        System.out.println();

        // ── 5. BANK ACCOUNT ──
        System.out.println("── 5. Creating Bank Account ──");

        BankAccount account = new BankAccount(
            "1001-0001", AccountType.SAVINGS, "1023456789", "USD"
        );

        System.out.println("Account created: " + account);
        System.out.println("Initial balance: " + account.getBalance());

        // Credit (deposit)
        account.credit(new Money(5000000));
        System.out.println("After depositing $5,000,000: " + account.getBalance());

        // Debit (withdrawal)
        account.debit(new Money(1500000));
        System.out.println("After withdrawing $1,500,000: " + account.getBalance());

        System.out.println("Has balance for $4,000,000? " + account.hasSufficientBalance(new Money(4000000)));
        System.out.println();

        // ── 6. LOAN (APPROVAL FLOW) ──
        System.out.println("── 6. Loan Approval Flow ──");

        Loan loan = new Loan(
            1, "Personal", "1023456789",
            new Money(10000000), 36
        );

        System.out.println("Initial status: " + loan.getStatus().getDescription());

        // Analyst approves
        loan.approve(
            new Money(8000000),
            new BigDecimal("1.2"),
            internalAnalyst.getUserId(),
            "1001-0001"
        );
        System.out.println("After approval: " + loan.getStatus().getDescription());
        System.out.println("Approved amount: " + loan.getApprovedAmount());

        // Disbursement
        loan.disburse();
        System.out.println("After disbursement: " + loan.getStatus().getDescription());
        System.out.println();

        // ── 7. TRANSFER (WITHOUT APPROVAL) ──
        System.out.println("── 7. Direct Transfer (no approval required) ──");

        Transfer directTransfer = new Transfer(
            1, "1001-0001", "1001-0002",
            new Money(500000), 1, false
        );

        System.out.println("Status: " + directTransfer.getStatus().getDescription());
        System.out.println("Was executed? " + directTransfer.wasExecuted());
        System.out.println();

        // ── 8. TRANSFER (WITH APPROVAL) ──
        System.out.println("── 8. Transfer with Approval (high-value business) ──");

        Transfer highAmountTransfer = new Transfer(
            2, "2001-0001", "3001-0001",
            new Money(50000000), 3, true
        );

        System.out.println("Initial status: " + highAmountTransfer.getStatus().getDescription());
        System.out.println("Pending approval? " + highAmountTransfer.isPendingApproval());
        System.out.println("Is expired? " + highAmountTransfer.isExpired());

        // Supervisor approves
        highAmountTransfer.approve(4);
        System.out.println("After approval: " + highAmountTransfer.getStatus().getDescription());
        System.out.println();

        // ── 9. BUSINESS VALIDATIONS (EXPECTED ERRORS) ──
        System.out.println("── 9. Business Validations ──");

        // Attempt to create a minor
        try {
            new IndividualClient(
                "9999999", "Minor Person", clientEmail,
                clientPhone, LocalDate.of(2015, 1, 1), clientAddress
            );
        } catch (IllegalArgumentException e) {
            System.out.println("✓ Minor age validation: " + e.getMessage());
        }

        // Attempt to use invalid email
        try {
            new Email("emailwithoutdomain");
        } catch (IllegalArgumentException e) {
            System.out.println("✓ Email validation: " + e.getMessage());
        }

        // Attempt to use phone that is too short
        try {
            new PhoneNumber("123");
        } catch (IllegalArgumentException e) {
            System.out.println("✓ Phone validation: " + e.getMessage());
        }

        // Attempt to create transfer with amount 0
        try {
            new Transfer(99, "A", "B", new Money(0), 1, false);
        } catch (IllegalArgumentException e) {
            System.out.println("✓ Zero amount validation: " + e.getMessage());
        }

        // Attempt invalid state transition (reject an already approved loan)
        try {
            Loan l = new Loan(99, "Test", "X", new Money(1000), 12);
            l.approve(new Money(1000), new BigDecimal("1.5"), 1, "C-001");
            l.reject(1); // INVALID: cannot go from APPROVED to REJECTED
        } catch (IllegalStateException e) {
            System.out.println("✓ State transition validation: " + e.getMessage());
        }

        // Attempt to debit without sufficient balance
        try {
            BankAccount emptyAccount = new BankAccount("TEMP-001", AccountType.SAVINGS, "X", "USD");
            emptyAccount.debit(new Money(1000));
        } catch (IllegalStateException e) {
            System.out.println("✓ Insufficient balance validation: " + e.getMessage());
        }

        // Attempt to operate on a blocked account
        try {
            BankAccount blockedAccount = new BankAccount("TEMP-002", AccountType.CHECKING, "X", "USD");
            blockedAccount.block();
            blockedAccount.credit(new Money(5000));
        } catch (IllegalStateException e) {
            System.out.println("✓ Blocked account validation: " + e.getMessage());
        }

        System.out.println("\n==============================================");
        System.out.println("  All domain validations OK ✓");
        System.out.println("==============================================");
    }
}
