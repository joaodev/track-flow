package com.joaodev.trackflowapi.auth.service;

import com.joaodev.trackflowapi.common.error.ApiException;

public class AccountDeactivatedException extends RuntimeException implements ApiException {
    public AccountDeactivatedException() {
        super("This account has been deactivated");
    }

    @Override
    public String getErrorCode() {
        return "ACCOUNT_DEACTIVATED";
    }
}
