package com.bank.application.port.in;

import com.bank.domain.model.transfer.Transfer;
import com.bank.domain.valueobject.Money;

public interface TransferUseCase {

    Transfer createTransfer(int transferId, String sourceAccount, String targetAccount, Money amount, int creatorUserId);

    Transfer approveTransfer(int transferId, int approverUserId);

    Transfer rejectTransfer(int transferId, int approverUserId);

    void expirePendingTransfers();
}
