package app.infrastructure.api.v1;

import app.application.service.LoanApplicationService;
import app.domain.model.loan.LoanApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/loans")
public class LoanApplicationController {

    private final LoanApplicationService loanApplicationService;

    public LoanApplicationController(LoanApplicationService loanApplicationService) {
        this.loanApplicationService = loanApplicationService;
    }

    @PostMapping("/request")
    public ResponseEntity<LoanApplication> requestLoan(@RequestBody LoanRequest request) {
        LoanApplication loanApplication = loanApplicationService.requestLoanApplication(
            request.getRequestedAmount(),
            request.getInterestRate(),
            request.getTermMonths()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(loanApplication);
    }

    public static class LoanRequest {

        private BigDecimal requestedAmount;
        private Double interestRate;
        private Integer termMonths;

        public LoanRequest() {
        }

        public BigDecimal getRequestedAmount() {
            return requestedAmount;
        }

        public void setRequestedAmount(BigDecimal requestedAmount) {
            this.requestedAmount = requestedAmount;
        }

        public Double getInterestRate() {
            return interestRate;
        }

        public void setInterestRate(Double interestRate) {
            this.interestRate = interestRate;
        }

        public Integer getTermMonths() {
            return termMonths;
        }

        public void setTermMonths(Integer termMonths) {
            this.termMonths = termMonths;
        }
    }
}
