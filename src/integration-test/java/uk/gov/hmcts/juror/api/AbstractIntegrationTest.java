package uk.gov.hmcts.juror.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.moj.exception.MojException;

import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ActiveProfiles("test")
@SuppressWarnings({
    "PMD.AbstractClassWithoutAbstractMethod",
    "PMD.TooManyMethods"
})
public abstract class AbstractIntegrationTest {
    @SuppressWarnings("SpringJavaAutowiredMembersInspection")
    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Value("${jwt.secret.bureau}")
    protected String bureauSecret;

    /**
     * Reset sequences in the database. This method must be called when overriding!
     *
     * @throws Exception Failed to reset sequences.
     */
    @Before
    public void setUp() throws Exception {
        // reset each sequence in oracle using a stored procedure.
        bulkResetSequences(1000, "JUROR_DIGITAL.SPEC_NEED_SEQ", "JUROR_DIGITAL.CJS_EMPLOYMENT_SEQ",
            "JUROR_DIGITAL.CHANGE_LOG_SEQ", "JUROR_DIGITAL.CHANGE_LOG_ITEM_SEQ");
    }

    /**
     * Call the Oracle procedure "JUROR_DIGITAL.RESET_SEQ_BULK" to reset specific sequences in bulk.
     * Note: The value supplied to this function will be the value returned by the first call to SEQUENCE.nextval
     *
     * @param resetToValue  Next val to be returned when the sequence is invoked.
     * @param sequenceNames A list of all the sequences to be reset.
     * @throws SQLException Failed to reset sequence
     */
    private void bulkResetSequences(final Integer resetToValue, final String... sequenceNames) throws SQLException {
        final String sequenceNameCommaSeperated = String.join(",", sequenceNames);
        final SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
            .withSchemaName("JUROR_DIGITAL")
            .withProcedureName("RESET_SEQ_BULK");
        final HashMap<String, String> args = new HashMap<>();
        args.put("P_NAME", sequenceNameCommaSeperated);
        args.put("P_VAL", resetToValue.toString());
        final SqlParameterSource params = new MapSqlParameterSource(args);
        jdbcCall.execute(params);
        log.info("Reset sequence {} to {}.", sequenceNameCommaSeperated, resetToValue);
    }

    protected HttpHeaders initialiseHeaders(String userLevel, Boolean passwordWarning,
                                            String loginUser, int daysToExpire, String owner) {

        BureauJWTPayload payload = buildJwtPayload(userLevel, passwordWarning, loginUser, daysToExpire, owner);
        return buildHttpHeaders(payload);
    }

    protected HttpHeaders initialiseHeaders(String userLevel, Boolean passwordWarning,
                                            String loginUser, int daysToExpire, String owner,
                                            BureauJWTPayload.Staff staff) {

        BureauJWTPayload payload = buildJwtPayload(userLevel, passwordWarning, loginUser, daysToExpire, owner);
        payload.setStaff(staff);
        return buildHttpHeaders(payload);
    }

    private BureauJWTPayload buildJwtPayload(String userLevel, Boolean passwordWarning, String loginUser,
                                             int daysToExpire, String owner) {
        return BureauJWTPayload.builder()
            .userLevel(userLevel)
            .passwordWarning(passwordWarning)
            .login(loginUser)
            .daysToExpire(daysToExpire)
            .owner(owner)
            .build();
    }

    private HttpHeaders buildHttpHeaders(BureauJWTPayload payload) {
        final String bureauJwt = TestUtil.mintBureauJwt(payload, SignatureAlgorithm.HS256, bureauSecret,
            Instant.now().plus(100L * 365L, ChronoUnit.DAYS));

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        return httpHeaders;
    }

    protected String mintBureauJwt(final BureauJWTPayload payload) throws Exception {
        return TestUtil.mintBureauJwt(payload, SignatureAlgorithm.HS256, bureauSecret, Instant.now().plus(100L * 365L,
            ChronoUnit.DAYS));
    }

    protected String createBureauJwt(String login, String owner) throws Exception {
        return mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("1")
            .login(login)
            .staff(BureauJWTPayload.Staff.builder()
                .name("Test User")
                .active(1)
                .rank(1)
                .build())
            .daysToExpire(89)
            .owner(owner)
            .build());
    }

    protected void validateForbiddenResponse(ResponseEntity<String> response, String url) {
        validateErrorResponse(response,
            HttpStatus.FORBIDDEN,
            url,
            AccessDeniedException.class,
            "Forbidden");
    }

    @SneakyThrows
    protected void validateErrorResponse(ResponseEntity<String> response,
                                         HttpStatus status,
                                         String url,
                                         Class<? extends Exception> exceptionClass,
                                         String message) {
        assertThat(response).isNotNull();
        log.debug("Response: {}", response);
        assertThat(response.getStatusCode()).isEqualTo(status);

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatusCode(status);
        errorResponse.setException(exceptionClass);
        errorResponse.setMessage(message);
        errorResponse.setPath(url);
        JSONAssert
            .assertEquals("Json Should match",
                TestUtil.parseToJsonString(errorResponse),
                response.getBody(), false);
    }

    @SneakyThrows
    protected void validateBusinessRuleViolation(ResponseEntity<String> response, String url, String message,
                                                 MojException.BusinessRuleViolation.ErrorCode code) {

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        JSONAssert
            .assertEquals("Json Should match",
                TestUtil.parseToJsonString(Map.of(
                    "message", message,
                    "code", code.name()
                )),
                response.getBody(), false);
    }

    protected void validateInternalServerErrorViolation(ResponseEntity<String> response, String url,
                                                        String expectedError) {
        validateErrorResponse(response,
            HttpStatus.INTERNAL_SERVER_ERROR,
            url,
            MojException.InternalServerError.class,
            expectedError);
    }

    protected void validateInvalidPathParam(ResponseEntity<String> response, String url,
                                            String expectedMessage) throws JsonProcessingException {

        assertThat(response).isNotNull();
        log.debug("Response: {}", response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        InvalidPathParam errorResponse = new InvalidPathParam();
        errorResponse.setMessage(expectedMessage);
        JSONAssert
            .assertEquals("Json Should match",
                TestUtil.parseToJsonString(errorResponse),
                response.getBody(), false);
    }

    @SneakyThrows
    protected void validateNotFound(ResponseEntity<String> response, String url, String message) {
        validateErrorResponse(response,
            HttpStatus.NOT_FOUND,
            url,
            MojException.NotFound.class,
            message);
    }

    @SneakyThrows
    protected void validateInvalidPayload(ResponseEntity<String> response,
                                          String... errors) {
        log.debug("Response: {}", response);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        InvalidPayload invalidPayload = new InvalidPayload();
        invalidPayload.setStatus(400);
        invalidPayload.setErrors(List.of(errors));


        JSONAssert
            .assertEquals("Json Should match",
                TestUtil.parseToJsonString(invalidPayload),
                response.getBody(), false);
    }

    @Data
    private static class InvalidPayload {
        private int status;
        private List<String> errors;
    }

    @Data
    private static class InvalidPathParam {
        private String message;
    }

    @Data
    private static class ErrorResponse {
        private int status;
        private String error;
        private String exception;
        private String message;
        private String path;

        public void setException(Class<? extends Exception> exceptionClass) {
            this.exception = exceptionClass == null ? null : exceptionClass.getName();
        }

        public void setStatusCode(HttpStatus status) {
            this.status = status.value();
            this.error = status.getReasonPhrase();
        }
    }
}
