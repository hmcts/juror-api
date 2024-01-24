package uk.gov.hmcts.juror.api.bureau.service;

import jakarta.persistence.EntityManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.juror.api.bureau.domain.BureauJurorSpecialNeedsRepository;
import uk.gov.hmcts.juror.api.bureau.domain.JurorResponseAuditRepository;
import uk.gov.hmcts.juror.api.bureau.domain.PartAmendment;
import uk.gov.hmcts.juror.api.bureau.domain.PartAmendmentRepository;
import uk.gov.hmcts.juror.api.bureau.domain.PartHist;
import uk.gov.hmcts.juror.api.bureau.domain.PartHistRepository;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponse;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponseRepository;
import uk.gov.hmcts.juror.api.juror.domain.Pool;
import uk.gov.hmcts.juror.api.juror.domain.PoolRepository;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.repository.UserRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Unit test for {@link ResponseStatusUpdateServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class ResponseStatusUpdateServiceImplTest {

    @Mock
    JurorResponseRepository jurorResponseRepository;

    @Mock
    JurorResponseAuditRepository auditRepository;

    @Mock
    PoolRepository poolDetailsRepository;

    @Mock
    PartAmendmentRepository partAmendmentRepository;

    @Mock
    PartHistRepository partHistRepository;

    @Mock
    EntityManager entityManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AssignOnUpdateService assignOnUpdateService;

    @Mock
    private BureauJurorSpecialNeedsRepository specialNeedsRepository;

    @Mock
    private WelshCourtLocationRepository welshCourtLocationRepository;

    @InjectMocks
    private ResponseStatusUpdateServiceImpl responseStatusUpdateService;

    @Test
    public void updateResponse_awaitingContact_happy() throws Exception {

        String jurorNumber = "1";
        ProcessingStatus currentProcessingStatus = ProcessingStatus.TODO;
        ProcessingStatus statusToChangeTo = ProcessingStatus.AWAITING_CONTACT;
        Integer version = 1;
        String auditorUsername = "testuser";

        // Configure mocks
        JurorResponse mockJurorResponse = mock(JurorResponse.class);
        given(jurorResponseRepository.findByJurorNumber(any(String.class))).willReturn(mockJurorResponse);
        given(mockJurorResponse.getProcessingStatus()).willReturn(currentProcessingStatus);

        User mockStaff = mock(User.class);
        given(mockJurorResponse.getStaff()).willReturn(mockStaff);

        // Execute logic
        responseStatusUpdateService.updateJurorResponseStatus(jurorNumber, statusToChangeTo, version, auditorUsername);

        // Verify mock interactions
        verify(entityManager).detach(mockJurorResponse);
        verify(mockJurorResponse).setVersion(version);
        verify(mockJurorResponse).setProcessingStatus(statusToChangeTo);
        verify(jurorResponseRepository).save(mockJurorResponse);

        // as we're not setting the status to CLOSED, we should not be merging data to Juror
        verify(poolDetailsRepository, times(0)).findByJurorNumber(any(String.class));
        verify(poolDetailsRepository, times(0)).save(any(Pool.class));
        verify(partAmendmentRepository, times(0)).save(any(PartAmendment.class));
        verify(partHistRepository, times(0)).save(any(PartHist.class));
    }

    @Test
    public void updateResponse_awaitingCourtReply_happy() throws Exception {

        String jurorNumber = "1";
        ProcessingStatus currentProcessingStatus = ProcessingStatus.TODO;
        ProcessingStatus statusToChangeTo = ProcessingStatus.AWAITING_COURT_REPLY;
        Integer version = 1;
        String auditorUsername = "testuser";

        // Configure mocks
        JurorResponse mockJurorResponse = mock(JurorResponse.class);
        given(jurorResponseRepository.findByJurorNumber(any(String.class))).willReturn(mockJurorResponse);
        given(mockJurorResponse.getProcessingStatus()).willReturn(currentProcessingStatus);

        User mockStaff = mock(User.class);
        given(mockJurorResponse.getStaff()).willReturn(mockStaff);

        // Execute logic
        responseStatusUpdateService.updateJurorResponseStatus(jurorNumber, statusToChangeTo, version, auditorUsername);

        // Verify mock interactions
        verify(entityManager).detach(mockJurorResponse);
        verify(mockJurorResponse).setVersion(version);
        verify(mockJurorResponse).setProcessingStatus(statusToChangeTo);
        verify(jurorResponseRepository).save(mockJurorResponse);

        // as we're not setting the status to CLOSED, we should not be merging data to Juror
        verify(poolDetailsRepository, times(0)).findByJurorNumber(any(String.class));
        verify(poolDetailsRepository, times(0)).save(any(Pool.class));
        verify(partAmendmentRepository, times(0)).save(any(PartAmendment.class));
        verify(partHistRepository, times(0)).save(any(PartHist.class));
    }

    @Test
    public void updateResponse_awaitingTranslation_happy() throws Exception {

        String jurorNumber = "1";
        ProcessingStatus currentProcessingStatus = ProcessingStatus.TODO;
        ProcessingStatus statusToChangeTo = ProcessingStatus.AWAITING_TRANSLATION;
        Integer version = 1;
        String auditorUsername = "testuser";

        // Configure mocks
        JurorResponse mockJurorResponse = mock(JurorResponse.class);
        given(jurorResponseRepository.findByJurorNumber(any(String.class))).willReturn(mockJurorResponse);
        given(mockJurorResponse.getProcessingStatus()).willReturn(currentProcessingStatus);

        User mockStaff = mock(User.class);
        given(mockJurorResponse.getStaff()).willReturn(mockStaff);

        // Execute logic
        responseStatusUpdateService.updateJurorResponseStatus(jurorNumber, statusToChangeTo, version, auditorUsername);

        // Verify mock interactions
        verify(entityManager).detach(mockJurorResponse);
        verify(mockJurorResponse).setVersion(version);
        verify(mockJurorResponse).setProcessingStatus(statusToChangeTo);
        verify(jurorResponseRepository).save(mockJurorResponse);

        // as we're not setting the status to CLOSED, we should not be merging data to Juror
        verify(poolDetailsRepository, times(0)).findByJurorNumber(any(String.class));
        verify(poolDetailsRepository, times(0)).save(any(Pool.class));
        verify(partAmendmentRepository, times(0)).save(any(PartAmendment.class));
        verify(partHistRepository, times(0)).save(any(PartHist.class));
    }

    @Test
    public void updateResponse_todo_happy() throws Exception {

        String jurorNumber = "1";
        ProcessingStatus currentProcessingStatus = ProcessingStatus.AWAITING_CONTACT;
        ProcessingStatus statusToChangeTo = ProcessingStatus.TODO;
        Integer version = 1;
        String auditorUsername = "testuser";

        // Configure mocks
        JurorResponse mockJurorResponse = mock(JurorResponse.class);
        given(jurorResponseRepository.findByJurorNumber(any(String.class))).willReturn(mockJurorResponse);
        given(mockJurorResponse.getProcessingStatus()).willReturn(currentProcessingStatus);

        // Execute logic
        responseStatusUpdateService.updateJurorResponseStatus(jurorNumber, statusToChangeTo, version, auditorUsername);

        // Verify mock interactions
        verify(entityManager).detach(mockJurorResponse);
        verify(mockJurorResponse).setVersion(version);
        verify(mockJurorResponse).setProcessingStatus(statusToChangeTo);
        verify(jurorResponseRepository).save(mockJurorResponse);

        // as we're not setting the status to CLOSED, we should not be merging data to Juror
        verify(poolDetailsRepository, times(0)).findByJurorNumber(any(String.class));
        verify(poolDetailsRepository, times(0)).save(any(Pool.class));
        verify(partAmendmentRepository, times(0)).save(any(PartAmendment.class));
        verify(partHistRepository, times(0)).save(any(PartHist.class));
    }

    @Test
    public void updateResponse_closed_happy() throws Exception {

        String jurorNumber = "1";
        ProcessingStatus currentProcessingStatus = ProcessingStatus.AWAITING_CONTACT;
        ProcessingStatus statusToChangeTo = ProcessingStatus.CLOSED;
        Integer version = 1;
        String auditorUsername = "testuser";

        // Configure mocks
        JurorResponse mockJurorResponse = mock(JurorResponse.class);
        Pool mockPool = mock(Pool.class);
        given(jurorResponseRepository.findByJurorNumber(any(String.class))).willReturn(mockJurorResponse);
        given(mockJurorResponse.getJurorNumber()).willReturn(jurorNumber);
        given(mockJurorResponse.getProcessingStatus()).willReturn(currentProcessingStatus);
        given(poolDetailsRepository.findByJurorNumber(jurorNumber)).willReturn(mockPool);

        User mockStaff = mock(User.class);
        given(mockJurorResponse.getStaff()).willReturn(mockStaff);

        // Execute logic
        responseStatusUpdateService.updateJurorResponseStatus(jurorNumber, statusToChangeTo, version, auditorUsername);

        // Verify mock interactions
        verify(entityManager).detach(mockJurorResponse);
        verify(mockJurorResponse).setVersion(version);
        verify(mockJurorResponse).setProcessingStatus(statusToChangeTo);
        verify(jurorResponseRepository).save(mockJurorResponse);

        // as we're setting the status to CLOSED, we should be merging data to Juror
        // and also setting RESPONDED to Y, so double the Pool interactions
        verify(poolDetailsRepository, times(2)).findByJurorNumber(jurorNumber);
        verify(poolDetailsRepository, times(3)).save(any(Pool.class));
        verify(partAmendmentRepository, times(1)).save(any(PartAmendment.class));
        verify(partHistRepository, times(2)).save(any(PartHist.class));
    }

    @Test
    public void updateResponse_alsoUpdatesStaffAssignment_happy() throws Exception {

        String jurorNumber = "1";
        ProcessingStatus currentProcessingStatus = ProcessingStatus.AWAITING_CONTACT;
        ProcessingStatus statusToChangeTo = ProcessingStatus.TODO;
        Integer version = 1;
        String auditorUsername = "testuser";

        // Configure mocks
        JurorResponse mockJurorResponse = mock(JurorResponse.class);
        given(jurorResponseRepository.findByJurorNumber(any(String.class))).willReturn(mockJurorResponse);
        given(mockJurorResponse.getProcessingStatus()).willReturn(currentProcessingStatus);
        given(mockJurorResponse.getStaff()).willReturn(null);

        // Execute logic
        responseStatusUpdateService.updateJurorResponseStatus(jurorNumber, statusToChangeTo, version, auditorUsername);

        // Verify mock interactions
        verify(entityManager).detach(mockJurorResponse);
        verify(mockJurorResponse).setVersion(version);
        verify(mockJurorResponse).setProcessingStatus(statusToChangeTo);
        verify(jurorResponseRepository).save(mockJurorResponse);

        // as we're not setting the status to CLOSED, we should not be merging data to Juror
        verify(poolDetailsRepository, times(0)).findByJurorNumber(any(String.class));
        verify(poolDetailsRepository, times(0)).save(any(Pool.class));
        verify(partAmendmentRepository, times(0)).save(any(PartAmendment.class));
        verify(partHistRepository, times(0)).save(any(PartHist.class));
    }
}
