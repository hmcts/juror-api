package uk.gov.hmcts.juror.api.moj.controller;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.history.Revision;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.bureau.controller.response.BureauJurorDetailDto;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.juror.controller.request.JurorResponseDto;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.DisqualificationLetterRepository;
import uk.gov.hmcts.juror.api.moj.controller.request.ConfirmIdentityDto;
import uk.gov.hmcts.juror.api.moj.controller.request.ContactLogRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.EditJurorRecordRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.FilterableJurorDetailsRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorAddressDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorCreateRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorNameDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorNotesRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorNumberAndPoolNumberDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorOpticRefRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorRecordFilterRequestQuery;
import uk.gov.hmcts.juror.api.moj.controller.request.PoliceCheckStatusDto;
import uk.gov.hmcts.juror.api.moj.controller.request.ProcessNameChangeRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.ProcessPendingJurorRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.RequestBankDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.UpdateAttendanceRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.ContactEnquiryTypeListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.ContactLogListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.FilterableJurorDetailsResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorAttendanceDetailsResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorBankDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorDetailsCommonResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorDetailsResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorNotesDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorOverviewResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorRecordSearchDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorSummonsReplyResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.NameDetails;
import uk.gov.hmcts.juror.api.moj.controller.response.PaymentDetails;
import uk.gov.hmcts.juror.api.moj.controller.response.PendingJurorsResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.BulkPrintData;
import uk.gov.hmcts.juror.api.moj.domain.ContactEnquiryType;
import uk.gov.hmcts.juror.api.moj.domain.ContactLog;
import uk.gov.hmcts.juror.api.moj.domain.FilterJurorRecord;
import uk.gov.hmcts.juror.api.moj.domain.FormCode;
import uk.gov.hmcts.juror.api.moj.domain.HistoryCode;
import uk.gov.hmcts.juror.api.moj.domain.IContactCode;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;
import uk.gov.hmcts.juror.api.moj.domain.PendingJuror;
import uk.gov.hmcts.juror.api.moj.domain.PoliceCheck;
import uk.gov.hmcts.juror.api.moj.domain.PoolHistory;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.Role;
import uk.gov.hmcts.juror.api.moj.domain.SortMethod;
import uk.gov.hmcts.juror.api.moj.domain.UserType;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.PaperResponse;
import uk.gov.hmcts.juror.api.moj.enumeration.ApprovalDecision;
import uk.gov.hmcts.juror.api.moj.enumeration.AttendanceType;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;
import uk.gov.hmcts.juror.api.moj.enumeration.IdCheckCodeEnum;
import uk.gov.hmcts.juror.api.moj.enumeration.PendingJurorStatusEnum;
import uk.gov.hmcts.juror.api.moj.enumeration.ReplyMethod;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.exception.RestResponseEntityExceptionHandler;
import uk.gov.hmcts.juror.api.moj.repository.BulkPrintDataRepository;
import uk.gov.hmcts.juror.api.moj.repository.ContactEnquiryTypeRepository;
import uk.gov.hmcts.juror.api.moj.repository.ContactLogRepository;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.repository.PendingJurorRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolTypeRepository;
import uk.gov.hmcts.juror.api.moj.repository.SummonsSnapshotRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorPaperResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorReasonableAdjustmentRepository;
import uk.gov.hmcts.juror.api.moj.utils.JurorPoolUtils;
import uk.gov.hmcts.juror.api.moj.utils.RepositoryUtils;

import java.net.URI;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpMethod.POST;
import static uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus.AWAITING_COURT_REPLY;
import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.FAILED_TO_ATTEND_HAS_ATTENDANCE_RECORD;
import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.FAILED_TO_ATTEND_HAS_COMPLETION_DATE;
import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.JUROR_STATUS_MUST_BE_FAILED_TO_ATTEND;
import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.JUROR_STATUS_MUST_BE_RESPONDED;


