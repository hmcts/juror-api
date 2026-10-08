package uk.gov.hmcts.juror.api.moj.exception;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.juror.api.juror.service.PublicAuthenticationServiceImpl;

import java.sql.SQLException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RestResponseEntityExceptionHandlerTest {

    private final RestResponseEntityExceptionHandler handler = new RestResponseEntityExceptionHandler();

    @Test
    void handleInternalServerError_sqlException_returnsGenericMessage() {
        SQLException exception = new SQLException("ERROR: relation juror_mod.secret_table does not exist");

        ResponseEntity<Object> response = handler.handleInternalServerError(exception, null);

        assertGenericInternalServerError(response);
    }

    @Test
    void handleInternalServerError_mojInternalServerError_returnsGenericMessage() {
        MojException.InternalServerError exception = new MojException.InternalServerError(
            "Error while fetching daily utilisation stats",
            new SQLException("ERROR: syntax error at or near select")
        );

        ResponseEntity<Object> response = handler.handleInternalServerError(exception, null);

        assertGenericInternalServerError(response);
    }

    @Test
    void handleDataIntegrityViolationException_returnsGenericMessage() {
        DataIntegrityViolationException exception = new DataIntegrityViolationException(
            "duplicate key value violates unique constraint juror_pool_pkey"
        );

        ResponseEntity<Object> response = handler.handleDataIntegrityViolationException(exception, null);

        assertGenericInternalServerError(response);
    }

    @Test
    void handleMojBadRequest_returnsApplicationMessage() {
        String message = "Juror status is already on call";
        MojException.BadRequest exception = new MojException.BadRequest(message, null);

        ResponseEntity<Object> response = handler.handleMojBadRequest(exception, null);

        assertApplicationError(response, HttpStatus.BAD_REQUEST, message, MojException.BadRequest.class);
    }

    @Test
    void handleMojNotFound_returnsApplicationMessage() {
        String message = "No appearances found for juror: 123456789";
        MojException.NotFound exception = new MojException.NotFound(message, null);

        ResponseEntity<Object> response = handler.handleMojNotFound(exception, null);

        assertApplicationError(response, HttpStatus.NOT_FOUND, message, MojException.NotFound.class);
    }

    @Test
    void handleUnhandledException_returnsGenericMessage() {
        RuntimeException exception = new RuntimeException("sensitive implementation detail");

        ResponseEntity<Object> response = handler.handleUnhandledException(exception, null);

        assertGenericInternalServerError(response);
    }

    @Test
    void handleUnhandledException_invalidJurorCredentials_returnsApplicationMessage() {
        String message = "Invalid credentials";
        PublicAuthenticationServiceImpl.InvalidJurorCredentialsException exception =
            new PublicAuthenticationServiceImpl.InvalidJurorCredentialsException(message);

        ResponseEntity<Object> response = handler.handleUnhandledException(exception, null);

        assertApplicationError(
            response,
            HttpStatus.UNAUTHORIZED,
            message,
            PublicAuthenticationServiceImpl.InvalidJurorCredentialsException.class);
    }

    @Test
    void handleUnhandledException_badCredentials_returnsApplicationMessage() {
        String message = "Bad credentials";
        PublicAuthenticationServiceImpl.InvalidJurorCredentialsException exception =
            new PublicAuthenticationServiceImpl.InvalidJurorCredentialsException(message);

        ResponseEntity<Object> response = handler.handleUnhandledException(exception, null);

        assertApplicationError(
            response,
            HttpStatus.UNAUTHORIZED,
            message,
            PublicAuthenticationServiceImpl.InvalidJurorCredentialsException.class);
    }

    @Test
    void handleUnhandledException_jurorAlreadyResponded_returnsApplicationMessage() {
        String message = "Juror already responded";
        PublicAuthenticationServiceImpl.JurorAlreadyRespondedException exception =
            new PublicAuthenticationServiceImpl.JurorAlreadyRespondedException(message);

        ResponseEntity<Object> response = handler.handleUnhandledException(exception, null);

        assertApplicationError(
            response,
            HttpStatus.CONFLICT,
            message,
            PublicAuthenticationServiceImpl.JurorAlreadyRespondedException.class);
    }

    @Test
    void handleUnhandledException_jurorAccountBlocked_returnsApplicationMessage() {
        String message = "Juror account is locked";
        PublicAuthenticationServiceImpl.JurorAccountBlockedException exception =
            new PublicAuthenticationServiceImpl.JurorAccountBlockedException(message);

        ResponseEntity<Object> response = handler.handleUnhandledException(exception, null);

        assertApplicationError(
            response,
            HttpStatus.FORBIDDEN,
            message,
            PublicAuthenticationServiceImpl.JurorAccountBlockedException.class);
    }

    @Test
    void handleUnhandledException_courtDateLapsed_returnsApplicationMessage() {
        String message = "Not allowed. Court Date has already passed";
        PublicAuthenticationServiceImpl.CourtDateLapsedException exception =
            new PublicAuthenticationServiceImpl.CourtDateLapsedException(message);

        ResponseEntity<Object> response = handler.handleUnhandledException(exception, null);

        assertApplicationError(
            response,
            HttpStatus.FORBIDDEN,
            message,
            PublicAuthenticationServiceImpl.CourtDateLapsedException.class);
    }

    private static void assertGenericInternalServerError(ResponseEntity<Object> response) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isInstanceOf(Map.class);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        assertThat(body)
            .containsEntry("message", "An unexpected error occurred")
            .containsKey("timestamp");
    }

    private static void assertApplicationError(ResponseEntity<Object> response, HttpStatus status,
                                               String message, Class<?> exceptionClass) {
        assertThat(response.getStatusCode()).isEqualTo(status);
        assertThat(response.getBody()).isInstanceOf(Map.class);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        assertThat(body)
            .containsEntry("status", status.value())
            .containsEntry("error", status.getReasonPhrase())
            .containsEntry("exception", exceptionClass.getName())
            .containsEntry("message", message)
            .containsKey("timestamp");
    }
}
