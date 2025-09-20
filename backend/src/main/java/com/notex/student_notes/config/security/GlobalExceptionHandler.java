package com.notex.student_notes.config.security;

import com.notex.student_notes.summary.exceptions.SummaryGenerationFailedException;
import com.notex.student_notes.auth.dto.NoChangesProvidedException;
import com.notex.student_notes.auth.exceptions.*;
import com.notex.student_notes.config.exceptions.RateLimitExceededException;
import com.notex.student_notes.group.exceptions.*;
import com.notex.student_notes.note.exceptions.*;
import com.notex.student_notes.user.exceptions.UserNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<Map<String, Object>> buildErrorResponse(Exception ex, HttpStatus status, String message){
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("details", ex.getMessage());
        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> errors = new LinkedHashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Validation Failed");
        body.put("message", "One or more fields have errors");
        body.put("errors", errors);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String,Object>> handleEntityNotFoundException(EntityNotFoundException ex) {
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND, "Resource not found");
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<Map<String,Object>> handleAuthorizationDeniedException(AuthorizationDeniedException ex) {
        return buildErrorResponse(ex, HttpStatus.FORBIDDEN, "Access denied");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String,Object>> handleAccessDeniedException(AccessDeniedException ex) {
        return buildErrorResponse(ex, HttpStatus.FORBIDDEN, "Access denied");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String,Object>> handleGenericException(Exception ex) {
        return buildErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, "An internal server error occurred");
    }

    @ExceptionHandler(EmailSendingException.class)
    public ResponseEntity<Map<String, Object>> handleEmailSendingException(Exception ex){
        return buildErrorResponse(ex, HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    }
    @ExceptionHandler(InvalidVerificationCodeException.class)
    public ResponseEntity<Map<String,Object>> handleInvalidVerificationCodeException(Exception ex){
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND, ex.getMessage());
    }
    @ExceptionHandler(VerificationCodeExpiredException.class)
    public ResponseEntity<Map<String,Object>> handleVerificationCodeExpiredException(Exception ex){
        return buildErrorResponse(ex, HttpStatus.GONE, ex.getMessage());
    }
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String,Object>> handleUserAlreadyExistsException(Exception ex){
        return buildErrorResponse(ex, HttpStatus.CONFLICT, ex.getMessage());
    }
    @ExceptionHandler(UserAlreadyVerifiedException.class)
    public ResponseEntity<Map<String,Object>> handleUserAlreadyVerifiedException(Exception ex){
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, ex.getMessage());
    }
    @ExceptionHandler(UserNotVerifiedException.class)
    public ResponseEntity<Map<String,Object>> handleUserNotVerifiedException(Exception ex){
        return buildErrorResponse(ex, HttpStatus.FORBIDDEN, ex.getMessage());
    }
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String,Object>> handleUserNotFoundException(Exception ex){
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND, ex.getMessage());
    }
    @ExceptionHandler(SamePasswordException.class)
    public ResponseEntity<Map<String, Object>> handleSamePasswordException(Exception ex){
        return buildErrorResponse(ex, HttpStatus.CONFLICT, ex.getMessage());
    }
    @ExceptionHandler(NoChangesProvidedException.class)
    public ResponseEntity<Map<String, Object>> handleNoChangesProvidedException(Exception ex){
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, ex.getMessage());
    }
    @ExceptionHandler(NoteNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoteNotFoundException(Exception ex){
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND, ex.getMessage());
    }
    @ExceptionHandler(NoteDeletedException.class)
    public ResponseEntity<Map<String, Object>> handleNoteDeletedException(Exception ex){
        return buildErrorResponse(ex, HttpStatus.GONE, ex.getMessage());
    }
    @ExceptionHandler(NoteImageDeleteException.class)
    public ResponseEntity<Map<String, Object>> handleNoteImageDeletingException(Exception ex){
        return buildErrorResponse(ex, HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    }
    @ExceptionHandler(NoteImageUploadException.class)
    public ResponseEntity<Map<String, Object>> handleNoteImageUploadingException(Exception ex){
        return buildErrorResponse(ex, HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    }
    @ExceptionHandler(AddUserRequestInvalidException.class)
    public ResponseEntity<Map<String, Object>> handleAddUserRequestInvalidException(Exception ex){
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, ex.getMessage());
    }
    @ExceptionHandler(GroupNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleGroupNotFoundException(Exception ex){
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND, ex.getMessage());
    }
    @ExceptionHandler(GroupAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleGroupAlreadyExists(Exception ex){
        return buildErrorResponse(ex, HttpStatus.CONFLICT, ex.getMessage());
    }
    @ExceptionHandler(InvalidGroupCreateRequestException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidGroupCreateRequest(Exception ex){
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, ex.getMessage());
    }
    @ExceptionHandler(UserAlreadyInGroupException.class)
    public ResponseEntity<Map<String, Object>> handleUserAlreadyInGroupException(Exception ex){
        return buildErrorResponse(ex, HttpStatus.CONFLICT, ex.getMessage());
    }
    @ExceptionHandler(GroupDeletedException.class)
    public ResponseEntity<Map<String, Object>> handleGroupDeletedException(Exception ex){
        return buildErrorResponse(ex, HttpStatus.GONE, ex.getMessage());
    }
    @ExceptionHandler(InvalidGroupUpdateRequestException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidGroupUpdateRequestException(Exception ex){
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, ex.getMessage());
    }
    @ExceptionHandler(UserNotInGroupException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotInGroupException(Exception ex){
        return buildErrorResponse(ex, HttpStatus.FORBIDDEN, ex.getMessage());
    }
    @ExceptionHandler(UserNotGroupOwnerException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotGroupOwnerException(Exception ex){
        return buildErrorResponse(ex, HttpStatus.FORBIDDEN, ex.getMessage());
    }
    @ExceptionHandler(UserNotNoteOwner.class)
    public ResponseEntity<Map<String, Object>> handleUserNotNoteOwner(Exception ex){
        return buildErrorResponse(ex, HttpStatus.FORBIDDEN, ex.getMessage());
    }
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handleRateLimitExceededException(Exception ex){
        return buildErrorResponse(ex, HttpStatus.TOO_MANY_REQUESTS, ex.getMessage());
    }
    @ExceptionHandler(EmptyNoteException.class)
    public ResponseEntity<Map<String, Object>> handleEmptyNoteException(Exception ex){
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, ex.getMessage());
    }
    @ExceptionHandler(SummaryGenerationFailedException.class)
    public ResponseEntity<Map<String, Object>> handleSummaryGenerationFailedException(Exception ex){
        return buildErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }
}
