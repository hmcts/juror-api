package uk.gov.hmcts.juror.api.moj.controller;

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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.jurorer.domain.Deadline;
import uk.gov.hmcts.juror.api.jurorer.domain.EmailRequestStatus;
import uk.gov.hmcts.juror.api.jurorer.domain.LaUser;
import uk.gov.hmcts.juror.api.jurorer.domain.LocalAuthority;
import uk.gov.hmcts.juror.api.jurorer.repository.DeadlineRepository;
import uk.gov.hmcts.juror.api.jurorer.repository.LaUserRepository;
import uk.gov.hmcts.juror.api.jurorer.repository.LocalAuthorityRepository;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.ActiveLaRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.DeactiveLaRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.UpdateDeadlineRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.UpdateDeadlineResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.UpdateEmailRequestSentDto;
import uk.gov.hmcts.juror.api.moj.domain.UserType;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the ER Administration controller.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SuppressWarnings("PMD.ExcessiveImports") // false positive, the imports are necessary for the test
class ErAdministrationControllerITest extends AbstractIntegrationTest {

    public static final String EXPECT_THE_STATUS_TO_BE_FORBIDDEN = "Expect the status to be forbidden.";
    private final TestRestTemplate restTemplate;

    private HttpHeaders httpHeaders;

    private final LocalAuthorityRepository localAuthorityRepository;
    private final DeadlineRepository deadlineRepository;
    private final LaUserRepository laUserRepository;

    @BeforeEach
    public void setUp() throws Exception {
        initHeadersBureau();
    }


    @Nested
    @DisplayName("GET /api/v1/moj/er-administration/deactivate-la")
    @Sql({"/db/mod/truncate.sql","/db/jurorer/ErDashboardData.sql"})
    class DeactivateLaTest {

        @Test
        void testDeactivateLocalAuthority() {

            DeactiveLaRequestDto requestDto = new DeactiveLaRequestDto();
            requestDto.setLaCode("002");
            requestDto.setReason("This is a test reason for deactivating LA2");

            ResponseEntity<Void> responseEntity =
                restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, HttpMethod.PUT,
                                         URI.create("/api/v1/moj/er-administration/deactivate-la")), Void.class);

            assertThat(responseEntity.getStatusCode())
                .as("Expect the status to be OK.")
                .isEqualTo(HttpStatus.OK);

            // the initial data has LA2 as active, so we can check that it has been deactivated
            // and the reason has been set correctly

