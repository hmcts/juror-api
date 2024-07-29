package uk.gov.hmcts.juror.api.moj.service.deferralmaintenance;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.controller.request.DeferralAllocateRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.DeferralDatesRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.DeferralReasonRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.deferralmaintenance.ProcessJurorPostponementRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.DeferralListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.DeferralOptionsDto;
import uk.gov.hmcts.juror.api.moj.controller.response.deferralmaintenance.DeferralResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.CurrentlyDeferred;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.PoliceCheck;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.Role;
import uk.gov.hmcts.juror.api.moj.domain.UserType;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.JurorResponseAuditMod;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.PaperResponse;
import uk.gov.hmcts.juror.api.moj.enumeration.PoolUtilisationDescription;
import uk.gov.hmcts.juror.api.moj.enumeration.ReplyMethod;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.CurrentlyDeferredRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorStatusRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorPaperResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorResponseAuditRepositoryMod;
import uk.gov.hmcts.juror.api.moj.service.AssignOnUpdateServiceMod;
import uk.gov.hmcts.juror.api.moj.service.JurorHistoryService;
import uk.gov.hmcts.juror.api.moj.service.PoolMemberSequenceService;
import uk.gov.hmcts.juror.api.moj.service.PrintDataService;
import uk.gov.hmcts.juror.api.moj.service.SummonsReplyMergeService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.juror.api.moj.domain.CurrentlyDeferredQueries.filterByCourtAndDate;
import static uk.gov.hmcts.juror.api.moj.service.deferralmaintenance.ManageDeferralsServiceTestData.createActivePoolsForDeferralsFirstDate;
import static uk.gov.hmcts.juror.api.moj.service.deferralmaintenance.ManageDeferralsServiceTestData.createActivePoolsForDeferralsSecondDate;
import static uk.gov.hmcts.juror.api.moj.service.deferralmaintenance.ManageDeferralsServiceTestData.createActivePoolsForDeferralsThirdDate;
import static uk.gov.hmcts.juror.api.moj.service.deferralmaintenance.ManageDeferralsServiceTestData.createJurorPoolForDeferrals;
import static uk.gov.hmcts.juror.api.moj.service.deferralmaintenance.ManageDeferralsServiceTestData.createJurorResponseForDeferrals;
import static uk.gov.hmcts.juror.api.moj.service.deferralmaintenance.ManageDeferralsServiceTestData.createJurorResponseWithoutDeferrals;

@ExtendWith(SpringExtension.class)
@SuppressWarnings({"PMD.TooManyMethods", "PMD.ExcessiveImports"})
class ManageDeferralsServiceTest {

    private static final String BUREAU_OWNER = "400";
    private static final String BUREAU_USER = "BUREAU_USER";
    private static final String LOC_CODE_415 = "415";
    private static final String JUROR_123456789 = "123456789";
    private static final String JUROR_111111111 = "111111111";
    private static final String POOL_111111111 = "111111111";
    private static final String POOL_111111112 = "111111112";

    @Mock
    private PoolRequestRepository poolRequestRepository;
    @Mock
    private JurorRepository jurorRepository;
    @Mock
    private JurorPoolRepository jurorPoolRepository;
    @Mock
    private CurrentlyDeferredRepository currentlyDeferredRepository;
    @Mock
    private PoolHistoryRepository poolHistoryRepository;
    @Mock
    private JurorHistoryRepository jurorHistoryRepository;
    @Mock
    private PoolMemberSequenceService poolMemberSequenceService;
    @Mock
    private JurorStatusRepository jurorStatusRepository;
    @Mock
    private JurorDigitalResponseRepositoryMod digitalResponseRepository;
    @Mock
    private JurorPaperResponseRepositoryMod paperResponseRepository;
    @Mock
    private JurorResponseAuditRepositoryMod auditRepository;
    @Mock
    private AssignOnUpdateServiceMod assignOnUpdateService;
    @Mock
    private SummonsReplyMergeService mergeService;
    @Mock
    private PrintDataService printDataService;
    @Mock
    private JurorHistoryService jurorHistoryService;
    @Mock
    private WelshCourtLocationRepository welshCourtLocationRepository;

    @InjectMocks
    ManageDeferralsServiceImpl manageDeferralsService;

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    public void setUp() {
        final Logger logger = (Logger) LoggerFactory.getLogger(ManageDeferralsServiceImpl.class);

        doReturn(Optional.of(createJurorStatus(2, "RESPONDED")))
            .when(jurorStatusRepository).findById(2);
        doReturn(Optional.of(createJurorStatus(7, "DEFERRED")))
            .when(jurorStatusRepository).findById(7);

        listAppender = new ListAppender<>();
        listAppender.start();

        logger.addAppender(listAppender);
    }

    @DisplayName("Process juror postponement")
    @Nested
    class ProcessJurorPostponement {

        @Test
        @SuppressWarnings({"PMD.TooManyFields"})
        void processJurorPostponementHappyPathMoveToActivePoolPoliceChecked() {
            LocalDate newAttendanceDate = LocalDate.now();
            LocalDate oldAttendanceDate = LocalDate.of(2023, 6, 6);

            final BureauJwtPayload bureauPayload = TestUtils.createJwt(BUREAU_OWNER, BUREAU_USER,
                UserType.BUREAU, Collections.singletonList(Role.MANAGER));

            final PoolRequest oldPoolRequest = createPoolRequest(BUREAU_OWNER, POOL_111111111, LOC_CODE_415,
                oldAttendanceDate);

            final PoolRequest newPoolRequest = createPoolRequest(BUREAU_OWNER, POOL_111111112, LOC_CODE_415,
                newAttendanceDate);

            JurorStatus jurorStatus = new JurorStatus();
            jurorStatus.setStatus(IJurorStatus.RESPONDED);

            List<JurorPool> jurorPools = createJurorPoolMember(JUROR_123456789);
            doReturn(jurorPools).when(jurorPoolRepository)
                .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(JUROR_123456789, true);

            doReturn(Optional.of(oldPoolRequest)).when(poolRequestRepository).findByPoolNumber(POOL_111111111);
            doReturn(Optional.of(jurorStatus)).when(jurorStatusRepository).findById(anyInt());
            doReturn(1).when(poolMemberSequenceService).getPoolMemberSequenceNumber(any());

            doReturn(Optional.of(newPoolRequest)).when(poolRequestRepository).findByPoolNumber(anyString());
            doNothing().when(printDataService).printPostponeLetter(any());

            DeferralResponseDto response =
                manageDeferralsService.processJurorPostponement(bureauPayload, createProcessJurorRequestDto());

            assertThat(response.getCountJurorsPostponed()).isEqualTo(1);

            verify(jurorPoolRepository, times(1))
                .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(JUROR_123456789, true);
            verify(jurorPoolRepository, times(2)).saveAndFlush(any());
            verify(jurorPoolRepository, times(2)).save(any());
            verify(jurorHistoryRepository, times(2)).save(any());
            verify(jurorHistoryService).createPostponementLetterHistory(jurorPools.get(0),"");
            verify(poolRequestRepository, times(1)).findByPoolNumber(POOL_111111111);
            verify(poolRequestRepository, times(1)).findByPoolNumber(POOL_111111112);
            verify(poolMemberSequenceService, times(1))
                .getPoolMemberSequenceNumber(any(String.class));
            verify(poolRequestRepository, times(1)).save(any());
            verify(poolRequestRepository, times(1)).saveAndFlush(any());
            verify(poolMemberSequenceService, times(1)).leftPadInteger(any(int.class));
            verify(printDataService, times(1)).printConfirmationLetter(any());
            verify(printDataService, times(1)).printPostponeLetter(any());
            verify(jurorHistoryService, times(1)).createConfirmationLetterHistory(any(), anyString());
            verify(currentlyDeferredRepository, times(0)).save(any());
        }

        @Test
        @SuppressWarnings({"PMD.TooManyFields"})
        void processJurorPostponementHappyPathMoveToActivePoolNotPoliceChecked() {
            LocalDate newAttendanceDate = LocalDate.now();
            LocalDate oldAttendanceDate = LocalDate.of(2023, 6, 6);

            final BureauJwtPayload bureauPayload = TestUtils.createJwt(BUREAU_OWNER, BUREAU_USER,
                UserType.BUREAU, Collections.singletonList(Role.MANAGER));

            final PoolRequest oldPoolRequest = createPoolRequest(BUREAU_OWNER, POOL_111111111, LOC_CODE_415,
                oldAttendanceDate);

            final PoolRequest newPoolRequest = createPoolRequest(BUREAU_OWNER, POOL_111111112, LOC_CODE_415,
                newAttendanceDate);

            JurorStatus jurorStatus = new JurorStatus();
            jurorStatus.setStatus(IJurorStatus.RESPONDED);
            List<JurorPool> jurorPool = createJurorPoolMember(JUROR_123456789);
            jurorPool.get(0).getJuror().setPoliceCheck(PoliceCheck.NOT_CHECKED);

            doReturn(jurorPool).when(jurorPoolRepository)
                .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(JUROR_123456789, true);

            doReturn(Optional.of(oldPoolRequest)).when(poolRequestRepository).findByPoolNumber(POOL_111111111);
            doReturn(Optional.of(jurorStatus)).when(jurorStatusRepository).findById(anyInt());
            doReturn(1).when(poolMemberSequenceService).getPoolMemberSequenceNumber(any());
            doReturn(Optional.of(newPoolRequest)).when(poolRequestRepository).findByPoolNumber(anyString());

            DeferralResponseDto response =
                manageDeferralsService.processJurorPostponement(bureauPayload, createProcessJurorRequestDto());

            assertThat(response.getCountJurorsPostponed()).isEqualTo(1);

            verify(jurorPoolRepository, times(1))
                .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(JUROR_123456789, true);
            verify(jurorPoolRepository, times(2)).saveAndFlush(any());
            verify(jurorPoolRepository, times(2)).save(any());
            verify(jurorHistoryRepository, times(2)).save(any());
            verify(jurorHistoryService).createPostponementLetterHistory(jurorPool.get(0),"");
            verify(poolRequestRepository, times(1)).findByPoolNumber(POOL_111111111);
            verify(poolRequestRepository, times(1)).findByPoolNumber(POOL_111111112);
            verify(poolMemberSequenceService, times(1))
                .getPoolMemberSequenceNumber(any(String.class));
            verify(poolRequestRepository, times(1)).save(any());
            verify(poolRequestRepository, times(1)).saveAndFlush(any());
            verify(poolMemberSequenceService, times(1)).leftPadInteger(any(int.class));
            verify(printDataService, times(1)).printPostponeLetter(any());
            verify(printDataService, never()).printConfirmationLetter(any());
            verify(jurorHistoryService, never()).createConfirmationLetterHistory(any(), anyString());
            verify(currentlyDeferredRepository, never()).save(any());
        }

