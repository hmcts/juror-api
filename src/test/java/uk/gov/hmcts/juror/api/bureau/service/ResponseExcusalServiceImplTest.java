package uk.gov.hmcts.juror.api.bureau.service;

import jakarta.persistence.EntityManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.juror.api.bureau.controller.ResponseExcusalController.ExcusalCodeDto;
import uk.gov.hmcts.juror.api.bureau.domain.ExcusalCodeRepository;
import uk.gov.hmcts.juror.api.bureau.exception.ExcusalException;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.domain.ExcusalCode;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.JurorResponseAuditMod;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorStatusRepository;
import uk.gov.hmcts.juror.api.moj.repository.UserRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorResponseAuditRepositoryMod;
import uk.gov.hmcts.juror.api.moj.service.PrintDataService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ResponseExcusalServiceImplTest {

    @Mock
    private JurorDigitalResponseRepositoryMod jurorResponseRepository;

    @Mock
    private JurorResponseAuditRepositoryMod jurorResponseAuditRepository;

    @Mock
    private JurorPoolRepository poolRepository;
    @Mock
    private JurorStatusRepository jurorStatusRepository;
    @Mock
    private JurorHistoryRepository partHistRepository;

    @Mock
    private ExcusalCodeRepository excusalCodeRepository;

    @Mock
    private ResponseMergeService mergeService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AssignOnUpdateService assignOnUpdateService;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private ResponseExcusalServiceImpl responseExcusalService;

    @Mock
    private PrintDataService printDataService;

    @Test
    public void getExcusalReasons_happy() throws Exception {
        List<ExcusalCode> excusalReasonsList = new ArrayList<>();
        excusalReasonsList.add(new ExcusalCode("A", "Description",false,true,false,false));
        excusalReasonsList.add(new ExcusalCode("B", "Description",false,true,false,false));
        excusalReasonsList.add(new ExcusalCode("C", "Description",false,true,false,false));
        given(excusalCodeRepository.findAll()).willReturn(excusalReasonsList);

        List<ExcusalCodeDto> excusalReasonsListDto = new ArrayList<>();
        excusalReasonsList.forEach(excusalCode -> excusalReasonsListDto.add(new ExcusalCodeDto(excusalCode)));

        List<ExcusalCodeDto> retrievedExcusalReasonsList = responseExcusalService.getExcusalReasons();

        assertThat(retrievedExcusalReasonsList.size()).isEqualTo(excusalReasonsListDto.size());
    }

    @Test
    public void excuseJuror_happy() throws Exception {
        JurorStatus excusedJurorStatus = mock(JurorStatus.class);
        when(jurorStatusRepository.findById(IJurorStatus.EXCUSED))
            .thenReturn(Optional.ofNullable(excusedJurorStatus));
        String jurorId = "123456789";

        // configure mocks
        DigitalResponse jurorResponse = mock(DigitalResponse.class);
        given(jurorResponse.getJurorNumber()).willReturn(jurorId);
        given(jurorResponseRepository.findByJurorNumber(any(String.class))).willReturn(jurorResponse);

        JurorPool poolDetails = mock(JurorPool.class);
        Juror juror = mock(Juror.class);
        when(poolDetails.getJuror()).thenReturn(juror);
        given(poolRepository.findByJurorJurorNumber(any(String.class))).willReturn(poolDetails);

        String excusalCode = "B";
        List<ExcusalCode> excusalCodeEntityList = new ArrayList<>();
        excusalCodeEntityList.add(new ExcusalCode("C", "Description of code",false,true,false,false));
        excusalCodeEntityList.add(new ExcusalCode(excusalCode, "Description of another code",false,true,false,false));
        given(excusalCodeRepository.findAll()).willReturn(excusalCodeEntityList);

        String login = "login";
        ExcusalCodeDto excusalCodeDto = new ExcusalCodeDto(1, excusalCode, "Description");
        // run process
        boolean result = responseExcusalService.excuseJuror(jurorId, excusalCodeDto, login);

        // assertions
        assertThat(result).isEqualTo(true);
        verify(jurorResponseRepository).findByJurorNumber(any(String.class));
        verify(jurorResponse).setProcessingStatus(ProcessingStatus.CLOSED);
        verify(mergeService).mergeResponse(jurorResponse, login);

        verify(jurorResponseAuditRepository).save(any(JurorResponseAuditMod.class));

        verify(poolDetails, times(3)).getJuror();
        verify(juror).setResponded(true);
        verify(juror).setExcusalDate(any(LocalDate.class));
        verify(juror).setExcusalCode(excusalCode);
        verify(poolDetails).setUserEdtq(login);
        verify(poolDetails).setStatus(excusedJurorStatus);
        verify(poolDetails).setNextDate(null);
        verify(poolRepository).save(poolDetails);

        verify(partHistRepository).save(any(JurorHistory.class));
        verify(printDataService).printExcusalLetter(poolDetails);
    }

    @Test
    public void excuseJuror_unhappy_excusalCodeNotValid() throws Exception {
        String excusalCode = "A";
        String jurorId = "123456789";
        String login = "login";
        ExcusalCodeDto excusalCodeDto = new ExcusalCodeDto(1, excusalCode, "Description");

        // configure mocks
        DigitalResponse jurorResponse = mock(DigitalResponse.class);
        JurorPool poolDetails = mock(JurorPool.class);

        List<ExcusalCode> excusalCodeEntityList = new ArrayList<>();
        // Add codes to list, but not the one we are using in this test
        excusalCodeEntityList.add(new ExcusalCode("B", "Description of code",false,true,false,false));
        excusalCodeEntityList.add(new ExcusalCode("C", "Description of another code",false,true,false,false));
        given(excusalCodeRepository.findAll()).willReturn(excusalCodeEntityList);

        // run process
        try {
            responseExcusalService.excuseJuror(jurorId, excusalCodeDto, login);
            fail("Expected RequestedCodeNotValid to be thrown");
        } catch (ExcusalException e) {
            // assertions
            assertThat(e.getClass()).isEqualTo(ExcusalException.RequestedCodeNotValid.class);
            verify(jurorResponseRepository, times(0)).findByJurorNumber(any(String.class));
            verify(jurorResponse, times(0)).setProcessingStatus(ProcessingStatus.CLOSED);
            verify(jurorResponseRepository, times(0)).save(jurorResponse);

            verify(jurorResponseAuditRepository, times(0)).save(any(JurorResponseAuditMod.class));

            verify(poolDetails, times(0)).getJuror();
            verify(poolDetails, times(0)).setUserEdtq(login);
            verify(poolDetails, times(0)).getStatus();
            verify(poolDetails, times(0)).setStatus(any());
            verify(poolDetails, times(0)).setNextDate(null);
            verify(poolRepository, times(0)).save(poolDetails);

            verify(partHistRepository, times(0)).save(any(JurorHistory.class));
            verifyNoInteractions(printDataService);
        }
    }

    @Test
    public void excuseJuror_unhappy_jurorNotFound() throws Exception {
        String excusalCode = "A";
        String jurorId = "123456789";
        String login = "login";
        ExcusalCodeDto excusalCodeDto = new ExcusalCodeDto(1, excusalCode, "Description");

        // configure mocks
        DigitalResponse jurorResponse = mock(DigitalResponse.class);
        given(jurorResponseRepository.findByJurorNumber(any(String.class))).willReturn(null);
        JurorPool poolDetails = mock(JurorPool.class);

        List<ExcusalCode> excusalCodeEntityList = new ArrayList<>();
        excusalCodeEntityList.add(new ExcusalCode("B", "Description of code",false,true,false,false));
        excusalCodeEntityList.add(new ExcusalCode(excusalCode, "Description of another code",false,true,false,false));
        given(excusalCodeRepository.findAll()).willReturn(excusalCodeEntityList);

        // run process
        try {
            responseExcusalService.excuseJuror(jurorId, excusalCodeDto, login);
            fail("Expected JurorNotFound to be thrown");
        } catch (ExcusalException e) {
            // assertions
            assertThat(e.getClass()).isEqualTo(ExcusalException.JurorNotFound.class);
            verify(jurorResponseRepository, times(1)).findByJurorNumber(any(String.class));
            verify(jurorResponse, times(0)).setProcessingStatus(ProcessingStatus.CLOSED);
            verify(jurorResponseRepository, times(0)).save(jurorResponse);

            verify(jurorResponseAuditRepository, times(0)).save(any(JurorResponseAuditMod.class));

            verify(poolDetails, times(0)).getJuror();
            verify(poolDetails, times(0)).setUserEdtq(login);
            verify(poolDetails, times(0)).getStatus();
            verify(poolDetails, times(0)).setStatus(any());
            verify(poolDetails, times(0)).setNextDate(null);
            verify(poolRepository, times(0)).save(poolDetails);

            verify(partHistRepository, times(0)).save(any(JurorHistory.class));
            verifyNoInteractions(printDataService);
        }
    }

    @Test
    public void rejectExcusalRequest_happy() throws Exception {
        JurorStatus respondedStatus = mock(JurorStatus.class);
        when(jurorStatusRepository.findById(IJurorStatus.RESPONDED))
            .thenReturn(Optional.of(respondedStatus));

        String jurorId = "123456789";

        // configure mocks
        DigitalResponse jurorResponse = mock(DigitalResponse.class);
        given(jurorResponse.getJurorNumber()).willReturn(jurorId);
        given(jurorResponseRepository.findByJurorNumber(any(String.class))).willReturn(jurorResponse);

        JurorPool poolDetails = mock(JurorPool.class);
        Juror juror = mock(Juror.class);
        when(poolDetails.getJuror()).thenReturn(juror);
        given(poolRepository.findByJurorJurorNumber(any(String.class))).willReturn(poolDetails);

        String excusalCode = "B";
        List<ExcusalCode> excusalCodeEntityList = new ArrayList<>();
        excusalCodeEntityList.add(new ExcusalCode("C", "Description of code",false,true,false,false));
        excusalCodeEntityList.add(new ExcusalCode(excusalCode, "Description of another code",false,true,false,false));
        given(excusalCodeRepository.findAll()).willReturn(excusalCodeEntityList);
        String login = "login";
        ExcusalCodeDto excusalCodeDto = new ExcusalCodeDto(1, excusalCode, "Description");

        // run process
        boolean result = responseExcusalService.rejectExcusalRequest(jurorId, excusalCodeDto, login);

        // assertions
        assertThat(result).isEqualTo(true);
        verify(jurorResponseRepository).findByJurorNumber(any(String.class));
        verify(jurorResponse).setProcessingStatus(ProcessingStatus.CLOSED);
        verify(mergeService).mergeResponse(jurorResponse, login);

        verify(jurorResponseAuditRepository).save(any(JurorResponseAuditMod.class));

        verify(poolDetails, times(4)).getJuror();
        verify(juror).setResponded(true);
        verify(juror).setExcusalDate(any(LocalDate.class));
        verify(juror).setExcusalCode(excusalCode);
        verify(juror).setExcusalRejected("Y");
        verify(poolDetails).setUserEdtq(login);
        verify(poolDetails).setStatus(respondedStatus);
        verify(jurorStatusRepository).findById(IJurorStatus.RESPONDED);
        verify(poolRepository).save(poolDetails);

        verify(partHistRepository, times(2)).save(any(JurorHistory.class));
        verify(printDataService).printExcusalDeniedLetter(poolDetails);
    }

    @Test
    public void rejectExcusalRequest_unhappy_excusalCodeNotValid() throws Exception {
        String excusalCode = "A";
        String jurorId = "123456789";
        String login = "login";
        ExcusalCodeDto excusalCodeDto = new ExcusalCodeDto(1, excusalCode, "Description");

        // configure mocks
        DigitalResponse jurorResponse = mock(DigitalResponse.class);
        JurorPool poolDetails = mock(JurorPool.class);

        List<ExcusalCode> excusalCodeEntityList = new ArrayList<>();
        // Add codes to list, but not the one we are using in this test
        excusalCodeEntityList.add(new ExcusalCode("B", "Description of code",false,true,false,false));
        excusalCodeEntityList.add(new ExcusalCode("C", "Description of another code",false,true,false,false));
        given(excusalCodeRepository.findAll()).willReturn(excusalCodeEntityList);

        // run process
        try {
            responseExcusalService.rejectExcusalRequest(jurorId, excusalCodeDto, login);
            fail("Expected RequestedCodeNotValid to be thrown");
        } catch (ExcusalException e) {
            // assertions
            assertThat(e.getClass()).isEqualTo(ExcusalException.RequestedCodeNotValid.class);
            verify(jurorResponseRepository, times(0)).findByJurorNumber(any(String.class));
            verify(jurorResponse, times(0)).setProcessingStatus(ProcessingStatus.CLOSED);
            verify(mergeService, times(0)).mergeResponse(jurorResponse, login);

            verify(jurorResponseAuditRepository, times(0)).save(any(JurorResponseAuditMod.class));

            verify(poolDetails, times(0)).getJuror();
            verify(poolDetails, times(0)).setUserEdtq(login);
            verify(poolDetails, times(0)).getStatus();
            verify(poolDetails, times(0)).setStatus(any(JurorStatus.class));
            verify(poolRepository, times(0)).save(poolDetails);

            verify(partHistRepository, times(0)).save(any(JurorHistory.class));
            verifyNoInteractions(printDataService);
        }
    }

    @Test
    public void rejectExcusalRequest_unhappy_jurorNotFound() throws Exception {
        String excusalCode = "A";
        String jurorId = "123456789";
        String login = "login";
        ExcusalCodeDto excusalCodeDto = new ExcusalCodeDto(1, excusalCode, "Description");

        // configure mocks
        DigitalResponse jurorResponse = mock(DigitalResponse.class);
        given(jurorResponseRepository.findByJurorNumber(any(String.class))).willReturn(null);
        JurorPool poolDetails = mock(JurorPool.class);
        List<ExcusalCode> excusalCodeEntityList = new ArrayList<>();
        excusalCodeEntityList.add(new ExcusalCode("B", "Description of code",false,true,false,false));
        excusalCodeEntityList.add(new ExcusalCode(excusalCode, "Description of another code",false,true,false,false));
        given(excusalCodeRepository.findAll()).willReturn(excusalCodeEntityList);

        // run process
        try {
            responseExcusalService.rejectExcusalRequest(jurorId, excusalCodeDto, login);
            fail("Expected JurorNotFound to be thrown");
        } catch (ExcusalException e) {
            // assertions
            assertThat(e.getClass()).isEqualTo(ExcusalException.JurorNotFound.class);
            verify(jurorResponse, times(0)).setProcessingStatus(ProcessingStatus.CLOSED);
            verify(mergeService, times(0)).mergeResponse(jurorResponse, login);

            verify(jurorResponseAuditRepository, times(0)).save(any(JurorResponseAuditMod.class));

            verify(poolDetails, times(0)).getJuror();
            verify(poolDetails, times(0)).setUserEdtq(login);
            verify(poolDetails, times(0)).getStatus();
            verify(poolDetails, times(0)).setStatus(any(JurorStatus.class));
            verify(poolRepository, times(0)).save(poolDetails);

            verify(partHistRepository, times(0)).save(any(JurorHistory.class));
            verifyNoInteractions(printDataService);
        }
    }
}
