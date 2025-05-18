package com.flamedavid.eurovision.exceptions.handlers;

import com.flamedavid.eurovision.dtos.ErrorResponseDTO;
import com.flamedavid.eurovision.exceptions.AppException;
import com.flamedavid.eurovision.exceptions.BadRequestException;
import com.flamedavid.eurovision.exceptions.ForbiddenException;
import com.flamedavid.eurovision.exceptions.NotFoundException;
import com.flamedavid.eurovision.exceptions.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(value = BadRequestException.class)
    public ResponseEntity<Object> handleAppException(BadRequestException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponseDTO(ex.getMessage(), HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(value = ForbiddenException.class)
    public ResponseEntity<Object> handleAppException(ForbiddenException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(new ErrorResponseDTO(ex.getMessage(), HttpStatus.FORBIDDEN.value()));
    }

    @ExceptionHandler(value = NotFoundException.class)
    public ResponseEntity<Object> handleAppException(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponseDTO(ex.getMessage(), HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(value = UnauthorizedException.class)
    public ResponseEntity<Object> handleAppException(UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new ErrorResponseDTO(ex.getMessage(), HttpStatus.UNAUTHORIZED.value()));
    }

    @ExceptionHandler(value = AppException.class)
    public ResponseEntity<Object> handleAppException(AppException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponseDTO(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
}
