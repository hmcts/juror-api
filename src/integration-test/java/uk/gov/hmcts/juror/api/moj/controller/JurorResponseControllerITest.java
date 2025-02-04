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
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.JurorDigitalApplication;
import uk.gov.hmcts.juror.api.bureau.controller.request.BureauResponseStatusUpdateDto;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorPaperResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorPersonalDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.summonsmanagement.JurorResponseRetrieveRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.summonsmanagement.JurorResponseRetrieveResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.BulkPrintData;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.Role;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.domain.UserType;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.AbstractJurorResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.PaperResponse;
import uk.gov.hmcts.juror.api.moj.enumeration.DisqualifyCode;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;
import uk.gov.hmcts.juror.api.moj.enumeration.ReplyMethod;
import uk.gov.hmcts.juror.api.moj.repository.BulkPrintDataRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.repository.UserRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorPaperResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorReasonableAdjustmentRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorResponseAuditRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorResponseCjsEmploymentRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorResponseCommonRepositoryMod;
import uk.gov.hmcts.juror.api.moj.utils.DataUtils;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

/**
 * Integration tests for the API endpoints defined in {@link JurorResponseController}.
 */

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Controller: /api/v1/moj/juror-response/")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SuppressWarnings({
    "java:S2259",
    "java:S5960",
    "PMD.ExcessiveImports",
    "PMD.TooManyMethods"
})
class JurorResponseControllerITest extends AbstractIntegrationTest {
    private HttpHeaders httpHeaders;

    private static final String JUROR_NUMBER_987654321 = "987654321";
    private static final String JUROR_NUMBER_123456789 = "123456789";
    private static final String JUROR_NUMBER_111111111 = "111111111";
    private static final String JUROR_NUMBER_111222333 = "111222333";
    private static final String JUROR_NUMBER_222222222 = "222222222";
    private static final String JUROR_NUMBER_555555555 = "555555555";

    private static final String URI_PERSONAL_DETAILS = "/api/v1/moj/juror-response/juror/%s/details/personal";
    private static final String URI_RETRIEVE_JUROR_RESPONSES = "/api/v1/moj/juror-response/retrieve";

    private static final String BUREAU_USER = "BUREAU_USER";
    private static final String COURT_USER = "COURT_USER";
    private static final String TEAM_LEADER = "TEAM_LEADER";

    private static final String OFFICER_ASSIGNED_BUREAU_OFFICER = "bureauOfficer";

    private static final String OWNER_400 = "400";
    private static final String OWNER_415 = "415";

    private final TestRestTemplate template;
    private final JurorPaperResponseRepositoryMod jurorPaperResponseRepository;
    private final JurorDigitalResponseRepositoryMod jurorDigitalResponseRepository;
    private final JurorReasonableAdjustmentRepository jurorReasonableAdjustmentRepository;
    private final JurorResponseCjsEmploymentRepositoryMod jurorResponseCjsEmploymentRepository;
    private final JurorPoolRepository jurorPoolRepository;
    private final JurorHistoryRepository jurorHistoryRepository;
    private final BulkPrintDataRepository bulkPrintDataRepository;
    private final JurorResponseAuditRepositoryMod auditRepository;
    private final JurorRepository jurorRepository;
    private final UserRepository userRepository;
    private final JurorResponseCommonRepositoryMod jurorResponseCommonRepositoryMod;

    @BeforeEach
    public void setUp() throws Exception {
        httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }

    @Nested
    @DisplayName("PATCH /api/v1/moj/juror-response/juror/%s/details/personal")
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_initPaperResponse.sql"})
    class UpdatePaperResponse {

        @Test
        void updatePaperResponsePersonalDetailsBureauUserBureauOwnerHappy() {
            JurorPersonalDetailsDto jurorPersonalDetailsDto = createJurorPersonalDetailsDto();

            assertTemplateExchange(jurorPersonalDetailsDto, JUROR_NUMBER_123456789, BUREAU_USER, "400", NO_CONTENT);

            assertUpdatedPaperResponsePersonalDetails(JUROR_NUMBER_123456789, jurorPersonalDetailsDto);
        }

        @Test
        void updatePaperResponsePersonalDetailsCourtUserCourtOwnerHappy() {
            JurorPersonalDetailsDto jurorPersonalDetailsDto = createJurorPersonalDetailsDto();

            assertTemplateExchange(jurorPersonalDetailsDto, JUROR_NUMBER_987654321, COURT_USER, "415", NO_CONTENT);

            assertUpdatedPaperResponsePersonalDetails(JUROR_NUMBER_987654321, jurorPersonalDetailsDto);
        }

        @Test
        void updatePaperResponsePersonalDetailsBureauUserCourtOwnerNotAuthorised() {
            JurorPersonalDetailsDto jurorPersonalDetailsDto = createJurorPersonalDetailsDto();

            assertTemplateExchange(jurorPersonalDetailsDto, JUROR_NUMBER_987654321, BUREAU_USER, "400", FORBIDDEN);
        }

        @Test
        void updatePaperResponsePersonalDetailsCourtUserCourtOwnerNotAuthorisedNotOwner() {
            JurorPersonalDetailsDto jurorPersonalDetailsDto = createJurorPersonalDetailsDto();

            assertTemplateExchange(jurorPersonalDetailsDto, JUROR_NUMBER_987654321, BUREAU_USER, "416", FORBIDDEN);
        }

        @Test
        void updatePaperResponsePersonalDetailsCourtUserBureauOwnerNotAuthorised() {
            JurorPersonalDetailsDto jurorPersonalDetailsDto = createJurorPersonalDetailsDto();

            assertTemplateExchange(jurorPersonalDetailsDto, JUROR_NUMBER_123456789, BUREAU_USER, "415", FORBIDDEN);
        }

        @Test
        void updatePaperResponsePersonalDetailsFirstLineAddressIsSql() {
            JurorPersonalDetailsDto jurorPersonalDetailsDto = createJurorPersonalDetailsDto();
            jurorPersonalDetailsDto.setAddressLineOne("Delete from XX where XX = XX;");

            assertTemplateExchange(jurorPersonalDetailsDto, JUROR_NUMBER_123456789, BUREAU_USER, "400", NO_CONTENT);

            assertUpdatedPaperResponsePersonalDetails(JUROR_NUMBER_123456789, jurorPersonalDetailsDto);
        }