            executeInTransaction(() -> {
                Optional<LocalAuthority> localAuthority = localAuthorityRepository.findByLaCode("002");
                assertThat(localAuthority)
                    .as("Expect the local authority to be present.")
                    .isPresent();
                assertThat(localAuthority.get().getActive())
                    .as("Expect the local authority to be deactivated.")
                    .isFalse();
                assertThat(localAuthority.get().getInactiveReason())
                    .as("Expect the local authority to have the correct inactive reason.")
                    .isEqualTo("This is a test reason for deactivating LA2");
            });

        }

        @Test
        void testDeactivateLaForCourtUserShouldBeForbidden() {
            initHeadersCourt();
            DeactiveLaRequestDto requestDto = new DeactiveLaRequestDto();
            requestDto.setLaCode("LA2");
            requestDto.setReason("This is a test reason for deactivating LA2");

            ResponseEntity<Void> responseEntity =
                restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, HttpMethod.PUT,
                                              URI.create("/api/v1/moj/er-administration/deactivate-la")), Void.class);

            assertThat(responseEntity.getStatusCode())
                .as(EXPECT_THE_STATUS_TO_BE_FORBIDDEN)
                .isEqualTo(HttpStatus.FORBIDDEN);

        }

    }


    @Nested
    @DisplayName("GET /api/v1/moj/er-administration/activate-la")
    @Sql({"/db/mod/truncate.sql","/db/jurorer/ErDashboardData.sql"})
    class ActivateLaTest {

        @Test
        void testActivateLocalAuthority() {

            ActiveLaRequestDto requestDto = new ActiveLaRequestDto("004");

            ResponseEntity<Void> responseEntity =
                restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, HttpMethod.PUT,
                                                  URI.create("/api/v1/moj/er-administration/activate-la")),
                                      Void.class);

            assertThat(responseEntity.getStatusCode())
                .as("Expect the status to be OK.")
                .isEqualTo(HttpStatus.OK);

            // the initial data has LA4 as inactive, so we can check that it has been activated
            // and the reason has been cleared (set to null)

            executeInTransaction(() -> {
                Optional<LocalAuthority> localAuthorityOpt = localAuthorityRepository.findByLaCode("004");
                assertThat(localAuthorityOpt)
                    .as("Expect the local authority to be present.")
                    .isPresent();
                LocalAuthority localAuthority = localAuthorityOpt.get();
                assertThat(localAuthority.getActive())
                    .as("Expect the local authority to be activated.")
                    .isTrue();
                assertThat(localAuthority.getInactiveReason())
                    .as("Expect the local authority to have no inactive reason (null).")
                    .isNull();

                List<LaUser> laUsers = laUserRepository.findByLocalAuthority(localAuthority);

                // also check that the users associated with the LA have been activated
                laUsers.forEach(user -> assertThat(user.isActive())
                    .as("Expect the users associated with the LA to be activated.")
                    .isTrue());
            });
        }

        @Test
        void testAactivateLaForCourtUserShouldBeForbidden() {
            initHeadersCourt();
            ActiveLaRequestDto requestDto = new ActiveLaRequestDto("004");

            ResponseEntity<Void> responseEntity =
                restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, HttpMethod.PUT,
                                                          URI.create("/api/v1/moj/er-administration/activate-la")),
                                      Void.class);

            assertThat(responseEntity.getStatusCode())
                .as(EXPECT_THE_STATUS_TO_BE_FORBIDDEN)
                .isEqualTo(HttpStatus.FORBIDDEN);

        }

    }

    @Nested
    @DisplayName("PUT /api/v1/moj/er-administration/deadline")
    @Sql({"/db/mod/truncate.sql","/db/jurorer/ErDashboardData.sql"})
    @SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage") // false positive
    class ChangeDeadlineTest {

        @Test
        void testChangeDeadlineHappy() {

            UpdateDeadlineRequestDto requestDto = new UpdateDeadlineRequestDto();
            requestDto.setDeadlineDate(LocalDate.now().plusDays(30));

            ResponseEntity<UpdateDeadlineResponseDto> responseEntity =
                restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, HttpMethod.PUT,
                                              URI.create("/api/v1/moj/er-administration/deadline")),
                                      UpdateDeadlineResponseDto.class);

            assertThat(responseEntity.getStatusCode())
                .as("Expect the status to be OK.")
                .isEqualTo(HttpStatus.OK);

            UpdateDeadlineResponseDto responseBody = responseEntity.getBody();
            assertThat(responseBody)
                .as("Expect the response body to be present.")
                .isNotNull();
            assertThat(responseBody.getDeadlineDate())
                .as("Expect the response body to have the correct deadline date.")
                .isEqualTo(LocalDate.now().plusDays(30));
            assertThat(responseBody.getDaysRemaining()).as("Expect the response body to have the correct days "
                                                               + "remaining.")
                .isEqualTo(30L);
            assertThat(responseBody.getLastUpdated())
                .as("Expect the response body to have the last updated date and time set to now.")
                .isBetween(LocalDateTime.now().minusSeconds(5), LocalDateTime.now().plusSeconds(5));
            assertThat(responseBody.getUpdatedBy())
                .as("Expect the response body to have the updated by field set to the current user.")
                .isEqualTo("BUREAU_USER");

            executeInTransaction(() -> {
                Optional<Deadline> deadline = deadlineRepository.getCurrentDeadline();
                assertThat(deadline)
                    .as("Expect the deadline to be present.")
                    .isPresent();
                assertThat(deadline.get().getDeadlineDate())
                    .as("Expect the deadline date to be updated.")
                    .isEqualTo(LocalDate.now().plusDays(30));

                // also check the audit fields have been updated correctly
                assertThat(deadline.get().getLastUpdated())
                    .as("Expect the last updated date to be set to now.")
                    .isBetween(LocalDateTime.now().minusSeconds(5), LocalDateTime.now().plusSeconds(5));
                assertThat(deadline.get().getUpdatedBy())
                    .as("Expect the updated by field to be set to the current user.")
                    .isEqualTo("BUREAU_USER");

            });

        }

        @Test
        void testChangeDeadlineForCourtUserShouldBeForbidden() {
            initHeadersCourt();

            UpdateDeadlineRequestDto requestDto = new UpdateDeadlineRequestDto();
            requestDto.setDeadlineDate(LocalDate.now().plusDays(30));

            ResponseEntity<UpdateDeadlineResponseDto> responseEntity =
                restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, HttpMethod.PUT,
                                                          URI.create("/api/v1/moj/er-administration/deadline")),
                                      UpdateDeadlineResponseDto.class);

            assertThat(responseEntity.getStatusCode())
                .as(EXPECT_THE_STATUS_TO_BE_FORBIDDEN)
                .isEqualTo(HttpStatus.FORBIDDEN);

        }

    }

    @Nested
    @DisplayName("PUT /api/v1/moj/er-administration/email-sent")
    @Sql({"/db/mod/truncate.sql","/db/jurorer/ErDashboardData.sql"})
    @SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage") // false positive
    class UpdateEmailRequestSentTest {

        @Test
        void testEmailRequestSentHappy() {

            // the initial data has LA1 and LA2 with the email request sent flag is null
            UpdateEmailRequestSentDto requestDto = new UpdateEmailRequestSentDto();
            requestDto.setLaCode("001");
            requestDto.setEmailRequestStatus(EmailRequestStatus.UNDELIVERED);

            ResponseEntity<Void> responseEntity =
                restTemplate.exchange(
                    new RequestEntity<>(
                        requestDto, httpHeaders, HttpMethod.PUT,
                        URI.create("/api/v1/moj/er-administration/email-sent")
                    ),
                    Void.class
                );

            assertThat(responseEntity.getStatusCode())
                .as("Expect the status to be OK.")
                .isEqualTo(HttpStatus.OK);

            executeInTransaction(() -> {
                Optional<LocalAuthority> localAuthorityOpt = localAuthorityRepository.findByLaCode("001");
                assertThat(localAuthorityOpt).isPresent();
                LocalAuthority localAuthority1 = localAuthorityOpt.get();
                assertThat(localAuthority1.getEmailRequestStatus())
                    .as("Expect LA 001 to have the email request sent flag set to UNDELIVERED.")
                    .isEqualTo(EmailRequestStatus.UNDELIVERED);

            });
        }

        @Test
        void testUpdateEmailSentForCourtUserShouldBeForbidden() {
            initHeadersCourt();
            UpdateEmailRequestSentDto requestDto = new UpdateEmailRequestSentDto();
            requestDto.setLaCode("001");
            requestDto.setEmailRequestStatus(EmailRequestStatus.UNDELIVERED);

            ResponseEntity<UpdateDeadlineResponseDto> responseEntity =
                restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, HttpMethod.PUT,
                                                          URI.create("/api/v1/moj/er-administration/email-sent")),
                                      UpdateDeadlineResponseDto.class);

            assertThat(responseEntity.getStatusCode())
                .as(EXPECT_THE_STATUS_TO_BE_FORBIDDEN)
                .isEqualTo(HttpStatus.FORBIDDEN);

        }

    }

    private void initHeadersCourt() {
        httpHeaders =
            initialiseHeaders("COURT_USER", UserType.COURT,null,"435");
    }

    private void initHeadersBureau() {
        httpHeaders =
            initialiseHeaders("BUREAU_USER", UserType.BUREAU,null,"400");
    }
}
