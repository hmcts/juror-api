package uk.gov.hmcts.juror.api.moj.service;

import jakarta.persistence.EntityManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorStatusRepository;
import uk.gov.hmcts.juror.api.moj.repository.UserRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorPaperResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorReasonableAdjustmentRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorResponseAuditRepositoryMod;
import uk.gov.hmcts.juror.api.moj.service.jurormanagement.JurorAuditChangeService;

import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


/**
 * Unit test for {@link uk.gov.hmcts.juror.api.bureau.service.ResponseStatusUpdateServiceImpl}.
 */
@RunWith(SpringRunner.class)
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.TooManyMethods"})
public class JurorResponseStatusUpdateServiceImplTest {

    @Mock
    private JurorPaperResponseRepositoryMod jurorPaperResponseRepository;
    @Mock
    private JurorDigitalResponseRepositoryMod jurorResponseRepository;
    @Mock
    private JurorResponseAuditRepositoryMod auditRepository;
    @Mock
    private JurorPoolRepository jurorPoolRepository;
    @Mock
    private JurorHistoryRepository jurorHistoryRepository;
    @Mock
    private EntityManager entityManager;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AssignOnUpdateServiceMod assignOnUpdateService;
    @Mock
    private JurorReasonableAdjustmentRepository jurorReasonableAdjustmentRepository;
    @Mock
    private WelshCourtLocationRepository welshCourtLocationRepository;
    @Mock
    private JurorStatusRepository jurorStatusRepository;
    @Mock
    private JurorRecordService jurorRecordService;
    @Mock
    private JurorAuditChangeService jurorAuditChangeService;
    @Mock
    private JurorRepository jurorRepository;

    @InjectMocks
    private SummonsReplyStatusUpdateServiceImpl summonsReplyStatusUpdateService;


    @Test
    public void updateResponse_awaitingContact_happy() throws Exception {
        BureauJwtPayload payload = TestUtils.createJwt("400", "testuser");
        String jurorNumber = "1";

        // Configure mocks
        DigitalResponse mockJurorResponse = mock(DigitalResponse.class);
        configureBureauMocks(jurorNumber, mockJurorResponse);

        User mockStaff = mock(User.class);
        given(mockJurorResponse.getStaff()).willReturn(mockStaff);

        // Execute logic
        summonsReplyStatusUpdateService.updateDigitalJurorResponseStatus(jurorNumber, ProcessingStatus.AWAITING_CONTACT,
            payload);

        // Verify mock interactions
        mockInteractionsVerification(mockJurorResponse);

        // as we're not setting the status to CLOSED, we should not be merging data to Juror
        assertNoMergeVerification();
    }

    @Test
    public void updateResponse_awaitingCourtReply_happy() throws Exception {
        BureauJwtPayload payload = TestUtils.createJwt("400", "testuser");
        String jurorNumber = "1";

        // Configure mocks
        DigitalResponse mockJurorResponse = mock(DigitalResponse.class);
        configureBureauMocks(jurorNumber, mockJurorResponse);

        User mockStaff = mock(User.class);
        given(mockJurorResponse.getStaff()).willReturn(mockStaff);

        // Execute logic
        summonsReplyStatusUpdateService.updateDigitalJurorResponseStatus(jurorNumber,
            ProcessingStatus.AWAITING_COURT_REPLY, payload);

        // Verify mock interactions
        mockInteractionsVerification(mockJurorResponse);

        // as we're not setting the status to CLOSED, we should not be merging data to Juror
        assertNoMergeVerification();
    }

    @Test
    public void updateResponse_awaitingTranslation_happy() throws Exception {
        BureauJwtPayload payload = TestUtils.createJwt("400", "testuser");
        String jurorNumber = "1";

        // Configure mocks
        DigitalResponse mockJurorResponse = mock(DigitalResponse.class);
        configureBureauMocks(jurorNumber, mockJurorResponse);

        User mockStaff = mock(User.class);
        given(mockJurorResponse.getStaff()).willReturn(mockStaff);

        // Execute logic
        summonsReplyStatusUpdateService.updateDigitalJurorResponseStatus(jurorNumber,
            ProcessingStatus.AWAITING_TRANSLATION, payload);

        mockInteractionsVerification(mockJurorResponse);
        assertNoMergeVerification();
    }

    @Test
    public void updateResponse_todo_happy() throws Exception {
        BureauJwtPayload payload = TestUtils.createJwt("400", "testuser");

        String jurorNumber = "1";
        // Configure mocks
        DigitalResponse mockJurorResponse = mock(DigitalResponse.class);
        configureBureauMocks(jurorNumber, mockJurorResponse);

        // Execute logic
        summonsReplyStatusUpdateService.updateDigitalJurorResponseStatus(jurorNumber, ProcessingStatus.TODO, payload);

        // Verify mock interactions
        mockInteractionsVerification(mockJurorResponse);
        assertNoMergeVerification();
    }

    @Test
    public void updateResponse_alsoUpdatesStaffAssignment_happy() throws Exception {
        final BureauJwtPayload payload = TestUtils.createJwt("400", "testuser");
        String jurorNumber = "1";
        ProcessingStatus currentProcessingStatus = ProcessingStatus.AWAITING_CONTACT;

        // Configure mocks
        DigitalResponse mockJurorResponse = mock(DigitalResponse.class);
        given(jurorResponseRepository.findByJurorNumber(any(String.class))).willReturn(mockJurorResponse);
        given(mockJurorResponse.getProcessingStatus()).willReturn(currentProcessingStatus);
        given(mockJurorResponse.getStaff()).willReturn(null);
        Mockito.doReturn(Collections.singletonList(createJurorPool(jurorNumber))).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        // Execute logic
        summonsReplyStatusUpdateService.updateDigitalJurorResponseStatus(jurorNumber, ProcessingStatus.TODO, payload);

        // verifications
        mockInteractionsVerification(mockJurorResponse);
        assertNoMergeVerification();
    }

