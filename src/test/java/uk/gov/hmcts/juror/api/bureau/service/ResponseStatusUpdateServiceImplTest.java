package uk.gov.hmcts.juror.api.bureau.service;

import jakarta.persistence.EntityManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorStatusRepository;
import uk.gov.hmcts.juror.api.moj.repository.UserRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorReasonableAdjustmentRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorResponseAuditRepositoryMod;
import uk.gov.hmcts.juror.api.moj.service.JurorPoolService;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link ResponseStatusUpdateServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class ResponseStatusUpdateServiceImplTest {

    @Mock
    JurorDigitalResponseRepositoryMod jurorResponseRepository;
    @Mock
    JurorStatusRepository jurorStatusRepository;
    @Mock
    JurorResponseAuditRepositoryMod auditRepository;
    @Mock
    JurorPoolRepository poolDetailsRepository;
    @Mock
    JurorHistoryRepository partHistRepository;
    @Mock
    EntityManager entityManager;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AssignOnUpdateService assignOnUpdateService;
    @Mock
    private JurorReasonableAdjustmentRepository specialNeedsRepository;
    @Mock
    private WelshCourtLocationRepository welshCourtLocationRepository;
    @Mock
    private JurorPoolService jurorPoolService;

    @InjectMocks
    private ResponseStatusUpdateServiceImpl responseStatusUpdateService;

    @Test
    public void updateResponse_awaitingContact_happy() throws Exception {
        // Configure mocks
        DigitalResponse mockJurorResponse = mock(DigitalResponse.class);
        given(jurorResponseRepository.findByJurorNumber(any(String.class))).willReturn(mockJurorResponse);
        ProcessingStatus currentProcessingStatus = ProcessingStatus.TODO;
        given(mockJurorResponse.getProcessingStatus()).willReturn(currentProcessingStatus);

        User mockStaff = mock(User.class);
        given(mockJurorResponse.getStaff()).willReturn(mockStaff);
        String jurorNumber = "1";
        ProcessingStatus statusToChangeTo = ProcessingStatus.AWAITING_CONTACT;
        Integer version = 1;
        String auditorUsername = "testuser";
        // Execute logic
        responseStatusUpdateService.updateJurorResponseStatus(jurorNumber, statusToChangeTo, version, auditorUsername);

        // Verify mock interactions
        verify(entityManager).detach(mockJurorResponse);
        verify(mockJurorResponse).setVersion(version);
        verify(mockJurorResponse).setProcessingStatus(auditRepository, statusToChangeTo);
        verify(jurorResponseRepository).save(mockJurorResponse);

        // as we're not setting the status to CLOSED, we should not be merging data to Juror
        verify(poolDetailsRepository, times(0)).findByJurorJurorNumber(any(String.class));
        verify(poolDetailsRepository, times(0)).save(any(JurorPool.class));
        verify(partHistRepository, times(0)).save(any(JurorHistory.class));
    }

    @Test
    public void updateResponse_awaitingCourtReply_happy() throws Exception {
        // Configure mocks
        DigitalResponse mockJurorResponse = mock(DigitalResponse.class);
        given(jurorResponseRepository.findByJurorNumber(any(String.class))).willReturn(mockJurorResponse);
        ProcessingStatus currentProcessingStatus = ProcessingStatus.TODO;
        given(mockJurorResponse.getProcessingStatus()).willReturn(currentProcessingStatus);

        User mockStaff = mock(User.class);
        given(mockJurorResponse.getStaff()).willReturn(mockStaff);


        String jurorNumber = "1";
        ProcessingStatus statusToChangeTo = ProcessingStatus.AWAITING_COURT_REPLY;
        Integer version = 1;
        String auditorUsername = "testuser";

        // Execute logic
        responseStatusUpdateService.updateJurorResponseStatus(jurorNumber, statusToChangeTo, version, auditorUsername);

        // Verify mock interactions
        verify(entityManager).detach(mockJurorResponse);
        verify(mockJurorResponse).setVersion(version);
        verify(mockJurorResponse).setProcessingStatus(auditRepository, statusToChangeTo);
        verify(jurorResponseRepository).save(mockJurorResponse);

        // as we're not setting the status to CLOSED, we should not be merging data to Juror
        verify(poolDetailsRepository, times(0)).findByJurorJurorNumber(any(String.class));
        verify(poolDetailsRepository, times(0)).save(any(JurorPool.class));
        verify(partHistRepository, times(0)).save(any(JurorHistory.class));
    }

    @Test
    public void updateResponse_awaitingTranslation_happy() throws Exception {

        // Configure mocks
        DigitalResponse mockJurorResponse = mock(DigitalResponse.class);
        given(jurorResponseRepository.findByJurorNumber(any(String.class))).willReturn(mockJurorResponse);
        ProcessingStatus currentProcessingStatus = ProcessingStatus.TODO;
        given(mockJurorResponse.getProcessingStatus()).willReturn(currentProcessingStatus);

        User mockStaff = mock(User.class);
        given(mockJurorResponse.getStaff()).willReturn(mockStaff);

        String jurorNumber = "1";
        ProcessingStatus statusToChangeTo = ProcessingStatus.AWAITING_TRANSLATION;
        Integer version = 1;
        String auditorUsername = "testuser";

        // Execute logic
        responseStatusUpdateService.updateJurorResponseStatus(jurorNumber, statusToChangeTo, version, auditorUsername);

        // Verify mock interactions
        verify(entityManager).detach(mockJurorResponse);
        verify(mockJurorResponse).setVersion(version);
        verify(mockJurorResponse).setProcessingStatus(auditRepository, statusToChangeTo);
        verify(jurorResponseRepository).save(mockJurorResponse);

        // as we're not setting the status to CLOSED, we should not be merging data to Juror
        verify(poolDetailsRepository, times(0)).findByJurorJurorNumber(any(String.class));
        verify(poolDetailsRepository, times(0)).save(any(JurorPool.class));
        verify(partHistRepository, times(0)).save(any(JurorHistory.class));
    }

    @Test
    public void updateResponse_todo_happy() throws Exception {

        String jurorNumber = "1";
        ProcessingStatus currentProcessingStatus = ProcessingStatus.AWAITING_CONTACT;
        ProcessingStatus statusToChangeTo = ProcessingStatus.TODO;
        Integer version = 1;
        String auditorUsername = "testuser";

        // Configure mocks
        DigitalResponse mockJurorResponse = mock(DigitalResponse.class);
        given(jurorResponseRepository.findByJurorNumber(any(String.class))).willReturn(mockJurorResponse);
        given(mockJurorResponse.getProcessingStatus()).willReturn(currentProcessingStatus);

        // Execute logic
        responseStatusUpdateService.updateJurorResponseStatus(jurorNumber, statusToChangeTo, version, auditorUsername);

        // Verify mock interactions
        verify(entityManager).detach(mockJurorResponse);
        verify(mockJurorResponse).setVersion(version);
        verify(mockJurorResponse).setProcessingStatus(auditRepository, statusToChangeTo);
        verify(jurorResponseRepository).save(mockJurorResponse);

        // as we're not setting the status to CLOSED, we should not be merging data to Juror
        verify(poolDetailsRepository, times(0)).findByJurorJurorNumber(any(String.class));
        verify(poolDetailsRepository, times(0)).save(any(JurorPool.class));
        verify(partHistRepository, times(0)).save(any(JurorHistory.class));
    }

    @Test
    public void updateResponse_closed_happy() throws Exception {
        JurorStatus respondedJurorStatus = mock(JurorStatus.class);
        when(jurorStatusRepository.findById(IJurorStatus.RESPONDED))
            .thenReturn(Optional.ofNullable(respondedJurorStatus));

        String jurorNumber = "1";
        ProcessingStatus currentProcessingStatus = ProcessingStatus.AWAITING_CONTACT;

        // Configure mocks
        DigitalResponse mockJurorResponse = mock(DigitalResponse.class);
        JurorPool mockPool = mock(JurorPool.class);
        Juror juror = mock(Juror.class);
        when(mockPool.getJuror()).thenReturn(juror);
        given(jurorResponseRepository.findByJurorNumber(any(String.class))).willReturn(mockJurorResponse);
        given(mockJurorResponse.getJurorNumber()).willReturn(jurorNumber);
        given(mockJurorResponse.getProcessingStatus()).willReturn(currentProcessingStatus);
        given(jurorPoolService.getJurorPoolFromUser(jurorNumber)).willReturn(mockPool);

        User mockStaff = mock(User.class);
        given(mockJurorResponse.getStaff()).willReturn(mockStaff);
        ProcessingStatus statusToChangeTo = ProcessingStatus.CLOSED;
        Integer version = 1;
        String auditorUsername = "testuser";

        // Execute logic
        responseStatusUpdateService.updateJurorResponseStatus(jurorNumber, statusToChangeTo, version, auditorUsername);

        // Verify mock interactions
        verify(entityManager).detach(mockJurorResponse);
        verify(mockJurorResponse).setVersion(version);
        verify(mockJurorResponse).setProcessingStatus(auditRepository, statusToChangeTo);
        verify(jurorResponseRepository).save(mockJurorResponse);

        // as we're setting the status to CLOSED, we should be merging data to Juror
        // and also setting RESPONDED to Y, so double the Pool interactions
        verify(jurorPoolService, times(2)).getJurorPoolFromUser(jurorNumber);
        verify(poolDetailsRepository, times(3)).save(any(JurorPool.class));
        verify(partHistRepository, times(2)).save(any(JurorHistory.class));
    }

    @Test
    public void updateResponse_alsoUpdatesStaffAssignment_happy() throws Exception {
        ProcessingStatus currentProcessingStatus = ProcessingStatus.AWAITING_CONTACT;

        // Configure mocks
        DigitalResponse mockJurorResponse = mock(DigitalResponse.class);
        given(jurorResponseRepository.findByJurorNumber(any(String.class))).willReturn(mockJurorResponse);
        given(mockJurorResponse.getProcessingStatus()).willReturn(currentProcessingStatus);
        given(mockJurorResponse.getStaff()).willReturn(null);

        String jurorNumber = "1";
        ProcessingStatus statusToChangeTo = ProcessingStatus.TODO;
        Integer version = 1;
        String auditorUsername = "testuser";

        // Execute logic
        responseStatusUpdateService.updateJurorResponseStatus(jurorNumber, statusToChangeTo, version, auditorUsername);

        // Verify mock interactions
        verify(entityManager).detach(mockJurorResponse);
        verify(mockJurorResponse).setVersion(version);
        verify(mockJurorResponse).setProcessingStatus(auditRepository, statusToChangeTo);
        verify(jurorResponseRepository).save(mockJurorResponse);

        // as we're not setting the status to CLOSED, we should not be merging data to Juror
        verify(poolDetailsRepository, times(0)).findByJurorJurorNumber(any(String.class));
        verify(poolDetailsRepository, times(0)).save(any(JurorPool.class));
        verify(partHistRepository, times(0)).save(any(JurorHistory.class));
    }
}
