package uk.gov.hmcts.juror.api;

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
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.domain.Role;
import uk.gov.hmcts.juror.api.moj.domain.UserType;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.exception.RestResponseEntityExceptionHandler;
import uk.gov.hmcts.juror.api.testsupport.ContainerTest;

import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ActiveProfiles("test")
@SuppressWarnings({
    "PMD.AbstractClassWithoutAbstractMethod",
    "PMD.TooManyMethods",
    "PMD.ExcessiveImports"
})
public abstract class AbstractIntegrationTest extends ContainerTest {

    protected static final String BUREAU_USER = "BUREAU_USER";
    protected static final String COURT_USER = "COURT_USER";

    @SuppressWarnings("SpringJavaAutowiredMembersInspection")
    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Value("${jwt.secret.bureau}")
    protected String bureauSecret;

    @Value("${jwt.secret.hmac}")
    protected String hmacSecret;

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

    protected HttpHeaders initialiseHeaders(String loginUser, UserType userType, Set<Role> roles, String owner) {
        BureauJwtPayload payload = createBureauJwtPayload(loginUser, userType, roles, owner);
        return buildHttpHeaders(payload);
    }

    protected HttpHeaders initialiseHeaders(String loginUser, UserType userType, Set<Role> roles, String owner,
                                            BureauJwtPayload.Staff staff) {
        BureauJwtPayload payload = createBureauJwtPayload(loginUser, userType, roles, owner);
        payload.setStaff(staff);
        return buildHttpHeaders(payload);
    }

    private HttpHeaders buildHttpHeaders(BureauJwtPayload payload) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintBureauJwt(payload));
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return httpHeaders;
    }

    protected String mintBureauJwt(final BureauJwtPayload payload) {
        return TestUtil.mintBureauJwt(payload, SignatureAlgorithm.HS256, bureauSecret,
            Instant.now().plus(100L * 365L, ChronoUnit.DAYS));
    }

    protected String createHmacJwt() {
        return TestUtil.mintHmacJwt(SignatureAlgorithm.HS256, hmacSecret,
            Instant.now().plus(100L * 365L, ChronoUnit.DAYS));
    }


    protected String createJwtBureau(String login) {
        return createJwtBureau(login, Collections.emptyList());
    }

    protected String createJwtBureau(String login, Collection<Role> roles) {
        return createJwt(login, "400", UserType.BUREAU, roles, "400");
    }

    protected String createJwtCourt(String login, Collection<Role> roles, String owner, String... courts) {
        return createJwt(login, owner, UserType.COURT, roles, courts);
    }

    protected String createJwtAdministrator(String login) {
        return createJwt(login, "400", UserType.ADMINISTRATOR, null);
    }

    protected String createJwt(String login, String owner, String... courts) {
        return createJwt(login, Collections.emptyList(), owner, courts);
    }

    protected String createJwt(String login, Collection<Role> roles, String owner, String... courts) {
        if ("400".equals(owner)) {
            return createJwtBureau(login, roles);
        }
        return createJwtCourt(login, roles, owner, courts);
    }

    protected String createJwt(String login, String owner, UserType userType, Collection<Role> roles,
                               String... courts) {
        return mintBureauJwt(createBureauJwtPayload(login, userType, roles, owner, courts));
    }

    @Deprecated(forRemoval = true)
    protected String createBureauJwt(String login, String owner, String... courts) {
        return createJwt(login, owner, null, null, courts);
    }


    @Deprecated(forRemoval = true)
    protected String createBureauJwt(String login, String owner, int rank) {
        return mintBureauJwt(BureauJwtPayload.builder()
            .userLevel(String.valueOf(rank))
            .login(login)
            .staff(BureauJwtPayload.Staff.builder()
                .name("Test User")
                .active(1)
                .rank(rank)
                .build())
            .owner(owner)
            .build());
    }

    protected BureauJwtPayload createBureauJwtPayload(String login, UserType userType, Collection<Role> roles,
                                                      String owner, String... courts) {
        Set<String> courtsToSet = new HashSet<>(Arrays.asList(courts));
        courtsToSet.add(owner);

        return BureauJwtPayload.builder()
            .login(login)
            .staff(BureauJwtPayload.Staff.builder()
                .name("Test User")
                .active(1)
                .courts(new ArrayList<>(courtsToSet))
                .build())
            .locCode(courts.length >= 1 ? courts[0] : null)
            .userType(userType)
            .activeUserType(userType)
            .roles(roles)
            .owner(owner)
            .build();
    }

    protected void assertForbiddenResponse(ResponseEntity<String> response, String url) {
        assertErrorResponse(response,
            HttpStatus.FORBIDDEN,
            url,
            AuthorizationDeniedException.class,
            "Forbidden");
    }


    protected void assertMojForbiddenResponse(ResponseEntity<String> response, String url, String message) {
        assertErrorResponse(response,
            HttpStatus.FORBIDDEN,
            url,
            MojException.Forbidden.class,
            message);
    }


    @SneakyThrows
    protected void assertErrorResponse(ResponseEntity<String> response,
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

    protected void assertBusinessRuleViolation(ResponseEntity<String> response, String message,
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

    protected void assertInternalServerErrorViolation(ResponseEntity<String> response, String url,
                                                      String expectedError) {
        assertErrorResponse(response,
            HttpStatus.INTERNAL_SERVER_ERROR,
            url,
            MojException.InternalServerError.class,
            expectedError);
    }

    protected void assertInternalServerErrorViolation(ResponseEntity<String> response, String url,
                                                      Class<? extends Exception> expectedException,
                                                      String expectedError) {
        assertErrorResponse(response,
            HttpStatus.INTERNAL_SERVER_ERROR,
            url,
            expectedException,
            expectedError);
    }

    protected void assertInvalidPathParam(ResponseEntity<String> response,
                                          String expectedMessage) {

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
    protected void assertNotFound(ResponseEntity<String> response, String url, String message) {
        assertErrorResponse(response,
            HttpStatus.NOT_FOUND,
            url,
            MojException.NotFound.class,
            message);
    }


    @SneakyThrows
    protected void assertInvalidPayload(ResponseEntity<String> response,
                                        RestResponseEntityExceptionHandler.FieldError... errors) {
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
        private List<RestResponseEntityExceptionHandler.FieldError> errors;
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

    public String getBureauJwt() {
        return createJwt("test_bureau_standard", "400",
            UserType.BUREAU, Set.of(), "400");
    }

    public String getCourtJwt(String number) {
        return getCourtJwt(number, Set.of());
    }

    public String getCourtJwt(String number, Set<Role> roles) {
        return createJwt("test_court_standard", number,
            UserType.COURT, roles, number);
    }
}
