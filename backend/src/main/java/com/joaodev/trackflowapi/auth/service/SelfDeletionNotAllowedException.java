package com.joaodev.trackflowapi.auth.service;

public class SelfDeletionNotAllowedException extends RuntimeException {
    public SelfDeletionNotAllowedException() {
        super("You cannot delete your own account");
    }
}