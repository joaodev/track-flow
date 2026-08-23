package com.joaodev.trackflowapi.auth.service;

public class AccountDeactivatedException extends RuntimeException {
    public AccountDeactivatedException() {
        super("This account has been deactivated");
    }
}
