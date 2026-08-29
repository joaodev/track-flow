package com.joaodev.trackflowapi.auth.service;

import com.joaodev.trackflowapi.common.error.ApiException;

public class SelfDeletionNotAllowedException extends RuntimeException implements ApiException {
    public SelfDeletionNotAllowedException() {
        super("You cannot delete your own account");
    }

    @Override
    public String getErrorCode() {
        return "SELF_DELETION_NOT_ALLOWED";
    }
}