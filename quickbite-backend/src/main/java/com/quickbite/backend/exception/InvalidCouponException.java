package com.quickbite.backend.exception;

public class InvalidCouponException extends RuntimeException {
    public InvalidCouponException(String message) { super(message); }
}