        @Test
        void processJurorPostponementHappyPathMoveToActivePoolMultipleJurors() {
            LocalDate newAttendanceDate = LocalDate.now();
            LocalDate oldAttendanceDate = LocalDate.of(2023, 6, 6);

            final BureauJwtPayload bureauPayload = TestUtils.createJwt(BUREAU_OWNER, BUREAU_USER,
                UserType.BUREAU, Collections.singletonList(Role.MANAGER));

            final PoolRequest oldPoolRequest = createPoolRequest(BUREAU_OWNER, POOL_111111111, LOC_CODE_415,
                oldAttendanceDate);

            final PoolRequest newPoolRequest = createPoolRequest(BUREAU_OWNER, POOL_111111112, LOC_CODE_415,
                newAttendanceDate);

            JurorStatus jurorStatus = new JurorStatus();
            jurorStatus.setStatus(IJurorStatus.RESPONDED);

            List<JurorPool> jurorPools1 = createJurorPoolMember(JUROR_123456789);
            doReturn(jurorPools1).when(jurorPoolRepository)
                .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(JUROR_123456789, true);


            List<JurorPool> jurorPools2 = createJurorPoolMember(JUROR_111111111);
            doReturn(jurorPools2).when(jurorPoolRepository)
                .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(JUROR_111111111, true);

            doReturn(Optional.of(oldPoolRequest)).when(poolRequestRepository).findByPoolNumber(POOL_111111111);
            doReturn(Optional.of(jurorStatus)).when(jurorStatusRepository).findById(anyInt());
            doReturn(1).when(poolMemberSequenceService).getPoolMemberSequenceNumber(any());

            doReturn(Optional.of(newPoolRequest)).when(poolRequestRepository).findByPoolNumber(anyString());

            ProcessJurorPostponementRequestDto request = createProcessJurorRequestDto();
            List<String> jurorNumbers = new ArrayList<>();
            jurorNumbers.add(JUROR_123456789);
            jurorNumbers.add(JUROR_111111111);
            request.setJurorNumbers(jurorNumbers);

            DeferralResponseDto response = manageDeferralsService.processJurorPostponement(bureauPayload, request);

            assertThat(response.getCountJurorsPostponed()).isEqualTo(2);

            verify(jurorPoolRepository, times(2))
                .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), anyBoolean());
            verify(jurorPoolRepository, times(4)).saveAndFlush(any());
            verify(jurorPoolRepository, times(4)).save(any());
            verify(jurorHistoryRepository, times(4)).save(any());
            verify(jurorHistoryService).createPostponementLetterHistory(jurorPools1.get(0),"");
            verify(jurorHistoryService).createPostponementLetterHistory(jurorPools2.get(0),"");
            verify(poolRequestRepository, times(4)).findByPoolNumber(anyString());
            verify(poolMemberSequenceService, times(2))
                .getPoolMemberSequenceNumber(any(String.class));
            verify(poolRequestRepository, times(2)).save(any());
            verify(poolRequestRepository, times(2)).saveAndFlush(any());
            verify(poolMemberSequenceService, times(2)).leftPadInteger(any(int.class));
            verify(printDataService, times(2)).printConfirmationLetter(any());
            verify(printDataService, times(2)).printPostponeLetter(any());
            verify(currentlyDeferredRepository, never()).save(any());
        }

        @Test
        void processJurorPostponementUnhappyPathInvalidReasonCode() {
            final BureauJwtPayload bureauPayload = TestUtils.createJwt(BUREAU_OWNER, BUREAU_USER);

            doReturn(createJurorPoolMember(JUROR_123456789)).when(jurorPoolRepository)
                .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(JUROR_123456789, true);

            ProcessJurorPostponementRequestDto request = createProcessJurorRequestDto();
            request.setExcusalReasonCode("C"); // setting an invalid reason code, should be "P"

            assertThatExceptionOfType(MojException.BadRequest.class).isThrownBy(() ->
                manageDeferralsService.processJurorPostponement(bureauPayload, request));

            verify(jurorPoolRepository, times(1))
                .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), anyBoolean());

            // make sure no letters are sent or deferral records created
            verify(printDataService, never()).printConfirmationLetter(any());
            verify(printDataService, never()).printPostponeLetter(any());
            verify(currentlyDeferredRepository, never()).save(any());
        }

        @Test
        void processJurorPostponementUnhappyPathJurorNumberNotFound() {
            final BureauJwtPayload bureauPayload = TestUtils.createJwt(BUREAU_OWNER, BUREAU_USER);
            DeferralReasonRequestDto dto = new DeferralReasonRequestDto();
            dto.setPoolNumber(POOL_111111111);
            dto.setExcusalReasonCode("P");

            List<JurorPool> poolMembers = new ArrayList<>();

            doReturn(poolMembers).when(jurorPoolRepository)
                .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(JUROR_123456789, true);

            assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(() ->
                manageDeferralsService.processJurorPostponement(bureauPayload, createProcessJurorRequestDto()));

            verify(jurorPoolRepository, times(1))
                .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), anyBoolean());

            // make sure no letters are sent or deferral records created
            verify(printDataService, never()).printConfirmationLetter(any());
            verify(printDataService, never()).printPostponeLetter(any());
            verify(currentlyDeferredRepository, times(0)).save(any());
        }

        @Test
        void processJurorPostponementUnhappyPathPoolNumberNotFound() {
            final BureauJwtPayload bureauPayload = TestUtils.createJwt(BUREAU_OWNER, BUREAU_USER);

            doReturn(createJurorPoolMember(JUROR_123456789)).when(jurorPoolRepository)
                .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(JUROR_123456789, true);
            doReturn(Optional.empty()).when(poolRequestRepository).findByPoolNumber(POOL_111111112);

            assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(() ->
                manageDeferralsService.processJurorPostponement(bureauPayload, createProcessJurorRequestDto()));

            verify(jurorPoolRepository, times(1))
                .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), anyBoolean());

            verify(poolRequestRepository, times(1)).findByPoolNumber(anyString());

            // make sure no letters are sent or deferral records created
            verify(printDataService, never()).printConfirmationLetter(any());
            verify(printDataService, never()).printPostponeLetter(any());
            verify(currentlyDeferredRepository, times(0)).save(any());
        }

        @Test
        void processJurorPostponementHappyPathMoveToCurrentlyDeferred() {
            final BureauJwtPayload bureauPayload = TestUtils.createJwt(BUREAU_OWNER, BUREAU_USER,
                UserType.BUREAU, Collections.singletonList(Role.MANAGER));

            List<JurorPool> jurorPools = createJurorPoolMember(JUROR_123456789);
            doReturn(jurorPools).when(jurorPoolRepository)
                .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(JUROR_123456789, true);

            DeferralResponseDto response = manageDeferralsService.processJurorPostponement(bureauPayload,
                createProcessJurorRequestDtoToCurrentlyDeferred());

            assertThat(response.getCountJurorsPostponed()).isEqualTo(1);

            verify(jurorPoolRepository, times(0)).saveAndFlush(any());
            verify(jurorPoolRepository, times(2)).save(any());
            verify(jurorHistoryRepository, times(1)).save(any());
            verify(jurorHistoryService).createPostponementLetterHistory(jurorPools.get(0),"");
            verify(poolRequestRepository, times(0)).findByPoolNumber(anyString());
            verify(poolMemberSequenceService, times(0))
                .getPoolMemberSequenceNumber(any(String.class));
            verify(jurorStatusRepository, times(1)).findById(any());
            verify(poolRequestRepository, times(0)).save(any());
            verify(poolRequestRepository, times(0)).saveAndFlush(any());
            verify(poolMemberSequenceService, times(0)).leftPadInteger(any(int.class));
            verify(printDataService, times(1)).printPostponeLetter(any());
            verify(printDataService, never()).printConfirmationLetter(any());
        }

        @Test
        void processJurorPostponementUnhappyPathPostponeToExistingPoolNumber() {
            final BureauJwtPayload bureauPayload = TestUtils.createJwt(BUREAU_OWNER, BUREAU_USER);

            ProcessJurorPostponementRequestDto request = new ProcessJurorPostponementRequestDto();
            request.setJurorNumbers(Collections.singletonList(JUROR_123456789));
            request.setExcusalReasonCode("P");
            request.setPoolNumber(POOL_111111111);

            doReturn(createJurorPoolMember(JUROR_123456789)).when(jurorPoolRepository)
                .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(JUROR_123456789, true);
            doReturn(Optional.empty()).when(poolRequestRepository).findByPoolNumber(POOL_111111111);

            MojException.BadRequest exception =
                assertThrows(MojException.BadRequest.class, () ->
                        manageDeferralsService.processJurorPostponement(bureauPayload, request),
                    "Should throw an exception");
            assertEquals("Cannot postpone to the same pool", exception.getMessage(),
                "Message should match");

            verify(jurorPoolRepository, times(1))
                .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), anyBoolean());

            // make sure no letters are sent or deferral records created
            verify(printDataService, never()).printConfirmationLetter(any());
            verify(printDataService, never()).printPostponeLetter(any());
            verify(currentlyDeferredRepository, times(0)).save(any());
        }

        private List<JurorPool> createJurorPoolMember(String jurorNumber) {
            List<JurorPool> poolMembers = new ArrayList<>();
            JurorPool member1 = createJurorPool(jurorNumber);
            poolMembers.add(member1);

            return poolMembers;
        }

        private ProcessJurorPostponementRequestDto createProcessJurorRequestDto() {
            ProcessJurorPostponementRequestDto request = new ProcessJurorPostponementRequestDto();
            request.setJurorNumbers(Collections.singletonList(JUROR_123456789));
            request.setExcusalReasonCode("P");
            request.setPoolNumber("111111112");
            request.setDeferralDate(LocalDate.of(2023, 8, 12));
            return request;
        }

        private ProcessJurorPostponementRequestDto createProcessJurorRequestDtoToCurrentlyDeferred() {
            ProcessJurorPostponementRequestDto request = new ProcessJurorPostponementRequestDto();
            request.setJurorNumbers(Collections.singletonList(JUROR_123456789));
            request.setExcusalReasonCode("P");
            request.setDeferralDate(LocalDate.of(2023, 8, 12));
            return request;
        }
    }

    private PoolRequest createPoolRequest(String poolNumber, String locationCode, LocalDate returnDate) {
        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCode(locationCode);
        courtLocation.setOwner(locationCode);

        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber(poolNumber);
        poolRequest.setCourtLocation(courtLocation);
        poolRequest.setReturnDate(returnDate);

        return poolRequest;
    }

    private PoolRequest createPoolRequest(String owner, String poolNumber, String locationCode, LocalDate returnDate) {
        PoolRequest poolRequest = createPoolRequest(poolNumber, locationCode, returnDate);
        poolRequest.setOwner(owner);

        return poolRequest;
    }

    @Test
    void deleteDeferralHappyPathBureauUser() {
        final ArgumentCaptor<JurorPool> jurorPoolArgumentCaptor = ArgumentCaptor.forClass(JurorPool.class);

        final BureauJwtPayload bureauPayload = TestUtils.createJwt("400", "BUREAU_USER");

        String jurorNumber = "123456789";
        String poolNumber = "987654321";
        CurrentlyDeferred deferredRecord = createDeferredRecord("400", jurorNumber);

        when(jurorPoolRepository.findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(
            any(),
            anyBoolean()
        ))
            .thenReturn(createDeferredJurorPools("400"));

        PoolRequest poolRequest = createPoolRequest(poolNumber, "415", LocalDate.now());

        doReturn(Optional.of(poolRequest)).when(poolRequestRepository)
            .findByPoolNumber(poolNumber);

        when(currentlyDeferredRepository.findById(any())).thenReturn(Optional.of(deferredRecord));

        doNothing().when(currentlyDeferredRepository).delete(any());

        manageDeferralsService.deleteDeferral(bureauPayload, jurorNumber);

        verify(jurorPoolRepository, times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), anyBoolean());
        verify(currentlyDeferredRepository, times(1)).findById(any());
        verify(jurorPoolRepository, times(1)).save(jurorPoolArgumentCaptor.capture());

        JurorPool jurorPool = jurorPoolArgumentCaptor.getValue();
        Juror juror = jurorPool.getJuror();

        assertThat(jurorPool.getDeferralDate()).isNull();
        assertThat(juror.getExcusalDate()).isNull();
        assertThat(jurorPool.getDeferralCode()).isNull();
        assertThat(jurorPool.getNextDate()).isNotNull();
        assertThat(juror.getNoDefPos()).isEqualTo(0L);
        assertThat(jurorPool.getStatus().getStatus()).isEqualTo(IJurorStatus.RESPONDED);
        assertThat(jurorPool.getUserEdtq()).isEqualTo(bureauPayload.getLogin());
    }

    @Test
    void deleteDeferralDeferralNotFound() {
        final BureauJwtPayload bureauPayload = TestUtils.createJwt("400", "BUREAU_USER");

        String jurorNumber = "123456789";
        String poolNumber = "987654321";

        when(jurorPoolRepository.findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(jurorNumber, true))
            .thenReturn(createDeferredJurorPools("400"));

        PoolRequest poolRequest = createPoolRequest(poolNumber, "415", LocalDate.now());

        doReturn(Optional.of(poolRequest)).when(poolRequestRepository)
            .findByPoolNumber(poolNumber);

        when(currentlyDeferredRepository.findById(any())).thenReturn(Optional.empty());

        assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(() -> {
            manageDeferralsService.deleteDeferral(bureauPayload, jurorNumber);
        });

        verify(jurorPoolRepository, times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), anyBoolean());
        verify(currentlyDeferredRepository, times(1)).findById(any());
        verify(jurorPoolRepository, never()).save(any());
        verify(currentlyDeferredRepository, never()).delete(any());
    }

    private CurrentlyDeferred createDeferredRecord(String owner, String jurorNumber) {
        CurrentlyDeferred deferredRecord = new CurrentlyDeferred();

        deferredRecord.setOwner(owner);
        deferredRecord.setJurorNumber(jurorNumber);
        deferredRecord.setDeferredTo(LocalDate.of(2023, 12, 4));
        deferredRecord.setLocCode("415");

        return deferredRecord;
    }

    @Test
    void useDeferralsNoDeferralsUsed() {
        PoolRequest poolRequest = createPoolRequest("123456789", "123", LocalDate.now());
        doReturn(new ArrayList<CurrentlyDeferred>()).when(currentlyDeferredRepository)
            .findAll((Predicate) any());

        int deferralsUsed = manageDeferralsService.useCourtDeferrals(poolRequest,
            0, "SOME_USER");
        assertThat(deferralsUsed).as("No deferrals requested, expect 0 to be used").isEqualTo(0);

        verify(jurorPoolRepository, never())
            .findByJurorJurorNumberAndOwnerAndDeferralDateAndIsActiveTrue(
                any(), any(), any());
        verify(jurorPoolRepository, never()).saveAndFlush(any());
        verify(poolRequestRepository, never()).save(any());
        verify(poolRequestRepository, never()).saveAndFlush(any());
        verify(poolHistoryRepository, never()).save(any());
        verify(jurorHistoryRepository, never()).save(any());
        verify(printDataService, never()).printConfirmationLetter(any());
    }

    private JurorStatus createJurorStatus(int status, String description) {
        JurorStatus jurorStatus = new JurorStatus();
        jurorStatus.setStatus(status);
        jurorStatus.setStatusDesc(description);
        jurorStatus.setActive(true);
        return jurorStatus;
    }

    @Test
    void useCourtDeferralsDeferralsUsedHappyPath() {
        LocalDate oldAttendanceDate = LocalDate.of(2022, 6, 6);
        String courtLocation = "415";
        LocalDate newAttendanceDate = LocalDate.now();

        final PoolRequest newPoolRequest = createPoolRequest("123456789", courtLocation, newAttendanceDate);
        PoolRequest oldPoolRequest = createPoolRequest("987654321", courtLocation, oldAttendanceDate);
        doReturn(Optional.of(oldPoolRequest)).when(poolRequestRepository)
            .findByPoolNumber("987654321");

        List<CurrentlyDeferred> courtDeferrals = new ArrayList<>();
        courtDeferrals.add(CurrentlyDeferred.builder().owner(courtLocation).build());
        courtDeferrals.add(CurrentlyDeferred.builder().owner(courtLocation).build());

        doReturn(courtDeferrals).when(currentlyDeferredRepository).findAll(filterByCourtAndDate(
            courtLocation,
            courtLocation,
            newAttendanceDate));

        JurorPool deferredJuror = createDeferredJuror(courtLocation);
        doReturn(Optional.of(deferredJuror)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndOwnerAndDeferralDateAndIsActiveTrue(
                any(), any(), any());
        doReturn(Optional.of(createJurorStatus(2, "Responded"))).when(jurorStatusRepository)
            .findById(2);
        doReturn(1).when(poolMemberSequenceService).getPoolMemberSequenceNumber(any());
        doReturn(null).when(jurorPoolRepository).saveAndFlush(any());
        doReturn(null).when(poolRequestRepository).save(any());
        doReturn(null).when(poolRequestRepository).saveAndFlush(any());
        doReturn(null).when(poolHistoryRepository).save(any());
        doReturn(null).when(jurorHistoryRepository).save(any());

        int deferralsUsed = courtDeferrals.size();
        int actualDeferralsUsed = manageDeferralsService.useCourtDeferrals(newPoolRequest,
            deferralsUsed, "SOME_USER");

        assertThat(actualDeferralsUsed)
            .as("For the happy path, expect all requested deferrals to be used")
            .isEqualTo(deferralsUsed);
        assertThat(deferredJuror.getIsActive())
            .as("Expect the old, deferred juror record to be updated to inactive")
            .isEqualTo(false);

        verify(jurorPoolRepository, times(deferralsUsed))
            .findByJurorJurorNumberAndOwnerAndDeferralDateAndIsActiveTrue(any(), any(), any());
        verify(jurorPoolRepository, times(deferralsUsed * 2))
            .saveAndFlush(any());
        verify(poolRequestRepository, times(deferralsUsed)).save(any());
        verify(poolRequestRepository, times(deferralsUsed)).saveAndFlush(any());
        verify(poolHistoryRepository, never()).save(any());
        verify(jurorHistoryRepository, times(deferralsUsed)).save(any());
    }

    @Test
    @SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
    void useCourtDeferralsDeferralsUsedNoJurorPool() {
        String courtLocation = "415";
        LocalDate newAttendanceDate = LocalDate.now();
        LocalDate oldAttendanceDate = LocalDate.of(2022, 6, 6);

        final PoolRequest newPoolRequest = createPoolRequest("123456789", courtLocation, newAttendanceDate);
        PoolRequest oldPoolRequest = createPoolRequest("987654321", courtLocation, oldAttendanceDate);
        doReturn(Optional.of(oldPoolRequest)).when(poolRequestRepository)
            .findByPoolNumber("987654321");

        List<CurrentlyDeferred> courtDeferrals = new ArrayList<>();
        courtDeferrals.add(
            CurrentlyDeferred.builder().jurorNumber("111111111").owner(courtLocation).deferredTo(newAttendanceDate)
                .build());
        courtDeferrals.add(
            CurrentlyDeferred.builder().jurorNumber("222222222").owner(courtLocation).deferredTo(newAttendanceDate)
                .build());
        doReturn(courtDeferrals).when(currentlyDeferredRepository).findAll(filterByCourtAndDate(
            courtLocation,
            courtLocation,
            newAttendanceDate));
        doReturn(Optional.of(createJurorStatus(2, "Responded"))).when(jurorStatusRepository)
            .findById(2);
        doReturn(1).when(poolMemberSequenceService).getPoolMemberSequenceNumber(any());

        JurorPool deferredJuror = createDeferredJuror(courtLocation);
        doReturn(Optional.of(deferredJuror)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndOwnerAndDeferralDateAndIsActiveTrue(
                "111111111", courtLocation, newAttendanceDate);
        doReturn(Optional.empty()).when(jurorPoolRepository)
            .findByJurorJurorNumberAndOwnerAndDeferralDateAndIsActiveTrue(
                "222222222", courtLocation, newAttendanceDate);

        int deferralsUsed = courtDeferrals.size();
        int actualDeferralsUsed = manageDeferralsService.useCourtDeferrals(newPoolRequest, deferralsUsed,
            "SOME_USER");

        assertThat(actualDeferralsUsed)
            .as("Unhappy path - no pool member for one deferral, expect only one of the two deferrals to "
                + "actually be used")
            .isEqualTo(1);
        assertThat(listAppender.list)
            .extracting(ILoggingEvent::getMessage, ILoggingEvent::getLevel)
            .contains(org.assertj.core.groups.Tuple.tuple(
                "An error occurred trying to add a deferred juror to the new Pool: "
                    + "123456789 - Unable to find an associated Pool Member for the deferred juror: 222222222",
                Level.ERROR), org.assertj.core.groups.Tuple.tuple(
                "1 deferred juror(s) have been added to Pool: 123456789",
                Level.INFO));
    }

    @Test
    @SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
    void useCourtDeferrals_deferralsUsed_noPoolRequest() {
        String courtLocation = "415";
        LocalDate newAttendanceDate = LocalDate.now();

        final PoolRequest newPoolRequest = createPoolRequest("123456789", courtLocation, newAttendanceDate);
        doReturn(Optional.empty()).when(poolRequestRepository)
            .findByPoolNumber("987654321");

        List<CurrentlyDeferred> courtDeferrals = new ArrayList<>();
        courtDeferrals.add(new CurrentlyDeferred());

        doReturn(courtDeferrals).when(currentlyDeferredRepository).findAll(filterByCourtAndDate(
            courtLocation,
            courtLocation,
            newAttendanceDate));
        doNothing().when(currentlyDeferredRepository).delete(any());

        JurorPool deferredJuror = createDeferredJuror(courtLocation);
        doReturn(Optional.of(deferredJuror)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndOwnerAndDeferralDateAndIsActiveTrue(
                any(), any(), any());
        doReturn(Optional.of(createJurorStatus(2, "Responded"))).when(jurorStatusRepository)
            .findById(2);
        doReturn(1).when(poolMemberSequenceService).getPoolMemberSequenceNumber(any());

        doReturn(null).when(jurorPoolRepository).saveAndFlush(any());
        doReturn(null).when(poolRequestRepository).save(any());
        doReturn(null).when(poolRequestRepository).saveAndFlush(any());

        int deferralsUsed = courtDeferrals.size();
        int actualDeferralsUsed = manageDeferralsService.useCourtDeferrals(newPoolRequest,
            deferralsUsed, "SOME_USER");

        assertThat(actualDeferralsUsed)
            .as("Unhappy path - no pool request, expect no deferrals to be used")
            .isEqualTo(0);
        assertThat(listAppender.list)
            .as("Verify error occurred when trying to add a deferred juror to the new pool")
            .extracting(ILoggingEvent::getMessage, ILoggingEvent::getLevel)
            .contains(
                org.assertj.core.groups.Tuple.tuple("An error occurred trying to add a deferred juror to "
                    + "the new Pool: 123456789 - Unable to find an active pool for 987654321", Level.ERROR),
                org.assertj.core.groups.Tuple.tuple("0 deferred juror(s) have been added to Pool: 123456789",
                    Level.INFO)
            );
    }

    @Test
    void useBureauDeferrals_deferralsUsed_happyPath() {
        LocalDate oldAttendanceDate = LocalDate.of(2022, 6, 6);
        LocalDate newAttendanceDate = LocalDate.now();
        String courtLocation = "415";

        final PoolRequest newPoolRequest
            = createPoolRequest(BUREAU_OWNER, "123456789", courtLocation, newAttendanceDate);
        PoolRequest oldPoolRequest = createPoolRequest(courtLocation, "987654321", courtLocation,
            oldAttendanceDate);
        doReturn(Optional.of(oldPoolRequest)).when(poolRequestRepository)
            .findByPoolNumber("987654321");

        List<CurrentlyDeferred> bureauDeferrals = new ArrayList<>();
        bureauDeferrals.add(CurrentlyDeferred.builder().owner(BUREAU_OWNER).jurorNumber("111111111").build());
        bureauDeferrals.add(CurrentlyDeferred.builder().owner(BUREAU_OWNER).jurorNumber("222222222").build());
        final int deferralsUsed = bureauDeferrals.size();
        doReturn(bureauDeferrals).when(currentlyDeferredRepository).findAll(filterByCourtAndDate(
            BUREAU_OWNER,
            courtLocation,
            newAttendanceDate));
        doNothing().when(currentlyDeferredRepository).delete(any());

        JurorPool deferredJuror = createDeferredJuror(BUREAU_OWNER);
        doReturn(Optional.of(deferredJuror)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndOwnerAndDeferralDateAndIsActiveTrue(
                any(), any(), any());
        doReturn(Optional.of(createJurorStatus(2, "Responded"))).when(jurorStatusRepository)
            .findById(2);
        doReturn(1).when(poolMemberSequenceService).getPoolMemberSequenceNumber(any());

        doReturn(null).when(jurorPoolRepository).saveAndFlush(any());
        doReturn(null).when(poolRequestRepository).save(any());
        doReturn(null).when(poolRequestRepository).saveAndFlush(any());
        doReturn(null).when(poolHistoryRepository).save(any());
        doReturn(null).when(jurorHistoryRepository).save(any());

        manageDeferralsService.useBureauDeferrals(newPoolRequest, deferralsUsed, "SOME_USER");

        assertThat(deferredJuror.getIsActive())
            .as("Expect the old, deferred juror record to be updated to inactive")
            .isEqualTo(false);

        verify(printDataService, times(bureauDeferrals.size())).printConfirmationLetter(any());

        verify(poolRequestRepository, times(deferralsUsed)).saveAndFlush(oldPoolRequest);
        verify(poolRequestRepository, times(deferralsUsed)).save(newPoolRequest);
        verify(jurorHistoryRepository, times(deferralsUsed)).save(any());
        verify(jurorPoolRepository, times(deferralsUsed))
            .findByJurorJurorNumberAndOwnerAndDeferralDateAndIsActiveTrue(any(), any(), any());
        verify(jurorPoolRepository, times(deferralsUsed * 2))
            .saveAndFlush(any());
        verify(poolHistoryRepository, times(1)).save(any());
        verify(printDataService, times(deferralsUsed)).printConfirmationLetter(any());
    }

    @Test
    void useBureauDeferrals_deferralsUsed_noJurorPool() {
        LocalDate oldAttendanceDate = LocalDate.of(2022, 6, 6);
        LocalDate newAttendanceDate = LocalDate.now();
        String courtLocation = "415";

        final PoolRequest newPoolRequest
            = createPoolRequest(BUREAU_OWNER, "123456789", courtLocation, newAttendanceDate);
        PoolRequest oldPoolRequest = createPoolRequest(courtLocation, "987654321", courtLocation,
            oldAttendanceDate);
        doReturn(Optional.of(oldPoolRequest)).when(poolRequestRepository)
            .findByPoolNumber("987654321");

        List<CurrentlyDeferred> bureauDeferrals = new ArrayList<>();
        bureauDeferrals.add(
            CurrentlyDeferred.builder().jurorNumber("111111111").owner(BUREAU_OWNER).deferredTo(newAttendanceDate)
                .build());
        bureauDeferrals.add(
            CurrentlyDeferred.builder().jurorNumber("222222222").owner(BUREAU_OWNER).deferredTo(newAttendanceDate)
                .build());
        final int deferralsUsed = bureauDeferrals.size();
        doReturn(bureauDeferrals).when(currentlyDeferredRepository).findAll(filterByCourtAndDate(
            BUREAU_OWNER,
            courtLocation,
            newAttendanceDate));
        doReturn(Optional.of(createJurorStatus(2, "Responded")))
            .when(jurorStatusRepository).findById(2);
        doReturn(1).when(poolMemberSequenceService).getPoolMemberSequenceNumber(any());

        JurorPool deferredJuror = createDeferredJuror(BUREAU_OWNER);
        doReturn(Optional.of(deferredJuror)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndOwnerAndDeferralDateAndIsActiveTrue(
                "111111111", BUREAU_OWNER, newAttendanceDate);
        doReturn(Optional.empty()).when(jurorPoolRepository)
            .findByJurorJurorNumberAndOwnerAndDeferralDateAndIsActiveTrue("222222222", BUREAU_OWNER,
                newAttendanceDate);

        manageDeferralsService.useBureauDeferrals(newPoolRequest, deferralsUsed, "SOME_USER");

        assertThat(listAppender.list)
            .as("Verify number of jurors added to the Pool")
            .extracting(ILoggingEvent::getMessage, ILoggingEvent::getLevel)
            .contains(org.assertj.core.groups.Tuple.tuple("1 deferred juror(s) have been added to Pool: "
                + "123456789", Level.INFO));
    }

    @Test
    void useBureauDeferrals_deferralsUsed_noPoolRequest() {
        String courtLocation = "415";
        LocalDate newAttendanceDate = LocalDate.now();

        final PoolRequest newPoolRequest = createPoolRequest(BUREAU_OWNER, "123456789", courtLocation,
            newAttendanceDate
        );
        doReturn(Optional.empty()).when(poolRequestRepository)
            .findByPoolNumber("987654321");

        List<CurrentlyDeferred> bureauDeferrals = new ArrayList<>();
        bureauDeferrals.add(new CurrentlyDeferred());
        final int deferralsUsed = bureauDeferrals.size();
        doReturn(bureauDeferrals).when(currentlyDeferredRepository).findAll(filterByCourtAndDate(
            BUREAU_OWNER,
            courtLocation,
            newAttendanceDate));
        doNothing().when(currentlyDeferredRepository).delete(any());

        JurorPool deferredJuror = createDeferredJuror(BUREAU_OWNER);
        doReturn(Optional.of(deferredJuror)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndOwnerAndDeferralDateAndIsActiveTrue(
                any(), any(), any());
        doReturn(Optional.of(createJurorStatus(2, "Responded"))).when(jurorStatusRepository)
            .findById(2);
        doReturn(1).when(poolMemberSequenceService).getPoolMemberSequenceNumber(any());

        doReturn(null).when(jurorPoolRepository).saveAndFlush(any());
        doReturn(null).when(poolRequestRepository).save(any());
        doReturn(null).when(poolRequestRepository).saveAndFlush(any());

        manageDeferralsService.useBureauDeferrals(newPoolRequest, deferralsUsed, "SOME_USER");

        assertThat(listAppender.list)
            .as("Verify number of jurors added to the pool")
            .extracting(ILoggingEvent::getMessage, ILoggingEvent::getLevel)
            .contains(org.assertj.core.groups.Tuple.tuple("0 deferred juror(s) have been added to Pool: "
                + "123456789", Level.INFO));
    }

    private void setupProcessJurorTestToActivePool(PoolRequest oldPoolRequest,
                                                   PoolRequest newPoolRequest,
                                                   String jurorNumber,
                                                   List<JurorPool> jurorPools,
                                                   JurorStatus jurorStatus) {
        setupProcessJurorTestToDeferralMaintenance(oldPoolRequest, jurorNumber, jurorPools, jurorStatus);
        doReturn(Optional.of(newPoolRequest)).when(poolRequestRepository)
            .findByPoolNumber("111111112");
    }

    private void setupProcessJurorTestToDeferralMaintenance(PoolRequest oldPoolRequest,
                                                            String jurorNumber,
                                                            List<JurorPool> jurorPools,
                                                            JurorStatus jurorStatus) {
        doReturn(Optional.of(oldPoolRequest)).when(poolRequestRepository)
            .findByPoolNumber("111111111");
        doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(jurorNumber, true);
        doReturn(Optional.of(jurorStatus)).when(jurorStatusRepository).findById(any());
    }


    private void verifyMoveToActivePoolTest() {
        verify(jurorHistoryRepository, times(3)).save(any());
        verify(jurorHistoryService, times(1)).createDeferredLetterHistory(any());
        verify(poolRequestRepository, times(1)).save(any());
        verify(poolRequestRepository, times(1)).saveAndFlush(any());
        verify(jurorPoolRepository, times(2)).saveAndFlush(any());
        verify(poolMemberSequenceService, times(1))
            .getPoolMemberSequenceNumber(any(String.class));
        verify(poolMemberSequenceService, times(1))
            .leftPadInteger(any(int.class));
        verify(currentlyDeferredRepository, times(0)).save(any());
        verify(printDataService, times(1)).printDeferralLetter(any());
        verify(printDataService, times(1)).printConfirmationLetter(any());
    }

    private void verifyJurorToDeferralMaintenanceTest() {
        verify(jurorHistoryRepository, times(1)).save(any());
        verify(jurorHistoryService, times(1)).createDeferredLetterHistory(any());
        verify(jurorPoolRepository, times(2)).save(any());
        verify(printDataService, never()).printConfirmationLetter(any());
    }

    private void verifyJurorToDeferralMaintenanceTestNoLetter() {
        verify(jurorHistoryRepository, times(1)).save(any());
        verify(jurorPoolRepository, times(2)).save(any());
        verify(printDataService, never()).printConfirmationLetter(any());
    }

    private void verifyLettersHappyPathTest() {
        verify(printDataService, times(1)).printDeferralLetter(any());
    }

    @Test
    void processJuror_deferral_digital_happy_path_moveToActivePool() {
        LocalDate newAttendanceDate = LocalDate.now();
        LocalDate oldAttendanceDate = LocalDate.of(2022, 6, 6);
        final BureauJwtPayload bureauPayload = TestUtils.createJwt("400", "BUREAU_USER");
        String jurorNumber = "123456789";
        final PoolRequest oldPoolRequest = createPoolRequest("400", "111111111", "415",
            oldAttendanceDate
        );
        final PoolRequest newPoolRequest = createPoolRequest("400", "111111112", "415",
            newAttendanceDate
        );

        List<JurorPool> poolMembers = new ArrayList<>();
        JurorPool member = createJurorPool(jurorNumber);
        poolMembers.add(member);

        JurorStatus jurorStatus = new JurorStatus();
        jurorStatus.setStatus(IJurorStatus.RESPONDED);

        DigitalResponse digitalResponse = new DigitalResponse();
        digitalResponse.setJurorNumber(jurorNumber);

        DeferralReasonRequestDto dto = createDeferralReasonRequestDtoToActivePool(ReplyMethod.DIGITAL);
        dto.setReplyMethod(ReplyMethod.DIGITAL);

        setupProcessJurorTestToActivePool(oldPoolRequest, newPoolRequest, jurorNumber, poolMembers, jurorStatus);
        doReturn(digitalResponse).when(digitalResponseRepository)
            .findByJurorNumber(any(String.class));

        manageDeferralsService.processJurorDeferral(bureauPayload, jurorNumber, dto);

        verifyMoveToActivePoolTest();
        verify(auditRepository, times(1))
            .save(any(JurorResponseAuditMod.class));

    }

    @Test
    void changeDeferralDate_happy_path_moveToActivePool() {
        LocalDate newAttendanceDate = LocalDate.now();
        LocalDate oldAttendanceDate = LocalDate.of(2022, 6, 6);
        final BureauJwtPayload bureauPayload = TestUtils.createJwt("400", "BUREAU_USER");
        String jurorNumber = "123456789";
        final PoolRequest oldPoolRequest = createPoolRequest("400",
            "111111111", "415", oldAttendanceDate
        );
        final PoolRequest newPoolRequest = createPoolRequest("400",
            "111111112", "415", newAttendanceDate
        );

        List<JurorPool> poolMembers = new ArrayList<>();
        JurorPool member = createJurorPool(jurorNumber);
        poolMembers.add(member);

        JurorStatus jurorStatus = new JurorStatus();
        jurorStatus.setStatus(IJurorStatus.RESPONDED);

        DigitalResponse digitalResponse = new DigitalResponse();
        digitalResponse.setJurorNumber(jurorNumber);

        setupProcessJurorTestToActivePool(oldPoolRequest, newPoolRequest, jurorNumber, poolMembers, jurorStatus);
        doReturn(Optional.of(digitalResponse)).when(digitalResponseRepository)
            .findById(any(String.class));

        DeferralReasonRequestDto dto = createDeferralReasonRequestDtoToActivePool(null);
        manageDeferralsService.changeJurorDeferralDate(bureauPayload, jurorNumber, dto);

        verifyMoveToActivePoolTest();
    }

    @Test
    void changeDeferralDate_happy_path_moveToActivePool_RemoveFromDeferralMaintenance() {
        LocalDate newAttendanceDate = LocalDate.now();
        LocalDate oldAttendanceDate = LocalDate.of(2022, 6, 6);
        final BureauJwtPayload bureauPayload = TestUtils.createJwt("400", "BUREAU_USER");
        String jurorNumber = "123456789";
        final PoolRequest oldPoolRequest = createPoolRequest("400",
            "111111111", "415", oldAttendanceDate
        );
        final PoolRequest newPoolRequest = createPoolRequest("400",
            "111111112", "415", newAttendanceDate
        );

        List<JurorPool> poolMembers = new ArrayList<>();
        JurorPool member = createJurorPool(jurorNumber);
        poolMembers.add(member);

        JurorStatus jurorStatus = new JurorStatus();
        jurorStatus.setStatus(IJurorStatus.RESPONDED);

        final DeferralReasonRequestDto dto = createDeferralReasonRequestDtoToActivePool(null);

        final Optional<CurrentlyDeferred> currentlyDeferred = Optional.of(createCurrentlyDeferred(jurorNumber));

        doReturn(currentlyDeferred).when(currentlyDeferredRepository).findById(any());
        doNothing().when(currentlyDeferredRepository).delete(currentlyDeferred.get());

        setupProcessJurorTestToActivePool(oldPoolRequest, newPoolRequest, jurorNumber, poolMembers, jurorStatus);

        manageDeferralsService.changeJurorDeferralDate(bureauPayload, jurorNumber, dto);

        verifyMoveToActivePoolTest();

    }

    @Test
    void changeDeferralDate_happy_path_moveToDeferralMaintenance() {
        final BureauJwtPayload bureauPayload = TestUtils.createJwt("400", "BUREAU_USER");
        String jurorNumber = "123456789";
        LocalDate oldAttendanceDate = LocalDate.of(2022, 6, 6);
        final DeferralReasonRequestDto dto = createDeferralReasonDtoToDeferralMaintenance(null);
        final PoolRequest oldPoolRequest = createPoolRequest("400",
            "111111111", "415", oldAttendanceDate
        );
        List<JurorPool> poolMembers = new ArrayList<>();
        JurorPool member = createJurorPool(jurorNumber);
        poolMembers.add(member);

        DigitalResponse digitalResponse = new DigitalResponse();
        digitalResponse.setJurorNumber(jurorNumber);

        JurorStatus jurorStatus = new JurorStatus();
        jurorStatus.setStatus(IJurorStatus.RESPONDED);

        setupProcessJurorTestToDeferralMaintenance(oldPoolRequest, jurorNumber, poolMembers, jurorStatus);
        doReturn(Optional.of(digitalResponse)).when(digitalResponseRepository)
            .findById(any(String.class));

        manageDeferralsService.changeJurorDeferralDate(bureauPayload, jurorNumber, dto);

        verifyJurorToDeferralMaintenanceTest();
        verifyLettersHappyPathTest();
    }

    @Test
    void processJuror_deferral_paper_happy_path_moveToActivePool() {
        LocalDate newAttendanceDate = LocalDate.now();
        LocalDate oldAttendanceDate = LocalDate.of(2022, 6, 6);
        final BureauJwtPayload bureauPayload = TestUtils.createJwt("400", "BUREAU_USER");
        String jurorNumber = "123456789";
        final PoolRequest oldPoolRequest = createPoolRequest("400", "111111111", "415",
            oldAttendanceDate);
        final PoolRequest newPoolRequest = createPoolRequest("400", "111111112", "415",
            newAttendanceDate
        );

        List<JurorPool> jurorPools = new ArrayList<>();
        JurorPool jurorPool = createJurorPool(jurorNumber);
        jurorPools.add(jurorPool);

        PaperResponse paperResponse = new PaperResponse();
        paperResponse.setJurorNumber(jurorNumber);

        final DeferralReasonRequestDto dto = createDeferralReasonRequestDtoToActivePool(ReplyMethod.PAPER);

        JurorStatus jurorStatus = new JurorStatus();
        jurorStatus.setStatus(IJurorStatus.RESPONDED);

        setupProcessJurorTestToActivePool(oldPoolRequest, newPoolRequest, jurorNumber, jurorPools, jurorStatus);
        doReturn(paperResponse).when(paperResponseRepository)
            .findByJurorNumber(any(String.class));

        manageDeferralsService.processJurorDeferral(bureauPayload, jurorNumber, dto);

        verifyMoveToActivePoolTest();
    }

    @Test
    void processJuror_deferral_digital_happy_path_moveToDeferralMaintenance() {
        final BureauJwtPayload bureauPayload = TestUtils.createJwt("400", "BUREAU_USER");
        String jurorNumber = "123456789";
        LocalDate oldAttendanceDate = LocalDate.of(2022, 6, 6);
        final DeferralReasonRequestDto dto = createDeferralReasonDtoToDeferralMaintenance(ReplyMethod.DIGITAL);
        final PoolRequest oldPoolRequest = createPoolRequest("400",
            "111111111", "415", oldAttendanceDate
        );
        List<JurorPool> poolMembers = new ArrayList<>();
        JurorPool jurorPool = createJurorPool(jurorNumber);
        poolMembers.add(jurorPool);

        DigitalResponse digitalResponse = new DigitalResponse();
        digitalResponse.setJurorNumber(jurorNumber);

        JurorStatus jurorStatus = new JurorStatus();
        jurorStatus.setStatus(IJurorStatus.RESPONDED);

        setupProcessJurorTestToDeferralMaintenance(oldPoolRequest, jurorNumber, poolMembers, jurorStatus);
        doReturn(digitalResponse).when(digitalResponseRepository)
            .findByJurorNumber(any(String.class));

        manageDeferralsService.processJurorDeferral(bureauPayload, jurorNumber, dto);

        verifyJurorToDeferralMaintenanceTest();
        verifyLettersHappyPathTest();
        verify(auditRepository, times(1))
            .save(any(JurorResponseAuditMod.class));
    }

    @Test
    void processJuror_deferral_paper_happy_path_moveToDeferralMaintenance() {
        final BureauJwtPayload bureauPayload = TestUtils.createJwt("400", "BUREAU_USER");
        String jurorNumber = "123456789";
        LocalDate oldAttendanceDate = LocalDate.of(2022, 6, 6);
        final PoolRequest oldPoolRequest = createPoolRequest("400",
            "111111111", "415", oldAttendanceDate
        );
        final DeferralReasonRequestDto dto = createDeferralReasonDtoToDeferralMaintenance(ReplyMethod.PAPER);

        List<JurorPool> poolMembers = new ArrayList<>();
        JurorPool jurorPool = createJurorPool(jurorNumber);
        poolMembers.add(jurorPool);

        PaperResponse paperResponse = new PaperResponse();
        paperResponse.setJurorNumber(jurorNumber);

        JurorStatus jurorStatus = new JurorStatus();
        jurorStatus.setStatus(IJurorStatus.RESPONDED);

        setupProcessJurorTestToDeferralMaintenance(oldPoolRequest, jurorNumber, poolMembers, jurorStatus);
        doReturn(paperResponse).when(paperResponseRepository)
            .findByJurorNumber(any(String.class));
        manageDeferralsService.processJurorDeferral(bureauPayload, jurorNumber, dto);
        verifyJurorToDeferralMaintenanceTest();
        verifyLettersHappyPathTest();
    }

    @Test
    void processJurorDeferralCourtUser() {
        final BureauJwtPayload courtPayload = TestUtils.createJwt("415", "COURT_USER");
        String jurorNumber = "123456789";
        LocalDate oldAttendanceDate = LocalDate.of(2022, 6, 6);
        final PoolRequest oldPoolRequest = createPoolRequest("415",
            "111111111", "415", oldAttendanceDate);
        final DeferralReasonRequestDto dto = createDeferralReasonDtoToDeferralMaintenance(ReplyMethod.PAPER);

        List<JurorPool> poolMembers = new ArrayList<>();
        JurorPool member = createJurorPool(jurorNumber);
        member.setOwner("415");
        member.getJuror().setPoliceCheck(PoliceCheck.ELIGIBLE);
        poolMembers.add(member);

        PaperResponse paperResponse = new PaperResponse();
        paperResponse.setJurorNumber(jurorNumber);

        JurorStatus jurorStatus = new JurorStatus();
        jurorStatus.setStatus(IJurorStatus.RESPONDED);

        setupProcessJurorTestToDeferralMaintenance(oldPoolRequest, jurorNumber, poolMembers, jurorStatus);
        doReturn(paperResponse).when(paperResponseRepository)
            .findByJurorNumber(any(String.class));
        manageDeferralsService.processJurorDeferral(courtPayload, jurorNumber, dto);
        verifyJurorToDeferralMaintenanceTestNoLetter();
    }

    @Test
    void test_findActivePoolsForDates_happyPath() {
        String bureauOwner = "400";
        final String jurorNumber = "123456789";
        final String currentCourtLocation = "415";
        final BureauJwtPayload payload = TestUtils.createJwt(bureauOwner, "BUREAU_USER");

        Tuple deferralOption = mock(Tuple.class);
        setUpMockQueryResult(deferralOption,
            "415230501",
            LocalDate.of(2023, 5, 29),
            2, 4);
        List<Tuple> firstDateQueryResult = new ArrayList<>();
        firstDateQueryResult.add(deferralOption);

        Tuple deferralOption2 = mock(Tuple.class);
        setUpMockQueryResult(deferralOption2,
            "415230601",
            LocalDate.of(2023, 6, 12),
            4, 2);
        Tuple deferralOption3 = mock(Tuple.class);
        setUpMockQueryResult(deferralOption3,
            "415230602",
            LocalDate.of(2023, 6, 14),
            2, 2);
        List<Tuple> secondDateQueryResult = new ArrayList<>();
        secondDateQueryResult.add(deferralOption2);
        secondDateQueryResult.add(deferralOption3);

        List<Tuple> thirdDateQueryResult = new ArrayList<>();

        doReturn(firstDateQueryResult).when(poolRequestRepository).findActivePoolsForDateRange(
            bureauOwner,
            currentCourtLocation,
            LocalDate.of(2023, 5, 29),
            LocalDate.of(2023, 6, 2),
            false
        );
        doReturn(secondDateQueryResult).when(poolRequestRepository).findActivePoolsForDateRange(
            bureauOwner,
            currentCourtLocation,
            LocalDate.of(2023, 6, 12),
            LocalDate.of(2023, 6, 16),
            false
        );
        doReturn(thirdDateQueryResult).when(poolRequestRepository)
            .findActivePoolsForDateRange(bureauOwner, currentCourtLocation,
                LocalDate.of(2023, 7, 3), LocalDate.of(2023, 7, 7),
                false);

        long deferralMaintenanceCount = 5L;
        doReturn(deferralMaintenanceCount).when(currentlyDeferredRepository)
            .count(filterByCourtAndDate(bureauOwner, currentCourtLocation,
                LocalDate.of(2023, 7, 3)));

        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCode(currentCourtLocation);

        Juror juror = new Juror();
        juror.setJurorNumber(jurorNumber);

        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setCourtLocation(courtLocation);

        JurorPool jurorPool = new JurorPool();
        jurorPool.setOwner(bureauOwner);
        jurorPool.setJuror(juror);
        jurorPool.setPool(poolRequest);

        DeferralDatesRequestDto deferralDatesRequestDto = new DeferralDatesRequestDto();
        deferralDatesRequestDto.setDeferralDates(Arrays.asList(
            LocalDate.of(2023, 5, 29),
            LocalDate.of(2023, 6, 16),
            LocalDate.of(2023, 7, 3)
        ));

        doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(jurorNumber, true);

        DeferralOptionsDto deferralOptionsDto = manageDeferralsService.findActivePoolsForDates(deferralDatesRequestDto,
            jurorNumber, payload);

        verify(poolRequestRepository, times(1))
            .findActivePoolsForDateRange(bureauOwner, currentCourtLocation,
                LocalDate.of(2023, 5, 29), LocalDate.of(2023, 6, 2),
                false);
        verify(poolRequestRepository, times(1))
            .findActivePoolsForDateRange(bureauOwner, currentCourtLocation,
                LocalDate.of(2023, 6, 12), LocalDate.of(2023, 6, 16),
                false);
        verify(poolRequestRepository, times(1))
            .findActivePoolsForDateRange(bureauOwner, currentCourtLocation,
                LocalDate.of(2023, 7, 3), LocalDate.of(2023, 7, 7),
                false);

        verify(currentlyDeferredRepository, never()).count(filterByCourtAndDate(
            bureauOwner,
            currentCourtLocation,
            LocalDate.of(2023, 5, 29)));
        verify(currentlyDeferredRepository, never()).count(filterByCourtAndDate(bureauOwner,
            currentCourtLocation,
            LocalDate.of(2023, 6, 12)));
        verify(currentlyDeferredRepository, times(1))
            .count(filterByCourtAndDate(bureauOwner, currentCourtLocation,
                LocalDate.of(2023, 7, 3)));

        List<DeferralOptionsDto.OptionSummaryDto> options = deferralOptionsDto.getDeferralPoolsSummary();
        DeferralOptionsDto.OptionSummaryDto firstOption = options.stream()
            .filter(option -> option.getWeekCommencing().equals(LocalDate.of(2023, 5, 29)))
            .findFirst().orElse(null);
        assertThat(firstOption).as("Verify first option is not null").isNotNull();
        assertThat(firstOption.getDeferralOptions().size()).as("Verify deferral size").isEqualTo(1);

        DeferralOptionsDto.DeferralOptionDto option1Summary =
            firstOption.getDeferralOptions().stream().findFirst().orElse(null);
        assert option1Summary != null;
        assertThat(option1Summary.getPoolNumber()).as("Verify pool number").isEqualTo("415230501");
        assertThat(option1Summary.getServiceStartDate()).as("Verify service start date")
            .isEqualTo(LocalDate.of(2023, 5, 29));
        assertThat(option1Summary.getUtilisation()).as("Verify utilisation").isEqualTo(2);
        assertThat(option1Summary.getUtilisationDescription()).isEqualTo(PoolUtilisationDescription.SURPLUS);

        DeferralOptionsDto.OptionSummaryDto secondOption = options.stream()
            .filter(option -> option.getWeekCommencing().equals(LocalDate.of(2023, 6, 12)))
            .findFirst().orElse(null);
        assertThat(secondOption).as("Verify second option is not null").isNotNull();
        assertThat(secondOption.getDeferralOptions().size()).as("Verify deferral options size")
            .isEqualTo(2);

        DeferralOptionsDto.DeferralOptionDto option2Summary = secondOption.getDeferralOptions().stream()
            .filter(option -> option.getPoolNumber()
                .equalsIgnoreCase("415230601")).findFirst().orElse(null);
        assert option2Summary != null;
        assertThat(option2Summary.getServiceStartDate()).as("Verify service start date")
            .isEqualTo(LocalDate.of(2023, 6, 12));
        assertThat(option2Summary.getUtilisation()).as("Verify utilisation").isEqualTo(2);
        assertThat(option2Summary.getUtilisationDescription()).as("Verify utilisation message")
            .isEqualTo(PoolUtilisationDescription.NEEDED);

        DeferralOptionsDto.DeferralOptionDto option3Summary = secondOption.getDeferralOptions().stream()
            .filter(option -> option.getPoolNumber().equalsIgnoreCase("415230602"))
            .findFirst().orElse(null);
        assert option3Summary != null;
        assertThat(option3Summary.getServiceStartDate()).as("Verify service start date")
            .isEqualTo(LocalDate.of(2023, 6, 14));
        assertThat(option3Summary.getUtilisation()).as("Verify utilisation").isZero();
        assertThat(option3Summary.getUtilisationDescription()).as("Verify utilisation description")
            .isEqualTo(PoolUtilisationDescription.NEEDED);

        DeferralOptionsDto.OptionSummaryDto thirdOption = options.stream()
            .filter(option -> option.getWeekCommencing().equals(LocalDate.of(2023, 7, 3)))
            .findFirst().orElse(null);
        assertThat(thirdOption).as("Verify third option is not null").isNotNull();
        assertThat(thirdOption.getDeferralOptions().size()).as("Verify deferral options size")
            .isEqualTo(1);

        DeferralOptionsDto.DeferralOptionDto option4Summary =
            thirdOption.getDeferralOptions().stream().findFirst().orElse(null);
        assert option4Summary != null;
        assertThat(option4Summary.getPoolNumber()).as("Verify pool number").isNull();
        assertThat(option4Summary.getServiceStartDate()).as("Verify service start date").isNull();
        assertThat(option4Summary.getUtilisation()).as("Verify utilisation")
            .isEqualTo(deferralMaintenanceCount);
        assertThat(option4Summary.getUtilisationDescription()).as("Verify utilisation description")
            .isEqualTo(PoolUtilisationDescription.IN_MAINTENANCE);
    }

    @Test
    void test_findActivePoolsForDates_invalidAccess() {
        String bureauOwner = "400";
        String jurorNumber = "123456789";
        String currentCourtLocation = "415";
        final BureauJwtPayload payload = TestUtils.createJwt(bureauOwner, "BUREAU_USER");

        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCode(currentCourtLocation);

        Juror juror = new Juror();
        juror.setJurorNumber(jurorNumber);

        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setCourtLocation(courtLocation);

        JurorPool jurorPool = new JurorPool();
        jurorPool.setOwner(currentCourtLocation);
        jurorPool.setJuror(juror);
        jurorPool.setPool(poolRequest);

        DeferralDatesRequestDto deferralDatesRequestDto = new DeferralDatesRequestDto();
        deferralDatesRequestDto.setDeferralDates(Arrays.asList(
            LocalDate.of(2023, 5, 29),
            LocalDate.of(2023, 6, 16),
            LocalDate.of(2023, 7, 3)));

        doReturn(Collections.singletonList(jurorPool))
            .when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(jurorNumber, true);

        assertThatExceptionOfType(MojException.Forbidden.class).isThrownBy(() ->
            manageDeferralsService.findActivePoolsForDates(
                deferralDatesRequestDto,
                jurorNumber,
                payload));

        verify(poolRequestRepository, never()).findActivePoolsForDateRange(
            bureauOwner,
            currentCourtLocation,
            LocalDate.of(2023, 5, 29),
            LocalDate.of(2023, 6, 2),
            false);
        verify(poolRequestRepository, never()).findActivePoolsForDateRange(
            bureauOwner,
            currentCourtLocation,
            LocalDate.of(2023, 6, 12),
            LocalDate.of(2023, 6, 16),
            false);
        verify(poolRequestRepository, never()).findActivePoolsForDateRange(
            bureauOwner,
            currentCourtLocation,
            LocalDate.of(2023, 7, 3),
            LocalDate.of(2023, 7, 7),
            false);

        verify(currentlyDeferredRepository, never()).count(filterByCourtAndDate(
            bureauOwner,
            currentCourtLocation,
            LocalDate.of(2023, 5, 29)));
        verify(currentlyDeferredRepository, never()).count(filterByCourtAndDate(
            bureauOwner,
            currentCourtLocation,
            LocalDate.of(2023, 6, 12)));
        verify(currentlyDeferredRepository, never()).count(filterByCourtAndDate(
            bureauOwner,
            currentCourtLocation,
            LocalDate.of(2023, 7, 3)));
    }

    @Test
    void test_findActivePoolsForDates_noDates() {
        String bureauOwner = "400";
        String jurorNumber = "123456789";
        String currentCourtLocation = "415";
        final BureauJwtPayload payload = TestUtils.createJwt(bureauOwner, "BUREAU_USER");

        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCode(currentCourtLocation);

        Juror juror = new Juror();
        juror.setJurorNumber(jurorNumber);

        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setCourtLocation(courtLocation);

        JurorPool jurorPool = new JurorPool();
        jurorPool.setOwner(bureauOwner);
        jurorPool.setJuror(juror);
        jurorPool.setPool(poolRequest);

        DeferralDatesRequestDto deferralDatesRequestDto = new DeferralDatesRequestDto();

        doReturn(Collections.singletonList(jurorPool))
            .when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(jurorNumber, true);

        DeferralOptionsDto deferralOptionsDto = manageDeferralsService.findActivePoolsForDates(deferralDatesRequestDto,
            jurorNumber, payload);

        assertThat(deferralOptionsDto.getDeferralPoolsSummary().size())
            .as("No dates provided - expect empty payload")
            .isEqualTo(0);
    }

    @Test
    @SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
    void test_getPreferredDeferralDates_threeValidDates() {
        final BureauJwtPayload payload = TestUtils.createJwt("400", "BUREAU_USER");
        String jurorNumber = "123456789";

        Juror juror = new Juror();
        juror.setJurorNumber(jurorNumber);

        JurorPool jurorPool = new JurorPool();
        jurorPool.setOwner("400");
        jurorPool.setJuror(juror);

        DigitalResponse jurorResponse = new DigitalResponse();
        jurorResponse.setJurorNumber(jurorNumber);
        jurorResponse.setDeferralReason("C");
        jurorResponse.setDeferralDate("29/05/2023, 12/6/2023, 3/7/2023");

        doReturn(Collections.singletonList(jurorPool))
            .when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        doReturn(jurorResponse).when(digitalResponseRepository)
            .findByJurorNumber(any(String.class));

        List<String> preferredDates = manageDeferralsService.getPreferredDeferralDates(jurorNumber, payload);
        assertThat(preferredDates.size()).as("Expect returned list to contain 3 dates").isEqualTo(3);
        assertThat(preferredDates).as("Verify preferred date").contains("2023-05-29");
        assertThat(preferredDates).as("Verify preferred date").contains("2023-06-12");
        assertThat(preferredDates).as("Verify preferred date").contains("2023-07-03");
    }

    @Test
    @SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
    void testFindActivePoolsForDatesAndLocationCodeHappyPath() {
        String bureauOwner = "400";
        final String jurorNumber = "123456789";
        final String currentCourtLocation = "415";
        final BureauJwtPayload payload = TestUtils.createJwt(bureauOwner, "BUREAU_USER");

        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCode(currentCourtLocation);

        Juror juror = new Juror();
        juror.setJurorNumber(jurorNumber);

        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setCourtLocation(courtLocation);

        JurorPool jurorPool = new JurorPool();
        jurorPool.setOwner(bureauOwner);
        jurorPool.setJuror(juror);
        jurorPool.setPool(poolRequest);

        doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(jurorNumber, true);

        Tuple deferralOption = mock(Tuple.class);
        setUpMockQueryResult(deferralOption,
            "415230501",
            LocalDate.of(2023, 5, 29),
            2, 4);
        List<Tuple> firstDateQueryResult = new ArrayList<>();
        firstDateQueryResult.add(deferralOption);

        Tuple deferralOption2 = mock(Tuple.class);
        setUpMockQueryResult(deferralOption2,
            "415230601",
            LocalDate.of(2023, 6, 12),
            4, 2);
        Tuple deferralOption3 = mock(Tuple.class);
        setUpMockQueryResult(deferralOption3,
            "415230602",
            LocalDate.of(2023, 6, 14),
            2, 2);
        List<Tuple> secondDateQueryResult = new ArrayList<>();
        secondDateQueryResult.add(deferralOption2);
        secondDateQueryResult.add(deferralOption3);

        List<Tuple> thirdDateQueryResult = new ArrayList<>();

        doReturn(firstDateQueryResult).when(poolRequestRepository).findActivePoolsForDateRange(
            bureauOwner,
            currentCourtLocation,
            LocalDate.of(2023, 5, 29),
            LocalDate.of(2023, 6, 2),
            false
        );
        doReturn(secondDateQueryResult).when(poolRequestRepository).findActivePoolsForDateRange(
            bureauOwner,
            currentCourtLocation,
            LocalDate.of(2023, 6, 12),
            LocalDate.of(2023, 6, 16),
            false
        );
        doReturn(thirdDateQueryResult).when(poolRequestRepository)
            .findActivePoolsForDateRange(bureauOwner, currentCourtLocation,
                LocalDate.of(2023, 7, 3), LocalDate.of(2023, 7, 7),
                false);

        long deferralMaintenanceCount = 5L;
        doReturn(deferralMaintenanceCount).when(currentlyDeferredRepository)
            .count(filterByCourtAndDate(bureauOwner, currentCourtLocation,
                LocalDate.of(2023, 7, 3)));

        DeferralDatesRequestDto deferralDatesRequestDto = new DeferralDatesRequestDto();
        deferralDatesRequestDto.setDeferralDates(Arrays.asList(
            LocalDate.of(2023, 5, 29),
            LocalDate.of(2023, 6, 16),
            LocalDate.of(2023, 7, 3)
        ));

        DeferralOptionsDto deferralOptionsDto = manageDeferralsService.findActivePoolsForDates(deferralDatesRequestDto,
            jurorNumber, payload);

        verify(poolRequestRepository, times(1))
            .findActivePoolsForDateRange(bureauOwner, currentCourtLocation,
                LocalDate.of(2023, 5, 29), LocalDate.of(2023, 6, 2),
                false);
        verify(poolRequestRepository, times(1))
            .findActivePoolsForDateRange(bureauOwner, currentCourtLocation,
                LocalDate.of(2023, 6, 12), LocalDate.of(2023, 6, 16),
                false);
        verify(poolRequestRepository, times(1))
            .findActivePoolsForDateRange(bureauOwner, currentCourtLocation,
                LocalDate.of(2023, 7, 3), LocalDate.of(2023, 7, 7),
                false);

        verify(currentlyDeferredRepository, never()).count(filterByCourtAndDate(
            bureauOwner,
            currentCourtLocation,
            LocalDate.of(2023, 5, 29)));
        verify(currentlyDeferredRepository, never()).count(filterByCourtAndDate(bureauOwner,
            currentCourtLocation,
            LocalDate.of(2023, 6, 12)));
        verify(currentlyDeferredRepository, times(1))
            .count(filterByCourtAndDate(bureauOwner, currentCourtLocation,
                LocalDate.of(2023, 7, 3)));

        List<DeferralOptionsDto.OptionSummaryDto> options = deferralOptionsDto.getDeferralPoolsSummary();
        DeferralOptionsDto.OptionSummaryDto firstOption = options.stream()
            .filter(option -> option.getWeekCommencing().equals(LocalDate.of(2023, 5, 29)))
            .findFirst().orElse(null);
        assertThat(firstOption).as("Verify first option is not null").isNotNull();
        assertThat(firstOption.getDeferralOptions().size()).as("Verify deferral option size")
            .isEqualTo(1);

        DeferralOptionsDto.DeferralOptionDto option1Summary =
            firstOption.getDeferralOptions().stream().findFirst().orElse(null);
        assert option1Summary != null;
        assertThat(option1Summary.getPoolNumber()).as("Verify pool number").isEqualTo("415230501");
        assertThat(option1Summary.getServiceStartDate()).as("Verify service start date")
            .isEqualTo(LocalDate.of(2023, 5, 29));
        assertThat(option1Summary.getUtilisation()).as("Verify utilisation").isEqualTo(2);
        assertThat(option1Summary.getUtilisationDescription()).as("Verify utilisation description")
            .isEqualTo(PoolUtilisationDescription.SURPLUS);

        DeferralOptionsDto.OptionSummaryDto secondOption = options.stream()
            .filter(option -> option.getWeekCommencing().equals(LocalDate.of(2023, 6, 12)))
            .findFirst().orElse(null);
        assertThat(secondOption).as("Verify second option is not null").isNotNull();
        assertThat(secondOption.getDeferralOptions().size()).as("Verify deferral option size")
            .isEqualTo(2);

        DeferralOptionsDto.DeferralOptionDto option2Summary = secondOption.getDeferralOptions().stream()
            .filter(option -> option.getPoolNumber()
                .equalsIgnoreCase("415230601")).findFirst().orElse(null);
        assert option2Summary != null;
        assertThat(option2Summary.getServiceStartDate()).as("Verify service start date")
            .isEqualTo(LocalDate.of(2023, 6, 12));
        assertThat(option2Summary.getUtilisation()).as("Verify utilisation").isEqualTo(2);
        assertThat(option2Summary.getUtilisationDescription()).as("Verify utilisation description")
            .isEqualTo(PoolUtilisationDescription.NEEDED);

        DeferralOptionsDto.DeferralOptionDto option3Summary = secondOption.getDeferralOptions().stream()
            .filter(option -> option.getPoolNumber().equalsIgnoreCase("415230602"))
            .findFirst().orElse(null);
        assert option3Summary != null;
        assertThat(option3Summary.getServiceStartDate()).as("Verify service start date")
            .isEqualTo(LocalDate.of(2023, 6, 14));
        assertThat(option3Summary.getUtilisation()).as("Verify utilisation").isEqualTo(0);
        assertThat(option3Summary.getUtilisationDescription()).as("Verify utilisation description")
            .isEqualTo(PoolUtilisationDescription.NEEDED);

        DeferralOptionsDto.OptionSummaryDto thirdOption = options.stream()
            .filter(option -> option.getWeekCommencing().equals(LocalDate.of(2023, 7, 3)))
            .findFirst().orElse(null);
        assertThat(thirdOption).as("Verify third option is not null").isNotNull();
        assertThat(thirdOption.getDeferralOptions().size()).as("Verify deferral options size")
            .isEqualTo(1);

        DeferralOptionsDto.DeferralOptionDto option4Summary =
            thirdOption.getDeferralOptions().stream().findFirst().orElse(null);
        assert option4Summary != null;
        assertThat(option4Summary.getPoolNumber()).as("Verify pool number").isNull();
        assertThat(option4Summary.getServiceStartDate()).as("Verify service start date").isNull();
        assertThat(option4Summary.getUtilisation()).as("Verify utilisation")
            .isEqualTo(deferralMaintenanceCount);
        assertThat(option4Summary.getUtilisationDescription()).as("Verify utilisation description")
            .isEqualTo(PoolUtilisationDescription.IN_MAINTENANCE);
    }

    @Test
    @SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
    void testFindActivePoolsForDatesAndLocationCodeNoDates() {
        String bureauOwner = "400";
        String jurorNumber = "123456789";
        String currentCourtLocation = "415";
        final BureauJwtPayload payload = TestUtils.createJwt(bureauOwner, "BUREAU_USER");

        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCode(currentCourtLocation);

        Juror juror = new Juror();
        juror.setJurorNumber(jurorNumber);

        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setCourtLocation(courtLocation);

        JurorPool jurorPool = new JurorPool();
        jurorPool.setOwner(bureauOwner);
        jurorPool.setJuror(juror);
        jurorPool.setPool(poolRequest);

        DeferralDatesRequestDto deferralDatesRequestDto = new DeferralDatesRequestDto();

        DeferralOptionsDto deferralOptionsDto = manageDeferralsService
            .findActivePoolsForDatesAndLocCode(deferralDatesRequestDto, jurorNumber, currentCourtLocation, payload);

        assertThat(deferralOptionsDto.getDeferralPoolsSummary().size())
            .as("No dates provided - expect empty payload")
            .isEqualTo(0);

    }

    @Test
    @SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
    void testFindActivePoolsForDatesAndLocationCodeNoLocationCode() {
        String bureauOwner = "400";
        String jurorNumber = "123456789";
        String currentCourtLocation = "415";
        final BureauJwtPayload payload = TestUtils.createJwt(bureauOwner, "BUREAU_USER");

        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCode(currentCourtLocation);

        Juror juror = new Juror();
        juror.setJurorNumber(jurorNumber);

        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setCourtLocation(courtLocation);

        JurorPool jurorPool = new JurorPool();
        jurorPool.setOwner(currentCourtLocation);
        jurorPool.setJuror(juror);
        jurorPool.setPool(poolRequest);

        DeferralDatesRequestDto deferralDatesRequestDto = new DeferralDatesRequestDto();
        deferralDatesRequestDto.setDeferralDates(Arrays.asList(
            LocalDate.of(2023, 5, 29),
            LocalDate.of(2023, 6, 16),
            LocalDate.of(2023, 7, 3)));

        assertThatExceptionOfType(MojException.BadRequest.class).isThrownBy(() ->
            manageDeferralsService.findActivePoolsForDatesAndLocCode(
                deferralDatesRequestDto,
                jurorNumber,
                null,
                payload)).as("Verify a Bad Request exception is thrown");

        verify(poolRequestRepository, never()).findActivePoolsForDateRange(any(), any(), any(), any(), anyBoolean());

        verify(currentlyDeferredRepository, never()).count(any(BooleanExpression.class));

    }

    @Test
    @SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
    void test_getPreferredDeferralDates_twoValidDates() {
        final BureauJwtPayload payload = TestUtils.createJwt("400", "BUREAU_USER");
        String jurorNumber = "123456789";

        Juror juror = new Juror();
        juror.setJurorNumber(jurorNumber);

        JurorPool jurorPool = new JurorPool();
        jurorPool.setOwner("400");
        jurorPool.setJuror(juror);

        DigitalResponse jurorResponse = new DigitalResponse();
        jurorResponse.setJurorNumber(jurorNumber);
        jurorResponse.setDeferralReason("C");
        jurorResponse.setDeferralDate("29/05/2023, 12/6/2023, some/invalid/date");

        doReturn(Collections.singletonList(jurorPool))
            .when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        doReturn(jurorResponse).when(digitalResponseRepository)
            .findByJurorNumber(any(String.class));

        List<String> preferredDates = manageDeferralsService.getPreferredDeferralDates(jurorNumber, payload);
        assertThat(preferredDates.size()).as("Expect returned list to contain 2 dates").isEqualTo(2);
        assertThat(preferredDates).as("Verify preferred date").contains("2023-05-29");
        assertThat(preferredDates).as("Verify preferred date").contains("2023-06-12");
    }

    @Test
    @SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
    void test_getPreferredDeferralDates_oneValidDate() {
        final BureauJwtPayload payload = TestUtils.createJwt("400", "BUREAU_USER");
        String jurorNumber = "123456789";

        Juror juror = new Juror();
        juror.setJurorNumber(jurorNumber);

        JurorPool jurorPool = new JurorPool();
        jurorPool.setOwner("400");
        jurorPool.setJuror(juror);

        DigitalResponse jurorResponse = new DigitalResponse();
        jurorResponse.setJurorNumber(jurorNumber);
        jurorResponse.setDeferralReason("C");
        jurorResponse.setDeferralDate("29/05/2023");

        doReturn(Collections.singletonList(jurorPool))
            .when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        doReturn(jurorResponse).when(digitalResponseRepository)
            .findByJurorNumber(any(String.class));

        List<String> preferredDates = manageDeferralsService.getPreferredDeferralDates(jurorNumber, payload);
        assertThat(preferredDates.size()).as("Expect returned list to contain 1 date").isEqualTo(1);
        assertThat(preferredDates).as("Verify preferred date").contains("2023-05-29");
    }

    @Test
    void test_getPreferredDeferralDates_noValidDates() {
        final BureauJwtPayload payload = TestUtils.createJwt("400", "BUREAU_USER");
        String jurorNumber = "123456789";

        Juror juror = new Juror();
        juror.setJurorNumber(jurorNumber);

        JurorPool jurorPool = new JurorPool();
        jurorPool.setOwner("400");
        jurorPool.setJuror(juror);

        DigitalResponse jurorResponse = new DigitalResponse();
        jurorResponse.setJurorNumber(jurorNumber);
        jurorResponse.setDeferralReason("C");

        doReturn(Collections.singletonList(jurorPool))
            .when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        doReturn(jurorResponse).when(digitalResponseRepository)
            .findByJurorNumber(any(String.class));

        List<String> preferredDates = manageDeferralsService.getPreferredDeferralDates(jurorNumber, payload);
        assertThat(preferredDates.size()).as("Expect returned list to contain 0 dates").isEqualTo(0);
    }

    @Test
    void test_getPreferredDeferralDates_invalidReadAccess() {
        final BureauJwtPayload payload = TestUtils.createJwt("415", "BUREAU_USER");
        String jurorNumber = "123456789";

        Juror juror = new Juror();
        juror.setJurorNumber(jurorNumber);

        JurorPool jurorPool = new JurorPool();
        jurorPool.setOwner("400");
        jurorPool.setJuror(juror);

        doReturn(Collections.singletonList(jurorPool))
            .when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        assertThatExceptionOfType(MojException.Forbidden.class).isThrownBy(() ->
            manageDeferralsService.getPreferredDeferralDates(
                jurorNumber,
                payload
            ));
    }

    @Test
    void test_getPreferredDeferralDates_noDigitalResponse() {
        final BureauJwtPayload payload = TestUtils.createJwt("400", "BUREAU_USER");
        String jurorNumber = "123456789";

        Juror juror = new Juror();
        juror.setJurorNumber(jurorNumber);

        JurorPool jurorPool = new JurorPool();
        jurorPool.setOwner("400");
        jurorPool.setJuror(juror);

        doReturn(Collections.singletonList(jurorPool))
            .when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(jurorNumber, true);
        doReturn(Optional.empty()).when(digitalResponseRepository).findById(jurorNumber);

        assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(() ->
            manageDeferralsService.getPreferredDeferralDates(
                jurorNumber,
                payload
            ));
    }

    @Test
    void test_moveJurorsToActivePool_singleJuror() {
        final BureauJwtPayload payload = TestUtils.createJwt("400", "BUREAU_USER");
        final String courtLocationCode = "415";
        String poolNumber = "123456789";
        List<String> jurorNumbers = new ArrayList<>();
        jurorNumbers.add("111111111");
        DeferralAllocateRequestDto dto = new DeferralAllocateRequestDto();
        dto.setJurors(jurorNumbers);
        dto.setPoolNumber(poolNumber);

        final List<JurorPool> poolMembers = createJurorPools(jurorNumbers, poolNumber, payload.getOwner(),
            courtLocationCode);

        final List<CurrentlyDeferred> deferrals = createDeferrals(payload.getOwner(), courtLocationCode, jurorNumbers,
            LocalDate.now().plusWeeks(5));
        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber(poolNumber);

        JurorStatus status = new JurorStatus();
        status.setStatus(2);
        doReturn(Optional.of(status)).when(jurorStatusRepository).findById(2);

        doReturn(Optional.of(poolRequest)).when(poolRequestRepository).findById(any());

        int index = 0;
        for (String juror : jurorNumbers) {
            doReturn(Collections.singletonList(poolMembers.get(index)))
                .when(jurorPoolRepository)
                .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(eq(juror), eq(true));
            doReturn(Optional.of(deferrals.get(index)))
                .when(currentlyDeferredRepository)
                .findById(juror);
            index++;
        }

        manageDeferralsService.allocateJurorsToActivePool(payload, dto);

        //verification code here
        verifyAllocateJurorsToActivePool(jurorNumbers);
    }

    private void verifyAllocateJurorsToActivePool(List<String> jurorNumbers) {
        verify(poolRequestRepository, times(1)).findById(any());
        verify(poolRequestRepository, times(jurorNumbers.size())).save(any());
        verify(jurorPoolRepository, times(jurorNumbers.size())).saveAndFlush(any());
        verify(poolMemberSequenceService, times(jurorNumbers.size()))
            .getPoolMemberSequenceNumber(any(String.class));
        verify(poolMemberSequenceService, times(jurorNumbers.size()))
            .leftPadInteger(any(int.class));
        verify(currentlyDeferredRepository, times(0)).save(any());
        verify(printDataService, times(jurorNumbers.size())).printConfirmationLetter(any());
        verify(jurorHistoryRepository, times(jurorNumbers.size())).save(any());
    }

    @Test
    void test_moveJurorsToActivePool_multipleJuror() {
        final BureauJwtPayload payload = TestUtils.createJwt("400", "BUREAU_USER");
        final String courtLocationCode = "415";
        final String poolNumber = "123456789";
        List<String> jurorNumbers = new ArrayList<>();
        jurorNumbers.add("111111111");
        jurorNumbers.add("222222222");
        jurorNumbers.add("333333333");
        jurorNumbers.add("444444444");
        jurorNumbers.add("555555555");
        DeferralAllocateRequestDto dto = new DeferralAllocateRequestDto();
        dto.setJurors(jurorNumbers);

        dto.setPoolNumber(poolNumber);

        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber(poolNumber);

        JurorStatus status = new JurorStatus();
        status.setStatus(2);

        List<JurorPool> poolMembers = createJurorPools(jurorNumbers, poolNumber,
            payload.getOwner(), courtLocationCode);

        List<CurrentlyDeferred> deferrals = createDeferrals(payload.getOwner(), courtLocationCode, jurorNumbers,
            LocalDate.now().plusWeeks(7));

        doReturn(Optional.of(status)).when(jurorStatusRepository).findById(2);

        doReturn(Optional.of(poolRequest)).when(poolRequestRepository).findById(any());

        int index = 0;
        for (String juror : jurorNumbers) {
            doReturn(Collections.singletonList(poolMembers.get(index)))
                .when(jurorPoolRepository)
                .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(eq(juror), eq(true));
            doReturn(Optional.of(deferrals.get(index)))
                .when(currentlyDeferredRepository)
                .findById(juror);
            index++;
        }
        manageDeferralsService.allocateJurorsToActivePool(payload, dto);

        //verification code here
        verifyAllocateJurorsToActivePool(jurorNumbers);
    }

    @Test
    void test_moveJurorsToActivePool_poolRequestNotFound() {
        final BureauJwtPayload payload = TestUtils.createJwt("400", "BUREAU_USER");
        final String courtLocationCode = "415";
        String poolNumber = "123456789";
        List<String> jurorNumbers = new ArrayList<>();
        jurorNumbers.add("111111111");
        DeferralAllocateRequestDto dto = new DeferralAllocateRequestDto();
        dto.setJurors(jurorNumbers);
        dto.setPoolNumber(poolNumber);

        final List<JurorPool> poolMembers = createJurorPools(jurorNumbers, poolNumber, payload.getOwner(),
            courtLocationCode);
        final List<CurrentlyDeferred> deferrals = createDeferrals(payload.getOwner(), courtLocationCode, jurorNumbers,
            LocalDate.now().plusWeeks(6));
        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber("987654321");

        JurorStatus status = new JurorStatus();
        status.setStatus(2);
        doReturn(Optional.of(status)).when(jurorStatusRepository).findById(2);

        doReturn(Optional.of(poolRequest))
            .when(poolRequestRepository)
            .findByOwnerAndPoolNumber(any(), eq("987654321"));

        int index = 0;
        for (String juror : jurorNumbers) {

            doReturn(Collections.singletonList(poolMembers.get(index)))
                .when(jurorPoolRepository)
                .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(eq(juror), eq(true));
            doReturn(Optional.of(deferrals.get(index)))
                .when(currentlyDeferredRepository)
                .findById(juror);
            index++;
        }

        assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(() ->
            manageDeferralsService.allocateJurorsToActivePool(
                payload,
                dto
            ));
    }

    @Test
    void test_getDeferralsByCourtLocationCode() {
        String poolNumber = "123456789";
        List<String> courtJurors = new ArrayList<>();
        courtJurors.add("111111111");
        courtJurors.add("222222222");
        courtJurors.add("333333333");

        BureauJwtPayload payload = TestUtils.createJwt("415", "COURT_USER");

        String courtLocationCode = "415";
        List<JurorPool> poolMembers = createJurorPools(courtJurors, poolNumber, payload.getOwner(),
            courtLocationCode);

        final LocalDate deferredTo = LocalDate.of(2023, 6, 16);
        List<CurrentlyDeferred> deferrals = createDeferrals(payload.getOwner(), courtLocationCode, courtJurors,
            deferredTo);
        List<Tuple> courtResults = setupDeferrals(courtJurors, poolNumber, courtLocationCode,
            deferredTo);
        doReturn(courtResults).when(currentlyDeferredRepository).getDeferralsByCourtLocationCode(
            payload,
            courtLocationCode);

        int index = 0;
        for (String juror : courtJurors) {
            doReturn(Collections.singletonList(poolMembers.get(index)))
                .when(jurorPoolRepository)
                .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(eq(juror), eq(true));
            doReturn(Optional.of(deferrals.get(index)))
                .when(currentlyDeferredRepository)
                .findById(juror);
            index++;
        }

        DeferralListDto dto = manageDeferralsService.getDeferralsByCourtLocationCode(payload, courtLocationCode);
        assertThat(dto.getDeferrals().size()).isEqualTo(3);
        assertThat(dto.getDeferrals().get(0).getCourtLocation()).isEqualTo(courtLocationCode);
        assertThat(dto.getDeferrals().get(0).getJurorNumber()).isEqualTo("111111111");
        assertThat(dto.getDeferrals().get(0).getFirstName()).isEqualTo("FNAME");
        assertThat(dto.getDeferrals().get(0).getLastName()).isEqualTo("LNAME");
        assertThat(dto.getDeferrals().get(0).getPoolNumber()).isEqualTo("123456789");
        assertThat(dto.getDeferrals().get(0).getDeferredTo()).isEqualTo("2023-06-16");
        verify(currentlyDeferredRepository, times(1))
            .getDeferralsByCourtLocationCode(any(), any());
    }

    @Test
    void test_findActivePoolsForCourtLocation() {
        final BureauJwtPayload payload = TestUtils.createJwt("400", "BUREAU_USER");
        final String courtLocationCode = "415";

        List<Tuple> results = new ArrayList<>();
        Tuple tuple = mock(Tuple.class);
        setUpMockQueryResult(tuple, "111111111",
            LocalDate.of(2023, 6, 22),
            5, 1);
        results.add(tuple);

        doReturn(results).when(poolRequestRepository).findActivePoolsForDateRange(any(), any(),
            any(), any(), anyBoolean());

        DeferralOptionsDto deferralOptionsDto = manageDeferralsService.findActivePoolsForCourtLocation(
            payload,
            courtLocationCode);

        verify(poolRequestRepository, times(1)).findActivePoolsForDateRange(
            any(),
            any(),
            any(),
            any(),
            anyBoolean());

        DeferralOptionsDto.OptionSummaryDto summaryDto = deferralOptionsDto.getDeferralPoolsSummary().get(0);
        assertThat(summaryDto.getDeferralOptions().get(0).getPoolNumber()).isEqualTo("111111111");
        assertThat(summaryDto.getDeferralOptions().get(0).getServiceStartDate())
            .isEqualTo(LocalDate.of(2023, 6, 22));
        assertThat(summaryDto.getDeferralOptions().get(0).getUtilisation()).isEqualTo(4);
        assertThat(summaryDto.getDeferralOptions().get(0).getUtilisationDescription())
            .isEqualTo(PoolUtilisationDescription.NEEDED);
    }

    //Method under test: getAvailablePoolsByCourtLocationCodeAndJurorNumber (START)
    @Test
    void availablePoolsByCourtLocationCodeAndJurorNumberHappy() {
        doReturn(Collections.singletonList(createJurorPoolForDeferrals("400")))
            .when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(anyString(), anyBoolean());

        String jurorNumber = "123456789";
        doReturn(createJurorResponseForDeferrals(jurorNumber))
            .when(digitalResponseRepository)
            .findByJurorNumber(jurorNumber);

        doReturn(createActivePoolsForDeferralsFirstDate())
            .when(poolRequestRepository).findActivePoolsForDateRange(
                "400",
                "415",
                LocalDate.of(2023, 5, 29),
                LocalDate.of(2023, 6, 2),
                false);

        doReturn(createActivePoolsForDeferralsSecondDate())
            .when(poolRequestRepository).findActivePoolsForDateRange(
                "400",
                "415",
                LocalDate.of(2023, 6, 12),
                LocalDate.of(2023, 6, 16),
                false);

        doReturn(createActivePoolsForDeferralsThirdDate())
            .when(poolRequestRepository).findActivePoolsForDateRange(
                "400",
                "415",
                LocalDate.of(2023, 7, 3),
                LocalDate.of(2023, 7, 7),
                false);

        //Invoke service method under test
        BureauJwtPayload payload = TestUtils.createJwt("400", "BUREAU_USER");
        DeferralOptionsDto deferralOptions =
            manageDeferralsService.getAvailablePoolsByCourtLocationCodeAndJurorNumber(payload,
                "415",
                jurorNumber);

        List<DeferralOptionsDto.OptionSummaryDto> options = deferralOptions.getDeferralPoolsSummary();

        //Deferral options
        //Option 1 (first date)
        DeferralOptionsDto.OptionSummaryDto firstDateOption = options.stream()
            .filter(option -> option.getWeekCommencing().equals(LocalDate.of(2023, 5, 29)))
            .findFirst().orElse(null);
        assertThat(firstDateOption).isNotNull();
        assertThat(firstDateOption.getDeferralOptions().size()).isEqualTo(2);

        DeferralOptionsDto.DeferralOptionDto option1ForFirstDate = firstDateOption.getDeferralOptions().stream()
            .filter(option -> option.getPoolNumber().equalsIgnoreCase("415220502"))
            .findFirst().orElse(null);
        assert option1ForFirstDate != null;
        assertThat(option1ForFirstDate.getServiceStartDate())
            .isEqualTo(LocalDate.of(2023, 6, 1));
        assertThat(option1ForFirstDate.getUtilisation()).isEqualTo(2);
        assertThat(option1ForFirstDate.getUtilisationDescription()).isEqualTo(PoolUtilisationDescription
            .NEEDED);

        DeferralOptionsDto.DeferralOptionDto option2ForFirstDate = firstDateOption.getDeferralOptions().stream()
            .filter(option -> option.getPoolNumber().equalsIgnoreCase("415220401"))
            .findFirst().orElse(null);
        assert option2ForFirstDate != null;
        assertThat(option2ForFirstDate.getServiceStartDate())
            .isEqualTo(LocalDate.of(2023, 5, 30));
        assertThat(option2ForFirstDate.getUtilisation()).isEqualTo(2);
        assertThat(option2ForFirstDate.getUtilisationDescription()).isEqualTo(PoolUtilisationDescription
            .SURPLUS);

        //Option 2 (second date)
        DeferralOptionsDto.OptionSummaryDto secondDateOption = options.stream()
            .filter(option -> option.getWeekCommencing().equals(LocalDate.of(2023, 6, 12)))
            .findFirst().orElse(null);
        assertThat(secondDateOption).isNotNull();
        assertThat(secondDateOption.getDeferralOptions().size()).isEqualTo(1);

        DeferralOptionsDto.DeferralOptionDto option2Summary1 = secondDateOption.getDeferralOptions().stream()
            .filter(option -> option.getPoolNumber().equalsIgnoreCase("415220503"))
            .findFirst().orElse(null);
        assert option2Summary1 != null;
        assertThat(option2Summary1.getServiceStartDate())
            .isEqualTo(LocalDate.of(2023, 6, 12));
        assertThat(option2Summary1.getUtilisation()).isEqualTo(4);
        assertThat(option2Summary1.getUtilisationDescription()).isEqualTo(PoolUtilisationDescription
            .NEEDED);

        //Option 3 (third date)
        DeferralOptionsDto.OptionSummaryDto thirdDateOption = options.stream()
            .filter(option -> option.getWeekCommencing().equals(LocalDate.of(2023, 7, 3)))
            .findFirst().orElse(null);
        assertThat(thirdDateOption).isNotNull();
        assertThat(thirdDateOption.getDeferralOptions().size()).isEqualTo(1);

        DeferralOptionsDto.DeferralOptionDto option3Summary1 = thirdDateOption.getDeferralOptions().stream()
            .findFirst().orElse(null);
        assert option3Summary1 != null;
        assertThat(option3Summary1.getPoolNumber()).isNull();
        assertThat(option3Summary1.getServiceStartDate()).isNull();
        assertThat(option3Summary1.getUtilisation()).isEqualTo(0);
        assertThat(option3Summary1.getUtilisationDescription())
            .isEqualTo(PoolUtilisationDescription.IN_MAINTENANCE);
    }

    @Test
    void availablePoolsByCourtLocationCodeAndJurorNumberActivePoolsEmpty() {
        doReturn(Collections.singletonList(createJurorPoolForDeferrals("400")))
            .when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(anyString(), anyBoolean());

        String jurorNumber = "123456789";
        doReturn(createJurorResponseForDeferrals(jurorNumber))
            .when(digitalResponseRepository)
            .findByJurorNumber(jurorNumber);

        doReturn(new ArrayList<>())
            .when(poolRequestRepository).findActivePoolsForDateRange(
                "400",
                "415",
                LocalDate.of(2023, 5, 29),
                LocalDate.of(2023, 6, 2), false);

        doReturn(new ArrayList<>())
            .when(poolRequestRepository).findActivePoolsForDateRange(
                "400",
                "415",
                LocalDate.of(2023, 6, 12),
                LocalDate.of(2023, 6, 16), false);

        doReturn(new ArrayList<>())
            .when(poolRequestRepository).findActivePoolsForDateRange(
                "400",
                "415",
                LocalDate.of(2023, 7, 3),
                LocalDate.of(2023, 7, 7), false);

        //Invoke service method under test
        BureauJwtPayload payload = TestUtils.createJwt("400", "BUREAU_USER");
        DeferralOptionsDto deferralOptions =
            manageDeferralsService.getAvailablePoolsByCourtLocationCodeAndJurorNumber(payload,
                "415",
                jurorNumber);

        List<DeferralOptionsDto.OptionSummaryDto> options = deferralOptions.getDeferralPoolsSummary();

        //Deferral options
        //Option 1 (first date)
        DeferralOptionsDto.OptionSummaryDto firstDateOption = options.stream()
            .filter(option -> option.getWeekCommencing().equals(LocalDate.of(2023, 5, 29)))
            .findFirst().orElse(null);
        assertThat(firstDateOption).isNotNull();
        assertThat(firstDateOption.getDeferralOptions().size()).isEqualTo(1);

        DeferralOptionsDto.DeferralOptionDto option1Summary1 = firstDateOption.getDeferralOptions().stream()
            .findFirst().orElse(null);
        assert option1Summary1 != null;
        assertThat(option1Summary1.getPoolNumber()).isNull();
        assertThat(option1Summary1.getServiceStartDate()).isNull();
        assertThat(option1Summary1.getUtilisation()).isEqualTo(0);
        assertThat(option1Summary1.getUtilisationDescription())
            .isEqualTo(PoolUtilisationDescription.IN_MAINTENANCE);

        //Option 2 (second date)
        DeferralOptionsDto.OptionSummaryDto secondDateOption = options.stream()
            .filter(option -> option.getWeekCommencing().equals(LocalDate.of(2023, 6, 12)))
            .findFirst().orElse(null);
        assertThat(secondDateOption).isNotNull();
        assertThat(secondDateOption.getDeferralOptions().size()).isEqualTo(1);

        DeferralOptionsDto.DeferralOptionDto option2Summary1 = secondDateOption.getDeferralOptions().stream()
            .findFirst().orElse(null);
        assert option2Summary1 != null;
        assertThat(option2Summary1.getPoolNumber()).isNull();
        assertThat(option2Summary1.getServiceStartDate()).isNull();
        assertThat(option2Summary1.getUtilisation()).isEqualTo(0);
        assertThat(option2Summary1.getUtilisationDescription())
            .isEqualTo(PoolUtilisationDescription.IN_MAINTENANCE);

        //Option 3 (third date)
        DeferralOptionsDto.OptionSummaryDto thirdDateOption = options.stream()
            .filter(option -> option.getWeekCommencing().equals(LocalDate.of(2023, 7, 3)))
            .findFirst().orElse(null);
        assertThat(thirdDateOption).isNotNull();
        assertThat(thirdDateOption.getDeferralOptions().size()).isEqualTo(1);

        DeferralOptionsDto.DeferralOptionDto option3Summary1 = thirdDateOption.getDeferralOptions().stream()
            .findFirst().orElse(null);
        assert option3Summary1 != null;
        assertThat(option3Summary1.getPoolNumber()).isNull();
        assertThat(option3Summary1.getServiceStartDate()).isNull();
        assertThat(option3Summary1.getUtilisation()).isEqualTo(0);
        assertThat(option3Summary1.getUtilisationDescription())
            .isEqualTo(PoolUtilisationDescription.IN_MAINTENANCE);
    }

    @Test
    void availablePoolsByCourtLocationCodeAndJurorNumberNoDeferralDates() {
        doReturn(Collections.singletonList(createJurorPoolForDeferrals("400")))
            .when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(anyString(), anyBoolean());

        String jurorNumber = "123456789";
        doReturn(createJurorResponseWithoutDeferrals(jurorNumber))
            .when(digitalResponseRepository)
            .findByJurorNumber(jurorNumber);

        BureauJwtPayload payload = TestUtils.createJwt("400", "BUREAU_USER");
        assertThatExceptionOfType(MojException.NotFound.class)
            .isThrownBy(() -> manageDeferralsService.getAvailablePoolsByCourtLocationCodeAndJurorNumber(payload,
                "415", "123456789"));

        verify(jurorPoolRepository, times(1)).findByJurorJurorNumberAndIsActive(
            any(String.class), any(Boolean.class));
        verify(digitalResponseRepository, times(1)).findByJurorNumber(any(String.class));
        verify(poolRequestRepository, never()).findActivePoolsForDateRange(
            any(String.class), any(String.class), any(LocalDate.class), any(LocalDate.class), anyBoolean());
    }

    @Test
    void availablePoolsByCourtLocationCodeAndJurorNumberJurorNotFound() {
        doReturn(new ArrayList<>()).when(jurorPoolRepository).findByJurorJurorNumberAndIsActive(
            "123456789", Boolean.TRUE);

        BureauJwtPayload payload = TestUtils.createJwt("400", "BUREAU_USER");
        assertThatExceptionOfType(MojException.NotFound.class)
            .isThrownBy(() -> manageDeferralsService.getAvailablePoolsByCourtLocationCodeAndJurorNumber(payload,
                "415", "123456789"));

        verify(jurorPoolRepository, times(1)).findByJurorJurorNumberAndIsActive(
            any(String.class), any(Boolean.class));
        verify(digitalResponseRepository, never()).findById(any(String.class));
        verify(poolRequestRepository, never()).findActivePoolsForDateRange(
            any(String.class), any(String.class), any(LocalDate.class), any(LocalDate.class), anyBoolean());
    }

    @Test
    void availablePoolsByCourtLocationCodeAndJurorNumberJurorForbidden() {
        doReturn(Collections.singletonList(createJurorPoolForDeferrals("400")))
            .when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(anyString(), anyBoolean());

        BureauJwtPayload payload = TestUtils.createJwt("123", "BUREAU_USER");
        assertThatExceptionOfType(MojException.Forbidden.class)
            .isThrownBy(() -> manageDeferralsService.getAvailablePoolsByCourtLocationCodeAndJurorNumber(payload,
                "415", "123456789"));

        verify(jurorPoolRepository, times(1)).findByJurorJurorNumberAndIsActive(
            any(String.class), any(Boolean.class));
        verify(digitalResponseRepository, never()).findById(any(String.class));
        verify(poolRequestRepository, never()).findActivePoolsForDateRange(
            any(String.class), any(String.class), any(LocalDate.class), any(LocalDate.class), any(Boolean.class));
    }

    @Test
    void availablePoolsByCourtLocationCodeAndJurorNumberNoResponseRecord() {
        doReturn(Collections.singletonList(createJurorPoolForDeferrals("400")))
            .when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(anyString(), anyBoolean());

        doReturn(null).when(digitalResponseRepository).findByJurorNumber(any());

        BureauJwtPayload payload = TestUtils.createJwt("400", "BUREAU_USER");
        assertThatExceptionOfType(MojException.NotFound.class)
            .isThrownBy(() -> manageDeferralsService.getAvailablePoolsByCourtLocationCodeAndJurorNumber(payload,
                "415", "123456789"));

        verify(jurorPoolRepository, times(1)).findByJurorJurorNumberAndIsActive(
            any(String.class), any(Boolean.class));
        verify(digitalResponseRepository, times(1)).findByJurorNumber(any(String.class));
        verify(poolRequestRepository, never()).findActivePoolsForDateRange(
            any(String.class), any(String.class), any(LocalDate.class), any(LocalDate.class), anyBoolean());
    }

    private void setUpDeferralQueryResult(Tuple deferral, String courtLocation,
                                          String jurorNumber, String poolNumber, LocalDate deferredTo) {
        doReturn(courtLocation).when(deferral).get(0, String.class);
        doReturn(jurorNumber).when(deferral).get(1, String.class);
        doReturn("FNAME").when(deferral).get(2, String.class);
        doReturn("LNAME").when(deferral).get(3, String.class);
        doReturn(poolNumber).when(deferral).get(4, String.class);
        doReturn(deferredTo)
            .when(deferral).get(5, LocalDate.class);
    }

    private List<Tuple> setupDeferrals(List<String> jurorNumbers,
                                       String poolNumber, String courtLocation,
                                       LocalDate deferredTo) {
        List<Tuple> results = new ArrayList<>();
        for (String jurorNumber : jurorNumbers) {
            Tuple tuple = mock(Tuple.class);
            setUpDeferralQueryResult(tuple, courtLocation, jurorNumber, poolNumber, deferredTo);
            results.add(tuple);
        }
        return results;
    }

    private void setUpMockQueryResult(Tuple deferralOption, String poolNumber,
                                      LocalDate serviceStartDate,
                                      int poolMembersRequested, int activeJurorPoolCount) {
        doReturn(poolNumber).when(deferralOption).get(0, String.class);
        doReturn(serviceStartDate).when(deferralOption).get(1, LocalDate.class);
        doReturn(poolMembersRequested).when(deferralOption).get(2, Integer.class);
        doReturn(activeJurorPoolCount).when(deferralOption).get(3, Integer.class);
    }


    private DeferralReasonRequestDto createDeferralReasonRequestDtoToActivePool(ReplyMethod replyMethod) {
        DeferralReasonRequestDto dto = new DeferralReasonRequestDto();
        dto.setReplyMethod(replyMethod);
        dto.setExcusalReasonCode("A");
        dto.setPoolNumber("111111112");
        dto.setDeferralDate(LocalDate.of(2023, 6, 12));
        return dto;
    }


    private DeferralReasonRequestDto createDeferralReasonDtoToDeferralMaintenance(ReplyMethod replyMethod) {
        DeferralReasonRequestDto dto = new DeferralReasonRequestDto();
        dto.setReplyMethod(replyMethod);
        dto.setExcusalReasonCode("A");
        dto.setDeferralDate(LocalDate.of(2023, 8, 1));
        return dto;
    }

    private CurrentlyDeferred createCurrentlyDeferred(String jurorNumber) {
        CurrentlyDeferred currentlyDeferred = new CurrentlyDeferred();
        currentlyDeferred.setOwner("400");
        currentlyDeferred.setJurorNumber(jurorNumber);
        currentlyDeferred.setLocCode("415");
        currentlyDeferred.setDeferredTo(LocalDate.now());

        return currentlyDeferred;
    }

    private JurorPool createJurorPool(String jurorNumber) {
        Juror juror = new Juror();
        juror.setJurorNumber(jurorNumber);
        juror.setFirstName("Test");
        juror.setLastName("Person");
        juror.setAddressLine1("Address Line 1");
        juror.setAddressLine2("Address Line 2");
        juror.setAddressLine4("Address Town");
        juror.setAddressLine5("Address County");
        juror.setPostcode("CH1 2AN");
        juror.setNoDefPos(0);
        juror.setPoliceCheck(PoliceCheck.ELIGIBLE);

        CourtLocation location = new CourtLocation();
        location.setLocCode("415");
        location.setName("Chester");
        location.setCourtAttendTime(LocalTime.of(9, 0));

        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber("111111111");
        poolRequest.setCourtLocation(location);

        JurorStatus jurorStatus = new JurorStatus();
        jurorStatus.setStatus(IJurorStatus.RESPONDED);

        JurorPool jurorPool = new JurorPool();
        jurorPool.setStatus(jurorStatus);
        jurorPool.setOwner("400");
        jurorPool.setJuror(juror);
        jurorPool.setPool(poolRequest);
        jurorPool.setIsActive(true);

        juror.setAssociatedPools(Set.of(jurorPool));

        return jurorPool;
    }

    private List<JurorPool> createJurorPools(List<String> jurorNumbers, String poolNumber, String owner,
                                             String courtLoc) {
        CourtLocation location = new CourtLocation();
        location.setLocCode(courtLoc);

        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber(poolNumber);
        poolRequest.setCourtLocation(location);

        JurorStatus status = new JurorStatus();
        status.setStatus(IJurorStatus.RESPONDED);

        List<JurorPool> jurorPools = new ArrayList<>();
        for (String jurorNumber : jurorNumbers) {
            Juror juror = new Juror();
            juror.setJurorNumber(jurorNumber);
            juror.setFirstName("Test");
            juror.setLastName("Person");
            juror.setAddressLine1("Address Line 1");
            juror.setAddressLine2("Address Line 2");
            juror.setAddressLine4("Address Town");
            juror.setAddressLine5("Address County");
            juror.setPostcode("CH1 2AN");
            juror.setPoliceCheck(PoliceCheck.ELIGIBLE);

            JurorPool jurorPool = new JurorPool();
            jurorPool.setOwner(owner);
            jurorPool.setPool(poolRequest);
            jurorPool.setJuror(juror);
            jurorPools.add(jurorPool);
            jurorPool.setIsActive(true);
            jurorPool.setStatus(status);

            juror.setAssociatedPools(Set.of(jurorPool));
        }
        return jurorPools;
    }

    private List<CurrentlyDeferred> createDeferrals(String owner, String locCode, List<String> partNos,
                                                    LocalDate deferredTo) {
        List<CurrentlyDeferred> deferrals = new ArrayList<>();

        for (String partNo : partNos) {
            CurrentlyDeferred dbf = new CurrentlyDeferred();
            dbf.setLocCode(locCode);
            dbf.setOwner(owner);
            dbf.setJurorNumber(partNo);
            dbf.setDeferredTo(deferredTo);
            deferrals.add(dbf);
        }
        return deferrals;
    }


    private List<JurorPool> createDeferredJurorPools(String owner) {
        final List<JurorPool> jurorPools = new ArrayList<>();

        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber("987654321");

        Juror juror = new Juror();
        juror.setJurorNumber("123456789");
        juror.setDateOfBirth(LocalDate.of(1990, 6, 1));
        juror.setExcusalDate(LocalDate.of(2022, 5, 4));
        juror.setExcusalCode("Y");
        juror.setNoDefPos(1);

        JurorStatus jurorStatus = new JurorStatus();
        jurorStatus.setStatus(IJurorStatus.DEFERRED);

        JurorPool jurorPool = new JurorPool();
        jurorPool.setIsActive(true);
        jurorPool.setStatus(jurorStatus);
        jurorPool.setOwner(owner);
        jurorPool.setDeferralDate(LocalDate.of(2022, 10, 3));
        jurorPool.setNextDate(LocalDate.of(2022, 6, 6));
        jurorPool.setUserEdtq("SOME_USER");

        jurorPool.setJuror(juror);
        jurorPool.setPool(poolRequest);

        juror.setAssociatedPools(Set.of(jurorPool));

        jurorPools.add(jurorPool);

        return jurorPools;
    }

    private JurorPool createDeferredJuror(String owner) {
        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber("987654321");

        Juror juror = new Juror();
        juror.setJurorNumber("111111111");
        juror.setDateOfBirth(LocalDate.of(1990, 6, 1));
        juror.setExcusalDate(LocalDate.of(2022, 5, 4));
        juror.setExcusalCode("Y");
        juror.setNoDefPos(1);

        JurorStatus jurorStatus = new JurorStatus();
        jurorStatus.setStatus(IJurorStatus.DEFERRED);

        JurorPool jurorPool = new JurorPool();
        jurorPool.setIsActive(true);
        jurorPool.setStatus(jurorStatus);
        jurorPool.setOwner(owner);
        jurorPool.setDeferralDate(LocalDate.of(2022, 10, 3));
        jurorPool.setNextDate(LocalDate.of(2022, 6, 6));
        jurorPool.setUserEdtq("SOME_USER");

        jurorPool.setPool(poolRequest);
        jurorPool.setJuror(juror);

        juror.setAssociatedPools(Set.of(jurorPool));

        return jurorPool;
    }
}
