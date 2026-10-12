package vn.edu.ptit.fooddelivery.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** 409 – dùng khi hai tài xế tranh nhau nhận đơn hoặc trùng lặp tài nguyên. */
@ResponseStatus(HttpStatus.CONFLICT)
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
