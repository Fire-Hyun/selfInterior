package com.selfinterior.api.common.web;

import com.selfinterior.api.common.api.ApiError;
import com.selfinterior.api.common.api.ApiException;
import com.selfinterior.api.common.api.ApiResponse;
import com.selfinterior.api.common.api.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(ApiException.class)
  public ResponseEntity<ApiResponse<Void>> handleApiException(
      ApiException exception, HttpServletRequest request) {
    return ResponseEntity.status(exception.getHttpStatus())
        .body(
            ApiResponse.error(
                new ApiError(
                    exception.getErrorCode().name(),
                    exception.getMessage(),
                    (String) request.getAttribute(RequestIdFilter.REQUEST_ID_ATTR))));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Void>> handleValidationException(
      MethodArgumentNotValidException exception, HttpServletRequest request) {
    String message =
        exception.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .orElse("잘못된 요청입니다.");
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(
            ApiResponse.error(
                new ApiError(
                    ErrorCode.BAD_REQUEST.name(),
                    message,
                    (String) request.getAttribute(RequestIdFilter.REQUEST_ID_ATTR))));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleUnhandledException(
      Exception exception, HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
            ApiResponse.error(
                new ApiError(
                    ErrorCode.INTERNAL_ERROR.name(),
                    exception.getMessage(),
                    (String) request.getAttribute(RequestIdFilter.REQUEST_ID_ATTR))));
  }
}
