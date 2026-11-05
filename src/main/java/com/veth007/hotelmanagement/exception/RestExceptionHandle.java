package com.veth007.hotelmanagement.exception;

import java.util.List;
import org.springframework.validation.BindException;
import javax.naming.AuthenticationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class RestExceptionHandle {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidation(MethodArgumentNotValidException e) {
        List<String> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .toList();
        return ResponseEntity.status(400)
                .body(ApiResponse.error(errors.toString(), 400));
    }

    // ✅ @ModelAttribute / @RequestParam binding failure
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<?>> handleBind(BindException e) {
        List<String> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .toList();
        return ResponseEntity.status(400)
                .body(ApiResponse.error(errors.toString(), 400));
    }

    // ✅ Wrong type in request param (e.g. string instead of long)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<?>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        String msg = e.getName() + " should be of type " + e.getRequiredType().getSimpleName();
        return ResponseEntity.status(400)
                .body(ApiResponse.error(msg, 400));
    }

    // ✅ Missing required @RequestParam
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<?>> handleMissingParam(MissingServletRequestParameterException e) {
        return ResponseEntity.status(400)
                .body(ApiResponse.error("Missing parameter: " + e.getParameterName(), 400));
    }

    // ✅ Invalid JSON body
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<?>> handleUnreadable(HttpMessageNotReadableException e) {
        return ResponseEntity.status(400)
                .body(ApiResponse.error("Malformed JSON request", 400));
    }

    // ✅ Wrong HTTP method (POST instead of PUT, etc.)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<?>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return ResponseEntity.status(405)
                .body(ApiResponse.error("Method not allowed: " + e.getMethod(), 405));
    }

    // ✅ Unsupported Content-Type
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<?>> handleMediaType(HttpMediaTypeNotSupportedException e) {
        return ResponseEntity.status(415)
                .body(ApiResponse.error("Unsupported media type: " + e.getContentType(), 415));
    }

    // ✅ JPA / DB entity not found
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleNotFound(EntityNotFoundException e) {
        return ResponseEntity.status(404)
                .body(ApiResponse.error(e.getMessage(), 404));
    }

    // ✅ DB constraint violation (duplicate key, FK, etc.)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleDataIntegrity(DataIntegrityViolationException e) {
        return ResponseEntity.status(409)
                .body(ApiResponse.error("Data integrity violation: " + e.getRootCause().getMessage(), 409));
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
public ResponseEntity<ApiResponse<?>> handleAuthorizationDenied(AuthorizationDeniedException e) {
    return ResponseEntity.status(403)
            .body(ApiResponse.error("Access denied", 403));
}

    // ✅ Access denied (Spring Security)
    // @ExceptionHandler(AccessDeniedException.class)
    // public ResponseEntity<ApiResponse<?>> handleAccessDenied(AccessDeniedException e) {
    //     return ResponseEntity.status(403)
    //             .body(ApiResponse.error("Access denied", 403));
    // }

    // ✅ Unauthenticated (Spring Security)
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<?>> handleAuthentication(AuthenticationException e) {
        return ResponseEntity.status(401)
                .body(ApiResponse.error("Unauthorized: " + e.getMessage(), 401));
    }

    // ✅ Your custom business logic exceptions
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.status(400)
                .body(ApiResponse.error(e.getMessage(), 400));
    }

    // ✅ Unauthorized — returns 401
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<?>> handleUnauthorized(UnauthorizedException e) {
        return ResponseEntity.status(401)
                .body(ApiResponse.error(e.getMessage(), 401));
    }

    // ✅ Bad credentials — returns 401
    @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
    public ResponseEntity<ApiResponse<?>> handleBadCredentials(org.springframework.security.authentication.BadCredentialsException e) {
        return ResponseEntity.status(401)
                .body(ApiResponse.error("Invalid email or password", 401));
    }

    // ✅ Resource not found — returns 404
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleResourceNotFound(NotFoundException e) {
        return ResponseEntity.status(404)
                .body(ApiResponse.error(e.getMessage(), 404));
    }

    // return 422
    @ExceptionHandler(BusinessException.class)
public ResponseEntity<ApiResponse<?>> handleBusiness(BusinessException e) {
    return ResponseEntity.status(422)
            .body(ApiResponse.error(e.getMessage(), 422));
}

   

    // ✅ Already exists / conflict — returns 409
    @ExceptionHandler(AlreadyExistException.class)
    public ResponseEntity<ApiResponse<?>> handleAlreadyExist(AlreadyExistException e) {
        return ResponseEntity.status(409)
                .body(ApiResponse.error(e.getMessage(), 409));
    }

    // ✅ Fallback — catch anything else
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGeneric(Exception e) {
        e.printStackTrace(); // Log for debugging
        return ResponseEntity.status(500)
                .body(ApiResponse.error("Internal server error: " + e.getMessage(), 500));
    }
//      @ExceptionHandler(Exception.class)
//     public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex) {
//         ErrorResponse err = new ErrorResponse(ex.getMessage(), (short) 500);
//         return ResponseEntity.status(500).body(err);
//     }

//     @ExceptionHandler(AlreadyExistException.class)
//     public ResponseEntity<ErrorResponse> handleAlreadyExistException(AlreadyExistException ex) {
//         ErrorResponse err = new ErrorResponse(ex.getMessage(), (short) 409);
//         return ResponseEntity.status(409).body(err);
//     }

//     @ExceptionHandler(NotFoundException.class)
//     public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException ex) {
//         ErrorResponse err = new ErrorResponse(ex.getMessage(), (short) 404);
//         return ResponseEntity.status(404).body(err);
//     }

//     @ExceptionHandler(MethodArgumentNotValidException.class)
//     public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
//    String message = "Invalid value";
//         for (ObjectError err : ex.getBindingResult().getAllErrors()) {
//             message = err.getDefaultMessage();
//         }
//         ErrorResponse err = new ErrorResponse(message, (short) 400);
//         return ResponseEntity.status(400).body(err);
//     }

//        @ExceptionHandler(IllegalArgumentException.class)
//     public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
//         return ResponseEntity.badRequest().body(Map.of(
//             "error", ex.getMessage(),
//             "allowedValues", Arrays.toString(ShiftGroupBy.values())
//         ));
//     }

//     // ✅ Wrong type in request param
//     @ExceptionHandler(MethodArgumentTypeMismatchException.class)
//     public ResponseEntity<?> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
//         return ResponseEntity.badRequest().body(Map.of(
//             "error", "Invalid value: '" + ex.getValue() + "'",
//             "allowedValues", Arrays.toString(ShiftGroupBy.values())
//         ));
//     }
}
