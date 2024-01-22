package uk.gov.hmcts.juror.api.moj.exception;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice(basePackages = {"uk.gov.hmcts.juror.api.moj"})
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @ExceptionHandler({PoolRequestException.DuplicatePoolRequest.class,
        PoolRequestException.PoolRequestDateInvalid.class})
    public ResponseEntity<Object> handlePoolRequestException(PoolRequestException ex, WebRequest request) {
        Map<String, Object> body = createGenericErrorResponseBody(ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({PoolCreateException.InvalidNoOfCitizensToSummon.class,
        PoolCreateException.InvalidNoOfJurorsRequested.class,
        PoolCreateException.InvalidAddCitizensToCoronersPool.class})
    public ResponseEntity<Object> handlePoolCreateException(PoolCreateException ex, WebRequest request) {
        Map<String, Object> body = createGenericErrorResponseBody(ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({PoolCreateException.InvalidNoOfCitizensToSummonForYield.class})
    public ResponseEntity<Object> handlePoolCreateInvalidYield(PoolCreateException ex, WebRequest request) {
        Map<String, Object> body = createGenericErrorResponseBody(ex.getMessage());
        String reason = ex.getClass().getAnnotation(ResponseStatus.class).reason();
        body.put("reasonCode", reason);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({PoolCreateException.CoronerPoolNotFound.class})
    public ResponseEntity<Object> handleCoronerPoolException(PoolCreateException ex, WebRequest request) {
        Map<String, Object> body = createGenericErrorResponseBody(ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<Object> handleIllegalArgumentException(RuntimeException ex, WebRequest request) {
        Map<String, Object> body = createGenericErrorResponseBody(ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ConstraintViolationException.class})
    public ResponseEntity<Object> handleValidationException(ValidationException ex, WebRequest request) {
        Map<String, Object> body = createGenericErrorResponseBody(ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);

    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Object> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                                   WebRequest request) {
        Map<String, Object> body = createGenericErrorResponseBody(ex.getValue()
            + " is the incorrect data type or is not in the expected format (" + ex.getName() + ")");
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({PoolDeleteException.PoolHasMembersException.class,
        PoolDeleteException.UnableToDeletePoolException.class})
    public ResponseEntity<Object> handlePoolDeleteException(PoolDeleteException ex, WebRequest request) {
        Map<String, Object> body = createGenericErrorResponseBody(ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({PoolDeleteException.InsufficientPermission.class})
    public ResponseEntity<Object> handlePoolDeleteInsufficientPermissionException(PoolDeleteException ex,
                                                                                  WebRequest request) {
        Map<String, Object> body = createGenericErrorResponseBody(ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({UserPermissionsException.CourtUnavailable.class})
    public ResponseEntity<Object> handleUserPermissionsPermissionException(UserPermissionsException ex,
                                                                           WebRequest request) {
        Map<String, Object> body = createGenericErrorResponseBody(ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(JurorRecordException.CannotAccessJurorRecord.class)
    public ResponseEntity<Object> handleJurorRecordAccessException(JurorRecordException ex, WebRequest request) {
        Map<String, Object> body = createGenericErrorResponseBody(ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({JurorRecordException.NoJurorRecordFound.class})
    public ResponseEntity<Object> handleJurorRecordNotFoundException(JurorRecordException ex, WebRequest request) {
        Map<String, Object> body = createGenericErrorResponseBody(ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({JurorPaperResponseException.JurorPaperResponseAlreadyExists.class,
        JurorPaperResponseException.InvalidCjsEmploymentEntry.class,
        JurorPaperResponseException.InvalidSpecialNeedEntry.class,
        JurorPaperResponseException.JurorPaperResponseDoesNotExist.class,
        JurorPaperResponseException.InvalidEligibilityEntry.class,
        JurorPaperResponseException.InvalidReplyTypeEntry.class})
    public ResponseEntity<Object> handlePaperResponseException(JurorPaperResponseException ex, WebRequest request) {
        Map<String, Object> body = createGenericErrorResponseBody(ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({MojException.BusinessRuleViolation.class})
    public ResponseEntity<Object> handleMojExceptionBusinessRuleViolation(MojException.BusinessRuleViolation ex,
                                                                          WebRequest request) {
        Map<String, Object> body = createGenericErrorResponseBody(ex.getMessage());
        body.put("code", ex.getErrorCode());
        return new ResponseEntity<>(body, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(value = {SQLException.class,
        DateException.DateParseException.class,
        JurorPaperResponseException.UnableToFindJurorRecord.class,
        JurorRecordException.MultipleJurorRecordsFound.class,
        PoolCreateException.UnableToCreatePool.class,
        JurorSequenceException.SequenceNextValNotFound.class
    })
    public ResponseEntity<Object> handleInternalServerError(Throwable ex, WebRequest request) {
        Map<String, Object> body = createGenericErrorResponseBody(ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<Object> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex, HttpHeaders headers,
        HttpStatusCode status, WebRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().format(dateTimeFormatter));
        body.put("status", status.value());

        List<String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .collect(Collectors.toList());

        body.put("errors", errors);

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    private Map<String, Object> createGenericErrorResponseBody(String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().format(dateTimeFormatter));
        body.put("message", message);
        return body;
    }

}
