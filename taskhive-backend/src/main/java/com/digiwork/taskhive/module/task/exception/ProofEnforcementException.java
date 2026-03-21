package com.digiwork.taskhive.module.task.exception;

import com.digiwork.taskhive.common.exception.BusinessException;

public class ProofEnforcementException extends BusinessException {

    public ProofEnforcementException() {
        super("TASK_3003", "Proof of completion is required before submitting this task for review");
    }

    public ProofEnforcementException(String detail) {
        super("TASK_3003", detail);
    }
}
