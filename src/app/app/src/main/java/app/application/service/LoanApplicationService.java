package app.application.service;

import app.domain.model.loan.LoanApplication;
import app.infrastructure.persistence.jpa.repositorios.LoanApplicationRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class LoanApplicationService {

    private static final String STATUS_UNDER_REVIEW = "UNDER REVIEW";

    private final LoanApplicationRepository loanApplicationRepository;

    public LoanApplicationService(LoanApplicationRepository loanApplicationRepository) {
        this.loanApplicationRepository = loanApplicationRepository;
    }

    public LoanApplication requestLoanApplication(BigDecimal requestedAmount, Double interestRate, Integer termMonths) {
        validateRequestedAmount(requestedAmount);

        LoanApplication loanApplication = new LoanApplication(requestedAmount, interestRate, termMonths);
        loanApplication.setStatus(STATUS_UNDER_REVIEW);

        return loanApplicationRepository.save(loanApplication);
    }

    private void validateRequestedAmount(BigDecimal requestedAmount) {
        if (requestedAmount == null || requestedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("The requested amount must be greater than 0.");
        }
    }
}
