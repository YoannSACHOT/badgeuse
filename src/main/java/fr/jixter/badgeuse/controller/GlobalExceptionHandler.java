package fr.jixter.badgeuse.controller;

import fr.jixter.badgeuse.exception.ResourceNotFoundException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  public static final String TIMESTAMP = "timestamp";
  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ProblemDetail> handleResourceNotFound(ResourceNotFoundException ex) {
    logger.error("Resource not found: {}", ex.getMessage());
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    problemDetail.setTitle("Resource Not Found");
    problemDetail.setProperty(TIMESTAMP, LocalDateTime.now());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
  }

  @ExceptionHandler(WebExchangeBindException.class)
  public ResponseEntity<ProblemDetail> handleValidationException(WebExchangeBindException ex) {
    String errorMsg = ex.getFieldErrors().stream()
        .map(error -> error.getField() + " : " + error.getDefaultMessage())
        .collect(Collectors.joining(", "));
    logger.error("Validation error: {}", errorMsg);
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, errorMsg);
    problemDetail.setTitle("Validation Error");
    problemDetail.setProperty(TIMESTAMP, LocalDateTime.now());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ProblemDetail> handleGenericException(Exception ex) {
    logger.error("Internal Server Error", ex);
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
        HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error: " + ex.getMessage());
    problemDetail.setTitle("Internal Server Error");
    problemDetail.setProperty(TIMESTAMP, LocalDateTime.now());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
  }
}