    @Test
    public void updateResponse_bureauUser_bureauOwned_awaitingTranslation() {
        BureauJwtPayload bureauPayload = TestUtils.createJwt("400", "testuser");
        String jurorNumber = "1";

        // Configure mocks
        DigitalResponse mockJurorResponse = mock(DigitalResponse.class);
        configureBureauMocks(jurorNumber, mockJurorResponse);

        // Execute logic
        summonsReplyStatusUpdateService.updateDigitalJurorResponseStatus(jurorNumber,
            ProcessingStatus.AWAITING_TRANSLATION, bureauPayload);

        mockInteractionsVerification(mockJurorResponse);
        assertNoMergeVerification();
    }

    @Test
    public void updateResponse_courtUser_bureauOwned_awaitingTranslation() {
        BureauJwtPayload courtPayload = TestUtils.createJwt("415", "testuser");
        String jurorNumber = "1";

        // Configure mocks
        DigitalResponse mockJurorResponse = mock(DigitalResponse.class);
        configureBureauMocks(jurorNumber, mockJurorResponse);

        // Execute logic
        assertThatExceptionOfType(MojException.Forbidden.class).isThrownBy(() ->
            summonsReplyStatusUpdateService.updateDigitalJurorResponseStatus(jurorNumber,
                ProcessingStatus.AWAITING_TRANSLATION, courtPayload));
    }

    @Test
    public void updateResponse_bureauUser_courtOwned_awaitingTranslation() {
        BureauJwtPayload bureauPayload = TestUtils.createJwt("400", "testuser");

        String jurorNumber = "1";
        ProcessingStatus currentProcessingStatus = ProcessingStatus.TODO;

        // Configure mocks
        DigitalResponse mockJurorResponse = mock(DigitalResponse.class);
        configureCourtMocks(currentProcessingStatus, jurorNumber, mockJurorResponse);

        // Execute logic
        assertThatExceptionOfType(MojException.Forbidden.class).isThrownBy(() ->
            summonsReplyStatusUpdateService.updateDigitalJurorResponseStatus(jurorNumber,
                ProcessingStatus.AWAITING_TRANSLATION, bureauPayload));
    }

    @Test
    public void updateResponse_courtUser_courtOwned_awaitingTranslation() {
        BureauJwtPayload courtPayload = TestUtils.createJwt("415", "testuser");

        String jurorNumber = "1";
        ProcessingStatus currentProcessingStatus = ProcessingStatus.TODO;
        // Configure mocks
        DigitalResponse mockJurorResponse = mock(DigitalResponse.class);
        configureCourtMocks(currentProcessingStatus, jurorNumber, mockJurorResponse);

        User mockStaff = mock(User.class);
        given(mockJurorResponse.getStaff()).willReturn(mockStaff);

        // Execute logic
        summonsReplyStatusUpdateService.updateDigitalJurorResponseStatus(jurorNumber,
            ProcessingStatus.AWAITING_TRANSLATION, courtPayload);

        mockInteractionsVerification(mockJurorResponse);
        assertNoMergeVerification();
    }


    private JurorPool createJurorPool(String jurorNumber) {
        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber("415230101");

        Juror juror = new Juror();
        juror.setJurorNumber(jurorNumber);
        juror.setTitle("Dr");
        juror.setFirstName("Test");
        juror.setLastName("Person");

        JurorPool jurorPool = new JurorPool();
        jurorPool.setOwner("400");
        jurorPool.setPool(poolRequest);

        juror.setAssociatedPools(Set.of(jurorPool));
        jurorPool.setJuror(juror);

        return jurorPool;
    }

    private void configureBureauMocks(String jurorNumber, DigitalResponse mockJurorResponse) {
        Mockito.doReturn(mockJurorResponse).when(jurorResponseRepository).findByJurorNumber(any(String.class));
        Mockito.doReturn(Collections.singletonList(createJurorPool(jurorNumber))).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
    }

    private void configureCourtMocks(ProcessingStatus currentProcessingStatus, String jurorNumber,
                                     DigitalResponse mockJurorResponse) {
        given(jurorResponseRepository.findByJurorNumber(any(String.class))).willReturn(mockJurorResponse);
        JurorPool courtOwnedMember = createJurorPool(jurorNumber);
        courtOwnedMember.setOwner("415");
        given(mockJurorResponse.getProcessingStatus()).willReturn(currentProcessingStatus);
        given(mockJurorResponse.getJurorNumber()).willReturn(jurorNumber);
        Mockito.doReturn(Collections.singletonList(courtOwnedMember)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
    }


    private void assertNoMergeVerification() {
        // as we're not setting the status to CLOSED, we should not be merging data to Juror
        verify(jurorPoolRepository, times(1))
            .findByJurorJurorNumberAndIsActive(any(String.class), eq(true));
        verify(jurorPoolRepository, times(0)).save(any(JurorPool.class));
        verify(jurorHistoryRepository, times(0)).save(any(JurorHistory.class));
    }

    private void mockInteractionsVerification(DigitalResponse mockJurorResponse) {
        // Verify mock interactions
        verify(entityManager).detach(mockJurorResponse);
        verify(jurorResponseRepository).save(mockJurorResponse);
    }
}

