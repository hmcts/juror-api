package uk.gov.hmcts.juror.api.moj.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.moj.controller.request.messages.BureauEmailRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.messages.EmailTemplateName;
import uk.gov.hmcts.juror.api.moj.controller.response.messages.BureauEmailResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;
import uk.gov.hmcts.juror.api.moj.exception.RestResponseEntityExceptionHandler;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("BureauMessagingControllerITest: /api/v1/moj/messages/bureau/send")
@Sql({"/db/mod/truncate.sql", "/db/mod/BureauMessagingControllerITest_typical.sql"})
@SuppressWarnings("PMD.ExcessiveImports")
class BureauMessagingControllerITest extends AbstractIntegrationTest {

    private static final String URL = "/api/v1/moj/messages/bureau/send";
    private static final String VALID_JUROR_NUMBER = "610000050";
    private static final String VALID_EMAIL = "test.juror@example.com";

    @Autowired
    private TestRestTemplate template;
    @Autowired
    private JurorHistoryRepository jurorHistoryRepository;

    private HttpHeaders httpHeaders;

    @BeforeEach
    void setUp() {
        httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    }

    private URI toUri() {
        return URI.create(URL);
    }

    private BureauEmailRequestDto.JurorEmailDetail detail(String jurorNumber, String email,
                                                          EmailTemplateName templateName) {
        return BureauEmailRequestDto.JurorEmailDetail.builder()
            .jurorNumber(jurorNumber)
            .email(email)
            .emailTemplateName(templateName)
            .build();
    }

    @Nested
    @DisplayName("Positive")
    class Positive {

        private ResponseEntity<BureauEmailResponseDto> triggerValid(BureauEmailRequestDto request) {
            final String jwt = createJwt(BUREAU_USER, "400", "400");
            httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);

            return template.exchange(
                new RequestEntity<>(request, httpHeaders, POST, toUri()),
                BureauEmailResponseDto.class);
        }

        @Test
        void typicalContactInformation() {
            BureauEmailRequestDto request = BureauEmailRequestDto.builder()
                .jurorEmails(List.of(detail(VALID_JUROR_NUMBER, VALID_EMAIL,
                                            EmailTemplateName.CONTACT_INFORMATION)))
                .build();

            ResponseEntity<BureauEmailResponseDto> response = triggerValid(request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            BureauEmailResponseDto body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.getTotalJurorsRequested()).isEqualTo(1);
            assertThat(body.getSuccessfulEmailsSent()).isEqualTo(1);
            assertThat(body.getFailedNotifications()).isEmpty();

            List<JurorHistory> history = jurorHistoryRepository.findByJurorNumberOrderById(VALID_JUROR_NUMBER);
            assertThat(history).isNotEmpty();
            JurorHistory lastHistory = history.get(history.size() - 1);
            assertThat(lastHistory.getHistoryCode()).isEqualTo(HistoryCodeMod.NOTIFY_MESSAGE_REQUESTED);
            assertThat(lastHistory.getOtherInformation())
                .isEqualTo("Bureau email sent: " + EmailTemplateName.CONTACT_INFORMATION.name());
            assertThat(lastHistory.getCreatedBy()).isEqualTo(BUREAU_USER);
        }

        @Test
        void typicalReferralConfirmed() {
            BureauEmailRequestDto request = BureauEmailRequestDto.builder()
                .jurorEmails(List.of(detail(VALID_JUROR_NUMBER, VALID_EMAIL,
                                            EmailTemplateName.REFERRAL_CONFIRMED)))
                .build();

            ResponseEntity<BureauEmailResponseDto> response = triggerValid(request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            BureauEmailResponseDto body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.getSuccessfulEmailsSent()).isEqualTo(1);
            assertThat(body.getFailedNotifications()).isEmpty();
        }

