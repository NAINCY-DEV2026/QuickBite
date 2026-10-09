package com.quickbite.backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.time.LocalDateTime;

// Runs before GlobalExceptionHandler, whose catch-all would otherwise turn these into 500s
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class FileUploadExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> tooLarge(MaxUploadSizeExceededException ex, HttpServletRequest req) {
        return build(HttpStatus.valueOf(413), "File is too large. Maximum size is 5 MB.", req);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorResponse> missingPart(MissingServletRequestPartException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Please choose a file to upload.", req);
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ErrorResponse> invalidUpload(MultipartException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Invalid file upload.", req);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, HttpServletRequest req) {
        return ResponseEntity.status(status).body(ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .message(message)
                .path(req.getRequestURI())
                .build());
    }
}