        @Test
        void updatePaperResponsePersonalDetailsFirstLineAddressIsEmpty() {
            JurorPersonalDetailsDto jurorPersonalDetailsDto = createJurorPersonalDetailsDto();
            jurorPersonalDetailsDto.setAddressLineOne(" ");

            assertTemplateExchange(jurorPersonalDetailsDto, JUROR_NUMBER_123456789, BUREAU_USER, "400", BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/moj/juror-response/juror/%s/details/personal")
    @Sql({"/db/mod/truncate.sql", "/db/JurorDigitalResponse_initDigitalResponse.sql"})
    class UpdateDigitalResponse {

        @Test
        void updateDigitalResponsePersonalDetailsBureauUserBureauOwnerHappy() {
            DigitalResponse jurorDigitalResponseOriginal =
                jurorDigitalResponseRepository.findByJurorNumber(JUROR_NUMBER_123456789);
            assertThat(jurorDigitalResponseOriginal).isNotNull();

            JurorPersonalDetailsDto jurorPersonalDetailsDto = createJurorPersonalDetailsDto();
            jurorPersonalDetailsDto.setReplyMethod(ReplyMethod.DIGITAL);

            assertTemplateExchange(jurorPersonalDetailsDto, JUROR_NUMBER_123456789, BUREAU_USER, "400", NO_CONTENT);

            validateUpdatedDigitalResponsePersonalDetails(JUROR_NUMBER_123456789, jurorDigitalResponseOriginal,
                jurorPersonalDetailsDto);
        }

        @Test
        void updateDigitalResponsePersonalDetailsCourtUserCourtOwnerHappy() {
            DigitalResponse jurorDigitalResponseOriginal =
                jurorDigitalResponseRepository.findByJurorNumber(JUROR_NUMBER_987654321);
            assertThat(jurorDigitalResponseOriginal).isNotNull();

            JurorPersonalDetailsDto jurorPersonalDetailsDto = createJurorPersonalDetailsDto();
            jurorPersonalDetailsDto.setReplyMethod(ReplyMethod.DIGITAL);

            assertTemplateExchange(jurorPersonalDetailsDto, JUROR_NUMBER_987654321, COURT_USER, "415", NO_CONTENT);

            validateUpdatedDigitalResponsePersonalDetails(JUROR_NUMBER_987654321, jurorDigitalResponseOriginal,
                jurorPersonalDetailsDto);
        }

        @Test
        void updateDigitalResponsePersonalDetailsBureauUserCourtOwnerNotAuthorised() {
            JurorPersonalDetailsDto jurorPersonalDetailsDto = createJurorPersonalDetailsDto();
            jurorPersonalDetailsDto.setReplyMethod(ReplyMethod.DIGITAL);

            assertTemplateExchange(jurorPersonalDetailsDto, JUROR_NUMBER_987654321, BUREAU_USER, "400", FORBIDDEN);
        }

        @Test
        void updateDigitalResponsePersonalDetailsCourtUserCourtOwnerNotAuthorisedNotOwner() {
            JurorPersonalDetailsDto jurorPersonalDetailsDto = createJurorPersonalDetailsDto();
            jurorPersonalDetailsDto.setReplyMethod(ReplyMethod.DIGITAL);

            assertTemplateExchange(jurorPersonalDetailsDto, JUROR_NUMBER_987654321, BUREAU_USER, "416", FORBIDDEN);
        }

        @Test
        @Sql({"/db/mod/truncate.sql", "/db/JurorDigitalResponse_initDigitalResponse.sql"})
        void updateDigitalResponsePersonalDetailsCourtUserBureauOwnerNotAuthorised() {
            JurorPersonalDetailsDto jurorPersonalDetailsDto = createJurorPersonalDetailsDto();
            jurorPersonalDetailsDto.setReplyMethod(ReplyMethod.DIGITAL);

            assertTemplateExchange(jurorPersonalDetailsDto, JUROR_NUMBER_123456789, BUREAU_USER, "415", FORBIDDEN);
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/moj/juror-response/juror/%s/details/personal")
    @Sql({"/db/mod/truncate.sql", "/db/JurorPaperResponse_initPaperResponse_ageDisqualification.sql"})
    class UpdatePaperResponseAgeDisqualification {

        @Test
        void updatePaperResponsePersonalDetailsAgeDisqualificationTooYoung() {
            JurorPersonalDetailsDto jurorPersonalDetailsDto = createJurorPersonalDetailsDto();
            jurorPersonalDetailsDto.setDateOfBirth(
                LocalDate.now().minusYears(18).plusWeeks(7));
            jurorPersonalDetailsDto.setThirdParty(null);

            assertTemplateExchange(jurorPersonalDetailsDto, JUROR_NUMBER_123456789, BUREAU_USER, "400", NO_CONTENT);

            assertUpdatedPaperResponsePersonalDetails(JUROR_NUMBER_123456789, jurorPersonalDetailsDto);
            validateAgeDisqualificationMergedJurorRecord(JUROR_NUMBER_123456789, ReplyMethod.PAPER,
                IJurorStatus.DISQUALIFIED);
            verifyAgeDisqualification(JUROR_NUMBER_123456789);
        }

        @Test
        void updatePaperResponsePersonalDetailsAgeDisqualificationTooOld() {
            JurorPersonalDetailsDto jurorPersonalDetailsDto = createJurorPersonalDetailsDto();
            jurorPersonalDetailsDto.setDateOfBirth(
                LocalDate.of(2022, 5, 3).minusYears(80));
            jurorPersonalDetailsDto.setThirdParty(null);

            assertTemplateExchange(jurorPersonalDetailsDto, JUROR_NUMBER_987654321, COURT_USER, "415", NO_CONTENT);

            assertUpdatedPaperResponsePersonalDetails(JUROR_NUMBER_987654321, jurorPersonalDetailsDto);
            validateAgeDisqualificationMergedJurorRecord(JUROR_NUMBER_987654321, ReplyMethod.PAPER,
                IJurorStatus.DISQUALIFIED);
            verifyAgeDisqualification(JUROR_NUMBER_987654321);
        }

        @Test
        void updatePaperResponsePersonalDetailsAgeDisqualificationThirdParty() {
            JurorPersonalDetailsDto jurorPersonalDetailsDto = createJurorPersonalDetailsDto();
            jurorPersonalDetailsDto.setDateOfBirth(
                LocalDate.of(2022, 5, 3).minusYears(17));

            assertTemplateExchange(jurorPersonalDetailsDto, JUROR_NUMBER_123456789, BUREAU_USER, "400", NO_CONTENT);

            assertUpdatedPaperResponsePersonalDetails(JUROR_NUMBER_123456789, jurorPersonalDetailsDto);
            verifyStraightThroughAgeDisqualificationNotProcessed(JUROR_NUMBER_123456789, ReplyMethod.PAPER,
                IJurorStatus.SUMMONED);
        }

        @Test
        void updatePaperResponsePersonalDetailsAgeDisqualificationInvalidStatus() {
            JurorPersonalDetailsDto jurorPersonalDetailsDto = createJurorPersonalDetailsDto();
            jurorPersonalDetailsDto.setDateOfBirth(
                LocalDate.of(2022, 5, 3).minusYears(80));
            jurorPersonalDetailsDto.setThirdParty(null);

            assertTemplateExchange(jurorPersonalDetailsDto, JUROR_NUMBER_111111111, COURT_USER, "415", NO_CONTENT);

            assertUpdatedPaperResponsePersonalDetails(JUROR_NUMBER_111111111, jurorPersonalDetailsDto);
            verifyStraightThroughAgeDisqualificationNotProcessed(JUROR_NUMBER_111111111, ReplyMethod.PAPER,
                IJurorStatus.RESPONDED);
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/moj/juror-response/juror/%s/details/personal")
    @Sql({"/db/mod/truncate.sql", "/db/JurorDigitalResponse_initDigitalResponse_ageDisqualification.sql"})
    class UpdateDigitalResponseAgeDisqualification {

        @Test
        void updateDigitalResponsePersonalDetailsAgeDisqualificationTooYoung() {
            DigitalResponse jurorDigitalResponseOriginal =
                jurorDigitalResponseRepository.findByJurorNumber(JUROR_NUMBER_987654321);
            assertThat(jurorDigitalResponseOriginal).isNotNull();

            JurorPersonalDetailsDto jurorPersonalDetailsDto = createJurorPersonalDetailsDto();
            jurorPersonalDetailsDto.setReplyMethod(ReplyMethod.DIGITAL);
            jurorPersonalDetailsDto.setDateOfBirth(
                LocalDate.now().minusYears(18).plusWeeks(7));
            jurorPersonalDetailsDto.setThirdParty(null);

            assertTemplateExchange(jurorPersonalDetailsDto, JUROR_NUMBER_987654321, BUREAU_USER, "415", NO_CONTENT);

            validateUpdatedDigitalResponsePersonalDetails(JUROR_NUMBER_987654321, jurorDigitalResponseOriginal,
                jurorPersonalDetailsDto);
            validateAgeDisqualificationMergedJurorRecord(JUROR_NUMBER_987654321, ReplyMethod.DIGITAL,
                IJurorStatus.DISQUALIFIED);
            verifyAgeDisqualification(JUROR_NUMBER_987654321);
        }

        @Test
        void updateDigitalResponsePersonalDetailsAgeDisqualificationTooOld() {
            DigitalResponse jurorDigitalResponseOriginal =
                jurorDigitalResponseRepository.findByJurorNumber(JUROR_NUMBER_987654321);
            assertThat(jurorDigitalResponseOriginal).isNotNull();

            JurorPersonalDetailsDto jurorPersonalDetailsDto = createJurorPersonalDetailsDto();
            jurorPersonalDetailsDto.setReplyMethod(ReplyMethod.DIGITAL);
            jurorPersonalDetailsDto.setDateOfBirth(
                LocalDate.of(2022, 5, 3).minusYears(80));
            jurorPersonalDetailsDto.setThirdParty(null);

            assertTemplateExchange(jurorPersonalDetailsDto, JUROR_NUMBER_987654321, COURT_USER, "415", NO_CONTENT);

            validateUpdatedDigitalResponsePersonalDetails(JUROR_NUMBER_987654321, jurorDigitalResponseOriginal,
                jurorPersonalDetailsDto);
            validateAgeDisqualificationMergedJurorRecord(JUROR_NUMBER_987654321, ReplyMethod.DIGITAL,
                IJurorStatus.DISQUALIFIED);
            verifyAgeDisqualification(JUROR_NUMBER_987654321);
        }

        @Test
        void updateDigitalResponsePersonalDetailsAgeDisqualificationThirdParty() {
            DigitalResponse jurorDigitalResponseOriginal =
                jurorDigitalResponseRepository.findByJurorNumber(JUROR_NUMBER_123456789);
            assertThat(jurorDigitalResponseOriginal).isNotNull();

            JurorPersonalDetailsDto jurorPersonalDetailsDto = createJurorPersonalDetailsDto();
            jurorPersonalDetailsDto.setReplyMethod(ReplyMethod.DIGITAL);
            jurorPersonalDetailsDto.setDateOfBirth(
                LocalDate.of(2022, 5, 3).minusYears(17));

            assertTemplateExchange(jurorPersonalDetailsDto, JUROR_NUMBER_123456789, BUREAU_USER, "400", NO_CONTENT);

            validateUpdatedDigitalResponsePersonalDetails(JUROR_NUMBER_123456789, jurorDigitalResponseOriginal,
                jurorPersonalDetailsDto);
            verifyStraightThroughAgeDisqualificationNotProcessed(JUROR_NUMBER_123456789, ReplyMethod.DIGITAL,
                IJurorStatus.SUMMONED);
        }

        @Test
        void updateDigitalResponsePersonalDetailsAgeDisqualificationInvalidStatus() {
            DigitalResponse jurorDigitalResponseOriginal =
                jurorDigitalResponseRepository.findByJurorNumber(JUROR_NUMBER_111111111);
            assertThat(jurorDigitalResponseOriginal).isNotNull();

            JurorPersonalDetailsDto jurorPersonalDetailsDto = createJurorPersonalDetailsDto();
            jurorPersonalDetailsDto.setReplyMethod(ReplyMethod.DIGITAL);
            jurorPersonalDetailsDto.setDateOfBirth(
                LocalDate.of(2022, 5, 3).minusYears(80));
            jurorPersonalDetailsDto.setThirdParty(null);

            assertTemplateExchange(jurorPersonalDetailsDto, JUROR_NUMBER_111111111, COURT_USER, "415", NO_CONTENT);

            validateUpdatedDigitalResponsePersonalDetails(JUROR_NUMBER_111111111, jurorDigitalResponseOriginal,
                jurorPersonalDetailsDto);
            verifyStraightThroughAgeDisqualificationNotProcessed(JUROR_NUMBER_111111111, ReplyMethod.DIGITAL,
                IJurorStatus.RESPONDED);
        }
    }

    @Nested
    @DisplayName("POST /api/v1/moj/juror-response/update-status/%s")
    @Sql({"/db/mod/truncate.sql",
        "/db/truncate.sql",
        "/db/standing_data.sql",
        "/db/JurorResponseITest.updateResponseStatus.sql"})
    class UpdateResponseStatus {

        /**
         * Todo - test needs review as part of juror response redesign (JM-5011).
         *
         * @see #updateResponseStatusHappyNonMergeStatusChangeAwaitingCourt()
         * @see #updateResponseStatusHappyNonMergeStatusChangeAwaitingTranslation()
         * @see #updateResponseStatusHappyNonMergeStatusChangeTodo()
         */
        @Test
        void updateResponseStatusHappyNonMergeStatusChangeAwaitingJurorContact() throws Exception {
            final ProcessingStatus newProcessingStatus = ProcessingStatus.AWAITING_CONTACT;

            final String description = "Update juror response status happy path.";
            final URI uri = URI.create("/api/v1/moj/juror-response/update-status/644892530");

            final String bureauJwt = mintBureauJwt(BureauJwtPayload.builder()
                .userType(UserType.BUREAU)
                .login("testlogin")
                .owner(JurorDigitalApplication.JUROR_OWNER)
                .build());

            final User staff = userRepository.findByUsername("testlogin"); // juror number in sql file

            // assert db state before status change
            assertDatabaseStateBefore();

            final BureauResponseStatusUpdateDto dto = BureauResponseStatusUpdateDto.builder()
                .status(newProcessingStatus)
                .version(2)
                .build();

            sendRequestAndValidateRequest(bureauJwt, dto, description, uri);

            // assert db state after status change.
            assertDatabaseStateAfter();

            final String jurorNumber = "644892530";
            executeInTransaction(() -> {
                List<JurorPool> jurorPools = jurorPoolRepository.findByJurorJurorNumberAndIsActive(
                    jurorNumber, true);
                JurorPool jurorPool = jurorPools.get(0);
                Juror juror = jurorPool.getJuror();

                // assert the changes to pool were not applied
                assertThat(juror.getLastName()).isEqualTo("CASTILLO");

                // assert change to processing status was audited
                assertThat(jdbcTemplate.queryForObject("SELECT NEW_PROCESSING_STATUS FROM juror_mod.JUROR_RESPONSE_AUD "
                    + "WHERE JUROR_NUMBER = '644892530'", String.class)).isEqualTo(newProcessingStatus.toString());
                assertThat(jdbcTemplate.queryForObject("SELECT OLD_PROCESSING_STATUS FROM juror_mod.JUROR_RESPONSE_AUD "
                    + "WHERE JUROR_NUMBER = '644892530'", String.class)).isEqualTo("TODO");
            });

            // assert staff assignment on response has changed and been audited
            validateStaffAssignment(staff);
        }

        /**
         * Todo - test needs review as part of juror response redesign (JM-5011).
         *
         * @see #updateResponseStatusHappyNonMergeStatusChangeAwaitingJurorContact()
         * @see #updateResponseStatusHappyNonMergeStatusChangeAwaitingTranslation()
         * @see #updateResponseStatusHappyNonMergeStatusChangeTodo()
         */
        @Test
        void updateResponseStatusHappyNonMergeStatusChangeAwaitingCourt() throws Exception {
            final ProcessingStatus newProcessingStatus = ProcessingStatus.AWAITING_COURT_REPLY;

            final String description = "Update juror response status happy path.";
            final URI uri = URI.create("/api/v1/moj/juror-response/update-status/644892530");

            final String bureauJwt = mintBureauJwt(BureauJwtPayload.builder()
                .userType(UserType.BUREAU)
                .login("testlogin")
                .owner(JurorDigitalApplication.JUROR_OWNER)
                .build());

            // assert db state before status change
            assertDatabaseStateBefore();

            final BureauResponseStatusUpdateDto dto = BureauResponseStatusUpdateDto.builder()
                .status(newProcessingStatus)
                .version(2)
                .build();

            sendRequestAndValidateRequest(bureauJwt, dto, description, uri);

            // assert db state after status change.
            assertDatabaseStateAfter();

            final String jurorNumber = "644892530";
            executeInTransaction(() -> {
                List<JurorPool> jurorPools = jurorPoolRepository.findByJurorJurorNumberAndIsActive(
                    jurorNumber, true);
                JurorPool jurorPool = jurorPools.get(0);
                Juror juror = jurorPool.getJuror();

                // assert the changes to pool were not applied
                assertThat(juror.getLastName()).isEqualTo("CASTILLO");

                // assert change to processing status was audited
                assertThat(jdbcTemplate.queryForObject("SELECT NEW_PROCESSING_STATUS FROM juror_mod.JUROR_RESPONSE_AUD "
                    + "WHERE JUROR_NUMBER = '644892530'", String.class)).isEqualTo(newProcessingStatus.toString());
                assertThat(jdbcTemplate.queryForObject("SELECT OLD_PROCESSING_STATUS FROM juror_mod.JUROR_RESPONSE_AUD "
                    + "WHERE JUROR_NUMBER = '644892530'", String.class)).isEqualTo("TODO");
            });
        }

        /**
         * Todo - test needs review as part of juror response redesign (JM-5011).
         *
         * @see #updateResponseStatusHappyNonMergeStatusChangeAwaitingJurorContact()
         * @see #updateResponseStatusHappyNonMergeStatusChangeAwaitingCourt()
         * @see #updateResponseStatusHappyNonMergeStatusChangeTodo()
         */
        @Test
        void updateResponseStatusHappyNonMergeStatusChangeAwaitingTranslation() throws Exception {

            final ProcessingStatus newProcessingStatus = ProcessingStatus.AWAITING_TRANSLATION;

            final String description = "Update juror response status happy path.";
            final URI uri = URI.create("/api/v1/moj/juror-response/update-status/644892530");

            final String bureauJwt = mintBureauJwt(BureauJwtPayload.builder()
                .userType(UserType.BUREAU)
                .login("testlogin")
                .owner(JurorDigitalApplication.JUROR_OWNER)
                .build());

            // assert db state before status change
            assertDatabaseStateBefore();

            final BureauResponseStatusUpdateDto dto = BureauResponseStatusUpdateDto.builder()
                .status(newProcessingStatus)
                .version(2)
                .build();

            sendRequestAndValidateRequest(bureauJwt, dto, description, uri);

            // assert db state after status change.
            assertDatabaseStateAfter();
            executeInTransaction(() -> {
                final String jurorNumber = "644892530";
                List<JurorPool> jurorPools = jurorPoolRepository.findByJurorJurorNumberAndIsActive(jurorNumber, true);
                JurorPool jurorPool = jurorPools.get(0);
                Juror juror = jurorPool.getJuror();

                // assert the changes to pool were not applied
                assertThat(juror.getLastName()).isEqualTo("CASTILLO");

                // assert change to processing status was audited
                assertThat(jdbcTemplate.queryForObject("SELECT NEW_PROCESSING_STATUS FROM juror_mod.JUROR_RESPONSE_AUD "
                    + "WHERE JUROR_NUMBER = '644892530'", String.class)).isEqualTo(newProcessingStatus.toString());
                assertThat(jdbcTemplate.queryForObject("SELECT OLD_PROCESSING_STATUS FROM juror_mod.JUROR_RESPONSE_AUD "
                    + "WHERE JUROR_NUMBER = '644892530'", String.class)).isEqualTo("TODO");
            });
        }

        /**
         * Todo - test needs review as part of juror response redesign (JM-5011).
         *
         * @see #updateResponseStatusHappyNonMergeStatusChangeAwaitingJurorContact()
         * @see #updateResponseStatusHappyNonMergeStatusChangeAwaitingCourt()
         * @see #updateResponseStatusHappyNonMergeStatusChangeAwaitingTranslation()
         */
        @Test
        void updateResponseStatusHappyNonMergeStatusChangeTodo() throws Exception {

            final ProcessingStatus newProcessingStatus = ProcessingStatus.TODO;

            final String description = "Update juror response status happy path.";
            final URI uri = URI.create("/api/v1/moj/juror-response/update-status/644892530");

            final String bureauJwt = mintBureauJwt(BureauJwtPayload.builder()
                .login("testlogin")
                .userType(UserType.BUREAU)
                .owner(JurorDigitalApplication.JUROR_OWNER)
                .build());

            // assert db state before status change
            assertDatabaseStateBefore();

            final BureauResponseStatusUpdateDto dto = BureauResponseStatusUpdateDto.builder()
                .status(newProcessingStatus)
                .version(2)
                .build();

            sendRequestAndValidateRequest(bureauJwt, dto, description, uri);

            // assert db state after status change.
            assertDatabaseStateAfter();
            executeInTransaction(() -> {
                final String jurorNumber = "644892530";
                List<JurorPool> jurorPools = jurorPoolRepository.findByJurorJurorNumberAndIsActive(jurorNumber, true);
                JurorPool jurorPool = jurorPools.get(0);
                Juror juror = jurorPool.getJuror();

                // assert the changes to pool were not applied
                assertThat(juror.getLastName()).isEqualTo("CASTILLO");

                // assert change to processing status was audited
                assertThat(jdbcTemplate.queryForObject("SELECT NEW_PROCESSING_STATUS FROM juror_mod.JUROR_RESPONSE_AUD "
                    + "WHERE JUROR_NUMBER = '644892530'", String.class)).isEqualTo(newProcessingStatus.toString());
                assertThat(jdbcTemplate.queryForObject("SELECT OLD_PROCESSING_STATUS FROM juror_mod.JUROR_RESPONSE_AUD "
                    + "WHERE JUROR_NUMBER = '644892530'", String.class)).isEqualTo("TODO");
            });
        }

        private void assertDatabaseStateBefore() {
            assertThat(jurorPoolRepository.count()).isEqualTo(1);
            assertThat(jurorDigitalResponseRepository.count()).isEqualTo(1);
            assertThat(auditRepository.count()).isEqualTo(0);
            assertThat(jurorReasonableAdjustmentRepository.count()).isEqualTo(1);
            assertThat(jurorResponseCjsEmploymentRepository.count()).isEqualTo(1);
        }

        private void assertDatabaseStateAfter() {
            assertThat(jurorPoolRepository.count()).isEqualTo(1);
            assertThat(jurorDigitalResponseRepository.count()).isEqualTo(1);
            assertThat(auditRepository.count()).isEqualTo(1);
            assertThat(jurorReasonableAdjustmentRepository.count()).isEqualTo(1);
            assertThat(jurorResponseCjsEmploymentRepository.count()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("POST /api/v1/moj/juror-response/retrieve")
    @Sql({"/db/mod/truncate.sql", "/db/jurorresponse/RetrieveJurorResponses.sql"})
    @SuppressWarnings({"java:S1192"})
    class RetrieveJurorResponses {
        @Test
        @DisplayName("Retrieve juror response, team leader - basic and advanced search criteria is okay")
        void teamLeaderFilterByBasicAndAdvancedSearchCriteria() {
            JurorResponseRetrieveRequestDto request = new JurorResponseRetrieveRequestDto();
            request.setJurorNumber(JUROR_NUMBER_111222333);
            request.setOfficerAssigned(OFFICER_ASSIGNED_BUREAU_OFFICER);

            setHeaders(OWNER_400, TEAM_LEADER, UserType.BUREAU, Role.MANAGER);
            ResponseEntity<JurorResponseRetrieveResponseDto> response = templateExchangeRetrieve(request, OK);

            JurorResponseRetrieveResponseDto body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.getRecordCount()).as("Record count should be 1").isEqualTo(1);

            // validate data
            JurorResponseRetrieveResponseDto.JurorResponseDetails data = body.getRecords().get(0);
            validateData(data, JUROR_NUMBER_111222333, "TestOne",
                "PersonOne", OFFICER_ASSIGNED_BUREAU_OFFICER,
                ProcessingStatus.TODO, LocalDateTime.of(2023, 3, 8, 0, 0, 0));
        }

        @Test
        @DisplayName("Retrieve juror response, team leader - advanced search for filter Processing Status Completed "
            + "is okay")
        void teamLeaderFilterAdvancedSearchFilterIsProcessingStatusCompleted() {
            JurorResponseRetrieveRequestDto request = new JurorResponseRetrieveRequestDto();
            request.setProcessingStatus(Collections.singletonList(ProcessingStatus.CLOSED));

            setHeaders(OWNER_400, TEAM_LEADER, UserType.BUREAU, Role.MANAGER);
            ResponseEntity<JurorResponseRetrieveResponseDto> response = templateExchangeRetrieve(request, OK);

            JurorResponseRetrieveResponseDto body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.getRecordCount()).as("Record count should be 2").isEqualTo(2);

            // validate data
            List<JurorResponseRetrieveResponseDto.JurorResponseDetails> records = body.getRecords();

            validateData(records.get(0), "666666666", "Test6Paper",
                         "Person6Paper", OFFICER_ASSIGNED_BUREAU_OFFICER,
                         ProcessingStatus.CLOSED, LocalDateTime.of(2023, 3, 10, 0, 0, 0));

            validateData(records.get(1), JUROR_NUMBER_222222222, "Test4Paper",
                "Person4Paper", OFFICER_ASSIGNED_BUREAU_OFFICER,
                         ProcessingStatus.CLOSED, LocalDateTime.of(2023, 3, 9, 0, 0, 0));

       }

        @Test
        @DisplayName("Retrieve juror response, team leader - advanced search for filter Processing Status Awaiting "
            + "Court Reply is okay")
        void teamLeaderFilterAdvancedSearchFilterIsProcessingStatusAwaitingCourtReply() {
            JurorResponseRetrieveRequestDto request = new JurorResponseRetrieveRequestDto();
            request.setProcessingStatus(Collections.singletonList(ProcessingStatus.AWAITING_COURT_REPLY));

            setHeaders(OWNER_400, TEAM_LEADER, UserType.BUREAU, Role.MANAGER);
            ResponseEntity<JurorResponseRetrieveResponseDto> response = templateExchangeRetrieve(request, OK);

            JurorResponseRetrieveResponseDto body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.getRecordCount()).as("Record count should be 1").isEqualTo(1);

            // validate data
            JurorResponseRetrieveResponseDto.JurorResponseDetails data = body.getRecords().get(0);
            validateData(data, JUROR_NUMBER_555555555, "Test5Paper",
                "Person5Paper", "JDoe",
                ProcessingStatus.AWAITING_COURT_REPLY, LocalDateTime.of(2023, 3, 9, 10, 0, 0));
        }

        @Test
        @DisplayName("Retrieve juror response, team leader - basic search for filter lastname is okay")
        void teamLeaderFilterBasicSearchFilterLastname() {
            JurorResponseRetrieveRequestDto request = new JurorResponseRetrieveRequestDto();
            request.setLastName("Person5Paper");

            setHeaders(OWNER_400, TEAM_LEADER, UserType.BUREAU, Role.MANAGER);
            ResponseEntity<JurorResponseRetrieveResponseDto> response = templateExchangeRetrieve(request, OK);

            JurorResponseRetrieveResponseDto body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.getRecordCount()).as("Record count should be 1").isEqualTo(1);

            // validate data
            JurorResponseRetrieveResponseDto.JurorResponseDetails data = body.getRecords().get(0);
            validateData(data, JUROR_NUMBER_555555555, "Test5Paper",
                "Person5Paper", "JDoe",
                ProcessingStatus.AWAITING_COURT_REPLY, LocalDateTime.of(2023, 3, 9, 10, 0, 0));
        }

        @Test
        @DisplayName("Retrieve juror response, team leader - advanced search for filter isUrgent is okay")
        void teamLeaderFilterAdvancedSearchFilterIsUrgent() {
            JurorResponseRetrieveRequestDto request = new JurorResponseRetrieveRequestDto();
            request.setLastName("Person5Paper");

            setHeaders(OWNER_400, TEAM_LEADER, UserType.BUREAU, Role.MANAGER);
            ResponseEntity<JurorResponseRetrieveResponseDto> response = templateExchangeRetrieve(request, OK);

            JurorResponseRetrieveResponseDto body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.getRecordCount()).as("Record count should be 1").isEqualTo(1);

            // validate data
            JurorResponseRetrieveResponseDto.JurorResponseDetails data = body.getRecords().get(0);
            validateData(data, JUROR_NUMBER_555555555, "Test5Paper",
                "Person5Paper", "JDoe",
                ProcessingStatus.AWAITING_COURT_REPLY, LocalDateTime.of(2023, 3, 9, 10, 0, 0));
        }

        @Test
        @DisplayName("Retrieve juror response, team leader - no records matching search criteria is okay")
        void teamLeaderFilterNoRecordsMatchingSearchCriteria() {
            JurorResponseRetrieveRequestDto request = new JurorResponseRetrieveRequestDto();
            request.setLastName("Person99Paper");

            setHeaders(OWNER_400, TEAM_LEADER, UserType.BUREAU, Role.MANAGER);
            ResponseEntity<JurorResponseRetrieveResponseDto> response = templateExchangeRetrieve(request, OK);

            JurorResponseRetrieveResponseDto body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.getRecordCount()).as("Record count should be 0").isEqualTo(0);
        }

        @Test
        @DisplayName("Retrieve juror response, bureau user - filter by juror number is okay")
        void bureauOfficerFilterByJurorNumber() {
            JurorResponseRetrieveRequestDto request = new JurorResponseRetrieveRequestDto();
            request.setJurorNumber(JUROR_NUMBER_111222333);

            setHeaders(OWNER_400, BUREAU_USER, UserType.BUREAU);
            ResponseEntity<JurorResponseRetrieveResponseDto> response = templateExchangeRetrieve(request, OK);

            JurorResponseRetrieveResponseDto body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.getRecordCount()).as("Record count should be 1").isEqualTo(1);

            // validate data
            JurorResponseRetrieveResponseDto.JurorResponseDetails data = body.getRecords().get(0);
            validateData(data, JUROR_NUMBER_111222333, "TestOne",
                "PersonOne", OFFICER_ASSIGNED_BUREAU_OFFICER,
                ProcessingStatus.TODO, LocalDateTime.of(2023, 3, 8, 0, 0, 0));
        }

        @Test
        @DisplayName("Retrieve juror response, bureau user - filter by pool number is ok")
        void bureauOfficerFilterByPoolNumber() {
            JurorResponseRetrieveRequestDto request = new JurorResponseRetrieveRequestDto();
            request.setPoolNumber("415220502");

            setHeaders(OWNER_400, BUREAU_USER, UserType.BUREAU);
            ResponseEntity<JurorResponseRetrieveResponseDto> response = templateExchangeRetrieve(request, OK);

            JurorResponseRetrieveResponseDto body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.getRecordCount()).as("Record count should be 6").isEqualTo(6);

            // validate data - results should be in correct
            List<JurorResponseRetrieveResponseDto.JurorResponseDetails> records = body.getRecords();
            validateData(records.get(0), JUROR_NUMBER_111222333, "TestOne",
                "PersonOne", OFFICER_ASSIGNED_BUREAU_OFFICER,
                ProcessingStatus.TODO, LocalDateTime.of(2023, 3, 8, 0, 0, 0));

            validateData(records.get(1), "333222111", "TestTwo",
                "PersonTwo", OFFICER_ASSIGNED_BUREAU_OFFICER,
                ProcessingStatus.TODO, LocalDateTime.of(2023, 3, 8, 10, 0, 0));

            validateData(records.get(2), "222222222", "Test4Paper",
                "Person4Paper", OFFICER_ASSIGNED_BUREAU_OFFICER,
                ProcessingStatus.CLOSED, LocalDateTime.of(2023, 3, 9, 0, 0, 0));

            validateData(records.get(3), JUROR_NUMBER_555555555, "Test5Paper",
                "Person5Paper", "JDoe",
                ProcessingStatus.AWAITING_COURT_REPLY, LocalDateTime.of(2023, 3, 9, 10, 0, 0));

            validateData(records.get(4), "666666666", "Test6Paper",
                "Person6Paper", OFFICER_ASSIGNED_BUREAU_OFFICER,
                         ProcessingStatus.CLOSED, LocalDateTime.of(2023, 3, 10, 0, 0, 0));

            validateData(records.get(5), "352004504", "Test3",
                         "Person3", OFFICER_ASSIGNED_BUREAU_OFFICER,
                         ProcessingStatus.TODO, LocalDateTime.of(2024, 3, 15, 0, 0, 0));
        }

        @Test
        @DisplayName("Retrieve juror response, bureau team leader user - filter by pool number is ok")
        void bureauTeamLeaderFilterByPoolNumberCloseResponses() {
            JurorResponseRetrieveRequestDto request = new JurorResponseRetrieveRequestDto();
            request.setPoolNumber("415220502");
            request.setProcessingStatus(Collections.singletonList(ProcessingStatus.CLOSED));

            setHeaders(OWNER_400, TEAM_LEADER, UserType.BUREAU, Role.MANAGER);
            ResponseEntity<JurorResponseRetrieveResponseDto> response = templateExchangeRetrieve(request, OK);

            JurorResponseRetrieveResponseDto body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.getRecordCount()).as("Record count should be 2").isEqualTo(2);

            // validate data - results should be in correct (desc order by received date)
            List<JurorResponseRetrieveResponseDto.JurorResponseDetails> records = body.getRecords();

            validateData(records.get(0), "666666666", "Test6Paper",
                         "Person6Paper", OFFICER_ASSIGNED_BUREAU_OFFICER,
                         ProcessingStatus.CLOSED, LocalDateTime.of(2023, 3, 10, 0, 0, 0));

            validateData(records.get(1), "222222222", "Test4Paper",
                         "Person4Paper", OFFICER_ASSIGNED_BUREAU_OFFICER,
                         ProcessingStatus.CLOSED, LocalDateTime.of(2023, 3, 9, 0, 0, 0));

        }

        @Test
        @DisplayName("Retrieve juror response, bureau user - empty request is bad request")
        void bureauOfficerEmptyRequest() {
            JurorResponseRetrieveRequestDto request = new JurorResponseRetrieveRequestDto();

            setHeaders(OWNER_400, BUREAU_USER, UserType.BUREAU);
            ResponseEntity<JurorResponseRetrieveResponseDto> response = templateExchangeRetrieve(request, BAD_REQUEST);

            JurorResponseRetrieveResponseDto body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.getRecordCount()).as("Record count should be 0").isEqualTo(0);
        }

