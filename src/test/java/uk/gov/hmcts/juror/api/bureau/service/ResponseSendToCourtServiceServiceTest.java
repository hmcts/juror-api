package uk.gov.hmcts.juror.api.bureau.service;


import jakarta.persistence.EntityManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.juror.api.bureau.controller.ResponseSendToCourtController;
import uk.gov.hmcts.juror.api.bureau.domain.JurorResponseAudit;
import uk.gov.hmcts.juror.api.bureau.domain.JurorResponseAuditRepository;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponse;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponseRepository;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ResponseSendToCourtServiceServiceTest {

    @Mock
    private JurorResponseAuditRepository jurorResponseAuditRepository;

    @Mock
    private JurorResponseRepository jurorResponseRepository;

    @InjectMocks
    private ResponseSendToCourtServiceImpl sendToCourtService;

    @Mock
    EntityManager entityManager;

    @Mock
    private AssignOnUpdateService assignOnUpdateService;


    @Test
    public void updateResponse_to_court_happy_assignStaff() throws Exception {

        String jurorNumber = "1";
        Integer version = 1;
        String currentUser = "testuser";

        ProcessingStatus currentProcessingStatus = ProcessingStatus.TODO;
        ProcessingStatus statusToChangeTo = ProcessingStatus.CLOSED;


        // Configure mocks
        JurorResponse mockJurorResponse = mock(JurorResponse.class);
        given(jurorResponseRepository.findByJurorNumber(any(String.class))).willReturn(mockJurorResponse);
        given(mockJurorResponse.getProcessingStatus()).willReturn(currentProcessingStatus);
        given(mockJurorResponse.getStaff()).willReturn(null);


        // Execute logic
        sendToCourtService.sendResponseToCourt(jurorNumber, new ResponseSendToCourtController.SendToCourtDto(1),
            currentUser);

        // Verify mock interactions
        verify(entityManager).detach(mockJurorResponse);
        verify(assignOnUpdateService).assignToCurrentLogin(any(JurorResponse.class), eq(currentUser));
        verify(mockJurorResponse).setVersion(version);
        verify(mockJurorResponse).setProcessingStatus(statusToChangeTo);
        verify(mockJurorResponse).setProcessingComplete(Boolean.TRUE);
        verify(jurorResponseRepository).save(mockJurorResponse);
        verify(jurorResponseAuditRepository).save(any(JurorResponseAudit.class));


    }

    @Test(expected = ResponseSendToCourtServiceImpl.ResponseAlreadyCompleted.class)
    public void juror_unhappy_jurorNotFound() throws Exception {

        String jurorNumber = "1";
        String auditorUsername = "testuser";

        // Configure mocks
        JurorResponse mockJurorResponse = mock(JurorResponse.class);
        given(jurorResponseRepository.findByJurorNumber(any(String.class))).willReturn(mockJurorResponse);
        given(mockJurorResponse.getProcessingComplete()).willReturn(Boolean.TRUE);


        // Execute logic
        sendToCourtService.sendResponseToCourt(jurorNumber, new ResponseSendToCourtController.SendToCourtDto(1),
            auditorUsername);


    }
}
