package uk.gov.hmcts.juror.api.jurorer.controller;

import lombok.RequiredArgsConstructor;
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
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.jurorer.controller.dto.LaNotificationRequestDto;
import uk.gov.hmcts.juror.api.jurorer.domain.ReminderHistory;
import uk.gov.hmcts.juror.api.jurorer.repository.ReminderHistoryRepository;
import uk.gov.hmcts.juror.api.moj.domain.UserType;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Controller: " + LaNotificationControllerITest.BASE_URL)
@SuppressWarnings("PMD")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Sql(
    scripts = {"/db/jurorer/teardownLaNotificationControllerITest.sql"},
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
@Sql(
    scripts = {"/db/jurorer/setupLaNotificationControllerITest.sql"},
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
@Sql(
    scripts = {"/db/jurorer/teardownLaNotificationControllerITest.sql"},
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class LaNotificationControllerITest extends AbstractIntegrationTest {

    public static final String BASE_URL = "/api/v1/moj/notification";

    private final TestRestTemplate restTemplate;
    private final ReminderHistoryRepository reminderHistoryRepository;

    private HttpHeaders httpHeaders;

    @BeforeEach
    public void setUp() throws Exception {
        initBureauHeaders();
    }

    @Nested
    @DisplayName("POST /send-la-reminder")
    class SendLaReminder {

        private static final String URL = BASE_URL + "/send-la-reminder";

        @Test
        @DisplayName("Should successfully send notification to single LA with single user")
        void sendNotificationToSingleLaHappy() {
            LaNotificationRequestDto request = LaNotificationRequestDto.builder()
                .laCodes(List.of("002"))  // LA 002 has 1 user
                .build();

            ResponseEntity<Void> response = restTemplate.exchange(
                new RequestEntity<>(request, httpHeaders, POST, URI.create(URL)),
                Void.class);

            assertThat(response.getStatusCode())
                .as("HTTP status should be NO_CONTENT")
                .isEqualTo(HttpStatus.NO_CONTENT);

            // Verify reminder history was created
            List<ReminderHistory> histories = reminderHistoryRepository.findByLaCodeOrderByTimeSentDesc("002");
            assertThat(histories)
                .as("Reminder history should be created for LA 002")
                .hasSize(1);

            ReminderHistory history = histories.get(0);
            assertThat(history.getLaCode()).isEqualTo("002");
            assertThat(history.getSentBy()).isEqualTo(BUREAU_USER);
            assertThat(history.getSentTo()).isEqualTo("user1@la002.gov.uk");
            assertThat(history.getTimeSent()).isNotNull();
        }

        @Test
        @DisplayName("Should successfully send notifications to LA with multiple users")
        void sendNotificationToLaWithMultipleUsers() {
            LaNotificationRequestDto request = LaNotificationRequestDto.builder()
                .laCodes(List.of("001"))  // LA 001 has 2 users
                .build();

            ResponseEntity<Void> response = restTemplate.exchange(
                new RequestEntity<>(request, httpHeaders, POST, URI.create(URL)),
                Void.class);

            assertThat(response.getStatusCode())
                .as("HTTP status should be NO_CONTENT")
                .isEqualTo(HttpStatus.NO_CONTENT);

            // Verify reminder history was created for both users
            List<ReminderHistory> histories = reminderHistoryRepository.findByLaCodeOrderByTimeSentDesc("001");
            assertThat(histories)
                .as("Two reminder history entries should be created for LA 001")
                .hasSize(2);

            // Verify both email addresses are recorded
            assertThat(histories)
                .extracting(ReminderHistory::getSentTo)
                .containsExactlyInAnyOrder("user1@la001.gov.uk", "user2@la001.gov.uk");

            // Verify all have same sent_by
            assertThat(histories)
                .allMatch(h -> BUREAU_USER.equals(h.getSentBy()));
        }

        @Test
        @DisplayName("Should successfully send notifications to multiple LAs")
        void sendNotificationToMultipleLasHappy() {
            LaNotificationRequestDto request = LaNotificationRequestDto.builder()
                .laCodes(List.of("001", "002", "003"))
                .build();

            ResponseEntity<Void> response = restTemplate.exchange(
                new RequestEntity<>(request, httpHeaders, POST, URI.create(URL)),
                Void.class);

            assertThat(response.getStatusCode())
                .as("HTTP status should be NO_CONTENT")
                .isEqualTo(HttpStatus.NO_CONTENT);

            // Verify reminder history was created for all LAs
            // LA 001 has 2 users, LA 002 has 1 user, LA 003 has 1 user = 4 total
            assertThat(reminderHistoryRepository.findByLaCodeOrderByTimeSentDesc("001"))
                .as("Two reminder history entries for LA 001")
                .hasSize(2);

            assertThat(reminderHistoryRepository.findByLaCodeOrderByTimeSentDesc("002"))
                .as("One reminder history entry for LA 002")
                .hasSize(1);

            assertThat(reminderHistoryRepository.findByLaCodeOrderByTimeSentDesc("003"))
                .as("One reminder history entry for LA 003")
                .hasSize(1);
        }

        @Test
        @DisplayName("Should skip LA with no active users")
        void sendNotificationSkipsLaWithNoActiveUsers() {
            LaNotificationRequestDto request = LaNotificationRequestDto.builder()
                .laCodes(List.of("001", "004")) // 004 has only inactive user
                .build();

            ResponseEntity<Void> response = restTemplate.exchange(
                new RequestEntity<>(request, httpHeaders, POST, URI.create(URL)),
                Void.class);

            assertThat(response.getStatusCode())
                .as("HTTP status should be NO_CONTENT")
                .isEqualTo(HttpStatus.NO_CONTENT);

            // Verify only LA 001 has reminder history
            assertThat(reminderHistoryRepository.findByLaCodeOrderByTimeSentDesc("001"))
                .as("Reminder history should be created for LA 001")
                .hasSize(2);  // LA 001 has 2 active users

            assertThat(reminderHistoryRepository.findByLaCodeOrderByTimeSentDesc("004"))
                .as("No reminder history should be created for LA 004 (no active users)")
                .isEmpty();
        }

        @Test
        @DisplayName("Should return NO_CONTENT when LA code does not exist")
        void sendNotificationLaNotFound() {
            LaNotificationRequestDto request = LaNotificationRequestDto.builder()
                .laCodes(List.of("999"))
                .build();

            ResponseEntity<String> response = restTemplate.exchange(
                new RequestEntity<>(request, httpHeaders, POST, URI.create(URL)),
                String.class);

            assertThat(response.getStatusCode())
                .as("HTTP status should be NO CONTENT")
                .isEqualTo(HttpStatus.NO_CONTENT);

            // Verify no reminder history was created
            assertThat(reminderHistoryRepository.count())
                .as("No reminder history should be created")
                .isEqualTo(0);
        }

        @Test
        @DisplayName("Should return BAD_REQUEST for empty LA codes list")
        void sendNotificationEmptyLaCodes() {
            LaNotificationRequestDto request = LaNotificationRequestDto.builder()
                .laCodes(List.of())
                .build();

            ResponseEntity<String> response = restTemplate.exchange(
                new RequestEntity<>(request, httpHeaders, POST, URI.create(URL)),
                String.class);

            assertThat(response.getStatusCode())
                .as("HTTP status should be BAD_REQUEST")
                .isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("Should return BAD_REQUEST for invalid LA code format")
        void sendNotificationInvalidLaCodeFormat() {
            LaNotificationRequestDto request = LaNotificationRequestDto.builder()
                .laCodes(List.of("INVALID"))
                .build();

            ResponseEntity<String> response = restTemplate.exchange(
                new RequestEntity<>(request, httpHeaders, POST, URI.create(URL)),
                String.class);

            assertThat(response.getStatusCode())
                .as("HTTP status should be BAD_REQUEST")
                .isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("Should return BAD_REQUEST for null request body")
        void sendNotificationNullRequestBody() {
            ResponseEntity<String> response = restTemplate.exchange(
                new RequestEntity<>(null, httpHeaders, POST, URI.create(URL)),
                String.class);

            assertThat(response.getStatusCode())
                .as("HTTP status should be BAD_REQUEST")
                .isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("Should return FORBIDDEN when user is not Bureau user")
        void sendNotificationNotBureauUser() {
            // Create court user JWT instead of bureau
            String courtJwt = createJwt("COURT_USER", "415", UserType.COURT, Set.of(), "415");
            httpHeaders.set(HttpHeaders.AUTHORIZATION, courtJwt);

            LaNotificationRequestDto request = LaNotificationRequestDto.builder()
                .laCodes(List.of("001"))
                .build();

            ResponseEntity<String> response = restTemplate.exchange(
                new RequestEntity<>(request, httpHeaders, POST, URI.create(URL)),
                String.class);

            assertThat(response.getStatusCode())
                .as("HTTP status should be FORBIDDEN")
                .isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        @DisplayName("Should return INTERNAL_SERVER_ERROR when no JWT provided")
        void sendNotificationNoJwt() {
            httpHeaders.remove(HttpHeaders.AUTHORIZATION);

            LaNotificationRequestDto request = LaNotificationRequestDto.builder()
                .laCodes(List.of("001"))
                .build();

            ResponseEntity<String> response = restTemplate.exchange(
                new RequestEntity<>(request, httpHeaders, POST, URI.create(URL)),
                String.class);

            assertThat(response.getStatusCode())
                .as("HTTP status should be INTERNAL_SERVER_ERROR")
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        @Test
        @DisplayName("Should handle mixed valid and invalid LA codes gracefully")
        void sendNotificationMixedValidInvalid() {
            LaNotificationRequestDto request = LaNotificationRequestDto.builder()
                .laCodes(List.of("001", "999", "002")) // 999 doesn't exist
                .build();

            ResponseEntity<String> response = restTemplate.exchange(
                new RequestEntity<>(request, httpHeaders, POST, URI.create(URL)),
                String.class);

            // Should continue processing valid LAs even when invalid ones exist
            assertThat(response.getStatusCode())
                .as("HTTP status should be NO_CONTENT")
                .isEqualTo(HttpStatus.NO_CONTENT);

            // Verify valid LAs still got emails
            assertThat(reminderHistoryRepository.findByLaCodeOrderByTimeSentDesc("001"))
                .as("LA 001 should have received emails")
                .hasSize(2);

            assertThat(reminderHistoryRepository.findByLaCodeOrderByTimeSentDesc("002"))
                .as("LA 002 should have received email")
                .hasSize(1);
        }

        @Test
        @DisplayName("Should create multiple reminder history entries when called multiple times")
        void sendNotificationMultipleTimes() {
            LaNotificationRequestDto request = LaNotificationRequestDto.builder()
                .laCodes(List.of("002"))  // LA 002 has 1 user
                .build();

            // Send first notification
            ResponseEntity<Void> response1 = restTemplate.exchange(
                new RequestEntity<>(request, httpHeaders, POST, URI.create(URL)),
                Void.class);

            assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            // Send second notification to same LA
            ResponseEntity<Void> response2 = restTemplate.exchange(
                new RequestEntity<>(request, httpHeaders, POST, URI.create(URL)),
                Void.class);

            assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            // Verify two reminder history entries exist
            assertThat(reminderHistoryRepository.findByLaCodeOrderByTimeSentDesc("002"))
                .as("Two reminder history entries should exist for LA 002")
                .hasSize(2)
                .allMatch(h -> "user1@la002.gov.uk".equals(h.getSentTo()));
        }
    }

    private void initBureauHeaders() {
        BureauJwtPayload payload = BureauJwtPayload.builder()
            .login(BUREAU_USER)
            .staff(BureauJwtPayload.Staff.builder()
                       .name("Bureau User")
                       .active(1)
                       .build())
            .userType(UserType.BUREAU)
            .owner("400")
            .build();

        httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintBureauJwt(payload));
        httpHeaders.setAccept(Collections.singletonList(org.springframework.http.MediaType.APPLICATION_JSON));
    }
}