/**
 * Integration tests for the Juror Record controller.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SuppressWarnings({
    "PMD.ExcessiveImports",
    "PMD.CouplingBetweenObjects",
    "PMD.ExcessivePublicCount",
    "PMD.LawOfDemeter",
    "PMD.LinguisticNaming",
    "PMD.NcssCount",
    "PMD.CyclomaticComplexity",
    "PMD.TooManyMethods",
    "PMD.TooManyFields"
})
class JurorRecordControllerITest extends AbstractIntegrationTest {

    private static final String BASE_URL = "/api/v1/moj/juror-record";
    private static final String CONTACT_LOG_URL = "/api/v1/moj/juror-record/create/contact-log";
    private static final String GET_JUROR_NOTES_URL = "/api/v1/moj/juror-record/notes/123456789";

    @Autowired
    private Clock clock;
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private ContactLogRepository contactLogRepository;
    @Autowired
    private ContactEnquiryTypeRepository contactEnquiryTypeRepository;
    @Autowired
    private JurorRepository jurorRepository;
    @Autowired
    private JurorPoolRepository jurorPoolRepository;
    @Autowired
    private PendingJurorRepository pendingJurorRepository;
    @Autowired
    private CourtLocationRepository courtLocationRepository;
    @Autowired
    private SummonsSnapshotRepository summonsSnapshotRepository;
    @Autowired
    private PoolRequestRepository poolRequestRepository;
    @Autowired
    private JurorDigitalResponseRepositoryMod jurorResponseRepository;
    @Autowired
    private JurorHistoryRepository jurorHistoryRepository;
    @Autowired
    private JurorPaperResponseRepositoryMod jurorPaperResponseRepository;

    @Autowired
    private DisqualificationLetterRepository disqualificationLetterRepository;
    @Autowired
    private PoolTypeRepository poolTypeRepository;
    @Autowired
    private PoolHistoryRepository poolHistoryRepository;
    @Autowired
    private BulkPrintDataRepository bulkPrintDataRepository;
    private JurorReasonableAdjustmentRepository jurorReasonableAdjustmentRepository;
    private HttpHeaders httpHeaders;


    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        initHeaders();
    }

    private void initHeaders() throws Exception {

        BureauJwtPayload.Staff staff = new BureauJwtPayload.Staff();
        staff.setCourts(Collections.singletonList("400"));

        final String bureauJwt = mintBureauJwt(BureauJwtPayload.builder()
            .userType(UserType.BUREAU)
            .login("BUREAU_USER")
            .owner("400")
            .staff(staff)
            .build());

        httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_createJurorRecord.sql"})
    void createJurorRecordHappyPath() throws Exception {

        String poolNumber = "415220502";
        JurorCreateRequestDto requestDto = createJurorRequestDto(poolNumber);

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("415", Collections.singletonList("415"),
            UserType.COURT));

        ResponseEntity<?> response =
            restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, POST,
                URI.create("/api/v1/moj/juror-record/create-juror")), String.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP POST request to be CREATED")
            .isEqualTo(HttpStatus.CREATED);

    }

    private JurorCreateRequestDto createJurorRequestDto(String poolNumber) {
        JurorCreateRequestDto requestDto = new JurorCreateRequestDto();
        requestDto.setTitle("Mr");
        requestDto.setFirstName("John");
        requestDto.setLastName("Smith");

        JurorAddressDto addressDto = JurorAddressDto.builder()
            .lineOne("1 High Street")
            .lineTwo("Test")
            .lineThree("Test")
            .town("Test")
            .county("Test")
            .postcode("TE1 1ST")
            .build();
        requestDto.setAddress(addressDto);

        requestDto.setDateOfBirth(LocalDate.of(1990, 1, 1));
        requestDto.setPrimaryPhone("01234567890");
        requestDto.setEmailAddress("test@mail.com");
        requestDto.setPoolNumber(poolNumber);
        requestDto.setLocationCode("415");

        return requestDto;
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_createJurorRecordProcess.sql"})
    void getAllPendingJurorRecordsHappyPath() throws Exception {

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("416", Collections.singletonList("416"),
            UserType.COURT));

        ResponseEntity<PendingJurorsResponseDto> response =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                URI.create("/api/v1/moj/juror-record/pending-jurors/416")), PendingJurorsResponseDto.class);

        assertThat(response.getStatusCode()).as("Expect the HTTP Response Status to be OK")
            .isEqualTo(HttpStatus.OK);

        verifyAllPendingJurorResponse(response);
    }

    private static void verifyAllPendingJurorResponse(ResponseEntity<PendingJurorsResponseDto> response) {
        PendingJurorsResponseDto pendingJurorsResponseDto = response.getBody();
        assertThat(pendingJurorsResponseDto).isNotNull();

        List<PendingJurorsResponseDto.PendingJurorsResponseData> pendingJuror = pendingJurorsResponseDto.getData();
        assertThat(pendingJuror).isNotNull();
        assertThat(pendingJuror.size()).as("Expect there to be four records returned").isEqualTo(4);

        // verify the records are as expected
        assertThat(pendingJuror.get(0).getJurorNumber()).isEqualTo("041600001");
        assertThat(pendingJuror.get(0).getFirstName()).isEqualTo("Johna");
        assertThat(pendingJuror.get(0).getLastName()).isEqualTo("Smitha");
        assertThat(pendingJuror.get(0).getPendingJurorStatus().getCode()).isEqualTo(
            PendingJurorStatusEnum.QUEUED.getCode());
        assertThat(pendingJuror.get(0).getNotes()).isEqualTo("Notes on record");
        assertThat(pendingJuror.get(0).getPostcode()).isEqualTo("TE1 1ST");

        assertThat(pendingJuror.get(1).getJurorNumber()).isEqualTo("041600002");
        assertThat(pendingJuror.get(1).getFirstName()).isEqualTo("Johnb");
        assertThat(pendingJuror.get(1).getLastName()).isEqualTo("Smithb");
        assertThat(pendingJuror.get(1).getPendingJurorStatus().getCode()).isEqualTo(
            PendingJurorStatusEnum.QUEUED.getCode());
        assertThat(pendingJuror.get(1).getNotes()).isNull();
        assertThat(pendingJuror.get(1).getPostcode()).isEqualTo("TE1 2ST");

        assertThat(pendingJuror.get(2).getJurorNumber()).isEqualTo("041600003");
        assertThat(pendingJuror.get(2).getFirstName()).isEqualTo("Johnc");
        assertThat(pendingJuror.get(2).getLastName()).isEqualTo("Smithc");
        assertThat(pendingJuror.get(2).getPendingJurorStatus().getCode()).isEqualTo(
            PendingJurorStatusEnum.REJECTED.getCode());
        assertThat(pendingJuror.get(2).getNotes()).isEqualTo("Notes on record 3");
        assertThat(pendingJuror.get(2).getPostcode()).isEqualTo("TE1 3ST");

        assertThat(pendingJuror.get(3).getJurorNumber()).isEqualTo("041600004");
        assertThat(pendingJuror.get(3).getFirstName()).isEqualTo("Johnd");
        assertThat(pendingJuror.get(3).getLastName()).isEqualTo("Smithd");
        assertThat(pendingJuror.get(3).getPendingJurorStatus().getCode()).isEqualTo(
            PendingJurorStatusEnum.AUTHORISED.getCode());
        assertThat(pendingJuror.get(3).getNotes()).isEqualTo("Notes on record 4");
        assertThat(pendingJuror.get(3).getPostcode()).isEqualTo("TE1 4ST");
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_createJurorRecordProcess.sql"})
    void getQueuedPendingJurorRecordsHappyPath() throws Exception {

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("416", Collections.singletonList("416"),
            UserType.COURT));

        ResponseEntity<PendingJurorsResponseDto> response =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                    URI.create("/api/v1/moj/juror-record/pending-jurors/416?status=QUEUED")),
                PendingJurorsResponseDto.class);

        assertThat(response.getStatusCode()).as("Expect the HTTP Response Status to be OK")
            .isEqualTo(HttpStatus.OK);

        verifyQueuedPendingJurorResponse(response);
    }

    private static void verifyQueuedPendingJurorResponse(ResponseEntity<PendingJurorsResponseDto> response) {
        PendingJurorsResponseDto pendingJurorsResponseDto = response.getBody();
        assertThat(pendingJurorsResponseDto).isNotNull();

        List<PendingJurorsResponseDto.PendingJurorsResponseData> pendingJuror = pendingJurorsResponseDto.getData();
        assertThat(pendingJuror).isNotNull();
        assertThat(pendingJuror.size()).as("Expect there to be two records returned").isEqualTo(2);

        // verify the records are as expected
        assertThat(pendingJuror.get(0).getJurorNumber()).isEqualTo("041600001");
        assertThat(pendingJuror.get(0).getFirstName()).isEqualTo("Johna");
        assertThat(pendingJuror.get(0).getLastName()).isEqualTo("Smitha");
        assertThat(pendingJuror.get(0).getPendingJurorStatus().getCode()).isEqualTo(
            PendingJurorStatusEnum.QUEUED.getCode());
        assertThat(pendingJuror.get(0).getNotes()).isEqualTo("Notes on record");
        assertThat(pendingJuror.get(0).getPostcode()).isEqualTo("TE1 1ST");

        assertThat(pendingJuror.get(1).getJurorNumber()).isEqualTo("041600002");
        assertThat(pendingJuror.get(1).getFirstName()).isEqualTo("Johnb");
        assertThat(pendingJuror.get(1).getLastName()).isEqualTo("Smithb");
        assertThat(pendingJuror.get(1).getPendingJurorStatus().getCode()).isEqualTo(
            PendingJurorStatusEnum.QUEUED.getCode());
        assertThat(pendingJuror.get(1).getNotes()).isNull();
        assertThat(pendingJuror.get(1).getPostcode()).isEqualTo("TE1 2ST");
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_createJurorRecordProcess.sql"})
    void approvePendingJurorRecordHappyPath() throws Exception {

        ProcessPendingJurorRequestDto requestDto = createProcessPendingJurorRequestDto(ApprovalDecision.APPROVE);

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("416", Collections.singletonList("416"),
            UserType.COURT, Role.SENIOR_JUROR_OFFICER));

        ResponseEntity<?> response =
            restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, POST,
                URI.create("/api/v1/moj/juror-record/process-pending-juror")), String.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP response to be OK")
            .isEqualTo(HttpStatus.OK);

        //Check the record has been updated
        PendingJuror pendingJuror = RepositoryUtils.retrieveFromDatabase("041600001", pendingJurorRepository);
        assertThat(pendingJuror.getStatus().getCode()).isEqualTo(PendingJurorStatusEnum.AUTHORISED.getCode());

        //Check the Juror record has been created as well
        RepositoryUtils.retrieveFromDatabase("041600001", jurorRepository);
        JurorPool jurorPool = JurorPoolUtils.getSingleActiveJurorPool(jurorPoolRepository, "041600001");
        assertThat(jurorPool.getPool().getPoolNumber()).as("Expect the Juror to be created and in pool 416220503")
            .isEqualTo("416220503");
        assertThat(jurorPool.getStatus().getStatus()).as("Expect the Juror to be active")
            .isEqualTo(IJurorStatus.RESPONDED);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_createJurorRecordProcess.sql"})
    void rejectPendingJurorRecordHappyPath() throws Exception {

        ProcessPendingJurorRequestDto requestDto = createProcessPendingJurorRequestDto(ApprovalDecision.REJECT);

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("416", Collections.singletonList("416"),
            UserType.COURT, Role.SENIOR_JUROR_OFFICER));

        ResponseEntity<?> response =
            restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, POST,
                URI.create("/api/v1/moj/juror-record/process-pending-juror")), String.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP response to be OK")
            .isEqualTo(HttpStatus.OK);

    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_createJurorRecordProcess.sql"})
    void approvePendingJurorRecordUnhappyJuryOfficer() throws Exception {

        ProcessPendingJurorRequestDto requestDto = createProcessPendingJurorRequestDto(ApprovalDecision.APPROVE);

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("416", Collections.singletonList("416"),
            UserType.COURT));

        ResponseEntity<?> response =
            restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, POST,
                URI.create("/api/v1/moj/juror-record/process-pending-juror")), String.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP response to be FORBIDDEN")
            .isEqualTo(HttpStatus.FORBIDDEN);

    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_createJurorRecordProcess.sql"})
    void approvePendingJurorRecordUnhappyBureauOfficer() throws Exception {

        ProcessPendingJurorRequestDto requestDto = createProcessPendingJurorRequestDto(ApprovalDecision.APPROVE);

        ResponseEntity<?> response =
            restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, POST,
                URI.create("/api/v1/moj/juror-record/process-pending-juror")), String.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP response to be FORBIDDEN")
            .isEqualTo(HttpStatus.FORBIDDEN);

    }


    private ProcessPendingJurorRequestDto createProcessPendingJurorRequestDto(ApprovalDecision decision) {
        return ProcessPendingJurorRequestDto.builder()
            .jurorNumber("041600001")
            .decision(decision)
            .comments("Test Comments")
            .build();
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_editJurorRecord.sql"})
    void editJurorDetailsHappyPathAllFieldsRequired() {
        String jurorNumber = "123456789";

        EditJurorRecordRequestDto requestDto = createEditJurorRecordRequestDto(true);
        ResponseEntity<?> response =
            restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, HttpMethod.PATCH,
                URI.create("/api/v1/moj/juror-record/edit-juror/" + jurorNumber)), String.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP status to be NO CONTENT")
            .isEqualTo(HttpStatus.NO_CONTENT);

        JurorPool jurorPool = JurorPoolUtils.getSingleActiveJurorPool(jurorPoolRepository, jurorNumber);
        Juror juror = jurorPool.getJuror();

        //Check data has been changed and now matches what was in the dto.
        assertThat(juror.getTitle()).isEqualTo(requestDto.getTitle());
        assertThat(juror.getFirstName()).isEqualTo(requestDto.getFirstName());
        assertThat(juror.getLastName()).isEqualTo(requestDto.getLastName());
        assertThat(juror.getAddressLine1()).isEqualTo(requestDto.getAddressLineOne());
        assertThat(juror.getAddressLine2()).isEqualTo(requestDto.getAddressLineTwo());
        assertThat(juror.getAddressLine3()).isEqualTo(requestDto.getAddressLineThree());
        assertThat(juror.getAddressLine4()).isEqualTo(requestDto.getAddressTown());
        assertThat(juror.getAddressLine5()).isEqualTo(requestDto.getAddressCounty());
        assertThat(juror.getPostcode()).isEqualTo(requestDto.getAddressPostcode());
        assertThat(juror.getDateOfBirth()).isEqualTo(requestDto.getDateOfBirth());
        assertThat(juror.getPhoneNumber()).isEqualTo(requestDto.getPrimaryPhone());
        assertThat(juror.getEmail()).isEqualTo(requestDto.getEmailAddress());
        assertThat(juror.getReasonableAdjustmentCode()).isEqualTo(requestDto.getSpecialNeed());
        assertThat(juror.getReasonableAdjustmentMessage()).isEqualTo(requestDto.getSpecialNeedMessage());

        assertThat(juror.getOpticRef()).isEqualTo(requestDto.getOpticReference());
        assertThat(juror.getPendingTitle()).isEqualTo(requestDto.getPendingTitle());
        assertThat(juror.getPendingFirstName()).isEqualTo(requestDto.getPendingFirstName());
        assertThat(juror.getPendingLastName()).isEqualTo(requestDto.getPendingLastName());
        assertThat(juror.getWelsh()).isEqualTo(requestDto.getWelshLanguageRequired());
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_editJurorRecord.sql"})
    void editJurorDetailsHappyPathMandatoryFieldsOnly() {
        String jurorNumber = "123456789";

        EditJurorRecordRequestDto requestDto = createEditJurorRecordRequestDto(false);
        ResponseEntity<?> response =
            restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, HttpMethod.PATCH,
                URI.create("/api/v1/moj/juror-record/edit-juror/" + jurorNumber)), String.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP status to be NO CONTENT")
            .isEqualTo(HttpStatus.NO_CONTENT);

        JurorPool jurorPool = JurorPoolUtils.getSingleActiveJurorPool(jurorPoolRepository, jurorNumber);
        Juror juror = jurorPool.getJuror();

        //Check data has been changed and now matches what was in the dto.
        assertThat(juror.getTitle()).isEqualTo(requestDto.getTitle());
        assertThat(juror.getFirstName()).isEqualTo(requestDto.getFirstName());
        assertThat(juror.getLastName()).isEqualTo(requestDto.getLastName());
        assertThat(juror.getAddressLine1()).isEqualTo(requestDto.getAddressLineOne());
        assertThat(juror.getAddressLine2()).isEqualTo(requestDto.getAddressLineTwo());
        assertThat(juror.getAddressLine3()).isEqualTo(requestDto.getAddressLineThree());
        assertThat(juror.getAddressLine4()).isEqualTo(requestDto.getAddressTown());
        assertThat(juror.getAddressLine5()).isEqualTo(requestDto.getAddressCounty());
        assertThat(juror.getPostcode()).isEqualTo(requestDto.getAddressPostcode());
        assertThat(juror.getDateOfBirth()).isEqualTo(requestDto.getDateOfBirth());
        assertThat(juror.getPhoneNumber()).isEqualTo(requestDto.getPrimaryPhone());
        assertThat(juror.getEmail()).isEqualTo(requestDto.getEmailAddress());
        assertThat(juror.getReasonableAdjustmentCode()).isEqualTo(requestDto.getSpecialNeed());
        assertThat(juror.getReasonableAdjustmentMessage()).isEqualTo(requestDto.getSpecialNeedMessage());

        assertThat(juror.getOpticRef()).isEqualTo(requestDto.getOpticReference());
        assertThat(juror.getPendingTitle()).isEqualTo(requestDto.getPendingTitle());
        assertThat(juror.getPendingFirstName()).isEqualTo(requestDto.getPendingFirstName());
        assertThat(juror.getPendingLastName()).isEqualTo(requestDto.getPendingLastName());
        assertThat(juror.getWelsh()).isEqualTo(requestDto.getWelshLanguageRequired());
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_editJurorRecord.sql"})
    void editJurorDetailsWrongAccess() throws Exception {
        String jurorNumber = "123456789";

        EditJurorRecordRequestDto requestDto = createEditJurorRecordRequestDto(true);
        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("415", Collections.singletonList("415"),
            UserType.COURT));

        ResponseEntity<?> response =
            restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, HttpMethod.PATCH,
                URI.create("/api/v1/moj/juror-record/edit-juror/" + jurorNumber)), String.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP status to be FORBIDDEN")
            .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_editJurorRecord.sql"})
    void editJurorDetailsRecordNotFound() {
        String jurorNumber = "111111111";

        EditJurorRecordRequestDto requestDto = createEditJurorRecordRequestDto(true);

        ResponseEntity<?> response =
            restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, HttpMethod.PATCH,
                URI.create("/api/v1/moj/juror-record/edit-juror/" + jurorNumber)), String.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP status to be NOT_FOUND")
            .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_createJurorOpticReference.sql"})
    void createJurorOpticReferenceHappyPathDigital() {
        String jurorNumber = "123456789";
        String poolNumber = "415220502";
        String opticRef = "12345678";
        JurorOpticRefRequestDto requestDto = createOpticRefRequestDto(jurorNumber, poolNumber, opticRef);
        ResponseEntity<?> response =
            restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, POST,
                URI.create("/api/v1/moj/juror-record/create/optic-reference")), String.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP POST request to be CREATED")
            .isEqualTo(HttpStatus.CREATED);


        DigitalResponse digitalResponse = jurorResponseRepository.findByJurorNumber(jurorNumber);
        assertThat(digitalResponse).isNotNull();
        assertThat(digitalResponse.getProcessingStatus()).isEqualTo(AWAITING_COURT_REPLY);

        //assert optic ref is same as set above
        JurorPool jurorPool =
            jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumberAndIsActive(jurorNumber, poolNumber,
                true).get();
        Juror juror = jurorPool.getJuror();
        assertThat(juror.getOpticRef()).isEqualTo(opticRef);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_createJurorOpticReference.sql"})
    void createJurorOpticReferenceHappyPathPaper() {
        String jurorNumber = "987654321";
        String poolNumber = "415220502";
        String opticRef = "12345678";
        JurorOpticRefRequestDto requestDto = createOpticRefRequestDto(jurorNumber, poolNumber, opticRef);
        ResponseEntity<?> response =
            restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, POST,
                URI.create("/api/v1/moj/juror-record/create/optic-reference")), String.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP POST request to be CREATED")
            .isEqualTo(HttpStatus.CREATED);

        PaperResponse paperResponse =
            jurorPaperResponseRepository.findByJurorNumber(jurorNumber);
        assertThat(paperResponse).isNotNull();
        assertThat(paperResponse.getProcessingStatus()).isEqualTo(AWAITING_COURT_REPLY);

        //assert optic ref is same as set above
        JurorPool jurorPool =
            jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumberAndIsActive(jurorNumber, poolNumber,
                true).get();
        Juror juror = jurorPool.getJuror();
        assertThat(juror.getOpticRef()).isEqualTo(opticRef);
    }


    @ParameterizedTest
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_createJurorOpticReference.sql"})
    @ValueSource(strings = {"123456789", "987654321"})
    void createJurorOpticReferenceWrongAccess(String jurorNumber) throws Exception {
        String poolNumber = "415220502";
        String opticRef = "12345678";
        JurorOpticRefRequestDto requestDto = createOpticRefRequestDto(jurorNumber, poolNumber, opticRef);
        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("415", Collections.singletonList("415"),
            UserType.COURT));
        ResponseEntity<?> response =
            restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, POST,
                URI.create("/api/v1/moj/juror-record/create/optic-reference")), String.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP POST request to be FORBIDDEN ")
            .isEqualTo(HttpStatus.FORBIDDEN);

    }

    @ParameterizedTest
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_createJurorOpticReference.sql"})
    @ValueSource(strings = {"111111111", "211111111"})
    void createJurorOpticReferencProcessStatusClosed() throws Exception {
        String jurorNumber = "111111111";
        String poolNumber = "415220502";
        String opticRef = "12345678";
        JurorOpticRefRequestDto requestDto = createOpticRefRequestDto(jurorNumber, poolNumber, opticRef);
        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("415", Collections.singletonList("415"),
            UserType.COURT));
        ResponseEntity<String> response =
            restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, POST,
                URI.create("/api/v1/moj/juror-record/create/optic-reference")), String.class);
        assertThat(response.getStatusCode())
            .as("Expect the HTTP POST request to be FORBIDDEN ")
            .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).contains(
            "Cannot check court accommodation - Response has been completed/closed");

    }

    @ParameterizedTest
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_createJurorOpticReference.sql"})
    @ValueSource(strings = {"111111112", "211111112"})
    void createJurorOpticReferenceProcessingCompleted(String jurorNumber) throws Exception {
        String poolNumber = "415220502";
        String opticRef = "12345678";
        JurorOpticRefRequestDto requestDto = createOpticRefRequestDto(jurorNumber, poolNumber, opticRef);
        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("415", Collections.singletonList("415"),
            UserType.COURT));
        ResponseEntity<String> response =
            restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, POST,
                URI.create("/api/v1/moj/juror-record/create/optic-reference")), String.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP POST request to be FORBIDDEN ")
            .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).contains(
            "Cannot check court accommodation - Response has been completed/closed");
    }

    @Test
    void createJurorOpticReferenceWrongJurorNumber() throws Exception {
        String poolNumber = "415220502";
        String opticRef = "12345678";
        JurorOpticRefRequestDto requestDto = createOpticRefRequestDto("900000000", poolNumber, opticRef);
        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("415", Collections.singletonList("415"),
            UserType.COURT));
        ResponseEntity<String> response =
            restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, POST,
                URI.create("/api/v1/moj/juror-record/create/optic-reference")), String.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP POST request to be NOT_FOUND ")
            .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).contains(
            "Cannot find juror response record for juror 900000000");
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_jurorGetOpticalReferenceBureau.sql"})
    void getOpticReferenceBureauUser() {
        ResponseEntity<String> response = restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
            URI.create("/api/v1/moj/juror-record/optic-reference/123456789/415220502")), String.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET request to be OK")
            .isEqualTo(HttpStatus.OK);

        String opticReference = response.getBody();

        assertThat(opticReference).isEqualTo("12345678");
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_jurorGetOpticalReferenceCourt.sql"})
    void getOpticReferenceCourtUser() throws Exception {
        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("415", Collections.singletonList("415"),
            UserType.COURT));
        ResponseEntity<String> response = restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
            URI.create("/api/v1/moj/juror-record/optic-reference/123456789/415220502")), String.class);

        assertThat(response.getStatusCode()).as("Expect the HTTP GET request to be OK")
            .isEqualTo(HttpStatus.OK);

        String opticReference = response.getBody();

        assertThat(opticReference).isEqualTo("12345678");
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_jurorGetOpticalReferenceBureau.sql"})
    void getOpticReferenceCourtUserWrongAccess() throws Exception {
        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("415", Collections.singletonList("415"),
            UserType.COURT));
        ResponseEntity<String> response = restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
            URI.create("/api/v1/moj/juror-record/optic-reference/123456789/415220502")), String.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET request to be FORBIDDEN")
            .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_jurorGetOpticalReferenceCourt.sql"})
    void getOpticReferenceBureauUserCourtRecord() {
        ResponseEntity<String> response = restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
            URI.create("/api/v1/moj/juror-record/optic-reference/123456789/415220502")), String.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET request to be OK")
            .isEqualTo(HttpStatus.OK);

        String opticReference = response.getBody();

        assertThat(opticReference).isEqualTo("12345678");
    }

    /**
     * Juror Record Detail tab tests.
     */

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_jurorDetailRequestCourt.sql"})
    void getJurorDetailsCourtUser() throws Exception {

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("416", Collections.singletonList("416"),
            UserType.COURT));
        ResponseEntity<JurorDetailsResponseDto> response =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                URI.create("/api/v1/moj/juror-record/detail/641600090/416")), JurorDetailsResponseDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(HttpStatus.OK);

        JurorDetailsResponseDto jurorDetails = response.getBody();

        assertThat(jurorDetails).isNotNull();
        verifyResponse(jurorDetails);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_jurorDetailRequestCourtOnly.sql"})
    void getJurorDetailsCourtUserCourtRecordOnly() throws Exception {

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("416", Collections.singletonList("416"),
            UserType.COURT));
        ResponseEntity<JurorDetailsResponseDto> response =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                URI.create("/api/v1/moj/juror-record/detail/641600090/416")), JurorDetailsResponseDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(HttpStatus.OK);

        JurorDetailsResponseDto jurorDetails = response.getBody();

        assertThat(jurorDetails).isNotNull();
        verifyResponse(jurorDetails);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_jurorDetailRequestCourt.sql"})
    void getJurorDetailsBureauUserActiveRecord() {

        ResponseEntity<JurorDetailsResponseDto> response =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                URI.create("/api/v1/moj/juror-record/detail/641600090/416")), JurorDetailsResponseDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(HttpStatus.OK);

        //Bureau should still get the active record when there are two records
        JurorDetailsResponseDto jurorDetails = response.getBody();

        assertThat(jurorDetails).isNotNull();
        verifyResponse(jurorDetails);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_jurorDetailRequestSummoned.sql"})
    void getJurorDetailsBureauUserActiveRecordSummoned() {

        ResponseEntity<JurorDetailsResponseDto> response =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                URI.create("/api/v1/moj/juror-record/detail/641600092/416")), JurorDetailsResponseDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(HttpStatus.OK);

        JurorDetailsResponseDto jurorDetails = response.getBody();
        assertThat(jurorDetails).isNotNull();

        assertThat(jurorDetails.getReplyMethod())
            .as("Expect the Juror response reply method to be Digital")
            .isEqualTo("DIGITAL");
        assertThat(jurorDetails.getReplyProcessingStatus())
            .as("Expect the Juror response processing status to be To Do")
            .isEqualTo("To Do");

    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/truncate.sql",
        "/db/standing_data.sql", "/db/JurorRecordController_jurorDetailThirdPartyRequestBureau.sql"})
    void getJurorDetailsBureauUser() {

        ResponseEntity<JurorDetailsResponseDto> response =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                URI.create("/api/v1/moj/juror-record/detail/641600096/416")), JurorDetailsResponseDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(HttpStatus.OK);

        JurorDetailsResponseDto jurorDetails = response.getBody();

        assertThat(jurorDetails).isNotNull();
        assertThat(jurorDetails.getThirdParty())
            .as("Expect the Juror response third party details to be not null")
            .isNotNull();

        JurorResponseDto.ThirdParty thirdParty = jurorDetails.getThirdParty();
        verifyJurorAndThirdPartyDetails(jurorDetails, thirdParty);
    }

    private void verifyJurorAndThirdPartyDetails(JurorDetailsResponseDto jurorDetails,
                                                 JurorResponseDto.ThirdParty thirdParty) {
        assertThat(jurorDetails.getCommonDetails().getFirstName())
            .as("Expect the Juror first name to be FNAMEFIVEFOURZERO")
            .isEqualTo("FNAMEFIVEFOURZERO");
        assertThat(jurorDetails.getCommonDetails().getJurorNumber())
            .as("Expect the Juror number to be returned to match request")
            .isEqualTo("641600096");
        assertThat(jurorDetails.getCommonDetails().getJurorStatus())
            .as("Expect the Juror record status to be responded")
            .isEqualTo("Responded");
        assertThat(jurorDetails.getCommonDetails().getCourtName())
            .as("Expect the Juror record to be in Chichester Crown Court")
            .isEqualTo("THE CROWN COURT AT CHICHESTER");
        assertThat(thirdParty.getThirdPartyFName())
            .as("Expect the third party first name to be TPFIRSTNAME")
            .isEqualTo("TPFIRSTNAME");
        assertThat(thirdParty.getThirdPartyLName())
            .as("Expect the third party last name to be TPLASTNAME")
            .isEqualTo("TPLASTNAME");
        assertThat(thirdParty.getMainPhone())
            .as("Expect the third party main phone number to be 012033223")
            .isEqualTo("012033223");
        assertThat(thirdParty.getOtherPhone())
            .as("Expect the third party other phone number to be 07878787323")
            .isEqualTo("07878787323");
        assertThat(thirdParty.getRelationship())
            .as("Expect the third party relationship to be Son of the juror")
            .isEqualTo("Son of the juror");
        assertThat(thirdParty.getThirdPartyReason())
            .as("Expect the third party relationship to be Unable to read english or welsh")
            .isEqualTo("Unable to read english or welsh");
        assertThat(thirdParty.getUseJurorEmailDetails())
            .as("Expect the use Juror email flag to be false")
            .isEqualTo(false);
        assertThat(thirdParty.getUseJurorPhoneDetails())
            .as("Expect the use Juror phone flag to be false")
            .isEqualTo(false);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_jurorDetailRequestCourtOnly.sql"})
    void getJurorDetailsBureauUserCourtRecordOnly() {

        ResponseEntity<JurorDetailsResponseDto> response =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                URI.create("/api/v1/moj/juror-record/detail/641600090/416")), JurorDetailsResponseDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(HttpStatus.OK);

        JurorDetailsResponseDto jurorDetails = response.getBody();

        assertThat(jurorDetails).isNotNull();
        verifyResponse(jurorDetails);

    }

    private void verifyResponse(JurorDetailsResponseDto jurorDetails) {
        assertThat(jurorDetails.getCommonDetails().getFirstName())
            .as("Expect the Juror first name to be FNAMEFIVEFOURZERO")
            .isEqualTo("FNAMEFIVEFOURZERO");
        assertThat(jurorDetails.getCommonDetails().getJurorNumber())
            .as("Expect the Juror number to be returned to match request")
            .isEqualTo("641600090");
        assertThat(jurorDetails.getCommonDetails().getJurorStatus())
            .as("Expect the Juror record status to be responded")
            .isEqualTo("Responded");
        assertThat(jurorDetails.getCommonDetails().getCourtName())
            .as("Expect the Juror record to be in Chichester Crown Court")
            .isEqualTo("THE CROWN COURT AT CHICHESTER");
        assertThat(jurorDetails.getThirdParty())
            .as("Expect the Juror third party details to be null")
            .isEqualTo(null);
        assertThat(jurorDetails.getSpecialNeed())
            .as("Expect the reasonable adjustment code to be M")
            .isEqualTo("M");
        assertThat(jurorDetails.getSpecialNeedDescription())
            .as("Expect the reasonable adjustment description to be Multiple")
            .isEqualTo("Multiple");
        assertThat(jurorDetails.getSpecialNeedMessage())
            .isEqualTo("Reasonable adjustment test message");
        assertThat(jurorDetails.getOpticReference())
            .as("Expect the optic reference to be 18273645")
            .isEqualTo("18273645");
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_jurorDetailRequestBureau.sql"})
    void getJurorDetailsCourtUserForbiddenBureauRecord() throws Exception {

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("416", Collections.singletonList("416"),
            UserType.COURT));
        ResponseEntity<JurorDetailsResponseDto> response =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                URI.create("/api/v1/moj/juror-record/detail/641600090/416")), JurorDetailsResponseDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET request to be forbidden as its a Bureau only record")
            .isEqualTo(HttpStatus.FORBIDDEN);

    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_jurorDetailRequestBureau.sql"})
    void getJurorDetailsCourtUserForbiddenDifferentCourt() throws Exception {

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("417", Collections.singletonList("417"),
            UserType.COURT));
        ResponseEntity<JurorDetailsResponseDto> response =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                URI.create("/api/v1/moj/juror-record/detail/641600090/416")), JurorDetailsResponseDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET request to be forbidden as its a different court record")
            .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_jurorDetailRequestCourt.sql"})
    void getJurorDetailsCourtUserNoRecordMatch() throws Exception {

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("416", Collections.singletonList("416"),
            UserType.COURT));
        ResponseEntity<JurorDetailsResponseDto> response =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                URI.create("/api/v1/moj/juror-record/detail/641600099/416")), JurorDetailsResponseDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP Status for request to be NOT_FOUND")
            .isEqualTo(HttpStatus.NOT_FOUND);
    }

    /**
     * Juror Record Overview tests.
     */
    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_jurorOverviewRequestBureau.sql"})
    void getJurorOverviewBureauUserPaperResponse() {

        ResponseEntity<JurorOverviewResponseDto> response =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                URI.create("/api/v1/moj/juror-record/overview/641600090/416")), JurorOverviewResponseDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(HttpStatus.OK);

        JurorOverviewResponseDto jurorOverview = response.getBody();
        assertThat(jurorOverview).isNotNull();

        assertThat(jurorOverview.getCommonDetails().getOwner())
            .as("Expect the owner to be 416")
            .isEqualTo("416");
        assertThat(jurorOverview.getCommonDetails().getJurorNumber())
            .as("Expect the Juror number to be returned to match request")
            .isEqualTo("641600090");
        assertThat(jurorOverview.getCommonDetails().getJurorStatus())
            .as("Expect the Juror status to be Panel")
            .isEqualTo("Panel");
        assertThat(jurorOverview.getSpecialNeed())
            .as("Expect the reasonable adjustment code to be M")
            .isEqualTo("M");
        assertThat(jurorOverview.getSpecialNeedDescription())
            .as("Expect the reasonable adjustment description to be Multiple")
            .isEqualTo("Multiple");
        assertThat(jurorOverview.getSpecialNeedMessage())
            .isEqualTo("Reasonable adjustment test message");
        assertThat(jurorOverview.getOpticReference())
            .as("Expect the optic reference to be 12345678")
            .isEqualTo("12345678");
        assertThat(jurorOverview.getWelshLanguageRequired())
            .as("Expect the Welsh language flag to be true")
            .isTrue();
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/truncate.sql", "/db/standing_data.sql",
        "/db/JurorRecordController_jurorOverviewRequestBureau.sql"})
    void getJurorOverviewBureauUserOnlineResponse() {

        ResponseEntity<JurorOverviewResponseDto> response =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                URI.create("/api/v1/moj/juror-record/overview/641600091/416")), JurorOverviewResponseDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(HttpStatus.OK);

        JurorOverviewResponseDto jurorOverview = response.getBody();
        assertThat(jurorOverview).isNotNull();

        assertThat(jurorOverview.getCommonDetails().getJurorNumber())
            .as("Expect the Juror number to be returned to match request")
            .isEqualTo("641600091");
        assertThat(jurorOverview.getCommonDetails().getJurorStatus())
            .as("Expect the Juror status to be Responded")
            .isEqualTo("Responded");
        assertThat(jurorOverview.getSpecialNeed())
            .as("Expect the reasonable adjustment code to be M")
            .isEqualTo("M");
        assertThat(jurorOverview.getSpecialNeedDescription())
            .as("Expect the reasonable adjustment description to be Multiple")
            .isEqualTo("Multiple");
        assertThat(jurorOverview.getSpecialNeedMessage())
            .isEqualTo("Reasonable adjustment test message");
        assertThat(jurorOverview.getOpticReference())
            .as("Expect the optic reference to be 87654321")
            .isEqualTo("87654321");
        assertThat(jurorOverview.getWelshLanguageRequired())
            .as("Expect the Welsh language flag to be false")
            .isFalse();
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_jurorOverviewRequestBureau.sql"})
    void getJurorOverviewBureauUserSummonedResponse() {

        ResponseEntity<JurorOverviewResponseDto> response =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                URI.create("/api/v1/moj/juror-record/overview/641600092/416")), JurorOverviewResponseDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(HttpStatus.OK);

        JurorOverviewResponseDto jurorOverview = response.getBody();
        assertThat(jurorOverview).isNotNull();

        assertThat(jurorOverview.getCommonDetails().getJurorNumber())
            .as("Expect the Juror number to be returned to match request")
            .isEqualTo("641600092");
        assertThat(jurorOverview.getCommonDetails().getJurorStatus())
            .as("Expect the Juror status to be Summoned")
            .isEqualTo("Summoned");
        assertThat(jurorOverview.getReplyMethod())
            .as("Expect the reply method to be empty for Summoned citizens")
            .isNull();
        assertThat(jurorOverview.getSpecialNeed())
            .as("Expect the reasonable adjustment code to be M")
            .isEqualTo("M");
        assertThat(jurorOverview.getSpecialNeedDescription())
            .as("Expect the reasonable adjustment description to be Multiple")
            .isEqualTo("Multiple");
        assertThat(jurorOverview.getSpecialNeedMessage())
            .isEqualTo("Reasonable adjustment test message");
        assertThat(jurorOverview.getOpticReference())
            .as("Expect the optic reference to be 18273645")
            .isNull();
        assertThat(jurorOverview.getWelshLanguageRequired())
            .as("Expect the Welsh language flag to be null")
            .isNull();
    }


    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_jurorOverviewRequestBureau.sql"})
    void getJurorOverviewBureauUserDisqualifiedOnSelectionResponse() {

        ResponseEntity<JurorOverviewResponseDto> response =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                URI.create("/api/v1/moj/juror-record/overview/641600093/416")), JurorOverviewResponseDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(HttpStatus.OK);

        JurorOverviewResponseDto jurorOverview = response.getBody();
        assertThat(jurorOverview).isNotNull();

        assertThat(jurorOverview.getCommonDetails().getJurorNumber())
            .as("Expect the Juror number to be returned to match request")
            .isEqualTo("641600093");
        assertThat(jurorOverview.getCommonDetails().getJurorStatus())
            .as("Expect the Juror status to be Disqualified")
            .isEqualTo("Disqualified");
        assertThat(jurorOverview.getReplyMethod())
            .as("Expect the reply method to be empty for Disqualified citizens")
            .isNull();
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_jurorOverviewRequestBureau.sql"})
    void getJurorOverviewBureauUserDisqualifiedNotOnSelectionResponse() {

        ResponseEntity<JurorOverviewResponseDto> response =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                URI.create("/api/v1/moj/juror-record/overview/641600095/416")), JurorOverviewResponseDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(HttpStatus.OK);

        JurorOverviewResponseDto jurorOverview = response.getBody();
        assertThat(jurorOverview).isNotNull();

        assertThat(jurorOverview.getCommonDetails().getJurorNumber())
            .as("Expect the Juror number to be returned to match request")
            .isEqualTo("641600095");
        assertThat(jurorOverview.getCommonDetails().getJurorStatus())
            .as("Expect the Juror status to be Disqualified")
            .isEqualTo("Disqualified");
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_jurorOverviewRequestBureau.sql"})
    void getJurorOverviewBureauUserNotAvailableResponse() {

        ResponseEntity<JurorOverviewResponseDto> response =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                URI.create("/api/v1/moj/juror-record/overview/641600094/416")), JurorOverviewResponseDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(HttpStatus.OK);

        JurorOverviewResponseDto jurorOverview = response.getBody();
        assertThat(jurorOverview).isNotNull();

        assertThat(jurorOverview.getCommonDetails().getJurorNumber())
            .as("Expect the Juror number to be returned to match request")
            .isEqualTo("641600094");
        assertThat(jurorOverview.getCommonDetails().getJurorStatus())
            .as("Expect the Juror status to be Responded")
            .isEqualTo("Responded");
    }


    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_jurorOverviewRequestBureau.sql"})
    void getOverviewDetailsCourtUserSummonedResponse() throws Exception {

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("416", Collections.singletonList("416"),
            UserType.COURT));
        ResponseEntity<JurorDetailsResponseDto> response =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                URI.create("/api/v1/moj/juror-record/overview/641600092/416")), JurorDetailsResponseDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET request to be forbidden as its a summoned user")
            .isEqualTo(HttpStatus.FORBIDDEN);
    }

    /**
     * Juror Record Summons Reply tests.
     */

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_jurorSummonsReplyBureau.sql"})
    void getJurorSummonsReplyBureauUserPaperResponse() {

        ResponseEntity<JurorSummonsReplyResponseDto> response =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                    URI.create("/api/v1/moj/juror-record/summons-reply/641600090/416")),
                JurorSummonsReplyResponseDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(HttpStatus.OK);

        JurorSummonsReplyResponseDto jurorSummonsReply = response.getBody();
        assertNotNull(jurorSummonsReply, "Should not be null");

        assertThat(jurorSummonsReply.getCommonDetails().getJurorNumber())
            .as("Expect the Juror number to be returned to match request")
            .isEqualTo("641600090");
        assertThat(jurorSummonsReply.getCommonDetails().getJurorStatus())
            .as("Expect the Juror status to be Panel")
            .isEqualTo("Panel");
        assertThat(jurorSummonsReply.getReplyMethod())
            .as("Expect the reply method to be PAPER")
            .isEqualTo("PAPER");
        assertThat(jurorSummonsReply.getReplyDate())
            .as("Expect the reply date to be empty")
            .isNull();
        assertThat(jurorSummonsReply.getReplyStatus())
            .as("Expect the reply status to be empty")
            .isNull();
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_jurorSummonsReplyBureau.sql"})
    void getJurorSummonsReplyBureauUserPaperResponseExcusalRefused() {

        ResponseEntity<JurorSummonsReplyResponseDto> response =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                    URI.create("/api/v1/moj/juror-record/summons-reply/641600096/416")),
                JurorSummonsReplyResponseDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(HttpStatus.OK);

        JurorSummonsReplyResponseDto jurorSummonsReply = response.getBody();
        assertThat(jurorSummonsReply).as("Expect the response to not be empty").isNotNull();

        JurorDetailsCommonResponseDto commonResponseDto = jurorSummonsReply.getCommonDetails();

        assertThat(commonResponseDto.getJurorNumber())
            .as("Expect the Juror number to be returned to match request")
            .isEqualTo("641600096");
        assertThat(commonResponseDto.getJurorStatus())
            .as("Expect the Juror status to be Responded")
            .isEqualTo("Responded");
        assertThat(jurorSummonsReply.getReplyMethod())
            .as("Expect the reply method to be PAPER")
            .isEqualTo("PAPER");
        assertThat(jurorSummonsReply.getReplyDate())
            .as("Expect the reply date to be 08/3/2023")
            .isEqualTo(LocalDate.of(2023, 3, 8));
        assertThat(jurorSummonsReply.getReplyStatus())
            .as("Expect the reply status to be Closed")
            .isEqualTo("Closed");
        assertThat(commonResponseDto.getExcusalRejected())
            .as("Expect the excusal rejected to be 'Y'")
            .isEqualTo("Y");
        assertThat(commonResponseDto.getExcusalCode())
            .as("Expect the excusal code to be 'C'")
            .isEqualTo("C");
        assertThat(commonResponseDto.getExcusalDescription())
            .as("Expect the excusal reason description to be 'Childcare'")
            .isEqualTo("Childcare");
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_jurorSummonsReplyBureau.sql"})
    void getJurorSummonsReplyBureauUserDigitalResponseExcusalRefused() {

        ResponseEntity<JurorSummonsReplyResponseDto> response =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                    URI.create("/api/v1/moj/juror-record/summons-reply/641600097/416")),
                JurorSummonsReplyResponseDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(HttpStatus.OK);

        JurorSummonsReplyResponseDto jurorSummonsReply = response.getBody();
        assertNotNull(jurorSummonsReply, "Should not be null");

        assertThat(jurorSummonsReply.getCommonDetails().getJurorNumber())
            .as("Expect the Juror number to be returned to match request")
            .isEqualTo("641600097");
        assertThat(jurorSummonsReply.getCommonDetails().getJurorStatus())
            .as("Expect the Juror status to be Responded")
            .isEqualTo("Responded");
        assertThat(jurorSummonsReply.getReplyMethod())
            .as("Expect the reply method to be DIGITAL")
            .isEqualTo("DIGITAL");
        assertThat(jurorSummonsReply.getReplyDate().toString())
            .as("Expect the reply date to be 08/10/2022")
            .isEqualTo(LocalDate.of(2022, 10, 8).toString());
        assertThat(jurorSummonsReply.getReplyStatus())
            .as("Expect the reply status to be Closed")
            .isEqualTo("Closed");
        assertThat(jurorSummonsReply.getCommonDetails().getExcusalRejected())
            .as("Expect the excusal rejected to be 'Y'")
            .isEqualTo("Y");
        assertThat(jurorSummonsReply.getCommonDetails().getExcusalCode())
            .as("Expect the excusal code to be 'C'")
            .isEqualTo("C");
        assertThat(jurorSummonsReply.getCommonDetails().getExcusalDescription())
            .as("Expect the excusal reason description to be 'Childcare'")
            .isEqualTo("Childcare");
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/truncate.sql", "/db/standing_data.sql",
        "/db/JurorRecordController_jurorSummonsReplyBureau.sql"})
    void getJurorSummonsReplyBureauUserOnlineResponse() {

        ResponseEntity<JurorSummonsReplyResponseDto> response =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                    URI.create("/api/v1/moj/juror-record/summons-reply/641600091/416")),
                JurorSummonsReplyResponseDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(HttpStatus.OK);

        JurorSummonsReplyResponseDto jurorSummonsReply = response.getBody();
        assertNotNull(jurorSummonsReply, "Should not be null");

        assertThat(jurorSummonsReply.getCommonDetails().getJurorNumber())
            .as("Expect the Juror number to be returned to match request")
            .isEqualTo("641600091");
        assertThat(jurorSummonsReply.getCommonDetails().getJurorStatus())
            .as("Expect the Juror status to be Responded")
            .isEqualTo("Responded");
        assertThat(jurorSummonsReply.getReplyMethod())
            .as("Expect the reply method to be DIGITAL for online response")
            .isEqualTo("DIGITAL");
        assertThat(jurorSummonsReply.getReplyDate().toString())
            .as("Expect the reply date to be 2022-10-08")
            .isEqualTo("2022-10-08");
        assertThat(jurorSummonsReply.getReplyStatus())
            .as("Expect the reply status to be Closed")
            .isEqualTo("Closed");
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_jurorSummonsReplyBureau.sql"})
    void getJurorSummonsReplyBureauUserSummonedResponse() {

        ResponseEntity<JurorSummonsReplyResponseDto> response =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                    URI.create("/api/v1/moj/juror-record/summons-reply/641600092/416")),
                JurorSummonsReplyResponseDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(HttpStatus.OK);

        JurorSummonsReplyResponseDto jurorSummonsReply = response.getBody();
        assertNotNull(jurorSummonsReply, "Should not be null");

        assertThat(jurorSummonsReply.getCommonDetails().getJurorNumber())
            .as("Expect the Juror number to be returned to match request")
            .isEqualTo("641600092");
        assertThat(jurorSummonsReply.getCommonDetails().getJurorStatus())
            .as("Expect the Juror status to be Summoned")
            .isEqualTo("Summoned");
        assertThat(jurorSummonsReply.getReplyMethod())
            .as("Expect the reply method to be empty for Summoned citizens")
            .isNull();
    }


    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_jurorSummonsReplyBureau.sql"})
    void getJurorSummonsReplyBureauUserDisqualifiedOnSelectionResponse() {

        ResponseEntity<JurorSummonsReplyResponseDto> response =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                    URI.create("/api/v1/moj/juror-record/summons-reply/641600093/416")),
                JurorSummonsReplyResponseDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(HttpStatus.OK);

        JurorSummonsReplyResponseDto jurorSummonsReply = response.getBody();
        assertNotNull(jurorSummonsReply, "Should not be null");

        assertThat(jurorSummonsReply.getCommonDetails().getJurorNumber())
            .as("Expect the Juror number to be returned to match request")
            .isEqualTo("641600093");
        assertThat(jurorSummonsReply.getCommonDetails().getJurorStatus())
            .as("Expect the Juror status to be Disqualified")
            .isEqualTo("Disqualified");
        assertThat(jurorSummonsReply.getReplyMethod())
            .as("Expect the reply method to be empty for Disqualified citizens")
            .isNull();
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_jurorSummonsReplyBureau.sql"})
    void getJurorSummonsReplyBureauUserDisqualifiedNotOnSelectionResponse() {

        ResponseEntity<JurorSummonsReplyResponseDto> response =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                    URI.create("/api/v1/moj/juror-record/summons-reply/641600095/416")),
                JurorSummonsReplyResponseDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(HttpStatus.OK);

        JurorSummonsReplyResponseDto jurorSummonsReply = response.getBody();
        assertNotNull(jurorSummonsReply, "Should not be null");

        assertThat(jurorSummonsReply.getCommonDetails().getJurorNumber())
            .as("Expect the Juror number to be returned to match request")
            .isEqualTo("641600095");
        assertThat(jurorSummonsReply.getCommonDetails().getJurorStatus())
            .as("Expect the Juror status to be Disqualified")
            .isEqualTo("Disqualified");
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_jurorSummonsReplyBureau.sql"})
    void getJurorSummonsReplyBureauUserNotAvailableResponse() {

        ResponseEntity<JurorSummonsReplyResponseDto> response =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                    URI.create("/api/v1/moj/juror-record/summons-reply/641600094/416")),
                JurorSummonsReplyResponseDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(HttpStatus.OK);

        JurorSummonsReplyResponseDto jurorSummonsReply = response.getBody();
        assertNotNull(jurorSummonsReply, "Should not be null");

        assertThat(jurorSummonsReply.getCommonDetails().getJurorNumber())
            .as("Expect the Juror number to be returned to match request")
            .isEqualTo("641600094");
        assertThat(jurorSummonsReply.getReplyMethod())
            .as("Expect the reply method to be RESPONSE N/A as reply was over 12 months ago")
            .isEqualTo("RESPONSE N/A");
        assertThat(jurorSummonsReply.getCommonDetails().getJurorStatus())
            .as("Expect the Juror status to be Responded")
            .isEqualTo("Responded");
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_jurorSummonsReplyBureau.sql"})
    void getJurorSummonsReplyCourtUserSummonedResponse() throws Exception {

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("416", Collections.singletonList("416"),
            UserType.COURT));
        ResponseEntity<JurorSummonsReplyResponseDto> response =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                    URI.create("/api/v1/moj/juror-record/summons-reply/641600092/416")),
                JurorSummonsReplyResponseDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET request to be forbidden as its a summoned user")
            .isEqualTo(HttpStatus.FORBIDDEN);

    }

    /**
     * Juror record Search tests.
     */

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_jurorSearch.sql"})
    void searchJurorBureauUserActiveBureauRecord() {

        ResponseEntity<JurorRecordSearchDto> response =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                    URI.create("/api/v1/moj/juror-record/single-search?jurorNumber=641600091")),
                JurorRecordSearchDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(HttpStatus.OK);

        JurorRecordSearchDto jurorSearchRecord = response.getBody();
        assertThat(jurorSearchRecord).isNotNull();

        List<JurorRecordSearchDto.JurorRecordSearchDataDto> dataDto = jurorSearchRecord.getData();

        assertThat(dataDto)
            .as("Search data is not null")
            .isNotNull();
        assertThat(dataDto.size())
            .as("There is one active record for this juror")
            .isEqualTo(1);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_jurorSearch.sql"})
    void searchJurorBureauUserActiveCourtRecord() {

        ResponseEntity<JurorRecordSearchDto> response =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                    URI.create("/api/v1/moj/juror-record/single-search?jurorNumber=641600090")),
                JurorRecordSearchDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(HttpStatus.OK);

        JurorRecordSearchDto jurorSearchRecord = response.getBody();
        assertThat(jurorSearchRecord).isNotNull();

        List<JurorRecordSearchDto.JurorRecordSearchDataDto> dataDto = jurorSearchRecord.getData();

        assertThat(dataDto)
            .as("Search data is not null")
            .isNotNull();
        assertThat(dataDto.size())
            .as("There is one active record for this juror")
            .isEqualTo(1);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_jurorSearch.sql"})
    void searchJurorBureauUserMultipleActiveRecords() {

        ResponseEntity<JurorRecordSearchDto> response =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                    URI.create("/api/v1/moj/juror-record/single-search?jurorNumber=641500091")),
                JurorRecordSearchDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(HttpStatus.OK);

        JurorRecordSearchDto jurorSearchRecord = response.getBody();
        assertThat(jurorSearchRecord).isNotNull();

        List<JurorRecordSearchDto.JurorRecordSearchDataDto> dataDto = jurorSearchRecord.getData();

        assertThat(dataDto)
            .as("Search data is not null")
            .isNotNull();
        assertThat(dataDto.size())
            .as("There are two active records for this juror")
            .isEqualTo(2);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_jurorSearch.sql"})
    void searchJurorBureauUserNoRecordFound() {

        ResponseEntity<JurorRecordSearchDto> response =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                    URI.create("/api/v1/moj/juror-record/single-search?jurorNumber=641600099")),
                JurorRecordSearchDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(HttpStatus.OK);

        JurorRecordSearchDto jurorSearchRecord = response.getBody();
        assertThat(jurorSearchRecord).isNotNull();

        List<JurorRecordSearchDto.JurorRecordSearchDataDto> dataDto = jurorSearchRecord.getData();

        assertThat(dataDto)
            .as("Search data is empty")
            .isEmpty();
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_jurorSearch.sql"})
    void searchJurorCourtUserActiveCourtRecord() throws Exception {

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("416",
            Collections.singletonList("416"), UserType.COURT));

        ResponseEntity<JurorRecordSearchDto> response =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                    URI.create("/api/v1/moj/juror-record/single-search?jurorNumber=641600090")),
                JurorRecordSearchDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(HttpStatus.OK);

        JurorRecordSearchDto jurorSearchRecord = response.getBody();
        assertThat(jurorSearchRecord).isNotNull();

        List<JurorRecordSearchDto.JurorRecordSearchDataDto> dataDto = jurorSearchRecord.getData();

        assertThat(dataDto)
            .as("Search data is not null")
            .isNotNull();
        assertThat(dataDto.size())
            .as("There is one active record for this juror")
            .isEqualTo(1);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_jurorSearch.sql"})
    void searchJurorCourtUserMultipleActiveRecords() throws Exception {

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("415", Arrays.asList("415", "767"),
            UserType.COURT));

        ResponseEntity<JurorRecordSearchDto> response =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                    URI.create("/api/v1/moj/juror-record/single-search?jurorNumber=641500091")),
                JurorRecordSearchDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(HttpStatus.OK);

        JurorRecordSearchDto jurorSearchRecord = response.getBody();
        assertThat(jurorSearchRecord).isNotNull();

        List<JurorRecordSearchDto.JurorRecordSearchDataDto> dataDto = jurorSearchRecord.getData();

        assertThat(dataDto)
            .as("Search data is not null")
            .isNotNull();
        assertThat(dataDto.size())
            .as("There are two active records for this juror")
            .isEqualTo(2);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_transferredRecord.sql"})
    void getJurorContactLogsBureauUserHappyPath() {
        ResponseEntity<ContactLogListDto> response =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                URI.create("/api/v1/moj/juror-record/contact-log/123456789")), ContactLogListDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(HttpStatus.OK);

        ContactLogListDto contactLogListDto = response.getBody();

        assertThat(contactLogListDto).isNotNull();
        assertThat(contactLogListDto.getData().size())
            .as("Expect the Response to contain two contact log data items")
            .isEqualTo(2);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_jurorSearch.sql"})
    void searchJurorCourtUserActiveBureauRecord() throws Exception {

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("416", Collections.singletonList("416"),
            UserType.COURT));

        ResponseEntity<JurorRecordSearchDto> response =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                    URI.create("/api/v1/moj/juror-record/single-search?jurorNumber=641600091")),
                JurorRecordSearchDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(HttpStatus.OK);

        JurorRecordSearchDto jurorSearchRecord = response.getBody();
        assertThat(jurorSearchRecord).isNotNull();

        List<JurorRecordSearchDto.JurorRecordSearchDataDto> dataDto = jurorSearchRecord.getData();

        assertThat(dataDto)
            .as("Search data is empty as no records returned")
            .isEmpty();

    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_jurorSearch.sql"})
    void searchJurorCourtUserNoRecordFound() throws Exception {

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("416", Collections.singletonList("416"),
            UserType.COURT));

        ResponseEntity<JurorRecordSearchDto> response =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                    URI.create("/api/v1/moj/juror-record/single-search?jurorNumber=641600099")),
                JurorRecordSearchDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(HttpStatus.OK);

        JurorRecordSearchDto jurorSearchRecord = response.getBody();
        assertThat(jurorSearchRecord).isNotNull();

        List<JurorRecordSearchDto.JurorRecordSearchDataDto> dataDto = jurorSearchRecord.getData();

        assertThat(dataDto)
            .as("Search data is empty")
            .isEmpty();
    }

    @Sql(statements = {"DELETE FROM juror_mod.juror_pool", "DELETE FROM juror_mod.juror", "DELETE FROM juror_mod.pool"})
    void getJurorContactLogsBureauUserJurorNotFound() {
        ResponseEntity<JurorDetailsResponseDto> response =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                URI.create("/api/v1/moj/juror-record/contact-log/123456789")), JurorDetailsResponseDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET request to be not found as the juror does not exist")
            .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_transferredRecord.sql"})
    void getJurorContactLogsCourtsUserHappyPath() throws Exception {
        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("415", Collections.singletonList("415"),
            UserType.COURT));
        ResponseEntity<ContactLogListDto> response =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                URI.create("/api/v1/moj/juror-record/contact-log/123456789")), ContactLogListDto.class);

        ContactLogListDto contactLogListDto = response.getBody();

        assertThat(contactLogListDto).isNotNull();
        assertThat(contactLogListDto.getData().size())
            .as("Expect the Response to contain two contact log data items")
            .isEqualTo(2);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_bureauOwnedRecord.sql"})
    void getJurorContactLogsCourtsUserBureauLogs() throws Exception {
        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("415", Collections.singletonList("415"),
            UserType.COURT));
        ResponseEntity<JurorDetailsResponseDto> response =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                URI.create("/api/v1/moj/juror-record/contact-log/123456789")), JurorDetailsResponseDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET request to be forbidden as the juror is owned by the bureau")
            .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_transferredRecord.sql"})
    void getJurorContactLogsCourtsUserDifferentCourtLogs() throws Exception {
        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("416", Collections.singletonList("416"),
            UserType.COURT));
        ResponseEntity<JurorDetailsResponseDto> response =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                URI.create("/api/v1/moj/juror-record/overview/123456789/415")), JurorDetailsResponseDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET request to be forbidden as the juror is owned by a court this user does "
                + "not have access to")
            .isEqualTo(HttpStatus.FORBIDDEN);
    }

    private ContactLogRequestDto createContactLogRequestDto(String jurorNumber, String enquiryCode,
                                                            String notes, Boolean repeatEnquiry) {
        ContactLogRequestDto requestDto = new ContactLogRequestDto();
        requestDto.setJurorNumber(jurorNumber);
        requestDto.setStartCall(LocalDateTime.now());
        requestDto.setEnquiryType(enquiryCode);
        requestDto.setNotes(notes);
        requestDto.setRepeatEnquiry(repeatEnquiry);
        return requestDto;
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_bureauOwnedRecord.sql"})
    void createJurorContactLogBureauUserHappyPath() {
        String jurorNumber = "123456789";
        ContactLogRequestDto requestDto = createContactLogRequestDto(jurorNumber, "LS",
            "Repeat Enquiry Notes", true);
        ResponseEntity<?> response =
            restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, POST,
                URI.create(CONTACT_LOG_URL)), String.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(HttpStatus.CREATED);

        List<ContactLog> contactLogs =
            contactLogRepository.findByJurorNumber(jurorNumber);

        assertThat(contactLogs.size())
            .as("Initial test data contained a single contact log, expect a second, new contact log to be created")
            .isEqualTo(2);
        ContactLog contactLog = contactLogs.stream().filter(cl ->
            cl.getEnquiryType().getCode().equals(IContactCode.LENGTH_OF_SERVICE.getCode())).findFirst().get();

        assertThat(contactLog.getJurorNumber())
            .as("The newly created contact log should have it's Juror Number value mapped from the request DTO")
            .isEqualTo(requestDto.getJurorNumber());
        LocalDateTime startCall = requestDto.getStartCall();
        assertThat(contactLog.getStartCall())
            .as("The newly created contact log should have it's Start Call value mapped from the request DTO")
            .hasDayOfMonth(startCall.getDayOfMonth()).hasMonth(startCall.getMonth()).hasYear(startCall.getYear());
        assertThat(contactLog.getUsername())
            .as("The newly created contact log should have it's Username value mapped from the request DTO")
            .isEqualTo("BUREAU_USER");
        assertThat(contactLog.getEnquiryType().getCode())
            .as("The newly created contact log should have it's Enquiry Type value mapped from the request DTO")
            .isEqualTo(requestDto.getEnquiryType());
        assertThat(contactLog.getNotes())
            .as("The newly created contact log should have it's Notes value mapped from the request DTO")
            .isEqualTo(requestDto.getNotes());
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_bureauOwnedRecord.sql"})
    void createJurorContactLogBureauUserDuplicateStartCall() {
        String jurorNumber = "111111111";
        LocalDateTime startCall = LocalDateTime.now();
        ContactLogRequestDto requestDto1 = createContactLogRequestDto(jurorNumber, "LS",
            "Repeat Enquiry Notes", true);
        ContactLogRequestDto requestDto2 = createContactLogRequestDto(jurorNumber, "GE",
            "General Enquiry Notes", false);

        requestDto1.setStartCall(startCall);
        requestDto2.setStartCall(startCall);

        ResponseEntity<?> response1 =
            restTemplate.exchange(new RequestEntity<>(requestDto1, httpHeaders, POST,
                URI.create(CONTACT_LOG_URL)), String.class);

        assertThat(response1.getStatusCode())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(HttpStatus.CREATED);

        List<ContactLog> contactLogs =
            contactLogRepository.findByJurorNumber(jurorNumber);

        assertThat(contactLogs.size())
            .as("Expect a new contact log to be created")
            .isEqualTo(1);

        ResponseEntity<?> response2 =
            restTemplate.exchange(new RequestEntity<>(requestDto2, httpHeaders, POST,
                URI.create(CONTACT_LOG_URL)), String.class);

        assertThat(response2.getStatusCode())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(HttpStatus.CREATED);

        contactLogs = contactLogRepository.findByJurorNumber(jurorNumber);

        assertThat(contactLogs.size())
            .as("Expect another new contact log to be created for the same call start time")
            .isEqualTo(2);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_bureauOwnedRecord.sql"})
    void createJurorContactLogBureauUserUniqueStartCall() {
        String jurorNumber = "111111111";
        LocalDateTime startCall = LocalDateTime.now();
        ContactLogRequestDto requestDto1 = createContactLogRequestDto(jurorNumber, "LS",
            "Repeat Enquiry Notes", true);
        ContactLogRequestDto requestDto2 = createContactLogRequestDto(jurorNumber, "GE",
            "General Enquiry Notes", false);

        requestDto1.setStartCall(startCall);
        requestDto2.setStartCall(startCall.plusSeconds(1));

        ResponseEntity<?> response1 =
            restTemplate.exchange(new RequestEntity<>(requestDto1, httpHeaders, POST,
                URI.create(CONTACT_LOG_URL)), String.class);

        assertThat(response1.getStatusCode())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(HttpStatus.CREATED);

        List<ContactLog> contactLogs =
            contactLogRepository.findByJurorNumber(jurorNumber);

        assertThat(contactLogs.size())
            .as("Expect a new contact log to be created")
            .isEqualTo(1);

        ResponseEntity<?> response2 =
            restTemplate.exchange(new RequestEntity<>(requestDto2, httpHeaders, POST,
                URI.create(CONTACT_LOG_URL)), String.class);

        assertThat(response2.getStatusCode())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(HttpStatus.CREATED);

        contactLogs = contactLogRepository.findByJurorNumber(jurorNumber);

        assertThat(contactLogs.size())
            .as("Expect a new contact log to be created")
            .isEqualTo(2);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_bureauOwnedRecord.sql"})
    void createJurorContactLogBureauUserJurorNotFound() {
        ContactLogRequestDto requestDto = createContactLogRequestDto("123456780", "LS",
            "Enquiry Notes", false);
        ResponseEntity<?> response =
            restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, POST,
                URI.create(CONTACT_LOG_URL)), String.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP POST request to be unsuccessful")
            .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_transferredRecord.sql"})
    void createJurorContactLogBureauUserCourtOwnedJuror() {
        String jurorNumber = "123456789";
        ContactLogRequestDto requestDto = createContactLogRequestDto(jurorNumber, "LS",
            "Enquiry Notes", false);
        ResponseEntity<?> response =
            restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, POST,
                URI.create(CONTACT_LOG_URL)), String.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP POST request to be successful")
            .isEqualTo(HttpStatus.CREATED);

        List<ContactLog> contactLogs = contactLogRepository.findByJurorNumber(jurorNumber);

        assertThat(contactLogs.size())
            .as("Expect a new contact log to be created")
            .isEqualTo(3);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_bureauOwnedRecord.sql"})
    void createJurorContactLogBureauUserInvalidEnquiryType() {
        ContactLogRequestDto requestDto = createContactLogRequestDto("123456789", "ZZ",
            "Enquiry Notes", false);
        ResponseEntity<?> response =
            restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, POST,
                URI.create(CONTACT_LOG_URL)), String.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP POST request to be unsuccessful")
            .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_transferredRecord.sql"})
    void createJurorContactLogCourtUserHappyPath() throws Exception {
        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("415", Collections.singletonList("415"),
            UserType.COURT));
        String jurorNumber = "123456789";
        ContactLogRequestDto requestDto = createContactLogRequestDto(jurorNumber, "ER",
            "Repeat Enquiry Notes", true);
        ResponseEntity<?> response =
            restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, POST,
                URI.create(CONTACT_LOG_URL)), String.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(HttpStatus.CREATED);

        List<ContactLog> contactLogs =
            contactLogRepository.findByJurorNumber(jurorNumber);

        assertThat(contactLogs.size())
            .as("Initial test data contained two contact logs, expect a third, new contact log to be created")
            .isEqualTo(3);
        ContactLog newContactLog = contactLogs.stream().filter(cl ->
            cl.getEnquiryType().getCode().equals(IContactCode.EARLY_RELEASE.getCode())).findFirst().get();


        assertThat(newContactLog.getJurorNumber())
            .as("The newly created contact log should have it's Juror Number value mapped from the request DTO")
            .isEqualTo(requestDto.getJurorNumber());
        LocalDateTime startCall = requestDto.getStartCall();
        assertThat(newContactLog.getStartCall())
            .as("The newly created contact log should have it's Start Call value mapped from the request DTO")
            .hasDayOfMonth(startCall.getDayOfMonth()).hasMonth(startCall.getMonth()).hasYear(startCall.getYear());
        assertThat(newContactLog.getUsername())
            .as("The newly created contact log should have it's Username value mapped from the request DTO")
            .isEqualTo("COURT_USER");
        assertThat(newContactLog.getEnquiryType().getCode())
            .as("The newly created contact log should have it's Enquiry Type value mapped from the request DTO")
            .isEqualTo(requestDto.getEnquiryType());
        assertThat(newContactLog.getNotes())
            .as("The newly created contact log should have it's Notes value mapped from the request DTO")
            .isEqualTo(requestDto.getNotes());
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_bureauOwnedRecord.sql"})
    void createJurorContactLogCourtUserBureauOwnedRecord() throws Exception {
        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("415", Collections.singletonList("415"),
            UserType.COURT));
        ContactLogRequestDto requestDto = createContactLogRequestDto("123456789", "ER",
            "Enquiry Notes", false);
        ResponseEntity<?> response =
            restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, POST,
                URI.create(CONTACT_LOG_URL)), String.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP POST request to be unsuccessful")
            .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_transferredRecord.sql"})
    void createJurorContactLogCourtUserDifferentCourt() throws Exception {
        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("416", Collections.singletonList("416"),
            UserType.COURT));
        ContactLogRequestDto requestDto = createContactLogRequestDto("123456789", "ER",
            "Enquiry Notes", false);
        ResponseEntity<?> response =
            restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, POST,
                URI.create(CONTACT_LOG_URL)), String.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP POST request to be unsuccessful")
            .isEqualTo(HttpStatus.FORBIDDEN);
    }

    private String initCourtsJwt(String owner, List<String> courts, UserType userType, Role... roles) throws Exception {

        return mintBureauJwt(BureauJwtPayload.builder()
            .login("COURT_USER")
            .userType(userType)
            .roles(Arrays.asList(roles))
            .owner(owner)
            .staff(BureauJwtPayload.Staff.builder().courts(courts).build())
            .build());
    }

    @Test
    void testGetContactEnquiryTypes() {
        ResponseEntity<ContactEnquiryTypeListDto> response =
            restTemplate.exchange(new RequestEntity<>(httpHeaders, HttpMethod.GET,
                    URI.create("/api/v1/moj/juror-record/contact-log/enquiry-types")),
                ContactEnquiryTypeListDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(HttpStatus.OK);

        assertThat(response.getBody()).isNotNull();
        List<ContactEnquiryType> contactEnquiryTypes = response.getBody().getData();

        Iterable<ContactEnquiryType> dbData = contactEnquiryTypeRepository.findAll();

        for (ContactEnquiryType contactEnquiryType : dbData) {

            assertThat(contactEnquiryTypes.stream().anyMatch(enquiryType ->
                enquiryType.getEnquiryCode() == contactEnquiryType.getEnquiryCode()
                    && enquiryType.getDescription().equals(contactEnquiryType.getDescription())))
                .as("Expect each record from the database to be correctly mapped in to the returned DTO")
                .isTrue();
        }
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_bureauOwnedRecord.sql"})
    void testGetJurorNotesBureauUserBureauOwnedRecord() {
        ResponseEntity<JurorNotesDto> response =
            restTemplate.exchange(new RequestEntity<>(httpHeaders, HttpMethod.GET,
                URI.create(GET_JUROR_NOTES_URL)), JurorNotesDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(HttpStatus.OK);

        assertThat(response.getBody()).isNotNull();
        JurorNotesDto responseBody = response.getBody();

        assertThat(responseBody.getNotes())
            .as("The juror record should be present and available to the current user to view notes")
            .isEqualTo("SOME EXAMPLE NOTES");
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_transferredRecord.sql"})
    void testGetJurorNotesBureauUserCourtOwnedRecord() {
        ResponseEntity<JurorNotesDto> response =
            restTemplate.exchange(new RequestEntity<>(httpHeaders, HttpMethod.GET,
                URI.create(GET_JUROR_NOTES_URL)), JurorNotesDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(HttpStatus.OK);

        assertThat(response.getBody()).isNotNull();
        JurorNotesDto responseBody = response.getBody();

        assertThat(responseBody.getNotes())
            .as("The juror record should be present and available to the current user to view notes")
            .isEqualTo("SOME EXAMPLE NOTES");
    }

    @Test
    @Sql("/db/mod/truncate.sql")
    void testGetJurorNotesBureauUserPoolMemberNotFound() {
        ResponseEntity<JurorNotesDto> response =
            restTemplate.exchange(new RequestEntity<>(httpHeaders, HttpMethod.GET,
                URI.create(GET_JUROR_NOTES_URL)), JurorNotesDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET request to be unsuccessful")
            .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_transferredRecord.sql"})
    void testGetJurorNotesCourtUserCourtOwnedRecord() throws Exception {
        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("415", Collections.singletonList("415"),
            UserType.COURT));
        ResponseEntity<JurorNotesDto> response =
            restTemplate.exchange(new RequestEntity<>(httpHeaders, HttpMethod.GET,
                URI.create(GET_JUROR_NOTES_URL)), JurorNotesDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(HttpStatus.OK);

        assertThat(response.getBody()).isNotNull();
        JurorNotesDto responseBody = response.getBody();

        assertThat(responseBody.getNotes())
            .as("The juror record should be present and available to the current user to view notes")
            .isEqualTo("SOME EXAMPLE NOTES");
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_bureauOwnedRecord.sql"})
    void testGetJurorNotesCourtUserBureauOwnedRecord() throws Exception {
        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("415", Collections.singletonList("415"),
            UserType.COURT));
        ResponseEntity<JurorNotesDto> response =
            restTemplate.exchange(new RequestEntity<>(httpHeaders, HttpMethod.GET,
                URI.create(GET_JUROR_NOTES_URL)), JurorNotesDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET request to be unsuccessful")
            .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_transferredRecord.sql"})
    void testGetJurorNotesCourtUserDifferentCourt() throws Exception {
        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("416", Collections.singletonList("416"),
            UserType.COURT));
        ResponseEntity<JurorNotesDto> response =
            restTemplate.exchange(new RequestEntity<>(httpHeaders, HttpMethod.GET,
                URI.create(GET_JUROR_NOTES_URL)), JurorNotesDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET request to be unsuccessful")
            .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_courtOwned_NoNotes.sql"})
    void testGetJurorNotesCourtUserNoNotes() throws Exception {
        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("415", Collections.singletonList("415"),
            UserType.COURT));
        ResponseEntity<JurorNotesDto> response =
            restTemplate.exchange(new RequestEntity<>(httpHeaders, HttpMethod.GET,
                URI.create(GET_JUROR_NOTES_URL)), JurorNotesDto.class);

        assertThat(response.getBody()).isNotNull();
        JurorNotesDto responseBody = response.getBody();

        assertThat(responseBody.getNotes())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(null);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_bureauOwnedRecord.sql"})
    void testSetJurorNotesBureauUserBureauOwnedRecord() {
        JurorNotesRequestDto updateNotes = new JurorNotesRequestDto("Some updated notes");
        ResponseEntity<?> patchResponse =
            restTemplate.exchange(new RequestEntity<>(updateNotes, httpHeaders, HttpMethod.PATCH,
                URI.create(GET_JUROR_NOTES_URL)), Void.class);

        assertThat(patchResponse.getStatusCode())
            .as("Expect the HTTP PATCH request to be successful")
            .isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<JurorNotesDto> getResponse =
            restTemplate.exchange(new RequestEntity<>(httpHeaders, HttpMethod.GET,
                URI.create(GET_JUROR_NOTES_URL)), JurorNotesDto.class);

        assertThat(getResponse.getStatusCode())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(HttpStatus.OK);

        assertThat(getResponse.getBody()).isNotNull();
        JurorNotesDto responseBody = getResponse.getBody();

        assertThat(responseBody.getNotes())
            .as("The juror record should be present and available to the current user to view notes")
            .isEqualTo(updateNotes.getNotes());
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_transferredRecord.sql"})
    void testSetJurorNotesBureauUserCourtOwnedRecord() {
        JurorNotesRequestDto updateNotes = new JurorNotesRequestDto("Some updated notes");
        ResponseEntity<?> patchResponse =
            restTemplate.exchange(new RequestEntity<>(updateNotes, httpHeaders, HttpMethod.PATCH,
                URI.create(GET_JUROR_NOTES_URL)), Void.class);

        assertThat(patchResponse.getStatusCode())
            .as("Expect the HTTP PATCH request to be successful")
            .isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<JurorNotesDto> getResponse =
            restTemplate.exchange(new RequestEntity<>(httpHeaders, HttpMethod.GET,
                URI.create(GET_JUROR_NOTES_URL)), JurorNotesDto.class);

        assertThat(getResponse.getStatusCode())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(HttpStatus.OK);

        assertThat(getResponse.getBody()).isNotNull();
        JurorNotesDto responseBody = getResponse.getBody();

        assertThat(responseBody.getNotes())
            .as("The juror record should be present and available to the current user to view notes")
            .isEqualTo(updateNotes.getNotes());
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_bureauOwnedRecord.sql"})
    void testSetJurorNotesBureauUserNotesTooLong() {
        JurorNotesRequestDto updateNotes = new JurorNotesRequestDto(generateString(2001));
        ResponseEntity<?> patchResponse =
            restTemplate.exchange(new RequestEntity<>(updateNotes, httpHeaders, HttpMethod.PATCH,
                URI.create(GET_JUROR_NOTES_URL)), Void.class);

        assertThat(patchResponse.getStatusCode())
            .as("Expect the HTTP PATCH request to be unsuccessful")
            .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_transferredRecord.sql"})
    void testSetJurorNotesCourtUserCourtOwnedRecord() throws Exception {
        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("415", Collections.singletonList("415"),
            UserType.COURT));
        JurorNotesRequestDto updateNotes = new JurorNotesRequestDto("Some updated notes");
        ResponseEntity<?> patchResponse =
            restTemplate.exchange(new RequestEntity<>(updateNotes, httpHeaders, HttpMethod.PATCH,
                URI.create(GET_JUROR_NOTES_URL)), Void.class);

        assertThat(patchResponse.getStatusCode())
            .as("Expect the HTTP PATCH request to be successful")
            .isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<JurorNotesDto> getResponse =
            restTemplate.exchange(new RequestEntity<>(httpHeaders, HttpMethod.GET,
                URI.create(GET_JUROR_NOTES_URL)), JurorNotesDto.class);

        assertThat(getResponse.getStatusCode())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(HttpStatus.OK);

        assertThat(getResponse.getBody()).isNotNull();
        JurorNotesDto responseBody = getResponse.getBody();

        assertThat(responseBody.getNotes())
            .as("The juror record should be present and available to the current user to view notes")
            .isEqualTo(updateNotes.getNotes());
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_bureauOwnedRecord.sql"})
    void testSetJurorNotesCourtUserBureauOwnedRecord() throws Exception {
        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("415", Collections.singletonList("415"),
            UserType.COURT));
        JurorNotesRequestDto updateNotes = new JurorNotesRequestDto("Some updated notes");
        ResponseEntity<?> patchResponse =
            restTemplate.exchange(new RequestEntity<>(updateNotes, httpHeaders, HttpMethod.PATCH,
                URI.create(GET_JUROR_NOTES_URL)), Void.class);

        assertThat(patchResponse.getStatusCode())
            .as("Expect the HTTP PATCH request to be unsuccessful")
            .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_transferredRecord.sql"})
    void testSetJurorNotesCourtsUserMaxNotesLength() throws Exception {
        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("415", Collections.singletonList("415"),
            UserType.COURT));
        JurorNotesRequestDto updateNotes = new JurorNotesRequestDto(generateString(2000));
        ResponseEntity<?> patchResponse =
            restTemplate.exchange(new RequestEntity<>(updateNotes, httpHeaders, HttpMethod.PATCH,
                URI.create(GET_JUROR_NOTES_URL)), Void.class);

        assertThat(patchResponse.getStatusCode())
            .as("Expect the HTTP PATCH request to be successful")
            .isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<JurorNotesDto> getResponse =
            restTemplate.exchange(new RequestEntity<>(httpHeaders, HttpMethod.GET,
                URI.create(GET_JUROR_NOTES_URL)), JurorNotesDto.class);

        assertThat(getResponse.getStatusCode())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(HttpStatus.OK);

        assertThat(getResponse.getBody()).isNotNull();
        JurorNotesDto responseBody = getResponse.getBody();

        assertThat(responseBody.getNotes())
            .as("The juror record should be present and available to the current user to view notes")
            .isEqualTo(updateNotes.getNotes());
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_transferredRecord.sql"})
    void testSetJurorNotesCourtUserDifferentCourt() throws Exception {
        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("416", Collections.singletonList("416"),
            UserType.COURT));
        JurorNotesRequestDto updateNotes = new JurorNotesRequestDto("Some updated notes");
        ResponseEntity<?> patchResponse =
            restTemplate.exchange(new RequestEntity<>(updateNotes, httpHeaders, HttpMethod.PATCH,
                URI.create(GET_JUROR_NOTES_URL)), Void.class);

        assertThat(patchResponse.getStatusCode())
            .as("Expect the HTTP PATCH request to be unsuccessful")
            .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Sql("/db/mod/truncate.sql")
    void testSetJurorNotesCourtUserPoolMemberNotFound() throws Exception {
        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("415", Collections.singletonList("415"),
            UserType.COURT));
        JurorNotesRequestDto updateNotes = new JurorNotesRequestDto("Some updated notes");
        ResponseEntity<?> patchResponse =
            restTemplate.exchange(new RequestEntity<>(updateNotes, httpHeaders, HttpMethod.PATCH,
                URI.create(GET_JUROR_NOTES_URL)), Void.class);

        assertThat(patchResponse.getStatusCode())
            .as("Expect the HTTP PATCH request to be unsuccessful")
            .isEqualTo(HttpStatus.NOT_FOUND);
    }


    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_bureauOwnedRecord.sql"})
    void testGetJurorNotesBureauUserEtagIsSet() {
        ResponseEntity<JurorNotesDto> response =
            restTemplate.exchange(new RequestEntity<>(httpHeaders, HttpMethod.GET,
                URI.create(GET_JUROR_NOTES_URL)), JurorNotesDto.class);

        assertThat(response.getHeaders().size()).isGreaterThan(0);

        assertThat(response.getHeaders().get("ETag"))
            .as("Expect the eTag header to be set")
            .isNotNull();
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_bureauOwnedRecord.sql"})
    void testGetJurorNotesBureauUserEtagIsSetThenNotModifiedReturned() {
        ResponseEntity<JurorNotesDto> response =
            restTemplate.exchange(new RequestEntity<>(httpHeaders, HttpMethod.GET,
                URI.create(GET_JUROR_NOTES_URL)), JurorNotesDto.class);

        assertThat(response.getHeaders().getETag()).isNotNull();

        String etagValue = response.getHeaders().getETag();

        httpHeaders.set("If-none-match", etagValue);
        ResponseEntity<JurorNotesDto> response2 =
            restTemplate.exchange(new RequestEntity<>(httpHeaders, HttpMethod.GET,
                URI.create(GET_JUROR_NOTES_URL)), JurorNotesDto.class);

        assertThat(response2.getStatusCode())
            .as("Expect the HTTP GET request to return not-modified 304")
            .isEqualTo(HttpStatus.NOT_MODIFIED);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_bureauOwnedRecord.sql"})
    void testGetJurorNotesBureauUserEtagIsSetThenUpdateNotesThenModifiedReturned() throws Exception {
        ResponseEntity<JurorNotesDto> response =
            restTemplate.exchange(new RequestEntity<>(httpHeaders, HttpMethod.GET,
                URI.create(GET_JUROR_NOTES_URL)), JurorNotesDto.class);

        assertThat(response.getHeaders().getETag()).isNotNull();

        String etagValue = response.getHeaders().getETag();

        // update the notes
        updateNotesForEtag();

        httpHeaders.set("If-none-match", etagValue);
        ResponseEntity<JurorNotesDto> response2 =
            restTemplate.exchange(new RequestEntity<>(httpHeaders, HttpMethod.GET,
                URI.create(GET_JUROR_NOTES_URL)), JurorNotesDto.class);

        // this is 200 but is seen as unsuccessful or modified by the frontend :D
        assertThat(response2.getStatusCode())
            .as("Expect the HTTP GET to return OK 200")
            .isEqualTo(HttpStatus.OK);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_bureauDigitalDetail.sql"})
    void testRetrieveJurorDetailsByIdBureauUserHappyPath() {
        final String jurorNumber = "222222222";
        ResponseEntity<BureauJurorDetailDto> response =
            restTemplate.exchange(new RequestEntity<>(httpHeaders, HttpMethod.GET,
                URI.create("/api/v1/moj/juror-record/digital-detail/" + jurorNumber)), BureauJurorDetailDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET to return OK 200")
            .isEqualTo(HttpStatus.OK);

        BureauJurorDetailDto dto = response.getBody();
        assertThat(dto).isNotNull();

        JurorPool jurorPool =
            jurorPoolRepository.findByJurorJurorNumberAndIsActive(jurorNumber, true).stream().findFirst().get();

        validateJurorDetailsMapping(dto, jurorPool, "415220502");
        // use snapshot loc_code 415 instead of latest, reassigned loc code 435
        validateCourtDetails(dto, "415");
        validateResponseDetails(dto);
        assertThat(dto.isWelshCourt()).isFalse();
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_bureauDigitalDetail.sql"})
    void testRetrieveJurorDetailsLatestTransferred() {
        final String jurorNumber = "641500001";
        ResponseEntity<BureauJurorDetailDto> response =
            restTemplate.exchange(new RequestEntity<>(httpHeaders, HttpMethod.GET,
                URI.create("/api/v1/moj/juror-record/digital-detail/" + jurorNumber)), BureauJurorDetailDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET to return OK 200")
            .isEqualTo(HttpStatus.OK);

        BureauJurorDetailDto dto = response.getBody();
        assertThat(dto).isNotNull();

        JurorPool jurorPool =
            jurorPoolRepository.findByJurorJurorNumberAndIsActive(jurorNumber, true).stream().findFirst().get();

        assertThat(dto.getCurrentOwner())
            .as("Expect current owner to be the owner of the transferred to pool")
            .isEqualToIgnoringCase("471");

        validateJurorDetailsMapping(dto, jurorPool, "415240601");
        validateCourtDetails(dto, "415");
        validateResponseDetails(dto);
        assertThat(dto.isWelshCourt()).isFalse();
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_bureauDigitalDetail.sql"})
    void testRetrieveJurorDetailsByIdBureauUserHappyPathWelshCourt() {
        final String jurorNumber = "555555555";
        final String poolNumber = "457230801";
        final String bureauOwner = "400";
        ResponseEntity<BureauJurorDetailDto> response =
            restTemplate.exchange(new RequestEntity<>(httpHeaders, HttpMethod.GET,
                URI.create("/api/v1/moj/juror-record/digital-detail/" + jurorNumber)), BureauJurorDetailDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET to return OK 200")
            .isEqualTo(HttpStatus.OK);

        BureauJurorDetailDto dto = response.getBody();
        assertThat(dto).isNotNull();

        JurorPool jurorPool =
            jurorPoolRepository.findByOwnerAndJurorJurorNumberAndPoolPoolNumber(bureauOwner, jurorNumber, poolNumber)
                .stream().findFirst().get();

        validateJurorDetailsMapping(dto, jurorPool, poolNumber);
        // use snapshot loc_code 415 instead of latest, reassigned loc code 435
        validateCourtDetails(dto, "457");
        validateResponseDetails(dto);
        assertThat(dto.isWelshCourt()).isTrue();
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_bureauDigitalDetail.sql"})
    void testRetrieveJurorDetailsByIdBureauUserNoJurorRecord() {
        String jurorNumber = "333333333";
        ResponseEntity<BureauJurorDetailDto> response =
            restTemplate.exchange(new RequestEntity<>(httpHeaders, HttpMethod.GET,
                URI.create("/api/v1/moj/juror-record/digital-detail/" + jurorNumber)), BureauJurorDetailDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET to return 404 NOT FOUND")
            .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_bureauDigitalDetail.sql"})
    void testRetrieveJurorDetailsByIdCourtUserHappyPathMultipleSummonsHistoryRecords() throws Exception {
        String jurorNumber = "444444444";
        String poolNumber = "435220502";
        String courtOwner = "435";
        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt(courtOwner, Collections.singletonList(courtOwner),
            UserType.COURT));
        ResponseEntity<BureauJurorDetailDto> response =
            restTemplate.exchange(new RequestEntity<>(httpHeaders, HttpMethod.GET,
                URI.create("/api/v1/moj/juror-record/digital-detail/" + jurorNumber)), BureauJurorDetailDto.class);

        BureauJurorDetailDto dto = response.getBody();
        assertThat(dto).isNotNull();

        JurorPool jurorPool = jurorPoolRepository.findByOwnerAndJurorJurorNumberAndPoolPoolNumber(courtOwner,
            jurorNumber, poolNumber).get();

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET to return OK 200")
            .isEqualTo(HttpStatus.OK);

        validateJurorDetailsMapping(dto, jurorPool, "415220502");
        // Earliest summons history event should be used (location code 415)
        validateCourtDetails(dto, "415");
        validateResponseDetails(dto);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_bureauDigitalDetail.sql"})
    void testRetrieveJurorDetailsByIdCourtUserInvalidPermissions() throws Exception {
        httpHeaders.set(HttpHeaders.AUTHORIZATION,
            initCourtsJwt("411", Collections.singletonList("411"), UserType.COURT));
        ResponseEntity<BureauJurorDetailDto> response =
            restTemplate.exchange(new RequestEntity<>(httpHeaders, HttpMethod.GET,
                URI.create("/api/v1/moj/juror-record/digital-detail/111111111")), BureauJurorDetailDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET to return 403 FORBIDDEN")
            .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @ParameterizedTest
    @ValueSource(strings = {"111111116"})
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_policeCheck.sql"})
    void testGetJurorOverviewBureauUserHappyPathPoliceCheckStatusNotChecked(String jurorNumber) {
        ResponseEntity<JurorOverviewResponseDto> response =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                    URI.create("/api/v1/moj/juror-record/overview/" + jurorNumber + "/415")),
                JurorOverviewResponseDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCommonDetails().getPoliceCheck()).as("Police check status")
            .isEqualTo(PoliceCheck.NOT_CHECKED);
    }

    @ParameterizedTest
    @ValueSource(strings = {"111111122"})
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_policeCheck.sql"})
    void testGetJurorOverviewBureauUserHappyPathPoliceCheckStatusInsufficientInformation(String jurorNumber) {
        ResponseEntity<JurorOverviewResponseDto> response =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                    URI.create("/api/v1/moj/juror-record/overview/" + jurorNumber + "/415")),
                JurorOverviewResponseDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCommonDetails().getPoliceCheck()).as("Police check status")
            .isEqualTo(PoliceCheck.INSUFFICIENT_INFORMATION);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_policeCheck.sql"})
    void testGetJurorOverviewBureauUserHappyPathPoliceCheckStatusNotCheckedThereWasAproblem() {
        ResponseEntity<JurorOverviewResponseDto> response =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                URI.create("/api/v1/moj/juror-record/overview/111111112/415")), JurorOverviewResponseDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCommonDetails().getPoliceCheck()).as("Police check status")
            .isEqualTo(PoliceCheck.UNCHECKED_MAX_RETRIES_EXCEEDED);
    }

    @ParameterizedTest
    @ValueSource(strings = {"111111113"})
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_policeCheck.sql"})
    void testGetJurorOverviewBureauUserHappyPathPoliceCheckStatusInProgress(String jurorNumber) {
        ResponseEntity<JurorOverviewResponseDto> response =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                    URI.create("/api/v1/moj/juror-record/overview/" + jurorNumber + "/415")),
                JurorOverviewResponseDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(HttpStatus.OK);

        JurorOverviewResponseDto jurorOverviewResponseDto = response.getBody();
        assertThat(jurorOverviewResponseDto).isNotNull();

        assertThat(jurorOverviewResponseDto.getCommonDetails().getPoliceCheck())
            .as("Expected police check: 'In Progress'")
            .isEqualTo(PoliceCheck.IN_PROGRESS);
    }


    @Nested
    class PoliceCheckErrors {
        @DisplayName("Error Retry - Connection Error")
        @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_policeCheck.sql"})
        void connectionError() {
            final String jurorNumber = "111111117";
            ResponseEntity<JurorOverviewResponseDto> response =
                restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                        URI.create("/api/v1/moj/juror-record/overview/" + jurorNumber + "/415")),
                    JurorOverviewResponseDto.class);

            assertThat(response.getStatusCode())
                .as("Expect the HTTP GET request to be successful")
                .isEqualTo(HttpStatus.OK);

            JurorOverviewResponseDto jurorOverviewResponseDto = response.getBody();
            assertThat(jurorOverviewResponseDto).isNotNull();

            assertThat(jurorOverviewResponseDto.getCommonDetails().getPoliceCheck())
                .as("Police check status")
                .isEqualTo(PoliceCheck.ERROR_RETRY_CONNECTION_ERROR);
        }

        @DisplayName("Error Retry - Name has numerics")
        @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_policeCheck.sql"})
        @Test
        void nameHasNumerics() {
            final String jurorNumber = "111111118";
            ResponseEntity<JurorOverviewResponseDto> response =
                restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                        URI.create("/api/v1/moj/juror-record/overview/" + jurorNumber + "/415")),
                    JurorOverviewResponseDto.class);

            assertThat(response.getStatusCode())
                .as("Expect the HTTP GET request to be successful")
                .isEqualTo(HttpStatus.OK);

            JurorOverviewResponseDto jurorOverviewResponseDto = response.getBody();
            assertThat(jurorOverviewResponseDto).isNotNull();

            assertThat(jurorOverviewResponseDto.getCommonDetails().getPoliceCheck())
                .as("Police check status")
                .isEqualTo(PoliceCheck.ERROR_RETRY_NAME_HAS_NUMERICS);
        }

        @DisplayName("Error Retry - Other Error Code")
        @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_policeCheck.sql"})
        @Test
        void otherErrorCode() {
            final String jurorNumber = "111111119";
            ResponseEntity<JurorOverviewResponseDto> response =
                restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                        URI.create("/api/v1/moj/juror-record/overview/" + jurorNumber + "/415")),
                    JurorOverviewResponseDto.class);

            assertThat(response.getStatusCode())
                .as("Expect the HTTP GET request to be successful")
                .isEqualTo(HttpStatus.OK);

            JurorOverviewResponseDto jurorOverviewResponseDto = response.getBody();
            assertThat(jurorOverviewResponseDto).isNotNull();

            assertThat(jurorOverviewResponseDto.getCommonDetails().getPoliceCheck())
                .as("Police check status")
                .isEqualTo(PoliceCheck.ERROR_RETRY_OTHER_ERROR_CODE);
        }

        @DisplayName("Error Retry -  No Error Reason")
        @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_policeCheck.sql"})
        @Test
        void noErrorReason() {
            final String jurorNumber = "111111120";
            ResponseEntity<JurorOverviewResponseDto> response =
                restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                        URI.create("/api/v1/moj/juror-record/overview/" + jurorNumber + "/415")),
                    JurorOverviewResponseDto.class);

            assertThat(response.getStatusCode())
                .as("Expect the HTTP GET request to be successful")
                .isEqualTo(HttpStatus.OK);

            JurorOverviewResponseDto jurorOverviewResponseDto = response.getBody();
            assertThat(jurorOverviewResponseDto).isNotNull();

            assertThat(jurorOverviewResponseDto.getCommonDetails().getPoliceCheck())
                .as("Police check status")
                .isEqualTo(PoliceCheck.ERROR_RETRY_NO_ERROR_REASON);
        }

        @DisplayName("Error Retry - Unexpected Exception")
        @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_policeCheck.sql"})
        @Test
        void errorRetryUnexpectedException() {
            final String jurorNumber = "111111121";
            ResponseEntity<JurorOverviewResponseDto> response =
                restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                        URI.create("/api/v1/moj/juror-record/overview/" + jurorNumber + "/415")),
                    JurorOverviewResponseDto.class);

            assertThat(response.getStatusCode())
                .as("Expect the HTTP GET request to be successful")
                .isEqualTo(HttpStatus.OK);

            JurorOverviewResponseDto jurorOverviewResponseDto = response.getBody();
            assertThat(jurorOverviewResponseDto).isNotNull();

            assertThat(jurorOverviewResponseDto.getCommonDetails().getPoliceCheck())
                .as("Police check status")
                .isEqualTo(PoliceCheck.ERROR_RETRY_UNEXPECTED_EXCEPTION);
        }
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_policeCheck.sql"})
    void testGetJurorOverviewBureauUserHappyPathPoliceCheckStatusPassed() {
        ResponseEntity<JurorOverviewResponseDto> response =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                URI.create("/api/v1/moj/juror-record/overview/111111114/415")), JurorOverviewResponseDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCommonDetails().getPoliceCheck()).as("Expected police check: 'Passed'")
            .isEqualTo(PoliceCheck.ELIGIBLE);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_policeCheck.sql"})
    void testGetJurorOverviewBureauUserHappyPathPoliceCheckStatusFailed() {
        ResponseEntity<JurorOverviewResponseDto> response =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                URI.create("/api/v1/moj/juror-record/overview/111111115/415")), JurorOverviewResponseDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(HttpStatus.OK);

        JurorOverviewResponseDto jurorOverviewResponseDto = response.getBody();
        assertThat(jurorOverviewResponseDto).isNotNull();

        assertThat(jurorOverviewResponseDto.getCommonDetails().getPoliceCheck()).as("Expected police check: 'Failed'")
            .isEqualTo(PoliceCheck.INELIGIBLE);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_fixJurorName.sql"})
    void testFixJurorNameCourtUserHappyPath() throws Exception {
        String username = "COURT_AGENT";
        String owner = "415";
        httpHeaders.set(HttpHeaders.AUTHORIZATION, initPayloadWithStaffRank(owner, username, UserType.COURT));

        JurorNameDetailsDto dto = new JurorNameDetailsDto("Mr", "First", "Last");
        String jurorNumber = "111111111";
        String poolNumber = "415230701";

        ResponseEntity<Void> response =
            restTemplate.exchange(new RequestEntity<>(dto, httpHeaders, HttpMethod.PATCH,
                URI.create("/api/v1/moj/juror-record/fix-name/" + jurorNumber)), Void.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP PATCH request to be successful")
            .isEqualTo(HttpStatus.NO_CONTENT);

        JurorPool jurorPool = jurorPoolRepository.findByOwnerAndJurorJurorNumberAndPoolPoolNumber(owner, jurorNumber,
            poolNumber).get();
        Juror juror = jurorPool.getJuror();

        assertThat(juror.getTitle()).isEqualTo(dto.getTitle());
        assertThat(juror.getFirstName()).isEqualTo(dto.getFirstName());
        assertThat(juror.getLastName()).isEqualTo(dto.getLastName());
        assertThat(juror.getUserEdtq()).isEqualTo(username);
        List<JurorHistory> jurorHistoryList = jurorHistoryRepository.findByJurorNumber(jurorNumber);
        assertThat(jurorHistoryList.size()).isEqualTo(3);
        for (JurorHistory jurorHistory : jurorHistoryList) {
            assertThat(jurorHistory.getPoolNumber()).isEqualTo(jurorPool.getPoolNumber());
            assertThat(jurorHistory.getHistoryCode()).isEqualTo(HistoryCodeMod.CHANGE_PERSONAL_DETAILS);
        }

        List<String> historyInfoList = initChangedHistoryProperties();

        for (String historyInfo : historyInfoList) {
            assertThat(jurorHistoryList.stream().filter(hist ->
                    hist.getOtherInformation().equalsIgnoreCase(historyInfo))
                .findFirst()
                .orElse(null))
                .isNotNull();
        }

        List<Juror> jurorAuditHistory =
            jurorRepository.findRevisions(jurorNumber).stream()
                .sorted(Comparator.comparingLong(rev -> rev.getRevisionNumber().orElse(0L)))
                .map(Revision::getEntity).toList();
        assertThat(jurorAuditHistory.size()).isEqualTo(2);

        Juror originalJurorVersion = jurorAuditHistory.get(0);
        assertThat(originalJurorVersion.getTitle()).isNull();
        assertThat(originalJurorVersion.getFirstName()).isEqualToIgnoringCase("FNAMEONE");
        assertThat(originalJurorVersion.getLastName()).isEqualToIgnoringCase("LNAMEONE");

        Juror updatedJurorVersion = jurorAuditHistory.get(1);
        assertThat(updatedJurorVersion.getTitle()).isEqualToIgnoringCase("Mr");
        assertThat(updatedJurorVersion.getFirstName()).isEqualToIgnoringCase("First");
        assertThat(updatedJurorVersion.getLastName()).isEqualToIgnoringCase("Last");
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_fixJurorName.sql"})
    void testFixJurorNameBureauUserHappyPath() throws Exception {
        final String username = "TEAM_LEADER";
        final String owner = "400";
        httpHeaders.set(HttpHeaders.AUTHORIZATION, initPayloadWithStaffRank(owner, username, UserType.BUREAU,
            Role.MANAGER));

        JurorNameDetailsDto dto = new JurorNameDetailsDto("Mr", "First", "Last");
        final String jurorNumber = "222222222";
        final String poolNumber = "415230701";

        ResponseEntity<Void> response =
            restTemplate.exchange(new RequestEntity<>(dto, httpHeaders, HttpMethod.PATCH,
                URI.create("/api/v1/moj/juror-record/fix-name/" + jurorNumber)), Void.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP PATCH request to be successful")
            .isEqualTo(HttpStatus.NO_CONTENT);

        JurorPool jurorPool =
            jurorPoolRepository.findByOwnerAndJurorJurorNumberAndPoolPoolNumber(owner, jurorNumber, poolNumber).get();
        Juror juror = jurorPool.getJuror();

        assertThat(juror.getTitle()).isEqualTo(dto.getTitle());
        assertThat(juror.getFirstName()).isEqualTo(dto.getFirstName());
        assertThat(juror.getLastName()).isEqualTo(dto.getLastName());
        assertThat(juror.getUserEdtq()).isEqualTo(username);

        List<JurorHistory> jurorHistoryList = jurorHistoryRepository.findByJurorNumber(jurorNumber);
        assertThat(jurorHistoryList.size()).isEqualTo(3);
        for (JurorHistory jurorHistory : jurorHistoryList) {
            assertThat(jurorHistory.getPoolNumber()).isEqualTo(jurorPool.getPoolNumber());
            assertThat(jurorHistory.getHistoryCode()).isEqualTo(HistoryCodeMod.CHANGE_PERSONAL_DETAILS);
        }

        List<String> historyInfoList = initChangedHistoryProperties();

        for (String historyInfo : historyInfoList) {
            assertThat(jurorHistoryList.stream().filter(hist ->
                    hist.getOtherInformation().equalsIgnoreCase(historyInfo))
                .findFirst()
                .orElse(null))
                .isNotNull();
        }

        List<Juror> jurorAuditHistory =
            jurorRepository.findRevisions(jurorNumber).stream()
                .sorted(Comparator.comparingLong(rev -> rev.getRevisionNumber().orElse(0L)))
                .map(Revision::getEntity).toList();
        assertThat(jurorAuditHistory.size()).isEqualTo(2);

        Juror originalJurorVersion = jurorAuditHistory.get(0);
        assertThat(originalJurorVersion.getTitle()).isNull();
        assertThat(originalJurorVersion.getFirstName()).isEqualToIgnoringCase("FNAMEONE");
        assertThat(originalJurorVersion.getLastName()).isEqualToIgnoringCase("LNAMEONE");

        Juror updatedJurorVersion = jurorAuditHistory.get(1);
        assertThat(updatedJurorVersion.getTitle()).isEqualToIgnoringCase("Mr");
        assertThat(updatedJurorVersion.getFirstName()).isEqualToIgnoringCase("First");
        assertThat(updatedJurorVersion.getLastName()).isEqualToIgnoringCase("Last");
    }


    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_fixJurorName.sql"})
    void testFixJurorNameBureauUserInsufficientRank() throws Exception {
        String username = "BUREAU_USER";
        String owner = "400";
        httpHeaders.set(HttpHeaders.AUTHORIZATION, initPayloadWithStaffRank(owner, username, UserType.BUREAU));

        JurorNameDetailsDto dto = new JurorNameDetailsDto("Mr", "First", "Last");
        String jurorNumber = "222222222";

        ResponseEntity<Void> response =
            restTemplate.exchange(new RequestEntity<>(dto, httpHeaders, HttpMethod.PATCH,
                URI.create("/api/v1/moj/juror-record/fix-name/" + jurorNumber)), Void.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP PATCH request to be unsuccessful")
            .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_processNameChangeApproval.sql"})
    void testProcessNameChangeApprovalCourtUserApprovedHappyPath() throws Exception {
        String username = "COURT_AGENT";
        String owner = "415";
        httpHeaders.set(HttpHeaders.AUTHORIZATION, initPayloadWithStaffRank(owner, username, UserType.COURT));

        ProcessNameChangeRequestDto dto = new ProcessNameChangeRequestDto(ApprovalDecision.APPROVE, "Some notes");
        String jurorNumber = "111111111";
        String poolNumber = "415230701";

        ResponseEntity<Void> response =
            restTemplate.exchange(new RequestEntity<>(dto, httpHeaders, HttpMethod.PATCH,
                URI.create("/api/v1/moj/juror-record/change-name/" + jurorNumber)), Void.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP PATCH request to be successful")
            .isEqualTo(HttpStatus.NO_CONTENT);

        JurorPool jurorPool =
            jurorPoolRepository.findByOwnerAndJurorJurorNumberAndPoolPoolNumber(owner, jurorNumber, poolNumber).get();
        Juror juror = jurorPool.getJuror();

        assertThat(juror.getTitle()).isEqualTo("Mr");
        assertThat(juror.getFirstName()).isEqualTo("Test");
        assertThat(juror.getLastName()).isEqualTo("Person");
        assertThat(juror.getUserEdtq()).isEqualTo(username);

        assertThat(juror.getPendingTitle()).isNull();
        assertThat(juror.getPendingFirstName()).isNull();
        assertThat(juror.getPendingLastName()).isNull();

        List<JurorHistory> jurorHistoryList = jurorHistoryRepository.findByJurorNumber(jurorNumber);
        assertThat(jurorHistoryList.size()).isEqualTo(4);
        for (JurorHistory jurorHistory : jurorHistoryList) {
            assertThat(jurorHistory.getPoolNumber()).isEqualTo(jurorPool.getPoolNumber());
            assertThat(jurorHistory.getHistoryCode()).isEqualTo(HistoryCodeMod.CHANGE_PERSONAL_DETAILS);
        }

        List<String> historyInfoList = initChangedHistoryProperties();
        historyInfoList.add("Name change approved");

        for (String historyInfo : historyInfoList) {
            assertThat(jurorHistoryList.stream().filter(hist ->
                    hist.getOtherInformation().equalsIgnoreCase(historyInfo))
                .findFirst()
                .orElse(null))
                .isNotNull();
        }

        List<Juror> jurorAuditHistory =
            jurorRepository.findRevisions(jurorNumber).stream()
                .sorted(Comparator.comparingLong(rev -> rev.getRevisionNumber().orElse(0L)))
                .map(Revision::getEntity).toList();
        assertThat(jurorAuditHistory.size()).isEqualTo(2);

        Juror originalJurorVersion = jurorAuditHistory.get(0);
        assertThat(originalJurorVersion.getTitle()).isNull();
        assertThat(originalJurorVersion.getFirstName()).isEqualToIgnoringCase("FNAMEONE");
        assertThat(originalJurorVersion.getLastName()).isEqualToIgnoringCase("LNAMEONE");
        assertThat(originalJurorVersion.getPendingTitle()).isEqualToIgnoringCase("Mr");
        assertThat(originalJurorVersion.getPendingFirstName()).isEqualToIgnoringCase("Test");
        assertThat(originalJurorVersion.getPendingLastName()).isEqualToIgnoringCase("Person");

        Juror updatedJurorVersion = jurorAuditHistory.get(1);
        assertThat(updatedJurorVersion.getTitle()).isEqualToIgnoringCase("Mr");
        assertThat(updatedJurorVersion.getFirstName()).isEqualToIgnoringCase("Test");
        assertThat(updatedJurorVersion.getLastName()).isEqualToIgnoringCase("Person");
        assertThat(updatedJurorVersion.getPendingTitle()).isNull();
        assertThat(updatedJurorVersion.getPendingFirstName()).isNull();
        assertThat(updatedJurorVersion.getPendingLastName()).isNull();
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_processNameChangeApproval.sql"})
    void testProcessNameChangeApprovalCourtUserRejectedHappyPath() throws Exception {
        String username = "COURT_AGENT";
        String owner = "415";
        httpHeaders.set(HttpHeaders.AUTHORIZATION, initPayloadWithStaffRank(owner, username, UserType.COURT));

        ProcessNameChangeRequestDto dto = new ProcessNameChangeRequestDto(ApprovalDecision.REJECT, "Some notes");
        String jurorNumber = "111111111";
        String poolNumber = "415230701";

        ResponseEntity<Void> response =
            restTemplate.exchange(new RequestEntity<>(dto, httpHeaders, HttpMethod.PATCH,
                URI.create("/api/v1/moj/juror-record/change-name/" + jurorNumber)), Void.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP PATCH request to be successful")
            .isEqualTo(HttpStatus.NO_CONTENT);

        JurorPool jurorPool =
            jurorPoolRepository.findByOwnerAndJurorJurorNumberAndPoolPoolNumber(owner, jurorNumber, poolNumber).get();
        Juror juror = jurorPool.getJuror();
        assertThat(juror.getTitle()).isNull();
        assertThat(juror.getFirstName()).isEqualTo("FNAMEONE");
        assertThat(juror.getLastName()).isEqualTo("LNAMEONE");
        assertThat(juror.getUserEdtq()).isEqualTo(username);

        assertThat(juror.getPendingTitle()).isNull();
        assertThat(juror.getPendingFirstName()).isNull();
        assertThat(juror.getPendingLastName()).isNull();

        List<JurorHistory> jurorHistoryList = jurorHistoryRepository.findByJurorNumber(jurorNumber);
        assertThat(jurorHistoryList.size()).isEqualTo(1);
        JurorHistory jurorHistory = jurorHistoryList.get(0);
        assertThat(jurorHistory.getHistoryCode()).isEqualTo(HistoryCodeMod.CHANGE_PERSONAL_DETAILS);
        assertThat(jurorHistory.getOtherInformation()).isEqualTo("Name change rejected");

        List<Juror> jurorAuditHistory =
            jurorRepository.findRevisions(jurorNumber).stream()
                .sorted(Comparator.comparingLong(rev -> rev.getRevisionNumber().orElse(0L)))
                .map(Revision::getEntity).toList();
        assertThat(jurorAuditHistory.size()).isEqualTo(2);

        Juror originalJurorVersion = jurorAuditHistory.get(0);
        assertThat(originalJurorVersion.getTitle()).isNull();
        assertThat(originalJurorVersion.getFirstName()).isEqualToIgnoringCase("FNAMEONE");
        assertThat(originalJurorVersion.getLastName()).isEqualToIgnoringCase("LNAMEONE");
        assertThat(originalJurorVersion.getPendingTitle()).isEqualToIgnoringCase("Mr");
        assertThat(originalJurorVersion.getPendingFirstName()).isEqualToIgnoringCase("Test");
        assertThat(originalJurorVersion.getPendingLastName()).isEqualToIgnoringCase("Person");

        Juror updatedJurorVersion = jurorAuditHistory.get(1);
        assertThat(updatedJurorVersion.getTitle()).isNull();
        assertThat(updatedJurorVersion.getFirstName()).isEqualToIgnoringCase("FNAMEONE");
        assertThat(updatedJurorVersion.getLastName()).isEqualToIgnoringCase("LNAMEONE");
        assertThat(updatedJurorVersion.getPendingTitle()).isNull();
        assertThat(updatedJurorVersion.getPendingFirstName()).isNull();
        assertThat(updatedJurorVersion.getPendingLastName()).isNull();
    }


    @Test
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_processNameChangeApproval.sql"})
    void testProcessNameChangeApprovalBureauUserForbidden() throws Exception {
        String username = "BUREAU_USER";
        String owner = "400";
        httpHeaders.set(HttpHeaders.AUTHORIZATION, initPayloadWithStaffRank(owner, username, UserType.BUREAU));

        ProcessNameChangeRequestDto dto = new ProcessNameChangeRequestDto(ApprovalDecision.REJECT, "Some notes");
        String jurorNumber = "111111111";

        ResponseEntity<Void> response =
            restTemplate.exchange(new RequestEntity<>(dto, httpHeaders, HttpMethod.PATCH,
                URI.create("/api/v1/moj/juror-record/change-name/" + jurorNumber)), Void.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP PATCH request to be unsuccessful")
            .isEqualTo(HttpStatus.FORBIDDEN);
    }

    private void updateNotesForEtag() throws Exception {
        final String bureauJwt = mintBureauJwt(BureauJwtPayload.builder()
            .userType(UserType.BUREAU)
            .login("BUREAU_USER")
            .owner("400")
            .build());

        HttpHeaders httpHeaders2 = new HttpHeaders();
        httpHeaders2.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        httpHeaders2.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        JurorNotesRequestDto updateNotes = new JurorNotesRequestDto("Some updated notes");
        restTemplate.exchange(new RequestEntity<>(updateNotes, httpHeaders2, HttpMethod.PATCH,
            URI.create(GET_JUROR_NOTES_URL)), Void.class);
    }

    private String generateString(int length) {
        int index = 0;
        StringBuilder sb = new StringBuilder();

        while (index < length) {
            sb.append('a');
            index++;
        }
        return sb.toString();
    }

    private void validateJurorDetailsMapping(BureauJurorDetailDto dto, JurorPool jurorPool,
                                             String expectedPoolNumber) {
        Juror juror = jurorPool.getJuror();

        // verify personal details
        assertThat(dto.getJurorNumber())
            .as("Expect Juror Number property to be mapped from the Juror record")
            .isEqualToIgnoringCase(juror.getJurorNumber());
        assertThat(dto.getTitle())
            .as("Expect Title property to be mapped from the Juror record")
            .isEqualToIgnoringCase(juror.getTitle());
        assertThat(dto.getFirstName())
            .as("Expect First Name property to be mapped from the Juror record")
            .isEqualToIgnoringCase(juror.getFirstName());
        assertThat(dto.getLastName())
            .as("Expect Last Name property to be mapped from the Juror record")
            .isEqualToIgnoringCase(juror.getLastName());
        LocalDate dob = dto.getDateOfBirth();
        assertThat(dob)
            .as("Expect Date Of Birth property to be mapped from the Juror record")
            .isEqualTo(juror.getDateOfBirth());

        // verify address details
        assertThat(dto.getJurorAddress1())
            .as("Expect Address Line 1 property to be mapped from the Juror record")
            .isEqualToIgnoringCase(juror.getAddressLine1());
        assertThat(dto.getJurorAddress2())
            .as("Expect Address Line 2 property to be mapped from the Juror record")
            .isEqualToIgnoringCase(juror.getAddressLine2());
        assertThat(dto.getJurorAddress3())
            .as("Expect Address Line 3 property to be mapped from the Juror record")
            .isEqualToIgnoringCase(juror.getAddressLine3());
        assertThat(dto.getJurorAddress4())
            .as("Expect Address Line 4 property to be mapped from the Juror record")
            .isEqualToIgnoringCase(juror.getAddressLine4());
        assertThat(dto.getJurorAddress5())
            .as("Expect Address Line 5 property to be mapped from the Juror record")
            .isEqualToIgnoringCase(juror.getAddressLine5());
        assertThat(dto.getJurorPostcode())
            .as("Expect Postcode property to be mapped from the Juror record")
            .isEqualToIgnoringCase(juror.getPostcode());

        // verify contact details
        if (dto.getThirdPartyRelationship() == null) {
            assertThat(dto.getPhoneNumber())
                .as("Expect Phone Number property to be mapped from the Juror record")
                .isEqualToIgnoringCase(juror.getPhoneNumber());
            assertThat(dto.getAltPhoneNumber())
                .as("Expect Alt Phone Number property to be mapped from the Juror record")
                .isEqualToIgnoringCase(juror.getAltPhoneNumber());
            assertThat(dto.getEmail())
                .as("Expect Email property to be mapped from the Juror record")
                .isEqualToIgnoringCase(juror.getEmail());
        }

        // verify pool details
        assertThat(dto.getPoolNumber()).as("Expect Pool Number property to be mapped from Part_Hist lookup")
            .isEqualTo(expectedPoolNumber);
        assertThat(dto.getHearingDate())
            .as("Expect Hearing Date property to be mapped from the Juror record")
            .isEqualTo(jurorPool.getNextDate());
        // verify additional details
        assertThat(TestUtils.compareDateToLocalDate(dto.getPoolDate(), jurorPool.getReturnDate()));
        assertThat(dto.getStatus()).as("Expect Status property to be mapped from the Juror record")
            .isEqualTo(jurorPool.getStatus().getStatus());
        assertThat(dto.getNotes()).as("Expect Notes property to be mapped from the Juror record")
            .isEqualToIgnoringCase(juror.getNotes());

        // verify reply method
        assertThat(dto.getReplyMethod()).as("Expect Reply Method property to be Digital")
            .isEqualTo(ReplyMethod.DIGITAL);
    }

    private void validateCourtDetails(BureauJurorDetailDto dto, String expectedLocCode) {
        CourtLocation courtLocation = courtLocationRepository.findByLocCode(expectedLocCode).get();

        assertThat(dto.getCourtName())
            .as("Expect Court Name property to be mapped from the expected loc code")
            .isEqualTo(courtLocation.getName());
        assertThat(dto.getCourtLocName())
            .as("Expect Court Location Name property to be mapped from the expected loc code")
            .isEqualTo(courtLocation.getLocCourtName());
        assertThat(LocalTime.parse(dto.getHearingTime()))
            .as("Expect Hearing Time property to be mapped from the expected loc code")
            .isEqualTo(courtLocation.getCourtAttendTime());

        // validate court location address data
        assertThat(dto.getCourtAddress1())
            .as("Expect Court Address 1 property to be mapped from the expected loc code")
            .isEqualTo(courtLocation.getAddress1());
        assertThat(dto.getCourtAddress2())
            .as("Expect Court Address 2 property to be mapped from the expected loc code")
            .isEqualTo(courtLocation.getAddress2());
        assertThat(dto.getCourtAddress3())
            .as("Expect Court Address 3 property to be mapped from the expected loc code")
            .isEqualTo(courtLocation.getAddress3());
        assertThat(dto.getCourtAddress4())
            .as("Expect Court Address 4 property to be mapped from the expected loc code")
            .isEqualTo(courtLocation.getAddress4());
        assertThat(dto.getCourtAddress5())
            .as("Expect Court Address 5 property to be mapped from the expected loc code")
            .isEqualTo(courtLocation.getAddress5());
        assertThat(dto.getCourtAddress6())
            .as("Expect Court Address 6 property to be mapped from the expected loc code")
            .isEqualTo(courtLocation.getAddress6());
        assertThat(dto.getCourtPostcode())
            .as("Expect Court Postcode property to be mapped from the expected loc code")
            .isEqualTo(courtLocation.getPostcode());
    }

    private void validateResponseDetails(BureauJurorDetailDto dto) {
        DigitalResponse jurorResponse = jurorResponseRepository.findByJurorNumber(dto.getJurorNumber());

        // verify personal details
        assertThat(dto.getNewTitle())
            .as("Expect New Title property to be mapped from the Juror Response record")
            .isEqualToIgnoringCase(jurorResponse.getTitle());
        assertThat(dto.getNewFirstName())
            .as("Expect New First Name property to be mapped from the Juror Response record")
            .isEqualToIgnoringCase(jurorResponse.getFirstName());
        assertThat(dto.getNewLastName())
            .as("Expect New Last Name property to be mapped from the Juror Response record")
            .isEqualToIgnoringCase(jurorResponse.getLastName());
        LocalDate convertedDob =
            jurorResponse.getDateOfBirth();
        assertThat(dto.getNewDateOfBirth())
            .as("Expect New Date Of Birth property to be mapped from the Juror Response record")
            .isEqualTo(convertedDob);

        // verify address details
        assertThat(dto.getNewJurorAddress1())
            .as("Expect New Address Line 1 property to be mapped from the Juror Response record")
            .isEqualToIgnoringCase(jurorResponse.getAddressLine1());
        assertThat(dto.getNewJurorAddress2())
            .as("Expect New Address Line 2 property to be mapped from the Juror Response record")
            .isEqualToIgnoringCase(jurorResponse.getAddressLine2());
        assertThat(dto.getNewJurorAddress3())
            .as("Expect New Address Line 3 property to be mapped from the Juror Response record")
            .isEqualToIgnoringCase(jurorResponse.getAddressLine3());
        assertThat(dto.getNewJurorAddress4())
            .as("Expect New Address Line 4 property to be mapped from the Juror Response record")
            .isEqualToIgnoringCase(jurorResponse.getAddressLine4());
        assertThat(dto.getNewJurorAddress5())
            .as("Expect New Address Line 5 property to be mapped from the Juror Response record")
            .isEqualToIgnoringCase(jurorResponse.getAddressLine5());
        assertThat(dto.getNewJurorPostcode())
            .as("Expect New Postcode property to be mapped from the Juror Response record")
            .isEqualToIgnoringCase(jurorResponse.getPostcode());

        // verify contact details
        assertThat(dto.getNewPhoneNumber())
            .as("Expect New Phone Number property to be mapped from the Juror Response record")
            .isEqualToIgnoringCase(jurorResponse.getPhoneNumber());
        assertThat(dto.getNewAltPhoneNumber())
            .as("Expect New Alt Phone Number property to be mapped from the Juror Response record")
            .isEqualToIgnoringCase(jurorResponse.getAltPhoneNumber());
        assertThat(dto.getNewEmail())
            .as("Expect New Email property to be mapped from the Juror Response record")
            .isEqualToIgnoringCase(jurorResponse.getEmail());

        // verify response details
        LocalDate convertedDateReceived =
            jurorResponse.getDateReceived().toLocalDate();
        assertThat(dto.getDateReceived())
            .as("Expect Date Received property to be mapped from the Juror Response record")
            .isEqualTo(convertedDateReceived);
        assertThat(dto.getProcessingStatus())
            .as("Expect Processing Status property to be mapped from the Juror Response record")
            .isEqualToIgnoringCase(jurorResponse.getProcessingStatus().toString());

        // verify third party details
        if (jurorResponse.getRelationship() != null) {
            assertThat(dto.getThirdPartyRelationship())
                .as("Expect Third Party data to be mapped from the Juror Response record")
                .isEqualToIgnoringCase(jurorResponse.getRelationship());
            assertThat(dto.getThirdPartyFirstName())
                .as("Expect Third Party data to be mapped from the Juror Response record")
                .isEqualToIgnoringCase(jurorResponse.getThirdPartyFName());
            assertThat(dto.getThirdPartyLastName())
                .as("Expect Third Party data to be mapped from the Juror Response record")
                .isEqualToIgnoringCase(jurorResponse.getThirdPartyLName());
            assertThat(dto.getThirdPartyReason())
                .as("Expect Third Party data to be mapped from the Juror Response record")
                .isEqualToIgnoringCase(jurorResponse.getThirdPartyReason());
            assertThat(dto.getThirdPartyReason())
                .as("Expect Third Party data to be mapped from the Juror Response record")
                .isEqualToIgnoringCase(jurorResponse.getThirdPartyReason());
            assertThat(dto.getThirdPartyOtherReason())
                .as("Expect Third Party data to be mapped from the Juror Response record")
                .isEqualToIgnoringCase(jurorResponse.getThirdPartyOtherReason());
        }

        // verify eligibility details
        assertThat(dto.getResidency())
            .as("Expect Residency property to be mapped from the Juror Response record")
            .isEqualTo(jurorResponse.getResidency());
        if (dto.getResidencyDetail() != null) {
            assertThat(dto.getResidencyDetail())
                .as("Expect Residency Detail property to be mapped from the Juror Response record")
                .isEqualTo(jurorResponse.getResidencyDetail());
        }
        assertThat(dto.getMentalHealthAct())
            .as("Expect Mental Health Act property to be mapped from the Juror Response record")
            .isEqualTo(jurorResponse.getMentalHealthAct());
        if (dto.getMentalHealthActDetails() != null) {
            assertThat(dto.getMentalHealthActDetails())
                .as("Expect Mental Health Act Details property to be mapped from the Juror Response record")
                .isEqualTo(jurorResponse.getMentalHealthActDetails());
        }
        assertThat(dto.getBail())
            .as("Expect Bail property to be mapped from the Juror Response record")
            .isEqualTo(jurorResponse.getBail());
        if (dto.getBailDetails() != null) {
            assertThat(dto.getBailDetails())
                .as("Expect Bail Details property to be mapped from the Juror Response record")
                .isEqualTo(jurorResponse.getBailDetails());
        }
        assertThat(dto.getConvictions())
            .as("Expect Convictions property to be mapped from the Juror Response record")
            .isEqualTo(jurorResponse.getConvictions());
        if (dto.getConvictionsDetails() != null) {
            assertThat(dto.getConvictionsDetails())
                .as("Expect Convictions Details property to be mapped from the Juror Response record")
                .isEqualTo(jurorResponse.getConvictionsDetails());
        }

        assertThat(dto.getWelsh())
            .as("Expect Welsh flag property to be mapped from the Juror Response record")
            .isEqualTo(jurorResponse.getWelsh());
    }

    @Nested
    @DisplayName("GET Juror Record Attendance Tab")
    class JurorRecordAttendanceTab {

        @Test
        @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_initAttendanceTests.sql"})
        void getJurorAttendanceHappy() {
            ResponseEntity<JurorAttendanceDetailsResponseDto> response =
                restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                        URI.create("/api/v1/moj/juror-record/attendance-detail/415/111111111")),
                    JurorAttendanceDetailsResponseDto.class);

            assertThat(response.getStatusCode())
                .as("Expect the HTTP GET request to be successful")
                .isEqualTo(HttpStatus.OK);

            validateHappyResponse(response);

        }

        private void validateHappyResponse(ResponseEntity<JurorAttendanceDetailsResponseDto> response) {
            assertThat(response.getBody()).isNotNull();
            JurorAttendanceDetailsResponseDto jurorAttendanceDetailsResponseDto = response.getBody();
            assertThat(jurorAttendanceDetailsResponseDto.getData().size()).isEqualTo(5);

            assertThat(jurorAttendanceDetailsResponseDto.getAttendances()).isEqualTo(3);
            assertThat(jurorAttendanceDetailsResponseDto.getAbsences()).isEqualTo(1);
            assertThat(jurorAttendanceDetailsResponseDto.getNonAttendances()).isEqualTo(1);
            assertThat(jurorAttendanceDetailsResponseDto.getNextDate()).isEqualTo(LocalDate.now().minusDays(4));
            assertThat(jurorAttendanceDetailsResponseDto.isOnCall()).isEqualTo(false);

            JurorAttendanceDetailsResponseDto.JurorAttendanceResponseData jurorAttendanceDetailsDto =
                jurorAttendanceDetailsResponseDto.getData().get(0);
            assertThat(jurorAttendanceDetailsDto.getAttendanceDate())
                .isEqualTo(LocalDate.now().minusDays(5));
            assertThat(jurorAttendanceDetailsDto.getAttendanceType())
                .isEqualTo(AttendanceType.NON_ATTENDANCE);

            jurorAttendanceDetailsDto =
                jurorAttendanceDetailsResponseDto.getData().get(1);
            assertThat(jurorAttendanceDetailsDto.getAttendanceDate())
                .isEqualTo(LocalDate.now().minusDays(4));
            assertThat(jurorAttendanceDetailsDto.getAttendanceType())
                .isEqualTo(AttendanceType.ABSENT);

            jurorAttendanceDetailsDto =
                jurorAttendanceDetailsResponseDto.getData().get(2);
            assertThat(jurorAttendanceDetailsDto.getAttendanceDate())
                .isEqualTo(LocalDate.now().minusDays(3));
            assertThat(jurorAttendanceDetailsDto.getHours())
                .isEqualTo("3.9");
            assertThat(jurorAttendanceDetailsDto.getAttendanceType())
                .isEqualTo(AttendanceType.HALF_DAY);

            jurorAttendanceDetailsDto =
                jurorAttendanceDetailsResponseDto.getData().get(3);
            assertThat(jurorAttendanceDetailsDto.getAttendanceDate())
                .isEqualTo(LocalDate.now().minusDays(2));
            assertThat(jurorAttendanceDetailsDto.getHours())
                .isEqualTo("4.0");
            assertThat(jurorAttendanceDetailsDto.getAttendanceType())
                .isEqualTo(AttendanceType.FULL_DAY);

            jurorAttendanceDetailsDto =
                jurorAttendanceDetailsResponseDto.getData().get(4);
            assertThat(jurorAttendanceDetailsDto.getAttendanceDate())
                .isEqualTo(LocalDate.now().minusDays(1));
            assertThat(jurorAttendanceDetailsDto.getHours())
                .isEqualTo("8.0");
            assertThat(jurorAttendanceDetailsDto.getAttendanceType())
                .isEqualTo(AttendanceType.FULL_DAY);

        }

        @Test
        @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_initAttendanceTests.sql"})
        void getJurorAttendanceOnCall() {
            ResponseEntity<JurorAttendanceDetailsResponseDto> response =
                restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                        URI.create("/api/v1/moj/juror-record/attendance-detail/415/222222222")),
                    JurorAttendanceDetailsResponseDto.class);

            assertThat(response.getStatusCode())
                .as("Expect the HTTP GET request to be successful")
                .isEqualTo(HttpStatus.OK);

            validateOnCallResponse(response);

        }

        private void validateOnCallResponse(ResponseEntity<JurorAttendanceDetailsResponseDto> response) {
            assertThat(response.getBody()).isNotNull();
            JurorAttendanceDetailsResponseDto jurorAttendanceDetailsResponseDto = response.getBody();
            assertThat(jurorAttendanceDetailsResponseDto.getData().size()).isEqualTo(1);

            assertThat(jurorAttendanceDetailsResponseDto.getAttendances()).isEqualTo(1);
            assertThat(jurorAttendanceDetailsResponseDto.getNextDate()).isNull();
            assertThat(jurorAttendanceDetailsResponseDto.isOnCall()).isEqualTo(true);

            JurorAttendanceDetailsResponseDto.JurorAttendanceResponseData jurorAttendanceDetailsDto =
                jurorAttendanceDetailsResponseDto.getData().get(0);
            assertThat(jurorAttendanceDetailsDto.getAttendanceDate())
                .isEqualTo(LocalDate.now().minusDays(2));
            assertThat(jurorAttendanceDetailsDto.getAttendanceType())
                .isEqualTo(AttendanceType.FULL_DAY);
            assertThat(jurorAttendanceDetailsDto.getHours())
                .isEqualTo("8.0");
            assertThat(jurorAttendanceDetailsDto.getTravelTime())
                .isEqualTo(LocalTime.of(1, 0));
        }

    }


    @Nested
    class UpdatePncCheckStatus {
        @ParameterizedTest
        @ValueSource(strings = {
            "111111111",
            "111111112",
            "111111113",
            "111111115",
            "111111116",
            "111111117",
            "111111118",
            "111111119",
            "111111120",
            "111111121",
            "111111122"})
        @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_policeCheck.sql"})
        void updatePncCheckStatusEligible(String jurorNumber) {
            ResponseEntity<Void> response =
                restTemplate.exchange(new RequestEntity<>(new PoliceCheckStatusDto(PoliceCheck.ELIGIBLE),
                        httpHeaders, HttpMethod.PATCH,
                        URI.create("/api/v1/moj/juror-record/pnc/" + jurorNumber)),
                    Void.class);

            assertThat(response.getStatusCode())
                .as("Expect the HTTP GET request to be accepted")
                .isEqualTo(HttpStatus.ACCEPTED);


            Juror juror = jurorRepository.findById(jurorNumber).get();
            final JurorPool jurorPool = jurorPoolRepository.findByJurorJurorNumberAndIsActive(jurorNumber, true).get(0);

            assertEquals(PoliceCheck.ELIGIBLE, juror.getPoliceCheck(),
                "Police check should match");

            assertNull(juror.getDisqualifyCode(),
                "Disqualify code should be null");
            assertNull(juror.getDisqualifyDate(),
                "Disqualify date should be null");
            assertEquals(2, jurorPool.getStatus().getStatus(),
                "Juror pool status should match");

            List<JurorHistory> jurorHistoryList = new ArrayList<>(
                jurorHistoryRepository.findByJurorNumber(jurorNumber));
            jurorHistoryList.sort(Comparator.comparing(JurorHistory::getHistoryCode));
            verifyStandardJurorHistory(jurorPool, jurorHistoryList,
                new JurorHistoryExpectedValues("RRES", "Confirmation Letter Auto"),
                new JurorHistoryExpectedValues("POLG", "Passed")
            );
            verifyBulkPrintData(jurorNumber, FormCode.ENG_CONFIRMATION.getCode());
        }


        @ParameterizedTest
        @ValueSource(strings = {
            "111111111",
            "111111112",
            "111111113",
            "111111114",
            "111111116",
            "111111117",
            "111111118",
            "111111119",
            "111111120",
            "111111121",
            "111111122"})
        @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_policeCheck.sql"})
        void updatePncCheckStatusIneligible(String jurorNumber) {
            ResponseEntity<Void> response =
                restTemplate.exchange(new RequestEntity<>(new PoliceCheckStatusDto(PoliceCheck.INELIGIBLE),
                        httpHeaders, HttpMethod.PATCH,
                        URI.create("/api/v1/moj/juror-record/pnc/" + jurorNumber)),
                    Void.class);

            assertThat(response.getStatusCode())
                .as("Expect the HTTP GET request to be accepted")
                .isEqualTo(HttpStatus.ACCEPTED);


            Juror juror = jurorRepository.findById(jurorNumber).get();
            final JurorPool jurorPool = jurorPoolRepository.findByJurorJurorNumberAndIsActive(jurorNumber, true).get(0);

            assertEquals(PoliceCheck.INELIGIBLE, juror.getPoliceCheck(),
                "Police check should match");

            assertEquals("E", juror.getDisqualifyCode(),
                "Disqualify code should match");
            assertEquals(LocalDate.now(clock), juror.getDisqualifyDate(),
                "Disqualify date should match");
            assertEquals(6, jurorPool.getStatus().getStatus(),
                "Juror pool status should match");

            List<JurorHistory> jurorHistoryList = new ArrayList<>(
                jurorHistoryRepository.findByJurorNumber(jurorNumber));

            verifyStandardJurorHistory(jurorPool,
                jurorHistoryList,
                new JurorHistoryExpectedValues("POLF", "Failed"),
                new JurorHistoryExpectedValues("PDIS", "Disqualify - E"),
                new JurorHistoryExpectedValues("RDIS", "Withdrawal Letter Auto")
            );
            verifyBulkPrintData(jurorNumber, FormCode.ENG_WITHDRAWAL.getCode());
        }


        @ParameterizedTest
        @ValueSource(strings = {
            "111111111",
            "111111113",
            "111111114",
            "111111115",
            "111111116",
            "111111122"
        })
        @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_policeCheck.sql"})
        void updatePncCheckStatusInError(String jurorNumber) {
            ResponseEntity<Void> response =
                restTemplate.exchange(
                    new RequestEntity<>(new PoliceCheckStatusDto(PoliceCheck.ERROR_RETRY_CONNECTION_ERROR),
                        httpHeaders, HttpMethod.PATCH,
                        URI.create("/api/v1/moj/juror-record/pnc/" + jurorNumber)),
                    Void.class);

            assertThat(response.getStatusCode())
                .as("Expect the HTTP GET request to be accepted")
                .isEqualTo(HttpStatus.ACCEPTED);


            Juror juror = jurorRepository.findById(jurorNumber).get();
            final JurorPool jurorPool = jurorPoolRepository.findByJurorJurorNumberAndIsActive(jurorNumber, true).get(0);

            assertEquals(PoliceCheck.ERROR_RETRY_CONNECTION_ERROR, juror.getPoliceCheck(),
                "Police check should match");

            assertNull(juror.getDisqualifyCode(),
                "Disqualify code should be null");
            assertNull(juror.getDisqualifyDate(),
                "Disqualify date should be null");
            assertEquals(2, jurorPool.getStatus().getStatus(),
                "Juror pool status should match");
            verifyNoBulkPrintData(jurorNumber);
        }


        @ParameterizedTest
        @ValueSource(strings = {
            "111111117",
            "111111118",
            "111111119",
            "111111120",
            "111111121"
        })
        @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_policeCheck.sql"})
        void updatePncCheckStatusMaxRetiesError(String jurorNumber) {
            ResponseEntity<Void> response =
                restTemplate.exchange(
                    new RequestEntity<>(new PoliceCheckStatusDto(PoliceCheck.ERROR_RETRY_CONNECTION_ERROR),
                        httpHeaders, HttpMethod.PATCH,
                        URI.create("/api/v1/moj/juror-record/pnc/" + jurorNumber)),
                    Void.class);

            assertThat(response.getStatusCode())
                .as("Expect the HTTP GET request to be accepted")
                .isEqualTo(HttpStatus.ACCEPTED);


            Juror juror = jurorRepository.findById(jurorNumber).get();
            final JurorPool jurorPool = jurorPoolRepository.findByJurorJurorNumberAndIsActive(jurorNumber, true).get(0);

            assertEquals(PoliceCheck.UNCHECKED_MAX_RETRIES_EXCEEDED, juror.getPoliceCheck(),
                "Police check should match");

            assertNull(juror.getDisqualifyCode(),
                "Disqualify code should be null");
            assertNull(juror.getDisqualifyDate(),
                "Disqualify date should be null");
            assertEquals(2, jurorPool.getStatus().getStatus(),
                "Juror pool status should match");

            List<JurorHistory> jurorHistoryList =
                jurorHistoryRepository.findByJurorNumber(jurorNumber);
            JurorHistory jurorHistory = jurorHistoryList.get(0);
            verifyStandardJurorHistory(jurorPool, List.of(jurorHistory),
                new JurorHistoryExpectedValues("POLG", "Unchecked - timed out")
            );
            verifyBulkPrintData(jurorNumber, FormCode.ENG_CONFIRMATION.getCode());
        }
    }

    @Nested
    @DisplayName("PATCH " + UpdateJurorToFailedToAttend.URL)
    @Sql({"/db/mod/truncate.sql",
        "/db/JurorRecordControllerITest_failedToAttend_typical.sql"})
    @SuppressWarnings("PMD.LawOfDemeter")
    class UpdateJurorToFailedToAttend {

        private static final String URL = BASE_URL + "/failed-to-attend";

        private static final String JUROR_NUMBER = "641500005";
        private static final String POOL_NUMBER = "415220901";

        private JurorNumberAndPoolNumberDto createDto(String jurorNumber, String poolNumber) {
            JurorNumberAndPoolNumberDto dto = new JurorNumberAndPoolNumberDto();
            dto.setJurorNumber(jurorNumber);
            dto.setPoolNumber(poolNumber);
            return dto;
        }

        @Test
        void positiveTypical() {
            setAuthorization("COURT_USER", "414", UserType.COURT);
            JurorNumberAndPoolNumberDto dto = createDto(JUROR_NUMBER, POOL_NUMBER);
            ResponseEntity<Void> response =
                restTemplate.exchange(
                    new RequestEntity<>(dto, httpHeaders, HttpMethod.PATCH, URI.create(URL)), Void.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

            JurorPool jurorPool =
                jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumber(JUROR_NUMBER, POOL_NUMBER);
            assertEquals(IJurorStatus.FAILED_TO_ATTEND, jurorPool.getStatus().getStatus(),
                "Juror pool status should be failed to attend");

            List<JurorHistory> jurorHistories = jurorHistoryRepository.findByJurorNumber(JUROR_NUMBER);
            assertEquals(1, jurorHistories.size(), "Should only be one history entry");
            JurorHistory jurorHistory = jurorHistories.get(0);
            assertEquals(POOL_NUMBER, jurorHistory.getPoolNumber(), "Pool number should match");
            assertEquals(JUROR_NUMBER, jurorHistory.getJurorNumber(), "Juror number should match");
            assertEquals("COURT_USER", jurorHistory.getCreatedBy(), "User id should match");
            assertEquals(HistoryCodeMod.FAILED_TO_ATTEND, jurorHistory.getHistoryCode(), "History code should match");
            assertEquals("FTA after responding", jurorHistory.getOtherInformation(),
                "Info should match");
        }

        @Test
        void negativeNotFound() {
            setAuthorization("COURT_USER", "414", UserType.COURT);
            JurorNumberAndPoolNumberDto dto = createDto("123456789", POOL_NUMBER);
            ResponseEntity<String> response =
                restTemplate.exchange(
                    new RequestEntity<>(dto, httpHeaders, HttpMethod.PATCH, URI.create(URL)), String.class);
            assertErrorResponse(response,
                HttpStatus.NOT_FOUND,
                URL,
                MojException.NotFound.class,
                "Juror number 123456789 not found in pool " + POOL_NUMBER);
        }

        @Test
        void negativeNotResponded() {
            final String jurorNumber = "641500004";
            setAuthorization("COURT_USER", "415", UserType.COURT, Role.SENIOR_JUROR_OFFICER);
            JurorNumberAndPoolNumberDto dto = createDto(jurorNumber, POOL_NUMBER);

            ResponseEntity<String> response =
                restTemplate.exchange(
                    new RequestEntity<>(dto, httpHeaders, HttpMethod.PATCH, URI.create(URL)), String.class);
            assertBusinessRuleViolation(response,
                "Juror status must be responded in order to undo the failed to attend status.",
                JUROR_STATUS_MUST_BE_RESPONDED
            );

            JurorPool jurorPool = jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumber(jurorNumber, POOL_NUMBER);
            assertEquals(IJurorStatus.PANEL, jurorPool.getStatus().getStatus(),
                "Juror pool status should not change");

            List<JurorHistory> jurorHistories = jurorHistoryRepository.findByJurorNumber(jurorNumber);
            assertEquals(0, jurorHistories.size(),
                "No new history entry as request should be rejected before processing");
        }

        @Test
        void negativeHasCompletionDate() {
            final String jurorNumber = "641500003";
            setAuthorization("COURT_USER", "415", UserType.COURT, Role.SENIOR_JUROR_OFFICER);
            JurorNumberAndPoolNumberDto dto = createDto(jurorNumber, POOL_NUMBER);

            ResponseEntity<String> response =
                restTemplate.exchange(
                    new RequestEntity<>(dto, httpHeaders, HttpMethod.PATCH, URI.create(URL)), String.class);
            assertBusinessRuleViolation(response,
                "This juror cannot be given a Failed To Attend status because they have been given a completion date. "
                    + "Only a Senior Jury Officer can be remove the completion date",
                FAILED_TO_ATTEND_HAS_COMPLETION_DATE
            );

            JurorPool jurorPool = jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumber(jurorNumber, POOL_NUMBER);
            assertEquals(IJurorStatus.RESPONDED, jurorPool.getStatus().getStatus(),
                "Juror pool status should not change");

            List<JurorHistory> jurorHistories = jurorHistoryRepository.findByJurorNumber(jurorNumber);
            assertEquals(0, jurorHistories.size(),
                "No new history entry as request should be rejected before processing");
        }

        @Test
        void negativeHasAppearances() {
            final String jurorNumber = "641500002";
            setAuthorization("COURT_USER", "415", UserType.COURT, Role.SENIOR_JUROR_OFFICER);
            JurorNumberAndPoolNumberDto dto = createDto(jurorNumber, POOL_NUMBER);

            ResponseEntity<String> response =
                restTemplate.exchange(
                    new RequestEntity<>(dto, httpHeaders, HttpMethod.PATCH, URI.create(URL)), String.class);
            assertBusinessRuleViolation(response,
                "This juror cannot be given a Failed To Attend status because they have had attendances recorded."
                    + " The Failed To Attend status is only for jurors who have not attended at all",
                FAILED_TO_ATTEND_HAS_ATTENDANCE_RECORD
            );

            JurorPool jurorPool = jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumber(jurorNumber, POOL_NUMBER);
            assertEquals(IJurorStatus.RESPONDED, jurorPool.getStatus().getStatus(),
                "Juror pool status should not change");

            List<JurorHistory> jurorHistories = jurorHistoryRepository.findByJurorNumber(jurorNumber);
            assertEquals(0, jurorHistories.size(),
                "No new history entry as request should be rejected before processing");
        }

        @Test
        void negativeInvalidPayload() {
            setAuthorization("COURT_USER", "414",  UserType.COURT);
            JurorNumberAndPoolNumberDto dto = createDto("INVALID", POOL_NUMBER);
            ResponseEntity<String> response =
                restTemplate.exchange(
                    new RequestEntity<>(dto, httpHeaders, HttpMethod.PATCH, URI.create(URL)), String.class);
            assertInvalidPayload(response,
                new RestResponseEntityExceptionHandler.FieldError("jurorNumber", "must match \"^\\d{9}$\""));

            JurorPool jurorPool =
                jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumber(JUROR_NUMBER, POOL_NUMBER);
            assertEquals(IJurorStatus.RESPONDED, jurorPool.getStatus().getStatus(),
                "Juror pool status should not change");

            List<JurorHistory> jurorHistories = jurorHistoryRepository.findByJurorNumber(JUROR_NUMBER);
            assertEquals(0, jurorHistories.size(),
                "No new history entry as request should be rejected before processing");
        }

        @Test
        void negativeUnauthorised() {
            setAuthorization("COURT_USER", "400",  UserType.BUREAU);
            JurorNumberAndPoolNumberDto dto = createDto(JUROR_NUMBER, POOL_NUMBER);
            ResponseEntity<String> response =
                restTemplate.exchange(
                    new RequestEntity<>(dto, httpHeaders, HttpMethod.PATCH, URI.create(URL)), String.class);

            assertForbiddenResponse(response, URL);

            JurorPool jurorPool =
                jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumber(JUROR_NUMBER, POOL_NUMBER);
            assertEquals(IJurorStatus.RESPONDED, jurorPool.getStatus().getStatus(),
                "Juror pool status should not change");

            List<JurorHistory> jurorHistories = jurorHistoryRepository.findByJurorNumber(JUROR_NUMBER);
            assertEquals(0, jurorHistories.size(),
                "No new history entry as request should be rejected before processing");
        }
    }

    @Nested
    @DisplayName("PATCH " + UndoUpdateJurorToFailedToAttend.URL)
    @Sql({"/db/mod/truncate.sql",
        "/db/JurorRecordControllerITest_failedToAttend_undo_typical.sql"})
    @SuppressWarnings("PMD.LawOfDemeter")
    class UndoUpdateJurorToFailedToAttend {

        private static final String URL = BASE_URL + "/failed-to-attend/undo";

        private static final String JUROR_NUMBER = "641500005";
        private static final String POOL_NUMBER = "415220901";

        private JurorNumberAndPoolNumberDto createDto(String jurorNumber, String poolNumber) {
            JurorNumberAndPoolNumberDto dto = new JurorNumberAndPoolNumberDto();
            dto.setJurorNumber(jurorNumber);
            dto.setPoolNumber(poolNumber);
            return dto;
        }

        @Test
        void positiveTypical() {
            setAuthorization("COURT_USER", "415", UserType.COURT, Role.SENIOR_JUROR_OFFICER);
            JurorNumberAndPoolNumberDto dto = createDto(JUROR_NUMBER, POOL_NUMBER);
            ResponseEntity<Void> response =
                restTemplate.exchange(
                    new RequestEntity<>(dto, httpHeaders, HttpMethod.PATCH, URI.create(URL)), Void.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

            JurorPool jurorPool =
                jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumber(JUROR_NUMBER, POOL_NUMBER);
            assertEquals(IJurorStatus.RESPONDED, jurorPool.getStatus().getStatus(),
                "Juror pool status should be responded");

            List<JurorHistory> jurorHistories = jurorHistoryRepository.findByJurorNumber(JUROR_NUMBER);
            assertEquals(1, jurorHistories.size(), "Should only be one history entry");
            JurorHistory jurorHistory = jurorHistories.get(0);
            assertEquals(POOL_NUMBER, jurorHistory.getPoolNumber(), "Pool number should match");
            assertEquals(JUROR_NUMBER, jurorHistory.getJurorNumber(), "Juror number should match");
            assertEquals("COURT_USER", jurorHistory.getCreatedBy(), "User id should match");
            assertEquals(HistoryCodeMod.FAILED_TO_ATTEND, jurorHistory.getHistoryCode(), "History code should match");
            assertEquals("FTA status removed", jurorHistory.getOtherInformation(),
                "Info should match");
        }

        @Test
        void negativeNotFound() {
            setAuthorization("COURT_USER", "415", UserType.COURT, Role.SENIOR_JUROR_OFFICER);
            JurorNumberAndPoolNumberDto dto = createDto("123456789", POOL_NUMBER);
            ResponseEntity<String> response =
                restTemplate.exchange(
                    new RequestEntity<>(dto, httpHeaders, HttpMethod.PATCH, URI.create(URL)), String.class);
            assertErrorResponse(response,
                HttpStatus.NOT_FOUND,
                URL,
                MojException.NotFound.class,
                "Juror number 123456789 not found in pool " + POOL_NUMBER);
        }

        @Test
        void negativeNotFailedToRespond() {
            final String jurorNumber = "641500004";
            setAuthorization("COURT_USER", "415", UserType.COURT, Role.SENIOR_JUROR_OFFICER);
            JurorNumberAndPoolNumberDto dto = createDto(jurorNumber, POOL_NUMBER);

            ResponseEntity<String> response =
                restTemplate.exchange(
                    new RequestEntity<>(dto, httpHeaders, HttpMethod.PATCH, URI.create(URL)), String.class);
            assertBusinessRuleViolation(response,
                "Juror status must be failed to attend in order to undo the failed to attend status.",
                JUROR_STATUS_MUST_BE_FAILED_TO_ATTEND
            );

            JurorPool jurorPool = jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumber(jurorNumber, POOL_NUMBER);
            assertEquals(IJurorStatus.RESPONDED, jurorPool.getStatus().getStatus(),
                "Juror pool status should not change");

            List<JurorHistory> jurorHistories = jurorHistoryRepository.findByJurorNumber(jurorNumber);
            assertEquals(0, jurorHistories.size(),
                "No new history entry as request should be rejected before processing");
        }


        @Test
        void negativeInvalidPayload() {
            setAuthorization("COURT_USER", "415", UserType.COURT, Role.SENIOR_JUROR_OFFICER);
            JurorNumberAndPoolNumberDto dto = createDto("INVALID", POOL_NUMBER);
            ResponseEntity<String> response =
                restTemplate.exchange(
                    new RequestEntity<>(dto, httpHeaders, HttpMethod.PATCH, URI.create(URL)), String.class);
            assertInvalidPayload(response,
                new RestResponseEntityExceptionHandler.FieldError("jurorNumber", "must match \"^\\d{9}$\""));


            JurorPool jurorPool =
                jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumber(JUROR_NUMBER, POOL_NUMBER);
            assertEquals(IJurorStatus.FAILED_TO_ATTEND, jurorPool.getStatus().getStatus(),
                "Juror pool status should not change");

            List<JurorHistory> jurorHistories = jurorHistoryRepository.findByJurorNumber(JUROR_NUMBER);
            assertEquals(0, jurorHistories.size(),
                "No new history entry as request should be rejected before processing");
        }

        @Test
        void negativeUnauthorisedWrongLevel() {
            setAuthorization("COURT_USER", "415", UserType.COURT);
            JurorNumberAndPoolNumberDto dto = createDto(JUROR_NUMBER, POOL_NUMBER);
            ResponseEntity<String> response =
                restTemplate.exchange(
                    new RequestEntity<>(dto, httpHeaders, HttpMethod.PATCH, URI.create(URL)), String.class);

            assertForbiddenResponse(response, URL);

            JurorPool jurorPool =
                jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumber(JUROR_NUMBER, POOL_NUMBER);
            assertEquals(IJurorStatus.FAILED_TO_ATTEND, jurorPool.getStatus().getStatus(),
                "Juror pool status should not change");

            List<JurorHistory> jurorHistories = jurorHistoryRepository.findByJurorNumber(JUROR_NUMBER);
            assertEquals(0, jurorHistories.size(),
                "No new history entry as request should be rejected before processing");
        }

        @Test
        void negativeUnauthorisedBureau() {
            setAuthorization("BUREAU_USER", "400", UserType.BUREAU);
            JurorNumberAndPoolNumberDto dto = createDto(JUROR_NUMBER, POOL_NUMBER);
            ResponseEntity<String> response =
                restTemplate.exchange(
                    new RequestEntity<>(dto, httpHeaders, HttpMethod.PATCH, URI.create(URL)), String.class);

            assertForbiddenResponse(response, URL);

            JurorPool jurorPool =
                jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumber(JUROR_NUMBER, POOL_NUMBER);
            assertEquals(IJurorStatus.FAILED_TO_ATTEND, jurorPool.getStatus().getStatus(),
                "Juror pool status should not change");

            List<JurorHistory> jurorHistories = jurorHistoryRepository.findByJurorNumber(JUROR_NUMBER);
            assertEquals(0, jurorHistories.size(),
                "No new history entry as request should be rejected before processing");
        }
    }

    @Nested
    @DisplayName("GET" + GetJurorBankDetails.URL)
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordControllerITest_getJurorBankDetails.sql"})
    class GetJurorBankDetails {

        private static final String URL = BASE_URL + "/{juror_number}/bank-details";

        private String toUrl(String jurorNumber) {
            return URL.replace("{juror_number}", jurorNumber);
        }

        @Test
        @DisplayName("GetJurorBankDetailsHappyPath")
        void getJurorBankDetailsHappyPath() throws Exception {
            httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("415", Collections.singletonList("415"),
                UserType.COURT));

            ResponseEntity<JurorBankDetailsDto> response =
                restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                        URI.create(toUrl("123456789"))),
                    JurorBankDetailsDto.class);

            assertThat(response.getStatusCode()).as("Expect the HTTP Response Status to be OK")
                .isEqualTo(HttpStatus.OK);

            JurorBankDetailsDto jurorBankDetailsDto = response.getBody();
            assertThat(jurorBankDetailsDto).isNotNull();

            //verify address details match
            assertThat(jurorBankDetailsDto.getAddressLineOne())
                .as("AddressLineOne - Expect property and dto match")
                .isEqualTo("Address Line 1");
            assertThat(jurorBankDetailsDto.getAddressLineTwo())
                .as("AddressLineTwo - Expect property and dto match")
                .isEqualTo("Address Line 2");
            assertThat(jurorBankDetailsDto.getAddressLineThree())
                .as("AddressLineThree - Expect property and dto match")
                .isEqualTo("Address Line 3");
            assertThat(jurorBankDetailsDto.getAddressLineFour())
                .as("AddressLineFour - Expect property and dto match")
                .isEqualTo("Address Line 4");
            assertThat(jurorBankDetailsDto.getAddressLineFive())
                .as("AddressLineFive - Expect property and dto match")
                .isEqualTo("Address Line 5");

            //verify bank details and notes match
            assertThat(jurorBankDetailsDto.getBankAccountNumber())
                .as("BankAccountNumber - Expect property and dto match")
                .isEqualTo("12345678");
            assertThat(jurorBankDetailsDto.getSortCode())
                .as("SortCode - Expect property and dto match")
                .isEqualTo("123456");
            assertThat(jurorBankDetailsDto.getAccountHolderName())
                .as("AccountHolderName - Expect property and dto match")
                .isEqualTo("Account Name");
            assertThat(jurorBankDetailsDto.getNotes())
                .as("Notes - Expect property and dto match")
                .isEqualTo("Notes");

        }

        @Test
        void getJurorBankDetailsUnhappyPathBureauUserDoesNotHaveAccess() throws Exception {

            httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("400", Collections.singletonList("400"),
                UserType.BUREAU));

            ResponseEntity<JurorBankDetailsDto> response =
                restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                        URI.create(toUrl("111111111"))),
                    JurorBankDetailsDto.class);

            assertThat(response.getStatusCode())
                .as("Expect the HTTP Response Status to be Forbidden")
                .isEqualTo(HttpStatus.FORBIDDEN);

        }

        @Test
        void getJurorBankDetailsUnhappyPathCourtUserDoesNotHaveAccessToJuror() throws Exception {

            httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("416", Collections.singletonList("416"),
                UserType.COURT));

            ResponseEntity<JurorBankDetailsDto> response =
                restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                        URI.create(toUrl("123456789"))),
                    JurorBankDetailsDto.class);

            assertThat(response.getStatusCode())
                .as("Expect the HTTP Response Status to be Forbidden")
                .isEqualTo(HttpStatus.FORBIDDEN);

        }

        @Test
        void getJurorBankDetailsUnhappyPathJurorNotFound() throws Exception {

            httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("415", Collections.singletonList("415"),
                UserType.COURT));

            ResponseEntity<JurorBankDetailsDto> response =
                restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                        URI.create(toUrl("000000000"))),
                    JurorBankDetailsDto.class);

            assertThat(response.getStatusCode())
                .as("Expect the HTTP Response Status to be Not Found")
                .isEqualTo(HttpStatus.NOT_FOUND);

            assertThat(response.getBody().getBankAccountNumber())
                .as("Expect BankAccountNumber to be null").isNull();
            assertThat(response.getBody().getSortCode())
                .as("Expect SortCode to be null").isNull();
            assertThat(response.getBody().getAddressLineOne())
                .as("Expect AddressLineOne to be null").isNull();
            assertThat(response.getBody().getAddressLineTwo())
                .as("Expect AddressLineTwo to be null").isNull();
            assertThat(response.getBody().getAddressLineThree())
                .as("Expect AddressLineThree to be null").isNull();
            assertThat(response.getBody().getAddressLineFour())
                .as("Expect AddressLineFour to be null").isNull();
            assertThat(response.getBody().getAddressLineFive())
                .as("Expect AddressLineFive to be null").isNull();
            assertThat(response.getBody().getNotes())
                .as("Expect Notes to be null").isNull();
        }

        @Test
        void getJurorBankDetailsUnhappyPathInvalidJurorNumber() throws Exception {
            httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("415", Collections.singletonList("415"),
                UserType.COURT));

            ResponseEntity<String> response =
                restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                        URI.create(toUrl("INVALID"))),
                    String.class);

            assertThat(response.getStatusCode())
                .as("Expect the HTTP Response Status to be Bad Request")
                .isEqualTo(HttpStatus.BAD_REQUEST);

            assertThat(response.getBody())
                .as("Expect the HTTP Response Body to be a JSON string").isNotNull();
            assertThat(response.getBody())
                .as("Expect the HTTP Response Body to contain the expected error message")
                .contains("getJurorBankDetails.jurorNumber: must match \\\"^\\\\d{9}$\\\"");
        }
    }

    @Nested
    @DisplayName("POST " + GetJurorDetailsBulkFilterable.URL)
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordControllerITest_getJurorDetailsFiltered_typical.sql"})
    class GetJurorDetailsBulkFilterable {
        private static final String URL = BASE_URL + "/details";

        private FilterableJurorDetailsRequestDto createDto(String jurorNumber, Long version,
                                                           FilterableJurorDetailsRequestDto.IncludeType... include) {
            return FilterableJurorDetailsRequestDto.builder()
                .jurorNumber(jurorNumber)
                .jurorVersion(version)
                .include(List.of(include))
                .build();
        }

        @DisplayName("Positive")
        @Nested
        class Positive {

            private ResponseEntity<FilterableJurorDetailsResponseDto[]> triggerValid(
                FilterableJurorDetailsRequestDto dto) {
                return triggerValid(Collections.singletonList(dto));
            }

            private ResponseEntity<FilterableJurorDetailsResponseDto[]> triggerValid(
                List<FilterableJurorDetailsRequestDto> dto) {
                setAuthorization("COURT_USER", "415", UserType.COURT);

                ResponseEntity<FilterableJurorDetailsResponseDto[]> response = restTemplate.exchange(
                    new RequestEntity<>(dto, httpHeaders, POST, URI.create(URL)),
                    FilterableJurorDetailsResponseDto[].class);

                assertThat(response).isNotNull();
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody().length).isEqualTo(dto.size());

                return response;
            }

            @Test
            void getSingleGetNameDetails() {
                ResponseEntity<FilterableJurorDetailsResponseDto[]> responseEntity = triggerValid(
                    createDto(TestConstants.VALID_JUROR_NUMBER, null,
                        FilterableJurorDetailsRequestDto.IncludeType.NAME_DETAILS));
                //Count validated in triggerValid so no need to do here
                FilterableJurorDetailsResponseDto responseDto = Objects.requireNonNull(responseEntity.getBody())[0];
                assertThat(responseDto.getJurorNumber()).isEqualTo(TestConstants.VALID_JUROR_NUMBER);
                assertThat(responseDto.getJurorVersion()).isNull();

                assertThat(responseDto.getNameDetails()).isNotNull();
                assertThat(responseDto.getPaymentDetails()).isNull();
                assertThat(responseDto.getAddress()).isNull();

                NameDetails nameDetails = responseDto.getNameDetails();
                assertThat(nameDetails.getTitle()).isEqualTo("Mr");
                assertThat(nameDetails.getFirstName()).isEqualTo("FNAME");
                assertThat(nameDetails.getLastName()).isEqualTo("LNAME");
            }

            @Test
            void getSingleGetPaymentDetails() {
                ResponseEntity<FilterableJurorDetailsResponseDto[]> responseEntity = triggerValid(
                    createDto(TestConstants.VALID_JUROR_NUMBER, null,
                        FilterableJurorDetailsRequestDto.IncludeType.PAYMENT_DETAILS));
                //Count validated in triggerValid so no need to do here
                FilterableJurorDetailsResponseDto responseDto = Objects.requireNonNull(responseEntity.getBody())[0];
                assertThat(responseDto.getJurorNumber()).isEqualTo(TestConstants.VALID_JUROR_NUMBER);
                assertThat(responseDto.getJurorVersion()).isNull();

                assertThat(responseDto.getNameDetails()).isNull();
                assertThat(responseDto.getPaymentDetails()).isNotNull();
                assertThat(responseDto.getAddress()).isNull();

                PaymentDetails paymentDetails = responseDto.getPaymentDetails();
                assertThat(paymentDetails.getSortCode()).isEqualTo("112233");
                assertThat(paymentDetails.getBankAccountName()).isEqualTo("Bank NAME");
                assertThat(paymentDetails.getBankAccountNumber()).isEqualTo("12345678");
                assertThat(paymentDetails.getBuildingSocietyRollNumber()).isNull();
            }

            @Test
            void getSingleGetAddressDetails() {
                ResponseEntity<FilterableJurorDetailsResponseDto[]> responseEntity = triggerValid(
                    createDto(TestConstants.VALID_JUROR_NUMBER, null,
                        FilterableJurorDetailsRequestDto.IncludeType.ADDRESS_DETAILS));
                //Count validated in triggerValid so no need to do here
                FilterableJurorDetailsResponseDto responseDto = Objects.requireNonNull(responseEntity.getBody())[0];
                assertThat(responseDto.getJurorNumber()).isEqualTo(TestConstants.VALID_JUROR_NUMBER);
                assertThat(responseDto.getJurorVersion()).isNull();

                assertThat(responseDto.getNameDetails()).isNull();
                assertThat(responseDto.getPaymentDetails()).isNull();
                assertThat(responseDto.getAddress()).isNotNull();

                JurorAddressDto jurorAddressDto = responseDto.getAddress();
                assertThat(jurorAddressDto.getLineOne()).isEqualTo("Address Line 1");
                assertThat(jurorAddressDto.getLineTwo()).isEqualTo("Address Line 2");
                assertThat(jurorAddressDto.getLineThree()).isEqualTo("Address Line 3");
                assertThat(jurorAddressDto.getTown()).isEqualTo("Address Line 4");
                assertThat(jurorAddressDto.getCounty()).isEqualTo("Address Line 5");
                assertThat(jurorAddressDto.getPostcode()).isEqualTo("CH1 2AN");
            }

            @Test
            void getSingleGetAll() {
                ResponseEntity<FilterableJurorDetailsResponseDto[]> responseEntity = triggerValid(
                    createDto(TestConstants.VALID_JUROR_NUMBER, null,
                        FilterableJurorDetailsRequestDto.IncludeType.NAME_DETAILS,
                        FilterableJurorDetailsRequestDto.IncludeType.PAYMENT_DETAILS,
                        FilterableJurorDetailsRequestDto.IncludeType.ADDRESS_DETAILS)
                );
                //Count validated in triggerValid so no need to do here
                FilterableJurorDetailsResponseDto responseDto = Objects.requireNonNull(responseEntity.getBody())[0];
                assertThat(responseDto.getJurorNumber()).isEqualTo(TestConstants.VALID_JUROR_NUMBER);
                assertThat(responseDto.getJurorVersion()).isNull();

                assertThat(responseDto.getNameDetails()).isNotNull();
                assertThat(responseDto.getPaymentDetails()).isNotNull();
                assertThat(responseDto.getAddress()).isNotNull();


                NameDetails nameDetails = responseDto.getNameDetails();
                assertThat(nameDetails.getTitle()).isEqualTo("Mr");
                assertThat(nameDetails.getFirstName()).isEqualTo("FNAME");
                assertThat(nameDetails.getLastName()).isEqualTo("LNAME");

                PaymentDetails paymentDetails = responseDto.getPaymentDetails();
                assertThat(paymentDetails.getSortCode()).isEqualTo("112233");
                assertThat(paymentDetails.getBankAccountName()).isEqualTo("Bank NAME");
                assertThat(paymentDetails.getBankAccountNumber()).isEqualTo("12345678");
                assertThat(paymentDetails.getBuildingSocietyRollNumber()).isNull();

                JurorAddressDto jurorAddressDto = responseDto.getAddress();
                assertThat(jurorAddressDto.getLineOne()).isEqualTo("Address Line 1");
                assertThat(jurorAddressDto.getLineTwo()).isEqualTo("Address Line 2");
                assertThat(jurorAddressDto.getLineThree()).isEqualTo("Address Line 3");
                assertThat(jurorAddressDto.getTown()).isEqualTo("Address Line 4");
                assertThat(jurorAddressDto.getCounty()).isEqualTo("Address Line 5");
                assertThat(jurorAddressDto.getPostcode()).isEqualTo("CH1 2AN");
            }

            @Test
            void getSingleGetAllFromVersion() {
                ResponseEntity<FilterableJurorDetailsResponseDto[]> responseEntity = triggerValid(
                    createDto(TestConstants.VALID_JUROR_NUMBER, 2L,
                        FilterableJurorDetailsRequestDto.IncludeType.NAME_DETAILS,
                        FilterableJurorDetailsRequestDto.IncludeType.PAYMENT_DETAILS,
                        FilterableJurorDetailsRequestDto.IncludeType.ADDRESS_DETAILS)
                );
                //Count validated in triggerValid so no need to do here
                FilterableJurorDetailsResponseDto responseDto = Objects.requireNonNull(responseEntity.getBody())[0];
                assertThat(responseDto.getJurorNumber()).isEqualTo(TestConstants.VALID_JUROR_NUMBER);
                assertThat(responseDto.getJurorVersion()).isEqualTo(2L);

                assertThat(responseDto.getNameDetails()).isNotNull();
                assertThat(responseDto.getPaymentDetails()).isNotNull();
                assertThat(responseDto.getAddress()).isNotNull();


                NameDetails nameDetails = responseDto.getNameDetails();
                assertThat(nameDetails.getTitle()).isEqualTo("Mr2");
                assertThat(nameDetails.getFirstName()).isEqualTo("FNAME2");
                assertThat(nameDetails.getLastName()).isEqualTo("LNAME2");

                PaymentDetails paymentDetails = responseDto.getPaymentDetails();
                assertThat(paymentDetails.getSortCode()).isNull();
                assertThat(paymentDetails.getBankAccountName()).isNull();
                assertThat(paymentDetails.getBankAccountNumber()).isNull();
                assertThat(paymentDetails.getBuildingSocietyRollNumber()).isEqualTo("Roll Number2");

                JurorAddressDto jurorAddressDto = responseDto.getAddress();
                assertThat(jurorAddressDto.getLineOne()).isEqualTo("2Address Line 1");
                assertThat(jurorAddressDto.getLineTwo()).isEqualTo("2Address Line 2");
                assertThat(jurorAddressDto.getLineThree()).isEqualTo("2Address Line 3");
                assertThat(jurorAddressDto.getTown()).isEqualTo("2Address Line 4");
                assertThat(jurorAddressDto.getCounty()).isEqualTo("2Address Line 5");
                assertThat(jurorAddressDto.getPostcode()).isEqualTo("CH2 2AN");
            }

            @Test
            void getMultiple() {
                ResponseEntity<FilterableJurorDetailsResponseDto[]> responseEntity = triggerValid(
                    List.of(
                        createDto(TestConstants.VALID_JUROR_NUMBER, null,
                            FilterableJurorDetailsRequestDto.IncludeType.NAME_DETAILS,
                            FilterableJurorDetailsRequestDto.IncludeType.PAYMENT_DETAILS,
                            FilterableJurorDetailsRequestDto.IncludeType.ADDRESS_DETAILS),
                        createDto("223456789", null,
                            FilterableJurorDetailsRequestDto.IncludeType.NAME_DETAILS),
                        createDto("323456789", null,
                            FilterableJurorDetailsRequestDto.IncludeType.NAME_DETAILS,
                            FilterableJurorDetailsRequestDto.IncludeType.ADDRESS_DETAILS)
                    )
                );
                //Count validated in triggerValid so no need to do here

                //Validate Person 1
                FilterableJurorDetailsResponseDto responseDto1 = Objects.requireNonNull(responseEntity.getBody())[0];
                assertThat(responseDto1.getJurorNumber()).isEqualTo(TestConstants.VALID_JUROR_NUMBER);
                assertThat(responseDto1.getJurorVersion()).isNull();

                assertThat(responseDto1.getNameDetails()).isNotNull();
                assertThat(responseDto1.getPaymentDetails()).isNotNull();
                assertThat(responseDto1.getAddress()).isNotNull();


                NameDetails nameDetails1 = responseDto1.getNameDetails();
                assertThat(nameDetails1.getTitle()).isEqualTo("Mr");
                assertThat(nameDetails1.getFirstName()).isEqualTo("FNAME");
                assertThat(nameDetails1.getLastName()).isEqualTo("LNAME");

                PaymentDetails paymentDetails1 = responseDto1.getPaymentDetails();
                assertThat(paymentDetails1.getSortCode()).isEqualTo("112233");
                assertThat(paymentDetails1.getBankAccountName()).isEqualTo("Bank NAME");
                assertThat(paymentDetails1.getBankAccountNumber()).isEqualTo("12345678");
                assertThat(paymentDetails1.getBuildingSocietyRollNumber()).isNull();

                JurorAddressDto jurorAddressDto1 = responseDto1.getAddress();
                assertThat(jurorAddressDto1.getLineOne()).isEqualTo("Address Line 1");
                assertThat(jurorAddressDto1.getLineTwo()).isEqualTo("Address Line 2");
                assertThat(jurorAddressDto1.getLineThree()).isEqualTo("Address Line 3");
                assertThat(jurorAddressDto1.getTown()).isEqualTo("Address Line 4");
                assertThat(jurorAddressDto1.getCounty()).isEqualTo("Address Line 5");
                assertThat(jurorAddressDto1.getPostcode()).isEqualTo("CH1 2AN");

                //Validate Person 2
                FilterableJurorDetailsResponseDto responseDto2 = Objects.requireNonNull(responseEntity.getBody())[1];
                assertThat(responseDto2.getJurorNumber()).isEqualTo("223456789");
                assertThat(responseDto2.getJurorVersion()).isNull();

                assertThat(responseDto2.getNameDetails()).isNotNull();
                assertThat(responseDto2.getPaymentDetails()).isNull();
                assertThat(responseDto2.getAddress()).isNull();


                NameDetails nameDetails2 = responseDto2.getNameDetails();
                assertThat(nameDetails2.getTitle()).isEqualTo("Miss");
                assertThat(nameDetails2.getFirstName()).isEqualTo("FNAME2");
                assertThat(nameDetails2.getLastName()).isEqualTo("LNAME2");

                //Validate Person 3
                FilterableJurorDetailsResponseDto responseDto3 = Objects.requireNonNull(responseEntity.getBody())[2];
                assertThat(responseDto3.getJurorNumber()).isEqualTo("323456789");
                assertThat(responseDto3.getJurorVersion()).isNull();

                assertThat(responseDto3.getNameDetails()).isNotNull();
                assertThat(responseDto3.getPaymentDetails()).isNull();
                assertThat(responseDto3.getAddress()).isNotNull();


                NameDetails nameDetails3 = responseDto3.getNameDetails();
                assertThat(nameDetails3.getTitle()).isEqualTo("Dr");
                assertThat(nameDetails3.getFirstName()).isEqualTo("John");
                assertThat(nameDetails3.getLastName()).isEqualTo("Joe");

                JurorAddressDto jurorAddressDto3 = responseDto3.getAddress();
                assertThat(jurorAddressDto3.getLineOne()).isEqualTo("Road 1");
                assertThat(jurorAddressDto3.getLineTwo()).isEqualTo("Unknown");
                assertThat(jurorAddressDto3.getLineThree()).isEqualTo("Person");
                assertThat(jurorAddressDto3.getTown()).isEqualTo("Street");
                assertThat(jurorAddressDto3.getCounty()).isEqualTo("Country123");
                assertThat(jurorAddressDto3.getPostcode()).isEqualTo("BH2 4AN");
            }
        }

        @DisplayName("Negative")
        @Nested
        class Negative {

            private ResponseEntity<String> triggerInvalid(FilterableJurorDetailsRequestDto request) {
                return triggerInvalid(List.of(request));
            }

            private ResponseEntity<String> triggerInvalid(
                List<FilterableJurorDetailsRequestDto> dto) {
                setAuthorization("COURT_USER", "415", UserType.COURT);
                return restTemplate.exchange(
                    new RequestEntity<>(dto, httpHeaders, POST, URI.create(URL)),
                    String.class);
            }

            @Test
            void jurorNotFound() throws Exception {
                FilterableJurorDetailsRequestDto request = createDto("023456789", null,
                    FilterableJurorDetailsRequestDto.IncludeType.NAME_DETAILS);
                assertNotFound(triggerInvalid(request), URL,
                    "Juror not found: JurorNumber: 023456789 Revision: null");
            }

            @Test
            void jurorVersionNotFound() throws Exception {
                FilterableJurorDetailsRequestDto request = createDto(TestConstants.VALID_JUROR_NUMBER, 9L,
                    FilterableJurorDetailsRequestDto.IncludeType.NAME_DETAILS);
                assertNotFound(triggerInvalid(request), URL,
                    "Juror not found: JurorNumber: " + TestConstants.VALID_JUROR_NUMBER + " Revision: 9");
            }

            @Test
            void invalidPayload() throws Exception {
                FilterableJurorDetailsRequestDto request = createDto("INVALID", 9L,
                    FilterableJurorDetailsRequestDto.IncludeType.NAME_DETAILS);

                assertInvalidPathParam(triggerInvalid(request),
                    "getJurorDetailsBulkFilterable.request[0].jurorNumber: must match \"^\\d{9}$\"");
            }

            @Test
            @DisplayName("Unauthorised - none court user")
            void unauthorisedNoneCourtUser() {
                List<FilterableJurorDetailsRequestDto> request =
                    List.of(createDto(TestConstants.VALID_JUROR_NUMBER, null,
                        FilterableJurorDetailsRequestDto.IncludeType.PAYMENT_DETAILS));
                setAuthorization("BUREAU_USER", "400", UserType.BUREAU, Role.MANAGER);

                assertForbiddenResponse(restTemplate.exchange(
                    new RequestEntity<>(request, httpHeaders, POST, URI.create(URL)), String.class), URL);
            }

            @Test
            @DisplayName("Empty request body")
            void emptyRequest() throws Exception {
                assertInvalidPathParam(triggerInvalid(List.of()),
                    "getJurorDetailsBulkFilterable.request: size must be between 1 and 20");
            }

            @Test
            @DisplayName("Request body with null")
            void requestWithNullItem() throws Exception {
                List<FilterableJurorDetailsRequestDto> request = new ArrayList<>();
                request.add(null);
                assertInvalidPathParam(triggerInvalid(request),
                    "getJurorDetailsBulkFilterable.request[0].<list element>: must not be null");
            }

            @Test
            @DisplayName("Request has too many items")
            void tooManyItems() throws Exception {
                List<FilterableJurorDetailsRequestDto> request = new ArrayList<>();
                final int maxRequests = 20;
                for (int index = 0;
                     index < maxRequests + 1;
                     index++) {
                    request.add(FilterableJurorDetailsRequestDto.builder()
                        .jurorNumber(String.valueOf(100_000_000 + index))
                        .include(List.of(FilterableJurorDetailsRequestDto.IncludeType.ADDRESS_DETAILS))
                        .build());
                }

                assertInvalidPathParam(triggerInvalid(request),
                    "getJurorDetailsBulkFilterable.request: size must be between 1 and 20");
            }
        }

    }

    @Nested
    @DisplayName("POST " + CreateJurorRecord.URL)
    class CreateJurorRecord {

        private static final String URL = BASE_URL + "/create-juror";

        @Test
        @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_createJurorRecord.sql"})
        void createJurorRecordExistingPoolHappyPath() throws Exception {
            String poolNumber = "415220502";
            JurorCreateRequestDto requestDto = createJurorRequestDto(poolNumber, "415");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("415", Collections.singletonList("415"),
                UserType.COURT));

            ResponseEntity<?> response =
                restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, POST,
                    URI.create(URL)), String.class);

            assertThat(response.getStatusCode())
                .as("Expect the HTTP POST request to be CREATED")
                .isEqualTo(HttpStatus.CREATED);


            List<PoolRequest> pools = poolRequestRepository.findAll();

            assertEquals(1, pools.size(), "Should only be one juror pool");
            PoolRequest pool = pools.get(0);
            assertEquals(LocalDateTime.of(2023, 11, 29, 9, 0, 0),
                pool.getLastUpdate(), "Last updated should not change as pool should not be updated");

            validatePendingJuror(requestDto, poolNumber, pool.getReturnDate());
        }

        @Test
        @Sql({"/db/mod/truncate.sql"})
        void createJurorRecordNewPoolHappyPath() throws Exception {
            JurorCreateRequestDto requestDto = createJurorRequestDto(null, "415");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("415", Collections.singletonList("415"),
                UserType.COURT));

            ResponseEntity<?> response =
                restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, POST,
                    URI.create(URL)), String.class);

            assertThat(response.getStatusCode())
                .as("Expect the HTTP POST request to be CREATED")
                .isEqualTo(HttpStatus.CREATED);

            List<PoolRequest> pools = poolRequestRepository.findAll();

            assertEquals(1, pools.size(), "Should only be one pools (The newly created one)");
            PoolRequest pool = pools.get(0);
            validatePoolCreation(requestDto, pool, "415");
            validatePendingJuror(requestDto, pool.getPoolNumber(), pool.getReturnDate());
        }

        @Test
        @Sql({"/db/mod/truncate.sql"})
        void negativeInvalidPayload() throws Exception {
            JurorCreateRequestDto requestDto = createJurorRequestDto(null, "415");
            requestDto.setFirstName(null);
            httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("415", Collections.singletonList("415"),
                UserType.COURT));

            ResponseEntity<?> response =
                restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, POST,
                    URI.create(URL)), String.class);

            assertThat(response.getStatusCode())
                .as("Expect the HTTP POST request to be BAD_REQUEST")
                .isEqualTo(HttpStatus.BAD_REQUEST);
            List<PendingJuror> pendingJurors = pendingJurorRepository.findAll();
            assertEquals(0, pendingJurors.size(), "Should be no pending jurors");
        }

        @Test
        @Sql({"/db/mod/truncate.sql"})
        void negativeBureauUser() throws Exception {
            JurorCreateRequestDto requestDto = createJurorRequestDto(null, "415");
            httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("400", Collections.singletonList("415"),
                UserType.BUREAU));

            ResponseEntity<?> response =
                restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, POST,
                    URI.create(URL)), String.class);

            assertThat(response.getStatusCode())
                .as("Expect the HTTP POST request to be FORBIDDEN")
                .isEqualTo(HttpStatus.FORBIDDEN);
            List<PendingJuror> pendingJurors = pendingJurorRepository.findAll();
            assertEquals(0, pendingJurors.size(), "Should be no pending jurors");
        }

        @Test
        @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_createJurorRecord.sql"})
        void negativeOwnersDoNotMatch() throws Exception {
            String poolNumber = "415220502";
            JurorCreateRequestDto requestDto = createJurorRequestDto(poolNumber, "415");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("416", Collections.singletonList("415"),
                UserType.COURT));

            ResponseEntity<?> response =
                restTemplate.exchange(new RequestEntity<>(requestDto, httpHeaders, POST,
                    URI.create(URL)), String.class);

            assertThat(response.getStatusCode())
                .as("Expect the HTTP POST request to be FORBIDDEN")
                .isEqualTo(HttpStatus.FORBIDDEN);

            List<PendingJuror> pendingJurors = pendingJurorRepository.findAll();
            assertEquals(0, pendingJurors.size(), "Should be no pending jurors");
        }

        @Test
        @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_updateAttendance.sql"})
        void updateAttendanceHappyPathPlaceOnCall() throws Exception {
            final String url = BASE_URL + "/update-attendance";

            UpdateAttendanceRequestDto dto = new UpdateAttendanceRequestDto();
            dto.setOnCall(true);
            dto.setJurorNumber("121212121");
            dto.setNextDate(null);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("415", Collections.singletonList("415"),
                UserType.COURT));

            ResponseEntity<?> response =
                restTemplate.exchange(new RequestEntity<>(dto, httpHeaders, HttpMethod.PATCH,
                    URI.create(url + "?juror_number=121212121")), String.class);

            assertThat(response.getStatusCode())
                .as("Expect the HTTP POST request to be ACCEPTED")
                .isEqualTo(HttpStatus.ACCEPTED);
        }

        @Test
        @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_updateAttendance.sql"})
        void updateAttendanceHappyPathUpdateNextDate() throws Exception {
            final String url = BASE_URL + "/update-attendance";

            UpdateAttendanceRequestDto dto = new UpdateAttendanceRequestDto();
            dto.setOnCall(false);
            dto.setJurorNumber("121212121");
            dto.setNextDate(LocalDate.now().plusWeeks(4));

            httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("415", Collections.singletonList("415"),
                UserType.COURT));

            ResponseEntity<?> response =
                restTemplate.exchange(new RequestEntity<>(dto, httpHeaders, HttpMethod.PATCH,
                    URI.create(url + "?juror_number=121212121")), String.class);

            assertThat(response.getStatusCode())
                .as("Expect the HTTP POST request to be ACCEPTED")
                .isEqualTo(HttpStatus.ACCEPTED);
        }

        @Test
        @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_updateAttendance.sql"})
        void updateAttendanceNoJurorPoolWithJurorNumber() throws Exception {
            final String url = BASE_URL + "/update-attendance";

            UpdateAttendanceRequestDto dto = new UpdateAttendanceRequestDto();
            dto.setOnCall(true);
            dto.setJurorNumber("111111111");
            dto.setNextDate(null);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("415", Collections.singletonList("415"),
                UserType.COURT));

            ResponseEntity<?> response =
                restTemplate.exchange(new RequestEntity<>(dto, httpHeaders, HttpMethod.PATCH,
                    URI.create(url + "?juror_number=111111111")), String.class);

            assertThat(response.getStatusCode())
                .as("Expect the HTTP POST request to be NOT_FOUND")
                .isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_updateAttendance.sql"})
        void updateAttendanceAlreadyOnCall() throws Exception {
            final String url = BASE_URL + "/update-attendance";

            UpdateAttendanceRequestDto dto = new UpdateAttendanceRequestDto();
            dto.setOnCall(true);
            dto.setJurorNumber("641600096");
            dto.setNextDate(null);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("415", Collections.singletonList("415"),
                UserType.COURT));

            ResponseEntity<?> response =
                restTemplate.exchange(new RequestEntity<>(dto, httpHeaders, HttpMethod.PATCH,
                    URI.create(url + "?juror_number=641600096")), String.class);

            assertThat(response.getStatusCode())
                .as("Expect the HTTP POST request to be NOT_FOUND")
                .isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_updateAttendance.sql"})
        void updateAttendanceInvalidAccess() {
            final String url = BASE_URL + "/update-attendance";
            setAuthorization("BUREAU_USER", "400", UserType.BUREAU);

            UpdateAttendanceRequestDto dto = new UpdateAttendanceRequestDto();
            dto.setOnCall(true);
            dto.setJurorNumber("641600096");
            dto.setNextDate(null);

            ResponseEntity<?> response =
                restTemplate.exchange(new RequestEntity<>(dto, httpHeaders, HttpMethod.PATCH,
                    URI.create(url + "?juror_number=641600096")), String.class);

            assertThat(response.getStatusCode())
                .as("Expect the HTTP POST request to be NOT_FOUND")
                .isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_editBankDetails.sql"})
        void editJurorsBankDetails() throws Exception {
            final String url = BASE_URL + "/update-bank-details";
            String jurorNumber = "123456789";

            RequestBankDetailsDto dto = new RequestBankDetailsDto();
            dto.setJurorNumber(jurorNumber);
            dto.setAccountNumber("12345678");
            dto.setSortCode("112233");
            dto.setAccountHolderName("Mr Fname Lname");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("415", Collections.singletonList("415"),
                UserType.COURT));

            ResponseEntity<Void> response =
                restTemplate.exchange(new RequestEntity<>(dto, httpHeaders, HttpMethod.PATCH,
                    URI.create(url)), Void.class);

            assertThat(response.getStatusCode())
                .as("Expect the HTTP POST request to be OK")
                .isEqualTo(HttpStatus.OK);

            Juror juror = jurorRepository.findByJurorNumber(jurorNumber);

            List<JurorHistory> historyList = jurorHistoryRepository.findByJurorNumber(jurorNumber);

            assertThat(juror.getSortCode()).isEqualTo(dto.getSortCode());
            assertThat(juror.getBankAccountName()).isEqualTo(dto.getAccountHolderName());
            assertThat(juror.getBankAccountNumber()).isEqualTo(dto.getAccountNumber());

            assertThat(historyList.get(0).getJurorNumber()).isEqualTo(jurorNumber);
            assertThat(historyList.get(0).getHistoryCode()).isEqualTo(HistoryCodeMod.CHANGE_PERSONAL_DETAILS);
            assertThat(historyList.get(0).getOtherInformation()).isEqualTo("Bank Account Name Changed");

            assertThat(historyList.get(1).getJurorNumber()).isEqualTo(jurorNumber);
            assertThat(historyList.get(1).getHistoryCode()).isEqualTo(HistoryCodeMod.CHANGE_PERSONAL_DETAILS);
            assertThat(historyList.get(1).getOtherInformation()).isEqualTo("Bank Acct No Changed");

            assertThat(historyList.get(2).getJurorNumber()).isEqualTo(jurorNumber);
            assertThat(historyList.get(2).getHistoryCode()).isEqualTo(HistoryCodeMod.CHANGE_PERSONAL_DETAILS);
            assertThat(historyList.get(2).getOtherInformation()).isEqualTo("Bank Sort Code Changed");

        }

        @Test
        @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_editBankDetails.sql"})
        void editJurorsBankDetailsJurorNumberNotFound() throws Exception {
            final String url = BASE_URL + "/update-bank-details";
            String jurorNumber = "987654321";

            RequestBankDetailsDto dto = new RequestBankDetailsDto();
            dto.setJurorNumber(jurorNumber);
            dto.setAccountNumber("12345678");
            dto.setSortCode("112233");
            dto.setAccountHolderName("Mr Fname Lname");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("415", Collections.singletonList("415"),
                UserType.COURT));

            ResponseEntity<Void> response =
                restTemplate.exchange(new RequestEntity<>(dto, httpHeaders, HttpMethod.PATCH,
                    URI.create(url)), Void.class);

            assertThat(response.getStatusCode())
                .as("Expect the HTTP POST request to be NOT_FOUND")
                .isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_editBankDetails.sql"})
        void editJurorsBankDetailsInvalidAccess() throws Exception {
            final String url = BASE_URL + "/update-bank-details";
            String jurorNumber = "987654321";

            RequestBankDetailsDto dto = new RequestBankDetailsDto();
            dto.setJurorNumber(jurorNumber);
            dto.setAccountNumber("12345678");
            dto.setSortCode("112233");
            dto.setAccountHolderName("Mr Fname Lname");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("400", Collections.singletonList("415"),
                UserType.BUREAU));

            ResponseEntity<Void> response =
                restTemplate.exchange(new RequestEntity<>(dto, httpHeaders, HttpMethod.PATCH,
                    URI.create(url)), Void.class);

            assertThat(response.getStatusCode())
                .as("Expect the HTTP POST request to be FORBIDDEN")
                .isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_editBankDetails.sql"})
        void editJurorsBankDetailsInvalidBankAccNo() throws Exception {
            final String url = BASE_URL + "/update-bank-details";
            String jurorNumber = "123456789";

            RequestBankDetailsDto dto = new RequestBankDetailsDto();
            dto.setJurorNumber(jurorNumber);
            dto.setAccountNumber("123456789");
            dto.setSortCode("112233");
            dto.setAccountHolderName("Mr Fname Lname");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("415", Collections.singletonList("415"),
                UserType.COURT));

            ResponseEntity<Void> response =
                restTemplate.exchange(new RequestEntity<>(dto, httpHeaders, HttpMethod.PATCH,
                    URI.create(url)), Void.class);

            assertThat(response.getStatusCode())
                .as("Expect the HTTP POST request to be BAD_REQUEST")
                .isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_editBankDetails.sql"})
        void editJurorsBankDetailsInvalidBankAccSortCode() throws Exception {
            final String url = BASE_URL + "/update-bank-details";
            String jurorNumber = "123456789";

            RequestBankDetailsDto dto = new RequestBankDetailsDto();
            dto.setJurorNumber(jurorNumber);
            dto.setAccountNumber("12345678");
            dto.setSortCode("11223344");
            dto.setAccountHolderName("Mr Fname Lname");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("415", Collections.singletonList("415"),
                UserType.COURT));

            ResponseEntity<Void> response =
                restTemplate.exchange(new RequestEntity<>(dto, httpHeaders, HttpMethod.PATCH,
                    URI.create(url)), Void.class);

            assertThat(response.getStatusCode())
                .as("Expect the HTTP POST request to be BAD_REQUEST")
                .isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_editBankDetails.sql"})
        void editJurorsBankDetailsInvalidBankAccName() throws Exception {
            final String url = BASE_URL + "/update-bank-details";
            String jurorNumber = "123456789";

            RequestBankDetailsDto dto = new RequestBankDetailsDto();
            dto.setJurorNumber(jurorNumber);
            dto.setAccountNumber("12345678");
            dto.setSortCode("112233");
            dto.setAccountHolderName("Mr Fname Lname Too Long");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("415", Collections.singletonList("415"),
                UserType.COURT));

            ResponseEntity<Void> response =
                restTemplate.exchange(new RequestEntity<>(dto, httpHeaders, HttpMethod.PATCH,
                    URI.create(url)), Void.class);

            assertThat(response.getStatusCode())
                .as("Expect the HTTP POST request to be BAD_REQUEST")
                .isEqualTo(HttpStatus.BAD_REQUEST);
        }


        private void validatePendingJuror(JurorCreateRequestDto dto, String poolNumber, LocalDate returnDate) {
            List<PendingJuror> pendingJurors = pendingJurorRepository.findAll();
            assertEquals(1, pendingJurors.size(), "Should be one pending jurors");
            PendingJuror pendingJuror = pendingJurors.get(0);

            assertNotNull(pendingJuror.getJurorNumber(), "Juror number must be set");
            assertEquals(poolNumber, pendingJuror.getPoolNumber(), "Pool number must match");
            assertEquals(dto.getTitle(), pendingJuror.getTitle(), "Title must match");
            assertEquals(dto.getFirstName(), pendingJuror.getFirstName(), "First name must match");
            assertEquals(dto.getLastName(), pendingJuror.getLastName(), "Last name must match");
            assertEquals(dto.getDateOfBirth(), pendingJuror.getDateOfBirth(), "Date of birth must match");

            //Validate address
            JurorAddressDto expected = dto.getAddress();
            assertEquals(expected.getLineOne(), pendingJuror.getAddressLine1(), "Address line one must match");
            assertEquals(expected.getLineTwo(), pendingJuror.getAddressLine2(), "Address line two must match");
            assertEquals(expected.getLineThree(), pendingJuror.getAddressLine3(), "Address line three must match");
            assertEquals(expected.getTown(), pendingJuror.getAddressLine4(), "Address town must match");
            assertEquals(expected.getCounty(), pendingJuror.getAddressLine5(), "Address county must match");
            assertEquals(expected.getPostcode(), pendingJuror.getPostcode(), "Address postcode must match");
            assertEquals(dto.getEmailAddress(), pendingJuror.getEmail(), "Email address must match");
            assertEquals(dto.getNotes(), pendingJuror.getNotes(), "Notes must match");
            assertEquals('Q', pendingJuror.getStatus().getCode(), "Status must match");
            assertEquals(returnDate, pendingJuror.getNextDate(), "Next day must match");
            assertTrue(pendingJuror.isResponded(), "Responded must be true");
        }

        private void validatePoolCreation(JurorCreateRequestDto dto, PoolRequest poolRequest, String owner) {
            final CourtLocation courtLocation = courtLocationRepository.findByLocCode(dto.getLocationCode()).get();

            assertNotNull(poolRequest.getPoolNumber(), "Pool number must be created");
            assertEquals(owner, poolRequest.getOwner(), "Owner must match");
            assertEquals(dto.getLocationCode(), poolRequest.getCourtLocation().getLocCode(),
                "CourtLocation must match");
            assertEquals('N', poolRequest.getNewRequest(), "new Request must match");
            assertEquals(dto.getStartDate(), poolRequest.getReturnDate(), "Return date must match");
            assertNull(poolRequest.getNumberRequested(), "Number requested must be null");

            assertEquals(LocalDateTime.of(dto.getStartDate(),
                    courtLocation.getCourtAttendTime()),
                poolRequest.getAttendTime(), "Attend Time must match");

            assertEquals(dto.getPoolType(), poolRequest.getPoolType().getPoolType(),
                "Pool type must match");

            List<PoolHistory> poolHistories = poolHistoryRepository.findAll();
            assertEquals(1, poolHistories.size(), "Should be one pool history");
            PoolHistory poolHistory = poolHistories.get(0);
            assertEquals(poolRequest.getPoolNumber(), poolHistory.getPoolNumber(), "Pool number must match");
            assertEquals("COURT_USER", poolHistory.getUserId(), "User id must match");
            assertEquals(HistoryCode.PREQ, poolHistory.getHistoryCode(), "History code must match");
            assertEquals(String.format("Pool Request %s created for pending Juror",
                    poolRequest.getPoolNumber()),
                poolHistory.getOtherInformation(), "Info must match");
        }

        private JurorCreateRequestDto createJurorRequestDto(String poolNumber, String locationCode) {
            JurorCreateRequestDto requestDto = new JurorCreateRequestDto();
            requestDto.setTitle("Mr");
            requestDto.setFirstName("John");
            requestDto.setLastName("Smith");

            JurorAddressDto addressDto = JurorAddressDto.builder()
                .lineOne("1 High Street")
                .lineTwo("Test")
                .lineThree("Test")
                .town("Test")
                .county("Test")
                .postcode("TE1 1ST")
                .build();
            requestDto.setAddress(addressDto);
            requestDto.setDateOfBirth(LocalDate.of(1990, 1, 1));
            requestDto.setPrimaryPhone("01234567890");
            requestDto.setEmailAddress("test@mail.com");
            requestDto.setLocationCode(locationCode);

            if (poolNumber == null) {
                requestDto.setStartDate(LocalDate.now().plusDays(10));
                requestDto.setPoolType("CRO");
            } else {
                requestDto.setPoolNumber(poolNumber);
            }

            return requestDto;
        }
    }

    @Nested
    @DisplayName("Confirm Juror Identity")
    class ConfirmJurorIdentity {

        @Test
        @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_InitConfirmJurorIdentity.sql"})
        void confirmJurorIdentityHappyPath() throws Exception {
            final String url = BASE_URL + "/confirm-identity";
            String jurorNumber = "111111111";

            ConfirmIdentityDto dto = ConfirmIdentityDto.builder()
                .jurorNumber(jurorNumber)
                .idCheckCode(IdCheckCodeEnum.C)
                .build();

            httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("415", Collections.singletonList("415"),
                UserType.COURT));

            ResponseEntity<Void> response =
                restTemplate.exchange(new RequestEntity<>(dto, httpHeaders, HttpMethod.PATCH,
                    URI.create(url)), Void.class);

            assertThat(response.getStatusCode())
                .as("Expect the HTTP POST request to be OK")
                .isEqualTo(HttpStatus.OK);

            JurorPool jurorPool = jurorPoolRepository.findByJurorJurorNumber(jurorNumber);

            assertThat(jurorPool.getIdChecked()).isEqualTo('C');

        }

        @Test
        void confirmJurorIdentityBureauNoAccess() throws Exception {
            final String url = BASE_URL + "/confirm-identity";
            String jurorNumber = "111111111";

            ConfirmIdentityDto dto = ConfirmIdentityDto.builder()
                .jurorNumber(jurorNumber)
                .idCheckCode(IdCheckCodeEnum.C)
                .build();

            ResponseEntity<Void> response =
                restTemplate.exchange(new RequestEntity<>(dto, httpHeaders, HttpMethod.PATCH,
                    URI.create(url)), Void.class);

            assertThat(response.getStatusCode())
                .as("Expect the HTTP POST request to be FORBIDDEN")
                .isEqualTo(HttpStatus.FORBIDDEN);
        }

    }

    @Nested
    @DisplayName("Search for Juror records")
    @Sql({"/db/mod/truncate.sql", "/db/JurorRecordController_searchForJurorRecords.sql"})
    class SearchForJurorRecords {

        private static final String URL = BASE_URL + "/search";

        @Test
        void searchForJurorRecordsBureauHappyPath() throws Exception {
            JurorRecordFilterRequestQuery request = JurorRecordFilterRequestQuery.builder()
                .jurorNumber("641600091")
                .pageNumber(1)
                .pageLimit(10)
                .sortMethod(SortMethod.ASC)
                .sortField(JurorRecordFilterRequestQuery.SortField.JUROR_NUMBER)
                .build();

            ResponseEntity<PaginatedList<FilterJurorRecord>> response =
                restTemplate.exchange(new RequestEntity<>(request, httpHeaders, POST,
                    URI.create(URL)), new ParameterizedTypeReference<>() {});

            assertThat(response.getStatusCode())
                .as("Expect the HTTP POST request (GET With Body) to be successful")
                .isEqualTo(HttpStatus.OK);

            assertThat(response.getBody()).isNotNull();
            PaginatedList<FilterJurorRecord> responseBody = response.getBody();

            validateSearchResult(responseBody);

        }

        @Test
        void searchForJurorRecordsCourtHappyPath() throws Exception {

            String bureauJwt = createBureauJwt("Court_User", "416", "416");
            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
            JurorRecordFilterRequestQuery request = JurorRecordFilterRequestQuery.builder()
                .jurorNumber("641600091")
                .pageNumber(1)
                .pageLimit(10)
                .sortMethod(SortMethod.ASC)
                .sortField(JurorRecordFilterRequestQuery.SortField.JUROR_NUMBER)
                .build();

            ResponseEntity<PaginatedList<FilterJurorRecord>> response =
                restTemplate.exchange(new RequestEntity<>(request, httpHeaders, POST,
                    URI.create(URL)), new ParameterizedTypeReference<>() {});

            assertThat(response.getStatusCode())
                .as("Expect the HTTP POST request (GET With Body) to be successful")
                .isEqualTo(HttpStatus.OK);

            assertThat(response.getBody()).isNotNull();
            PaginatedList<FilterJurorRecord> responseBody = response.getBody();

            validateSearchResult(responseBody);

        }

        @Test
        void searchForJurorRecordsCourtUserBureauOwnedJuror() throws Exception {

            String bureauJwt = createBureauJwt("Court_User", "416", "416");
            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
            JurorRecordFilterRequestQuery request = JurorRecordFilterRequestQuery.builder()
                .jurorNumber("641500101")
                .pageNumber(1)
                .pageLimit(10)
                .sortMethod(SortMethod.ASC)
                .sortField(JurorRecordFilterRequestQuery.SortField.JUROR_NUMBER)
                .build();

            ResponseEntity<PaginatedList<FilterJurorRecord>> response =
                restTemplate.exchange(new RequestEntity<>(request, httpHeaders, POST,
                    URI.create(URL)), new ParameterizedTypeReference<>() {});

            assertThat(response.getStatusCode())
                .as("Expect the HTTP POST request (GET With Body) to be NO CONTENT")
                .isEqualTo(HttpStatus.NO_CONTENT);

        }

        @Test
        void searchForJurorRecordsBureauByPool() throws Exception {
            JurorRecordFilterRequestQuery request = JurorRecordFilterRequestQuery.builder()
                .poolNumber("415220901")
                .pageNumber(2)
                .pageLimit(6)
                .sortMethod(SortMethod.ASC)
                .sortField(JurorRecordFilterRequestQuery.SortField.JUROR_NUMBER)
                .build();

            ResponseEntity<PaginatedList<FilterJurorRecord>> response =
                restTemplate.exchange(new RequestEntity<>(request, httpHeaders, POST,
                    URI.create(URL)), new ParameterizedTypeReference<>() {});

            assertThat(response.getStatusCode())
                .as("Expect the HTTP POST request (GET With Body) to be successful")
                .isEqualTo(HttpStatus.OK);

            assertThat(response.getBody()).isNotNull();
            PaginatedList<FilterJurorRecord> responseBody = response.getBody();

            validateSearchResultByPool(responseBody);

        }


        @Test
        void searchForJurorRecordsCourtSorted() throws Exception {

            String bureauJwt = createBureauJwt("Court_User", "415", "415");
            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
            JurorRecordFilterRequestQuery request = JurorRecordFilterRequestQuery.builder()
                .poolNumber("415220901")
                .pageNumber(1)
                .pageLimit(10)
                .sortMethod(SortMethod.DESC)
                .sortField(JurorRecordFilterRequestQuery.SortField.JUROR_NUMBER)
                .build();

            ResponseEntity<PaginatedList<FilterJurorRecord>> response =
                restTemplate.exchange(new RequestEntity<>(request, httpHeaders, POST,
                    URI.create(URL)), new ParameterizedTypeReference<>() {});

            assertThat(response.getStatusCode())
                .as("Expect the HTTP POST request (GET With Body) to be successful")
                .isEqualTo(HttpStatus.OK);

            assertThat(response.getBody()).isNotNull();
            PaginatedList<FilterJurorRecord> responseBody = response.getBody();

            validateSearchResultSortedDesc(responseBody);

        }

        @Test
        void searchForJurorRecordsByName() throws Exception {

            String bureauJwt = createBureauJwt("Court_User", "415", "415");
            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
            JurorRecordFilterRequestQuery request = JurorRecordFilterRequestQuery.builder()
                .jurorName("Lnameninefive")
                .pageNumber(1)
                .pageLimit(10)
                .sortMethod(SortMethod.DESC)
                .sortField(JurorRecordFilterRequestQuery.SortField.JUROR_NUMBER)
                .build();

            ResponseEntity<PaginatedList<FilterJurorRecord>> response =
                restTemplate.exchange(new RequestEntity<>(request, httpHeaders, POST,
                    URI.create(URL)), new ParameterizedTypeReference<>() {});

            assertThat(response.getStatusCode())
                .as("Expect the HTTP POST request (GET With Body) to be successful")
                .isEqualTo(HttpStatus.OK);

            assertThat(response.getBody()).isNotNull();
            PaginatedList<FilterJurorRecord> responseBody = response.getBody();

            validateSearchResultByName(responseBody);

        }

        @Test
        void searchForJurorRecordsCourtNoResult() throws Exception {

            String bureauJwt = createBureauJwt("Court_User", "415", "415");
            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
            JurorRecordFilterRequestQuery request = JurorRecordFilterRequestQuery.builder()
                .jurorNumber("641600091")
                .pageNumber(1)
                .pageLimit(10)
                .sortMethod(SortMethod.ASC)
                .sortField(JurorRecordFilterRequestQuery.SortField.JUROR_NUMBER)
                .build();

            ResponseEntity<PaginatedList<FilterJurorRecord>> response =
                restTemplate.exchange(new RequestEntity<>(request, httpHeaders, POST,
                    URI.create(URL)), new ParameterizedTypeReference<>() {});

            assertThat(response.getStatusCode())
                .as("Expect the HTTP POST request (GET With Body) to be NO CONTENT")
                .isEqualTo(HttpStatus.NO_CONTENT);

        }

        private void validateSearchResult(PaginatedList<FilterJurorRecord> responseBody) {
            assertThat(responseBody.getTotalItems()).as("Expect the response body to contain a total count value of 1")
                .isEqualTo(1);
            List<FilterJurorRecord> data = responseBody.getData();
            assertThat(data.size()).as("Expect the response body to contain all 1 data items").isEqualTo(1);
            FilterJurorRecord juror = data.get(0);
            assertThat(juror.getJurorNumber()).as("Expect the response body to contain the correct juror number")
                .isEqualTo("641600091");
            assertThat(juror.getJurorName()).as("Expect the response body to contain the correct juror name")
                .isEqualTo("FNAMEFIVEFOURZERO LNAMEFIVEFOURZERO");
            assertThat(juror.getPostcode()).as("Expect the response body to contain the correct postcode")
                .isEqualTo("CH1 2AN");
            assertThat(juror.getPoolNumber()).as("Expect the response body to contain the correct pool number")
                .isEqualTo("416220902");
            assertThat(juror.getCourtName()).as("Expect the response body to contain the correct court name")
                .isEqualTo("LEWES SITTING AT CHICHESTER");
            assertThat(juror.getStatus()).as("Expect the response body to contain the correct status")
                .isEqualTo("Responded");
        }

        private void validateSearchResultByPool(PaginatedList<FilterJurorRecord> responseBody) {
            assertThat(responseBody.getTotalItems()).as("Expect the response body to contain a total count "
                    + "value of 10").isEqualTo(10);
            assertThat(responseBody.getTotalPages()).as("Expect the response body to contain a total page value of 2")
                .isEqualTo(2);
            assertThat(responseBody.getCurrentPage()).as("Expect the response body to contain a current page value of"
                    + " 2")
                .isEqualTo(2);
            List<FilterJurorRecord> data = responseBody.getData();
            assertThat(data.size()).as("Expect the response body to contain all 4 data items").isEqualTo(4);
            // validate the first juror in the response
            FilterJurorRecord juror = data.get(0);
            assertThat(juror.getJurorNumber()).as("Expect the response body to contain the correct juror number")
                .isEqualTo("641500097");
            assertThat(juror.getJurorName()).as("Expect the response body to contain the correct juror name")
                .isEqualTo("Fnamenineseven Lnamenineseven");
            assertThat(juror.getPostcode()).as("Expect the response body to contain the correct postcode")
                .isEqualTo("CH1 2AN");
            assertThat(juror.getPoolNumber()).as("Expect the response body to contain the correct pool number")
                .isEqualTo("415220901");
            assertThat(juror.getCourtName()).as("Expect the response body to contain the correct court name")
                .isEqualTo("CHESTER");
            assertThat(juror.getStatus()).as("Expect the response body to contain the correct status")
                .isEqualTo("Responded");
        }

        private void validateSearchResultSortedDesc(PaginatedList<FilterJurorRecord> responseBody) {
            assertThat(responseBody.getTotalItems()).as("Expect the response body to contain a total count value "
                    + "of 10").isEqualTo(10);
            assertThat(responseBody.getTotalPages()).as("Expect the response body to contain a total page value of 1")
                .isEqualTo(1);
            assertThat(responseBody.getCurrentPage()).as("Expect the response body to contain a current page value of"
                    + " 1")
                .isEqualTo(1);
            List<FilterJurorRecord> data = responseBody.getData();
            assertThat(data.size()).as("Expect the response body to contain all 10 data items").isEqualTo(10);
            // validate the first juror in the response
            FilterJurorRecord juror = data.get(0);
            assertThat(juror.getJurorNumber()).as("Expect the response body to contain the correct juror number")
                .isEqualTo("641500100");
            assertThat(juror.getJurorName()).as("Expect the response body to contain the correct juror name")
                .isEqualTo("Fnamenineten Lnamenineten");
            assertThat(juror.getPostcode()).as("Expect the response body to contain the correct postcode")
                .isEqualTo("CH1 2AN");
            assertThat(juror.getPoolNumber()).as("Expect the response body to contain the correct pool number")
                .isEqualTo("415220901");
            assertThat(juror.getCourtName()).as("Expect the response body to contain the correct court name")
                .isEqualTo("CHESTER");
            assertThat(juror.getStatus()).as("Expect the response body to contain the correct status")
                .isEqualTo("Responded");
        }

        private void validateSearchResultByName(PaginatedList<FilterJurorRecord> responseBody) {
            assertThat(responseBody.getTotalItems()).as("Expect the response body to contain a total count value of 1")
                .isEqualTo(1);
            assertThat(responseBody.getTotalPages()).as("Expect the response body to contain a total page value of 1")
                .isEqualTo(1);
            assertThat(responseBody.getCurrentPage()).as("Expect the response body to contain a current page value of"
                    + " 1")
                .isEqualTo(1);
            List<FilterJurorRecord> data = responseBody.getData();
            assertThat(data.size()).as("Expect the response body to contain all 1 data items").isEqualTo(1);
            FilterJurorRecord juror = data.get(0);
            assertThat(juror.getJurorNumber()).as("Expect the response body to contain the correct juror number")
                .isEqualTo("641500095");
            assertThat(juror.getJurorName()).as("Expect the response body to contain the correct juror name")
                .isEqualTo("Fnameninefive Lnameninefive");
            assertThat(juror.getPostcode()).as("Expect the response body to contain the correct postcode")
                .isEqualTo("CH1 2AN");
            assertThat(juror.getPoolNumber()).as("Expect the response body to contain the correct pool number")
                .isEqualTo("415220901");
            assertThat(juror.getCourtName()).as("Expect the response body to contain the correct court name")
                .isEqualTo("CHESTER");
            assertThat(juror.getStatus()).as("Expect the response body to contain the correct status")
                .isEqualTo("Responded");
        }

    }

    private void verifyBulkPrintData(String jurorNumber, String formCode) {
        List<BulkPrintData> bulkPrintData = bulkPrintDataRepository.findByJurorNo(jurorNumber);
        assertThat(bulkPrintData).hasSize(1);
        assertThat(bulkPrintData.get(0).getFormAttribute().getFormType()).isEqualTo(formCode);
    }

    private void verifyNoBulkPrintData(String jurorNumber) {
        List<BulkPrintData> bulkPrintData = bulkPrintDataRepository.findByJurorNo(jurorNumber);
        assertThat(bulkPrintData).isEmpty();
    }

    private void verifyStandardJurorHistory(JurorPool jurorPool,
                                            List<JurorHistory> jurorHistoryList,
                                            JurorHistoryExpectedValues... expectedValues) {
        Iterator<JurorHistory> jurorHistoryValues = jurorHistoryList.iterator();
        for (JurorHistoryExpectedValues expectedValue : expectedValues) {
            JurorHistory jurorHistory = jurorHistoryValues.next();
            assertEquals(jurorPool.getPoolNumber(), jurorHistory.getPoolNumber(),
                "Pool Number must match");
            assertEquals(jurorPool.getJuror().getJurorNumber(), jurorHistory.getJurorNumber(),
                "Juror Number must match");
            assertEquals(jurorPool.getPoolNumber(), jurorHistory.getPoolNumber(),
                "Pool Number must match");
            assertEquals("SYSTEM", jurorHistory.getCreatedBy(),
                "User Id must match");
            assertEquals(expectedValue.historyCode, jurorHistory.getHistoryCode().getCode(),
                "History Code must match");
            assertEquals(expectedValue.info, jurorHistory.getOtherInformation(),
                "Info must match");
            assertThat(jurorHistory.getDateCreated())
                .as("Date Part must match")
                .isEqualToIgnoringHours(LocalDateTime.now());
        }
    }

    private record JurorHistoryExpectedValues(String historyCode, String info) {
    }

    private String convertBooleanToYesNo(Boolean bool) {
        if (bool.equals(Boolean.TRUE)) {
            return "Y";
        } else if (bool.equals(Boolean.FALSE)) {
            return "N";
        }

        return null;
    }

    private String initPayloadWithStaffRank(String owner, String username, UserType userType,
                                            Role... roles) throws Exception {
        return mintBureauJwt(BureauJwtPayload.builder()
            .userType(userType)
            .roles(List.of(roles))
            .login(username)
            .owner(owner)
            .staff(BureauJwtPayload.Staff.builder().build())
            .build());
    }

    private List<String> initChangedHistoryProperties() {
        List<String> historyInfoList = new ArrayList<>();
        historyInfoList.add("Title Changed");
        historyInfoList.add("First Name Changed");
        historyInfoList.add("Last Name Changed");
        return historyInfoList;
    }

    private EditJurorRecordRequestDto createEditJurorRecordRequestDto(boolean allFieldsRequired) {
        EditJurorRecordRequestDto editJurorRecordRequestDto = new EditJurorRecordRequestDto();

        editJurorRecordRequestDto.setFirstName("NewFirstName");
        editJurorRecordRequestDto.setLastName("NewLastName");
        editJurorRecordRequestDto.setAddressLineOne("addressLineOne");
        editJurorRecordRequestDto.setAddressPostcode("M24 4BP");
        editJurorRecordRequestDto.setAddressTown("addressTown");

        if (allFieldsRequired) {
            editJurorRecordRequestDto.setTitle("Mr");
            editJurorRecordRequestDto.setAddressLineTwo("addressLineTwo");
            editJurorRecordRequestDto.setAddressLineThree("addressLineThree");
            editJurorRecordRequestDto.setAddressCounty("addressCounty");
            editJurorRecordRequestDto.setDateOfBirth(LocalDate.parse("2022-02-01"));
            editJurorRecordRequestDto.setPrimaryPhone("071234566790");
            editJurorRecordRequestDto.setSecondaryPhone(null);
            editJurorRecordRequestDto.setEmailAddress("someEmail@exampleEmail.co.uk");
            editJurorRecordRequestDto.setSpecialNeed("M");
            editJurorRecordRequestDto.setSpecialNeedMessage("Multiple");
            editJurorRecordRequestDto.setOpticReference("22222222");
            editJurorRecordRequestDto.setPendingTitle("Mx");
            editJurorRecordRequestDto.setPendingFirstName("Pending First Name");
            editJurorRecordRequestDto.setPendingLastName("Pending Last Name");
            editJurorRecordRequestDto.setWelshLanguageRequired(true);
        }

        return editJurorRecordRequestDto;
    }


    @SneakyThrows
    private void setAuthorization(String login, String owner, UserType userType, Role... roles) {
        httpHeaders.remove(HttpHeaders.AUTHORIZATION);
        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintBureauJwt(BureauJwtPayload.builder()
            .userType(userType)
            .roles(List.of(roles))
            .login(login)
            .owner(owner)
            .build()));
    }

    private JurorOpticRefRequestDto createOpticRefRequestDto(String jurorNumber, String poolNumber,
                                                             String opticReference) {

        JurorOpticRefRequestDto requestDto = new JurorOpticRefRequestDto();

        requestDto.setJurorNumber(jurorNumber);
        requestDto.setPoolNumber(poolNumber);
        requestDto.setOpticReference(opticReference);

        return requestDto;
    }

}
