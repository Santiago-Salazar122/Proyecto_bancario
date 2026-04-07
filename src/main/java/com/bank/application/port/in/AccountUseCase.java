package com.bank.application.port.in;

import com.bank.domain.enums.AccountType;
import com.bank.domain.model.account.BankAccount;
import com.bank.domain.valueobject.Money;

public interface AccountUseCase {

    BankAccount openAccount(String accountNumber, AccountType accountType, String ownerId, String currency);

    BankAccount deposit(String accountNumber, Money amount);

    BankAccount withdraw(String accountNumber, Money amount);

    BankAccount blockAccount(String accountNumber);

    BankAccount reactivateAccount(String accountNumber);

    BankAccount cancelAccount(String accountNumber);
}
