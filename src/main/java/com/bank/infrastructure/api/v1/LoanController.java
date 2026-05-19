package com.bank.infrastructure.api.v1;

import com.bank.domain.enums.LoanStatus;
import com.bank.domain.enums.LoanType;
import com.bank.domain.model.loan.Loan;
import com.bank.domain.ports.LoanRepositoryPort;
import com.bank.domain.service.*;
import com.bank.domain.valueobject.Money;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Controller REST para el ciclo de vida de préstamos.
 * URL base: /api/v1/loans
 *
 * Flujo completo:
 * 1. POST   /api/v1/loans               → Solicitar (UNDER_REVIEW)
 * 2. PATCH  /api/v1/loans/{id}/approve  → Aprobar (APPROVED)
 *    PATCH  /api/v1/loans/{id}/reject   → Rechazar (REJECTED)
 * 3. PATCH  /api/v1/loans/{id}/disburse → Desembolsar (DISBURSED)
 */
@RestController
@RequestMapping("/api/v1/loans")
public class LoanController {

    private final RequestLoanService requestLoanService;
    private final ApproveLoanService approveLoanService;
    private final RejectLoanService rejectLoanService;
    private final DisburseLoanService disburseLoanService;
    private final LoanRepositoryPort loanRepository;

    public LoanController(RequestLoanService req, ApproveLoanService app, RejectLoanService rej,
                           DisburseLoanService dis, LoanRepositoryPort lr) {
        this.requestLoanService = req; this.approveLoanService = app;
        this.rejectLoanService = rej; this.disburseLoanService = dis;
        this.loanRepository = lr;
    }

    @PostMapping
    public ResponseEntity<Loan> requestLoan(@Valid @RequestBody RequestLoanBody req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(requestLoanService.execute(
            req.requestingClientId, req.loanType, new Money(req.requestedAmount),
            req.interestRate, req.termMonths, req.creatorUserId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Loan> findById(@PathVariable Long id) {
        return loanRepository.findById(id).map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Loan>> listAll(@RequestParam(required = false) LoanStatus status) {
        if (status != null) return ResponseEntity.ok(loanRepository.findByStatus(status));
        return ResponseEntity.ok(loanRepository.findAll());
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<Loan>> findByClient(@PathVariable String clientId) {
        return ResponseEntity.ok(loanRepository.findByClientId(clientId));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<Loan> approveLoan(@PathVariable Long id,
                                             @Valid @RequestBody ApproveLoanBody req) {
        return ResponseEntity.ok(approveLoanService.execute(
            id, new Money(req.approvedAmount), req.interestRate, req.analystUserId, req.targetAccount));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<Loan> rejectLoan(@PathVariable Long id, @RequestParam Long analystUserId) {
        return ResponseEntity.ok(rejectLoanService.execute(id, analystUserId));
    }

    @PatchMapping("/{id}/disburse")
    public ResponseEntity<Loan> disburseLoan(@PathVariable Long id,
                                              @Valid @RequestBody DisburseLoanBody req) {
        return ResponseEntity.ok(disburseLoanService.execute(id, req.targetAccountNumber, req.analystUserId));
    }

    static class RequestLoanBody {
        @NotBlank String requestingClientId;
        @NotNull LoanType loanType;
        @NotNull @DecimalMin("0.01") BigDecimal requestedAmount;
        @NotNull @DecimalMin("0.01") BigDecimal interestRate;
        @NotNull @Min(1) Integer termMonths;
        @NotNull Long creatorUserId;
    }
    static class ApproveLoanBody {
        @NotNull @DecimalMin("0.01") BigDecimal approvedAmount;
        @NotNull @DecimalMin("0.01") BigDecimal interestRate;
        @NotNull Long analystUserId;
        String targetAccount;
    }
    static class DisburseLoanBody {
        @NotBlank String targetAccountNumber;
        @NotNull Long analystUserId;
    }
}