        @Test
        void jurorNotFoundReturnsFailureNotError() {
            BureauEmailRequestDto request = BureauEmailRequestDto.builder()
                .jurorEmails(List.of(detail("999999999", VALID_EMAIL,
                                            EmailTemplateName.CONTACT_INFORMATION)))
                .build();

            ResponseEntity<BureauEmailResponseDto> response = triggerValid(request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            BureauEmailResponseDto body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.getSuccessfulEmailsSent()).isEqualTo(0);
            assertThat(body.getFailedNotifications()).hasSize(1);
            assertThat(body.getFailedNotifications().get(0).getFailureReason())
                .isEqualTo(BureauEmailResponseDto.FailureReason.JUROR_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("Negative")
    class Negative {

        private ResponseEntity<String> triggerInvalid(BureauEmailRequestDto request, String jwt) {
            httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
            return template.exchange(
                new RequestEntity<>(request, httpHeaders, POST, toUri()),
                String.class);
        }

        @Test
        void unauthorisedNotBureauUser() {
            BureauEmailRequestDto request = BureauEmailRequestDto.builder()
                .jurorEmails(List.of(detail(VALID_JUROR_NUMBER, VALID_EMAIL,
                                            EmailTemplateName.CONTACT_INFORMATION)))
                .build();

            final String jwt = createJwt(COURT_USER, "415", "415");
            ResponseEntity<String> response = triggerInvalid(request, jwt);

            assertForbiddenResponse(response, URL);
        }

        @Test
        void invalidEmailFormat() {
            BureauEmailRequestDto request = BureauEmailRequestDto.builder()
                .jurorEmails(List.of(detail(VALID_JUROR_NUMBER, "not-an-email",
                                            EmailTemplateName.CONTACT_INFORMATION)))
                .build();

            final String jwt = createBureauJwt(BUREAU_USER, "400", "400");
            ResponseEntity<String> response = triggerInvalid(request, jwt);

            assertInvalidPayload(response,
                                 new RestResponseEntityExceptionHandler.FieldError(
                                     "jurorEmails[0].email", "Invalid email address"));
        }

        @Test
        void invalidJurorNumberFormat() {
            BureauEmailRequestDto request = BureauEmailRequestDto.builder()
                .jurorEmails(List.of(detail("123", VALID_EMAIL,
                                            EmailTemplateName.CONTACT_INFORMATION)))
                .build();

            final String jwt = createBureauJwt(BUREAU_USER, "400", "400");
            ResponseEntity<String> response = triggerInvalid(request, jwt);

            assertInvalidPayload(response,
                                 new RestResponseEntityExceptionHandler.FieldError(
                                     "jurorEmails[0].jurorNumber", "Invalid juror number"));
        }

        @Test
        void invalidTemplateNameEnumValue() {
            // raw JSON used directly since an invalid enum value can't be expressed via the builder
            final String jwt = createBureauJwt(BUREAU_USER, "400", "400");
            httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);

            String rawBody = """
                {
                  "juror_emails": [
                    {
                      "juror_number": "%s",
                      "email": "%s",
                      "email_template_name": "NOT_A_REAL_TEMPLATE"
                    }
                  ]
                }""".formatted(VALID_JUROR_NUMBER, VALID_EMAIL);

            ResponseEntity<String> response = template.exchange(
                new RequestEntity<>(rawBody, httpHeaders, POST, toUri()),
                String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        void emptyJurorEmailsList() {
            BureauEmailRequestDto request = BureauEmailRequestDto.builder()
                .jurorEmails(List.of())
                .build();

            final String jwt = createBureauJwt(BUREAU_USER, "400", "400");
            ResponseEntity<String> response = triggerInvalid(request, jwt);

            assertInvalidPayload(response,
                                 new RestResponseEntityExceptionHandler.FieldError(
                                     "jurorEmails", "At least one juror email detail must be provided"));
        }

        @Test
        void missingTemplateConfiguration() {
            // TODO: needs its own @Sql fixture (e.g. without app_setting rows) or a delete-then-call
            // pattern, since the class-level @Sql seeds both template IDs. Consider a nested @Sql
            // override or moving this to its own @Sql-decorated nested class.
        }
    }


}
