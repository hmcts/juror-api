package uk.gov.hmcts.juror.api.bureau.service;


import jakarta.persistence.EntityManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.juror.api.bureau.controller.ResponseSendToCourtController;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.JurorResponseAuditMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorResponseAuditRepositoryMod;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ResponseSendToCourtServiceServiceTest {

    @Mock
    private JurorResponseAuditRepositoryMod jurorResponseAuditRepository;

    @Mock
    private JurorDigitalResponseRepositoryMod jurorResponseRepository;

    @InjectMocks
    private ResponseSendToCourtServiceImpl sendToCourtService;

    @Mock
    EntityManager entityManager;

    @Mock
    private AssignOnUpdateService assignOnUpdateService;


    @Test
    public void updateResponse_to_court_happy_assignStaff() throws Exception {

        ProcessingStatus currentProcessingStatus = ProcessingStatus.TODO;


        // Configure mocks
        DigitalResponse mockJurorResponse = mock(DigitalResponse.class);
        given(jurorResponseRepository.findByJurorNumber(any(String.class))).willReturn(mockJurorResponse);
        given(mockJurorResponse.getProcessingStatus()).willReturn(currentProcessingStatus);
        given(mockJurorResponse.getStaff()).willReturn(null);


        String jurorNumber = "1";
        String currentUser = "testuser";
        // Execute logic
        sendToCourtService.sendResponseToCourt(jurorNumber, new ResponseSendToCourtController.SendToCourtDto(1),
            currentUser);

        // Verify mock interactions
        verify(entityManager).detach(mockJurorResponse);
        verify(assignOnUpdateService).assignToCurrentLogin(any(DigitalResponse.class), eq(currentUser));
        verify(mockJurorResponse).setVersion(1);
        verify(mockJurorResponse).setProcessingStatus(ProcessingStatus.CLOSED);
        verify(mockJurorResponse).setProcessingComplete(Boolean.TRUE);
        verify(jurorResponseRepository).save(mockJurorResponse);
        verify(jurorResponseAuditRepository).save(any(JurorResponseAuditMod.class));


    }

    @Test(expected = ResponseSendToCourtServiceImpl.ResponseAlreadyCompleted.class)
    public void juror_unhappy_jurorNotFound() throws Exception {

        String jurorNumber = "1";
        String auditorUsername = "testuser";

        // Configure mocks
        DigitalResponse mockJurorResponse = mock(DigitalResponse.class);
        given(jurorResponseRepository.findByJurorNumber(any(String.class))).willReturn(mockJurorResponse);
        given(mockJurorResponse.getProcessingComplete()).willReturn(Boolean.TRUE);


        // Execute logic
        sendToCourtService.sendResponseToCourt(jurorNumber, new ResponseSendToCourtController.SendToCourtDto(1),
            auditorUsername);


    }
}
