package org.fsm.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AppException extends RuntimeException {

    private final HttpStatus status;
    private final String code; // optional business code

    public AppException(String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.code = null;
    }

    public AppException(String message, HttpStatus status, String code) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public AppException(String message, Throwable cause, HttpStatus status) {
        super(message, cause);
        this.status = status;
        this.code = null;
    }

    public class ResourceNotFoundException extends AppException {
        public ResourceNotFoundException(String message) {
            super(message, HttpStatus.NOT_FOUND, "NOT_FOUND");
        }
    }

    public class BadRequestException extends AppException {
        public BadRequestException(String message) {
            super(message, HttpStatus.BAD_REQUEST, "BAD_REQUEST");
        }
    }

    public class UnauthorizedException extends AppException {
        public UnauthorizedException(String message) {
            super(message, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
        }
    }

    public class ForbiddenException extends AppException {
        public ForbiddenException(String message) {
            super(message, HttpStatus.FORBIDDEN, "FORBIDDEN");
        }
    }

    public class ConflictException extends AppException {
        public ConflictException(String message) {
            super(message, HttpStatus.CONFLICT, "CONFLICT");
        }
    }

    public class PaymentFailedException extends AppException {
        public PaymentFailedException(String message) {
            super(message, HttpStatus.PAYMENT_REQUIRED, "PAYMENT_FAILED");
        }
    }
}
