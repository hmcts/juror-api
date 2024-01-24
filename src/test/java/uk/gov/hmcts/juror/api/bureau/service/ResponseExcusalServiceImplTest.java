package uk.gov.hmcts.juror.api.bureau.service;

import jakarta.persistence.EntityManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.juror.api.bureau.controller.ResponseExcusalController.ExcusalCodeDto;
import uk.gov.hmcts.juror.api.bureau.domain.ExcusalCodeEntity;
import uk.gov.hmcts.juror.api.bureau.domain.ExcusalCodeRepository;
import uk.gov.hmcts.juror.api.bureau.domain.IPoolStatus;
import uk.gov.hmcts.juror.api.bureau.domain.JurorResponseAudit;
import uk.gov.hmcts.juror.api.bureau.domain.JurorResponseAuditRepository;
import uk.gov.hmcts.juror.api.bureau.domain.PartHist;
import uk.gov.hmcts.juror.api.bureau.domain.PartHistRepository;
import uk.gov.hmcts.juror.api.bureau.exception.ExcusalException;
import uk.gov.hmcts.juror.api.juror.domain.ExcusalDeniedLetter;
import uk.gov.hmcts.juror.api.juror.domain.ExcusalDeniedLetterRepository;
import uk.gov.hmcts.juror.api.juror.domain.ExcusalLetter;
import uk.gov.hmcts.juror.api.juror.domain.ExcusalLetterRepository;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponse;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponseRepository;
import uk.gov.hmcts.juror.api.juror.domain.Pool;
import uk.gov.hmcts.juror.api.juror.domain.PoolRepository;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.repository.UserRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ResponseExcusalServiceImplTest {

    @Mock
    private JurorResponseRepository jurorResponseRepository;

    @Mock
    private JurorResponseAuditRepository jurorResponseAuditRepository;

    @Mock
    private PoolRepository poolRepository;

    @Mock
    private PartHistRepository partHistRepository;

    @Mock
    private ExcusalCodeRepository excusalCodeRepository;

    @Mock
    private ExcusalLetterRepository excusalLetterRepository;

    @Mock
    private ExcusalDeniedLetterRepository excusalDeniedLetterRepository;

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

    @Test
    public void getExcusalReasons_happy() throws Exception {
        List<ExcusalCodeEntity> excusalReasonsList = new ArrayList<>();
        excusalReasonsList.add(new ExcusalCodeEntity("A", "Description"));
        excusalReasonsList.add(new ExcusalCodeEntity("B", "Description"));
        excusalReasonsList.add(new ExcusalCodeEntity("C", "Description"));
        given(excusalCodeRepository.findAll()).willReturn(excusalReasonsList);

        List<ExcusalCodeDto> excusalReasonsListDto = new ArrayList<>();
        for (ExcusalCodeEntity excusalCodeEntity : excusalReasonsList) {
            excusalReasonsListDto.add(new ExcusalCodeDto(excusalCodeEntity));
        }

        List<ExcusalCodeDto> retrievedExcusalReasonsList = responseExcusalService.getExcusalReasons();

        assertThat(retrievedExcusalReasonsList.size()).isEqualTo(excusalReasonsListDto.size());
    }

    @Test
    public void excuseJuror_happy() throws Exception {
        String excusalCode = "B";
        String jurorId = "123456789";
        String login = "login";
        ExcusalCodeDto excusalCodeDto = new ExcusalCodeDto(1, excusalCode, "Description");

        // configure mocks
        JurorResponse jurorResponse = mock(JurorResponse.class);
        given(jurorResponse.getJurorNumber()).willReturn(jurorId);
        given(jurorResponseRepository.findByJurorNumber(any(String.class))).willReturn(jurorResponse);

        Pool poolDetails = mock(Pool.class);
        given(poolRepository.findByJurorNumber(any(String.class))).willReturn(poolDetails);

        List<ExcusalCodeEntity> excusalCodeEntityList = new ArrayList<>();
        excusalCodeEntityList.add(new ExcusalCodeEntity("C", "Description of code"));
        excusalCodeEntityList.add(new ExcusalCodeEntity(excusalCode, "Description of another code"));
        given(excusalCodeRepository.findAll()).willReturn(excusalCodeEntityList);

        // run process
        boolean result = responseExcusalService.excuseJuror(jurorId, excusalCodeDto, login);

        // assertions
        assertThat(result).isEqualTo(true);
        verify(jurorResponseRepository).findByJurorNumber(any(String.class));
        verify(jurorResponse).setProcessingStatus(ProcessingStatus.CLOSED);
        verify(mergeService).mergeResponse(jurorResponse, login);

        verify(jurorResponseAuditRepository).save(any(JurorResponseAudit.class));

        verify(poolDetails).setResponded(Pool.RESPONDED);
        verify(poolDetails).setExcusalDate(any(Date.class));
        verify(poolDetails).setExcusalCode(excusalCode);
        verify(poolDetails).setUserEdtq(login);
        verify(poolDetails).setStatus(IPoolStatus.EXCUSED);
        verify(poolDetails).setHearingDate(null);
        verify(poolRepository).save(poolDetails);

        verify(partHistRepository).save(any(PartHist.class));
        verify(excusalLetterRepository).save(any(ExcusalLetter.class));
    }

    @Test
    public void excuseJuror_unhappy_excusalCodeNotValid() throws Exception {
        String excusalCode = "A";
        String jurorId = "123456789";
        String login = "login";
        ExcusalCodeDto excusalCodeDto = new ExcusalCodeDto(1, excusalCode, "Description");

        // configure mocks
        JurorResponse jurorResponse = mock(JurorResponse.class);
        Pool poolDetails = mock(Pool.class);

        List<ExcusalCodeEntity> excusalCodeEntityList = new ArrayList<>();
        // Add codes to list, but not the one we are using in this test
        excusalCodeEntityList.add(new ExcusalCodeEntity("B", "Description of code"));
        excusalCodeEntityList.add(new ExcusalCodeEntity("C", "Description of another code"));
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

            verify(jurorResponseAuditRepository, times(0)).save(any(JurorResponseAudit.class));

            verify(poolDetails, times(0)).setResponded(Pool.RESPONDED);
            verify(poolDetails, times(0)).setExcusalDate(any(Date.class));
            verify(poolDetails, times(0)).setExcusalCode(excusalCode);
            verify(poolDetails, times(0)).setUserEdtq(login);
            verify(poolDetails, times(0)).setStatus(IPoolStatus.EXCUSED);
            verify(poolDetails, times(0)).setHearingDate(null);
            verify(poolRepository, times(0)).save(poolDetails);

            verify(partHistRepository, times(0)).save(any(PartHist.class));
            verify(excusalLetterRepository, times(0)).save(any(ExcusalLetter.class));
        }
    }

    @Test
    public void excuseJuror_unhappy_jurorNotFound() throws Exception {
        String excusalCode = "A";
        String jurorId = "123456789";
        String login = "login";
        ExcusalCodeDto excusalCodeDto = new ExcusalCodeDto(1, excusalCode, "Description");

        // configure mocks
        JurorResponse jurorResponse = mock(JurorResponse.class);
        given(jurorResponseRepository.findByJurorNumber(any(String.class))).willReturn(null);
        Pool poolDetails = mock(Pool.class);

        List<ExcusalCodeEntity> excusalCodeEntityList = new ArrayList<>();
        excusalCodeEntityList.add(new ExcusalCodeEntity("B", "Description of code"));
        excusalCodeEntityList.add(new ExcusalCodeEntity(excusalCode, "Description of another code"));
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

            verify(jurorResponseAuditRepository, times(0)).save(any(JurorResponseAudit.class));

            verify(poolDetails, times(0)).setResponded(Pool.RESPONDED);
            verify(poolDetails, times(0)).setExcusalDate(any(Date.class));
            verify(poolDetails, times(0)).setExcusalCode(excusalCode);
            verify(poolDetails, times(0)).setUserEdtq(login);
            verify(poolDetails, times(0)).setStatus(IPoolStatus.EXCUSED);
            verify(poolDetails, times(0)).setHearingDate(null);
            verify(poolRepository, times(0)).save(poolDetails);

            verify(partHistRepository, times(0)).save(any(PartHist.class));
            verify(excusalLetterRepository, times(0)).save(any(ExcusalLetter.class));
        }
    }

    @Test
    public void rejectExcusalRequest_happy() throws Exception {
        String excusalCode = "B";
        String jurorId = "123456789";
        String login = "login";
        ExcusalCodeDto excusalCodeDto = new ExcusalCodeDto(1, excusalCode, "Description");

        // configure mocks
        JurorResponse jurorResponse = mock(JurorResponse.class);
        given(jurorResponse.getJurorNumber()).willReturn(jurorId);
        given(jurorResponseRepository.findByJurorNumber(any(String.class))).willReturn(jurorResponse);

        Pool poolDetails = mock(Pool.class);
        given(poolRepository.findByJurorNumber(any(String.class))).willReturn(poolDetails);

        List<ExcusalCodeEntity> excusalCodeEntityList = new ArrayList<>();
        excusalCodeEntityList.add(new ExcusalCodeEntity("C", "Description of code"));
        excusalCodeEntityList.add(new ExcusalCodeEntity(excusalCode, "Description of another code"));
        given(excusalCodeRepository.findAll()).willReturn(excusalCodeEntityList);

        // run process
        boolean result = responseExcusalService.rejectExcusalRequest(jurorId, excusalCodeDto, login);

        // assertions
        assertThat(result).isEqualTo(true);
        verify(jurorResponseRepository).findByJurorNumber(any(String.class));
        verify(jurorResponse).setProcessingStatus(ProcessingStatus.CLOSED);
        verify(mergeService).mergeResponse(jurorResponse, login);

        verify(jurorResponseAuditRepository).save(any(JurorResponseAudit.class));

        verify(poolDetails).setResponded(Pool.RESPONDED);
        verify(poolDetails).setExcusalDate(any(Date.class));
        verify(poolDetails).setExcusalCode(excusalCode);
        verify(poolDetails).setUserEdtq(login);
        verify(poolDetails).setExcusalRejected("Y");
        verify(poolDetails).setStatus(IPoolStatus.RESPONDED);
        verify(poolRepository).save(poolDetails);

        verify(partHistRepository, times(2)).save(any(PartHist.class));
        verify(excusalDeniedLetterRepository).save(any(ExcusalDeniedLetter.class));
    }

    @Test
    public void rejectExcusalRequest_unhappy_excusalCodeNotValid() throws Exception {
        String excusalCode = "A";
        String jurorId = "123456789";
        String login = "login";
        ExcusalCodeDto excusalCodeDto = new ExcusalCodeDto(1, excusalCode, "Description");

        // configure mocks
        JurorResponse jurorResponse = mock(JurorResponse.class);
        Pool poolDetails = mock(Pool.class);

        List<ExcusalCodeEntity> excusalCodeEntityList = new ArrayList<>();
        // Add codes to list, but not the one we are using in this test
        excusalCodeEntityList.add(new ExcusalCodeEntity("B", "Description of code"));
        excusalCodeEntityList.add(new ExcusalCodeEntity("C", "Description of another code"));
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

            verify(jurorResponseAuditRepository, times(0)).save(any(JurorResponseAudit.class));

            verify(poolDetails, times(0)).setResponded(Pool.RESPONDED);
            verify(poolDetails, times(0)).setExcusalDate(any(Date.class));
            verify(poolDetails, times(0)).setExcusalCode(excusalCode);
            verify(poolDetails, times(0)).setUserEdtq(login);
            verify(poolDetails, times(0)).setExcusalRejected("Y");
            verify(poolDetails, times(0)).setStatus(IPoolStatus.RESPONDED);
            verify(poolRepository, times(0)).save(poolDetails);

            verify(partHistRepository, times(0)).save(any(PartHist.class));
            verify(excusalDeniedLetterRepository, times(0)).save(any(ExcusalDeniedLetter.class));
        }
    }

    @Test
    public void rejectExcusalRequest_unhappy_jurorNotFound() throws Exception {
        String excusalCode = "A";
        String jurorId = "123456789";
        String login = "login";
        ExcusalCodeDto excusalCodeDto = new ExcusalCodeDto(1, excusalCode, "Description");

        // configure mocks
        JurorResponse jurorResponse = mock(JurorResponse.class);
        given(jurorResponseRepository.findByJurorNumber(any(String.class))).willReturn(null);
        Pool poolDetails = mock(Pool.class);

        List<ExcusalCodeEntity> excusalCodeEntityList = new ArrayList<>();
        excusalCodeEntityList.add(new ExcusalCodeEntity("B", "Description of code"));
        excusalCodeEntityList.add(new ExcusalCodeEntity(excusalCode, "Description of another code"));
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

            verify(jurorResponseAuditRepository, times(0)).save(any(JurorResponseAudit.class));

            verify(poolDetails, times(0)).setResponded(Pool.RESPONDED);
            verify(poolDetails, times(0)).setExcusalDate(any(Date.class));
            verify(poolDetails, times(0)).setExcusalCode(excusalCode);
            verify(poolDetails, times(0)).setUserEdtq(login);
            verify(poolDetails, times(0)).setExcusalRejected("Y");
            verify(poolDetails, times(0)).setStatus(IPoolStatus.RESPONDED);
            verify(poolRepository, times(0)).save(poolDetails);

            verify(partHistRepository, times(0)).save(any(PartHist.class));
            verify(excusalDeniedLetterRepository, times(0)).save(any(ExcusalDeniedLetter.class));
        }
    }
}