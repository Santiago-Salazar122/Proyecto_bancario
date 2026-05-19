package com.bank.infrastructure.api.v1;

import com.bank.domain.enums.TransferStatus;
import com.bank.domain.model.transfer.Transfer;
import com.bank.domain.ports.TransferRepositoryPort;
import com.bank.domain.service.*;
import com.bank.domain.valueobject.Money;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

/** Controller REST para transferencias. URL base: /api/v1/transfers */
@RestController
@RequestMapping("/api/v1/transfers")
public class TransferController {

    private final CreateTransferService createTransferService;
    private final ApproveTransferService approveTransferService;
    private final RejectTransferService rejectTransferService;
    private final TransferRepositoryPort transferRepository;

    public TransferController(CreateTransferService c, ApproveTransferService a,
                               RejectTransferService r, TransferRepositoryPort tr) {
        this.createTransferService = c; this.approveTransferService = a;
        this.rejectTransferService = r; this.transferRepository = tr;
    }

    @PostMapping
    public ResponseEntity<Transfer> createTransfer(@Valid @RequestBody CreateTransferBody req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(createTransferService.execute(
            req.sourceAccount, req.targetAccount, new Money(req.amount), req.creatorUserId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transfer> findById(@PathVariable Long id) {
        return transferRepository.findById(id).map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Transfer>> listAll(@RequestParam(required = false) TransferStatus status) {
        if (status != null) return ResponseEntity.ok(transferRepository.findByStatus(status));
        return ResponseEntity.ok(transferRepository.findAll());
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Transfer>> findPending() {
        return ResponseEntity.ok(transferRepository.findPendingApproval());
    }

    @GetMapping("/source/{accountNumber}")
    public ResponseEntity<List<Transfer>> findBySourceAccount(@PathVariable String accountNumber) {
        return ResponseEntity.ok(transferRepository.findBySourceAccount(accountNumber));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<Transfer> approve(@PathVariable Long id, @RequestParam Long approverUserId) {
        return ResponseEntity.ok(approveTransferService.execute(id, approverUserId));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<Transfer> reject(@PathVariable Long id, @RequestParam Long approverUserId) {
        return ResponseEntity.ok(rejectTransferService.execute(id, approverUserId));
    }

    static class CreateTransferBody {
        @NotBlank String sourceAccount;
        @NotBlank String targetAccount;
        @NotNull @DecimalMin("0.01") BigDecimal amount;
        @NotNull Long creatorUserId;
    }
}
