package uk.gov.hmcts.juror.api.moj.exception;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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

    private static void assertGenericInternalServerError(ResponseEntity<Object> response) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isInstanceOf(Map.class);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        assertThat(body)
            .containsEntry("message", "An unexpected error occurred")
            .containsKey("timestamp");
    }
}
