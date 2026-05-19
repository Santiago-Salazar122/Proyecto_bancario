package com.bank.infrastructure.api.v1;

import com.bank.domain.enums.AccountType;
import com.bank.domain.model.account.BankAccount;
import com.bank.domain.ports.BankAccountRepositoryPort;
import com.bank.domain.service.*;
import com.bank.domain.valueobject.Money;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

/** Controller REST para cuentas bancarias. URL base: /api/v1/accounts */
@RestController
@RequestMapping("/api/v1/accounts")
public class BankAccountController {

    private final OpenBankAccountService openAccountService;
    private final DepositToBankAccountService depositService;
    private final WithdrawFromBankAccountService withdrawService;
    private final BlockBankAccountService blockService;
    private final ReactivateBankAccountService reactivateService;
    private final CancelBankAccountService cancelService;
    private final BankAccountRepositoryPort accountRepository;

    public BankAccountController(OpenBankAccountService o, DepositToBankAccountService d,
                                  WithdrawFromBankAccountService w, BlockBankAccountService b,
                                  ReactivateBankAccountService r, CancelBankAccountService c,
                                  BankAccountRepositoryPort ar) {
        this.openAccountService = o; this.depositService = d; this.withdrawService = w;
        this.blockService = b; this.reactivateService = r; this.cancelService = c;
        this.accountRepository = ar;
    }

    /** POST /api/v1/accounts — Abre una cuenta nueva */
    @PostMapping
    public ResponseEntity<BankAccount> openAccount(@Valid @RequestBody OpenAccountRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            openAccountService.execute(req.ownerIdentificationId, req.accountType, req.currency, req.operatorUserId));
    }

    /** GET /api/v1/accounts/{accountNumber} — Consulta una cuenta */
    @GetMapping("/{accountNumber}")
    public ResponseEntity<BankAccount> findByAccountNumber(@PathVariable String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
            .map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /** GET /api/v1/accounts/owner/{ownerId} — Cuentas de un titular */
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<BankAccount>> findByOwner(@PathVariable String ownerId) {
        return ResponseEntity.ok(accountRepository.findByOwnerId(ownerId));
    }

    /** GET /api/v1/accounts — Lista todas */
    @GetMapping
    public ResponseEntity<List<BankAccount>> listAll() {
        return ResponseEntity.ok(accountRepository.findAll());
    }

    /** POST /api/v1/accounts/{accountNumber}/deposit */
    @PostMapping("/{accountNumber}/deposit")
    public ResponseEntity<BankAccount> deposit(@PathVariable String accountNumber,
                                                @Valid @RequestBody AmountRequest req) {
        return ResponseEntity.ok(depositService.execute(accountNumber, new Money(req.amount), req.operatorUserId));
    }

    /** POST /api/v1/accounts/{accountNumber}/withdraw */
    @PostMapping("/{accountNumber}/withdraw")
    public ResponseEntity<BankAccount> withdraw(@PathVariable String accountNumber,
                                                 @Valid @RequestBody AmountRequest req) {
        return ResponseEntity.ok(withdrawService.execute(accountNumber, new Money(req.amount), req.operatorUserId));
    }

    /** PATCH /api/v1/accounts/{accountNumber}/block?operatorUserId=1 */
    @PatchMapping("/{accountNumber}/block")
    public ResponseEntity<BankAccount> block(@PathVariable String accountNumber,
                                              @RequestParam Long operatorUserId) {
        return ResponseEntity.ok(blockService.execute(accountNumber, operatorUserId));
    }

    /** PATCH /api/v1/accounts/{accountNumber}/reactivate?operatorUserId=1 */
    @PatchMapping("/{accountNumber}/reactivate")
    public ResponseEntity<BankAccount> reactivate(@PathVariable String accountNumber,
                                                   @RequestParam Long operatorUserId) {
        return ResponseEntity.ok(reactivateService.execute(accountNumber, operatorUserId));
    }

    /** PATCH /api/v1/accounts/{accountNumber}/cancel?operatorUserId=1 */
    @PatchMapping("/{accountNumber}/cancel")
    public ResponseEntity<BankAccount> cancel(@PathVariable String accountNumber,
                                               @RequestParam Long operatorUserId) {
        return ResponseEntity.ok(cancelService.execute(accountNumber, operatorUserId));
    }

    static class OpenAccountRequest {
        @NotBlank String ownerIdentificationId;
        @NotNull AccountType accountType;
        @NotBlank String currency;
        @NotNull Long operatorUserId;
    }

    static class AmountRequest {
        @NotNull @DecimalMin("0.01") BigDecimal amount;
        @NotNull Long operatorUserId;
    }
}