        @Test
        @DisplayName("Retrieve juror response, bureau officer - advanced search is forbidden")
        void bureauOfficerAdvancedSearch() {
            JurorResponseRetrieveRequestDto request = new JurorResponseRetrieveRequestDto();
            request.setJurorNumber("352004504");
            request.setOfficerAssigned(OFFICER_ASSIGNED_BUREAU_OFFICER);

            setHeaders(OWNER_400, BUREAU_USER, UserType.BUREAU);
            ResponseEntity<JurorResponseRetrieveResponseDto> response = templateExchangeRetrieve(request, FORBIDDEN);

            // validate body
            JurorResponseRetrieveResponseDto body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.getRecordCount()).as("Record count should be 0").isEqualTo(0);
        }

        @Test
        @DisplayName("Retrieve juror response, court team leader - search is forbidden")
        void courtTeamLeaderSearch() {
            JurorResponseRetrieveRequestDto request = new JurorResponseRetrieveRequestDto();
            request.setJurorNumber("352004504");

            setHeaders(OWNER_415, COURT_USER, UserType.COURT, Role.MANAGER);
            ResponseEntity<JurorResponseRetrieveResponseDto> response = templateExchangeRetrieve(request, FORBIDDEN);

            // validate body
            JurorResponseRetrieveResponseDto body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.getRecordCount()).as("Record count should be 0").isEqualTo(0);
        }

        @Test
        @DisplayName("Retrieve juror response, court officer - search is forbidden")
        void courtOfficerAdvancedSearch() {
            JurorResponseRetrieveRequestDto request = new JurorResponseRetrieveRequestDto();
            request.setJurorNumber("352004504");

            setHeaders(OWNER_415, COURT_USER, UserType.COURT);
            ResponseEntity<JurorResponseRetrieveResponseDto> response = templateExchangeRetrieve(request, FORBIDDEN);

            JurorResponseRetrieveResponseDto body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.getRecordCount()).as("Record count should be 0").isEqualTo(0);
        }

        private void setHeaders(String owner, String user, UserType userType, Role... role) {
            httpHeaders = initialiseHeaders(user, userType, Set.of(role), owner);
        }

        private ResponseEntity<JurorResponseRetrieveResponseDto> templateExchangeRetrieve(
            JurorResponseRetrieveRequestDto request, HttpStatus httpStatus) {

            RequestEntity<JurorResponseRetrieveRequestDto> requestEntity = new RequestEntity<>(request, httpHeaders,
                POST, URI.create(URI_RETRIEVE_JUROR_RESPONSES));

            ResponseEntity<JurorResponseRetrieveResponseDto> response =
                template.exchange(requestEntity, JurorResponseRetrieveResponseDto.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(httpStatus);

            return response;
        }

        private void validateData(JurorResponseRetrieveResponseDto.JurorResponseDetails actual,
                                  String jurorNumber, String firstName, String lastName,
                                  String officerAssigned,
                                  ProcessingStatus processingStatus, LocalDateTime dateReceived) {

            assertThat(actual.getJurorNumber()).as("Juror number should be " + jurorNumber)
                .isEqualTo(jurorNumber);
            assertThat(actual.getPoolNumber()).as("Pool number should be 415220502")
                .isEqualTo("415220502");
            assertThat(actual.getFirstName()).as("First name should be " + firstName)
                .isEqualTo(firstName);
            assertThat(actual.getLastName()).as("Last name should be " + lastName)
                .isEqualTo(lastName);
            assertThat(actual.getPostcode()).as("Postcode should be CH1 2AN")
                .isEqualTo("CH1 2AN");
            assertThat(actual.getCourtName()).as("Court name should be Chester")
                .isEqualTo("Chester");
            assertThat(actual.getOfficerAssigned()).as("Officer assigned should be " + officerAssigned)
                .isEqualTo(officerAssigned);
            assertThat(actual.getReplyStatus()).as("Processing/reply status should be " + processingStatus)
                .isEqualTo(processingStatus);
            assertThat(actual.getDateReceived()).as("Date received should be " + dateReceived)
                .isEqualTo(dateReceived);
        }
    }

    private void assertTemplateExchange(JurorPersonalDetailsDto jurorPersonalDetailsDto, String jurorNumber,
                                        String userType, String owner, HttpStatus httpStatus) {
        final URI uri = URI.create(String.format(URI_PERSONAL_DETAILS, jurorNumber));
        httpHeaders =
            initialiseHeaders(userType,
                "400".equals(owner) ? UserType.BUREAU : UserType.COURT,
                Set.of(Role.MANAGER), owner);

        RequestEntity<JurorPersonalDetailsDto> requestEntity = new RequestEntity<>(jurorPersonalDetailsDto,
            httpHeaders, HttpMethod.PATCH, uri);
        ResponseEntity<String> response = template.exchange(requestEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(httpStatus);
    }

    private JurorPersonalDetailsDto createJurorPersonalDetailsDto() {
        JurorPersonalDetailsDto jurorPersonalDetailsDto = new JurorPersonalDetailsDto();
        jurorPersonalDetailsDto.setReplyMethod(ReplyMethod.PAPER);
        jurorPersonalDetailsDto.setTitle("Mrs");
        jurorPersonalDetailsDto.setDateOfBirth(LocalDate.of(2001, 10, 1));
        jurorPersonalDetailsDto.setFirstName("FNameNew");
        jurorPersonalDetailsDto.setLastName("LNameNew");
        jurorPersonalDetailsDto.setAddressLineOne("New Address Line1");
        jurorPersonalDetailsDto.setAddressLineTwo("New Address Line2");
        jurorPersonalDetailsDto.setAddressLineThree("New Address Line3");

        jurorPersonalDetailsDto.setAddressTown("New Address Town");
        jurorPersonalDetailsDto.setAddressCounty("New County");
        jurorPersonalDetailsDto.setAddressPostcode("SE1 2BD");
        jurorPersonalDetailsDto.setPrimaryPhone("0102 121231232");
        jurorPersonalDetailsDto.setSecondaryPhone("0798 123456");
        jurorPersonalDetailsDto.setEmailAddress("newemail@add.com");
        JurorPaperResponseDto.ThirdParty thirdParty = JurorPaperResponseDto.ThirdParty.builder()
            .relationship("New Relationship")
            .thirdPartyReason("New Relationship reason")
            .build();
        jurorPersonalDetailsDto.setThirdParty(thirdParty);

        return jurorPersonalDetailsDto;
    }

    private void assertUpdatedPaperResponsePersonalDetails(String jurorNumber,
                                                           JurorPersonalDetailsDto jurorPersonalDetailsDto) {
        executeInTransaction(() -> {
            PaperResponse updatedJurorPaperResponseOpt = jurorPaperResponseRepository.findByJurorNumber(jurorNumber);
            assertThat(updatedJurorPaperResponseOpt).isNotNull();
            PaperResponse updatedJurorPaperResponse = updatedJurorPaperResponseOpt;

            assertThat(updatedJurorPaperResponse.getTitle()).isEqualTo(jurorPersonalDetailsDto.getTitle());
            assertThat(updatedJurorPaperResponse.getFirstName()).isEqualTo(jurorPersonalDetailsDto.getFirstName());
            assertThat(updatedJurorPaperResponse.getLastName()).isEqualTo(jurorPersonalDetailsDto.getLastName());

            assertThat(updatedJurorPaperResponse.getDateOfBirth()).isEqualTo(jurorPersonalDetailsDto.getDateOfBirth());
            assertThat(updatedJurorPaperResponse.getEmail()).isEqualTo(jurorPersonalDetailsDto.getEmailAddress());

            assertThat(updatedJurorPaperResponse.getAddressLine1()).isEqualTo(
                jurorPersonalDetailsDto.getAddressLineOne());
            assertThat(updatedJurorPaperResponse.getAddressLine2()).isEqualTo(
                jurorPersonalDetailsDto.getAddressLineTwo());
            assertThat(updatedJurorPaperResponse.getAddressLine3()).isEqualTo(jurorPersonalDetailsDto
                .getAddressLineThree());
            assertThat(updatedJurorPaperResponse.getAddressLine4()).isEqualTo(jurorPersonalDetailsDto.getAddressTown());
            assertThat(updatedJurorPaperResponse.getAddressLine5()).isEqualTo(
                jurorPersonalDetailsDto.getAddressCounty());

            JurorPaperResponseDto.ThirdParty thirdParty = jurorPersonalDetailsDto.getThirdParty();
            if (thirdParty != null) {
                assertThat(updatedJurorPaperResponse.getRelationship()).isEqualTo(thirdParty.getRelationship());
                assertThat(updatedJurorPaperResponse.getThirdPartyReason()).isEqualTo(thirdParty.getThirdPartyReason());
            }
        });
    }

    private void validateUpdatedDigitalResponsePersonalDetails(String jurorNumber,
                                                               DigitalResponse originalJurorDigitalResponse,
                                                               JurorPersonalDetailsDto jurorPersonalDetailsDto) {
        executeInTransaction(() -> {
            DigitalResponse updatedJurorDigitalResponse = jurorDigitalResponseRepository.findByJurorNumber(jurorNumber);
            assertThat(updatedJurorDigitalResponse).isNotNull();

            assertThat(updatedJurorDigitalResponse.getTitle()).isEqualTo(originalJurorDigitalResponse.getTitle());
            assertThat(updatedJurorDigitalResponse.getFirstName()).isEqualTo(
                originalJurorDigitalResponse.getFirstName());
            assertThat(updatedJurorDigitalResponse.getLastName()).isEqualTo(originalJurorDigitalResponse.getLastName());

            assertThat(updatedJurorDigitalResponse.getDateOfBirth()).isEqualTo(
                jurorPersonalDetailsDto.getDateOfBirth());
            assertThat(updatedJurorDigitalResponse.getEmail()).isEqualTo(originalJurorDigitalResponse.getEmail());

            assertThat(updatedJurorDigitalResponse.getAddressLine1())
                .isEqualTo(originalJurorDigitalResponse.getAddressLine1());
            assertThat(updatedJurorDigitalResponse.getAddressLine2())
                .isEqualTo(originalJurorDigitalResponse.getAddressLine2());
            assertThat(updatedJurorDigitalResponse.getAddressLine3())
                .isEqualTo(originalJurorDigitalResponse.getAddressLine3());
            assertThat(updatedJurorDigitalResponse.getAddressLine4())
                .isEqualTo(originalJurorDigitalResponse.getAddressLine4());
            assertThat(updatedJurorDigitalResponse.getAddressLine5())
                .isEqualTo(originalJurorDigitalResponse.getAddressLine5());

            JurorPaperResponseDto.ThirdParty thirdParty = jurorPersonalDetailsDto.getThirdParty();
            if (thirdParty != null) {
                assertThat(updatedJurorDigitalResponse.getRelationship()).isEqualTo(
                    originalJurorDigitalResponse.getRelationship());
                assertThat(updatedJurorDigitalResponse.getThirdPartyReason()).isEqualTo(
                    originalJurorDigitalResponse.getThirdPartyReason());
            }
        });
    }

    private void validateAgeDisqualificationMergedJurorRecord(String jurorNumber, ReplyMethod replyMethod,
                                                              int statusCode) {
        executeInTransaction(() -> {
            AbstractJurorResponse jurorResponse = createGenericJurorResponse(replyMethod, jurorNumber);
            JurorPool jurorPool =
                jurorPoolRepository.findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(jurorNumber, true)
                    .get(0);
            Juror juror = jurorPool.getJuror();

            assertThat(juror.getPendingTitle()).isEqualToIgnoringCase(jurorResponse.getTitle());
            assertThat(juror.getPendingFirstName()).isEqualToIgnoringCase(jurorResponse.getFirstName());
            assertThat(juror.getPendingLastName()).isEqualToIgnoringCase(jurorResponse.getLastName());

            assertThat(juror.getAddressLine1()).isEqualToIgnoringCase(jurorResponse.getAddressLine1());
            assertThat(juror.getAddressLine2()).isEqualToIgnoringCase(jurorResponse.getAddressLine2());
            assertThat(juror.getAddressLine3()).isEqualToIgnoringCase(jurorResponse.getAddressLine3());
            assertThat(juror.getAddressLine4()).isEqualToIgnoringCase(jurorResponse.getAddressLine4());
            assertThat(juror.getAddressLine5()).isEqualToIgnoringCase(jurorResponse.getAddressLine5());
            assertThat(juror.getPostcode()).isEqualToIgnoringCase(jurorResponse.getPostcode());

            assertThat(juror.isResponded()).isTrue();
            assertThat(jurorPool.getStatus().getStatus()).isEqualTo(statusCode);

            if (Boolean.TRUE.equals(jurorResponse.getWelsh())) {
                assertThat(juror.getWelsh()).isTrue();
            } else {
                assertThat(juror.getWelsh()).isNull();
            }
        });
    }

    private AbstractJurorResponse createGenericJurorResponse(ReplyMethod replyMethod, String jurorNumber) {
        if (replyMethod.equals(ReplyMethod.PAPER)) {
            return DataUtils.getJurorPaperResponse(jurorNumber, jurorPaperResponseRepository);
        } else {
            return DataUtils.getJurorDigitalResponse(jurorNumber, jurorDigitalResponseRepository);
        }
    }

    private void verifyAgeDisqualification(String jurorNumber) {
        executeInTransaction(() -> {
            JurorPool jurorPool =
                jurorPoolRepository.findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(jurorNumber, true)
                    .get(0);
            Juror juror = jurorPool.getJuror();

            assertThat(juror.isResponded()).as("Juror record should be updated and marked as responded").isTrue();
            assertThat(juror.getDisqualifyDate()).as("Juror record should be updated with a disqualified date")
                .isNotNull();
            assertThat(juror.getDisqualifyCode()).as("Juror record should be updated with a disqualification code")
                .isEqualTo(DisqualifyCode.A.toString());
            assertThat(jurorPool.getNextDate()).as("Juror record is no longer due to attend, expect NEXT_DATE to be "
                + "null").isNull();

            LocalDate yesterday = LocalDate.now().minusDays(1);

            List<JurorHistory> jurorHistoryList =
                jurorHistoryRepository.findByJurorNumberAndDateCreatedGreaterThanEqual(
                    jurorPool.getJurorNumber(), yesterday);
            assertThat(
                jurorHistoryList.stream()
                    .anyMatch(ph -> ph.getHistoryCode().equals(HistoryCodeMod.DISQUALIFY_POOL_MEMBER)))
                .as("Expect history record to be created for juror disqualification").isTrue();
            assertThat(
                jurorHistoryList.stream()
                    .anyMatch(ph -> ph.getHistoryCode().equals(HistoryCodeMod.WITHDRAWAL_LETTER))).as(
                "Expect history record to be created for disqualification letter").isTrue();

            List<BulkPrintData> bulkPrintData = bulkPrintDataRepository.findByJurorNo(jurorNumber);

            assertThat(bulkPrintData.size()).as("Expect a single disqualification letter to exist "
                + "(existing record updated)").isEqualTo(1);
        });
    }

    private void verifyStraightThroughAgeDisqualificationNotProcessed(String jurorNumber, ReplyMethod replyMethod,
                                                                      int statusCode) {
        executeInTransaction(() -> {
            AbstractJurorResponse jurorResponse = createGenericJurorResponse(replyMethod, jurorNumber);
            JurorPool jurorPool =
                jurorPoolRepository.findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(jurorNumber, true)
                    .get(0);
            final Juror juror = jurorPool.getJuror();

            assertThat(jurorResponse.getProcessingComplete())
                .as("No automatic processing, so processing complete flag remains unset").isNotEqualTo(Boolean.TRUE);
            assertThat(jurorResponse.getCompletedAt()).as("No automatic processing, so completed date remains unset")
                .isNull();
            assertThat(jurorResponse.getProcessingStatus()).as("No automatic processing, so processing status "
                + "remains as To Do").isEqualTo(ProcessingStatus.TODO);

            if (statusCode != IJurorStatus.RESPONDED) {
                assertThat(juror.isResponded()).as("No automatic processing, so juror record is not set to "
                    + "responded").isFalse();
            }
            assertThat(juror.getDisqualifyDate()).as("No automatic processing, so disqualification date remains "
                + "unset").isNull();
            assertThat(juror.getDisqualifyCode()).as("No automatic processing, so disqualification code remains "
                + "unset").isNull();
            assertThat(jurorPool.getStatus().getStatus()).as("No automatic processing, so status remains unchanged")
                .isEqualTo(statusCode);
            assertThat(jurorPool.getNextDate()).as("No automatic processing, so next date remains set").isNotNull();

            LocalDate yesterday = LocalDate.now().minusDays(1);

            List<JurorHistory> jurorHistoryList =
                jurorHistoryRepository.findByJurorNumberAndDateCreatedGreaterThanEqual(
                    jurorPool.getJurorNumber(), yesterday);
            assertThat(
                jurorHistoryList.stream()
                    .anyMatch(ph -> ph.getHistoryCode().equals(HistoryCodeMod.DISQUALIFY_POOL_MEMBER))).as(
                "Expect no history record to be created for juror disqualification").isFalse();
            assertThat(
                jurorHistoryList.stream().anyMatch(ph -> ph.getHistoryCode().equals(HistoryCodeMod.WITHDRAWAL_LETTER)))
                .as("Expect no history record to be created for disqualification letter").isFalse();

            List<BulkPrintData> bulkPrintData = bulkPrintDataRepository.findByJurorNo(jurorNumber);

            assertThat(bulkPrintData.size()).as("No disqualification letter expected to be generated")
                .isEqualTo(0);
        });
    }

    private void validateStaffAssignment(User staff) {
        executeInTransaction(() -> {
            assertThat(staff).isNotNull();
            assertThat(staff.getUsername()).isEqualTo("testlogin");
            assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.user_juror_response_audit",
                Integer.class))
                .isEqualTo(1);
            assertThat(jdbcTemplate.queryForObject("SELECT assigned_to FROM juror_mod.user_juror_response_audit",
                String.class))
                .isEqualTo("testlogin");
        });
    }

    private void sendRequestAndValidateRequest(String bureauJwt, BureauResponseStatusUpdateDto dto,
                                               final String description, final URI uri) {
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<BureauResponseStatusUpdateDto> requestEntity = new RequestEntity<>(dto, httpHeaders,
            POST, uri);
        ResponseEntity<String> exchange = template.exchange(requestEntity, String.class);
        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(exchange.getBody()).isNullOrEmpty();
    }
}
