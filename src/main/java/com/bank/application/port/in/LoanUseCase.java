package com.bank.application.port.in;

import com.bank.domain.model.loan.Loan;
import com.bank.domain.valueobject.Money;

import java.math.BigDecimal;

public interface LoanUseCase {

    Loan requestLoan(int loanId, String loanType, String requestingClientId, Money requestedAmount, int termMonths);

    Loan approveLoan(int loanId, Money approvedAmount, BigDecimal interestRate, int analystUserId, String targetAccount);

    Loan rejectLoan(int loanId, int analystUserId);

    Loan disburseLoan(int loanId, int analystUserId);
}
