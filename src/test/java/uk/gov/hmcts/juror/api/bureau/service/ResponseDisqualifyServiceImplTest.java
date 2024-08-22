package uk.gov.hmcts.juror.api.bureau.service;

import jakarta.persistence.EntityManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.juror.api.bureau.controller.ResponseDisqualifyController.DisqualifyCodeDto;
import uk.gov.hmcts.juror.api.bureau.exception.DisqualifyException;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.domain.DisqualifiedCode;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.JurorResponseAuditMod;
import uk.gov.hmcts.juror.api.moj.repository.DisqualifiedCodeRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorStatusRepository;
import uk.gov.hmcts.juror.api.moj.repository.UserRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorResponseAuditRepositoryMod;
import uk.gov.hmcts.juror.api.moj.service.JurorHistoryService;
import uk.gov.hmcts.juror.api.moj.service.JurorPoolService;
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
public class ResponseDisqualifyServiceImplTest {

    @Mock
    private JurorDigitalResponseRepositoryMod jurorResponseRepository;
    @Mock
    private JurorResponseAuditRepositoryMod jurorResponseAuditRepository;
    @Mock
    private JurorStatusRepository jurorStatusRepository;
    @Mock
    private JurorPoolRepository poolRepository;
    @Mock
    private JurorHistoryService jurorHistoryService;
    @Mock
    private DisqualifiedCodeRepository disqualifyCodeRepository;
    @Mock
    private ResponseMergeService mergeService;
    @Mock
    private EntityManager entityManager;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AssignOnUpdateService assignOnUpdateService;
    @Mock
    private PrintDataService printDataService;
    @Mock
    private JurorPoolService jurorPoolService;

    @InjectMocks
    private ResponseDisqualifyServiceImpl responseDisqualifyService;

    @Test
    public void getDisqualifyReasons_happy() throws Exception {
        List<DisqualifiedCode> disqualifyReasonsList = new ArrayList<>();
        disqualifyReasonsList.add(new DisqualifiedCode("A", "Description", true));
        disqualifyReasonsList.add(new DisqualifiedCode("B", "Description", true));
        disqualifyReasonsList.add(new DisqualifiedCode("C", "Description", true));
        given(disqualifyCodeRepository.findAll()).willReturn(disqualifyReasonsList);

        List<DisqualifyCodeDto> disqualifyReasonsListDto = new ArrayList<>();
        for (DisqualifiedCode disqualifyCodeEntity : disqualifyReasonsList) {
            disqualifyReasonsListDto.add(new DisqualifyCodeDto(1,
                disqualifyCodeEntity.getCode(), disqualifyCodeEntity.getDescription()));
        }

        List<DisqualifyCodeDto> retrievedDisqualifyReasonsList = responseDisqualifyService.getDisqualifyReasons();

        assertThat(retrievedDisqualifyReasonsList.size()).isEqualTo(disqualifyReasonsListDto.size());
    }

    @Test
    public void disqualifyJuror_happy() throws Exception {
        JurorStatus disqualifiedJurorStatus = mock(JurorStatus.class);
        when(jurorStatusRepository.findById(IJurorStatus.DISQUALIFIED))
            .thenReturn(Optional.ofNullable(disqualifiedJurorStatus));
        String jurorId = "123456789";

        // configure mocks
        DigitalResponse jurorResponse = mock(DigitalResponse.class);
        given(jurorResponse.getJurorNumber()).willReturn(jurorId);
        given(jurorResponseRepository.findByJurorNumber(any(String.class))).willReturn(jurorResponse);

        JurorPool poolDetails = mock(JurorPool.class);
        Juror juror = mock(Juror.class);
        when(poolDetails.getJuror()).thenReturn(juror);
        given(jurorPoolService.getJurorPoolFromUser(any(String.class))).willReturn(poolDetails);

        List<DisqualifiedCode> disqualifyCodeEntityList = new ArrayList<>();
        String disqualifyCode = "B";
        disqualifyCodeEntityList.add(new DisqualifiedCode("B", "Description of code", true));
        disqualifyCodeEntityList.add(new DisqualifiedCode(disqualifyCode, "Description of another code", true));
        given(disqualifyCodeRepository.findAll()).willReturn(disqualifyCodeEntityList);

        String login = "login";
        DisqualifyCodeDto disqualifyCodeDto = new DisqualifyCodeDto(1, disqualifyCode, "A code");
        // run process
        boolean result = responseDisqualifyService.disqualifyJuror(jurorId, disqualifyCodeDto, login);

        // assertions
        assertThat(result).isTrue();
        verify(jurorResponseRepository).findByJurorNumber(any(String.class));
        verify(jurorResponse).setProcessingStatus(jurorResponseAuditRepository, ProcessingStatus.CLOSED);
        verify(mergeService).mergeResponse(jurorResponse, login);

        verify(poolDetails, times(3)).getJuror();
        verify(juror).setResponded(true);
        verify(juror).setDisqualifyDate(any(LocalDate.class));
        verify(juror).setDisqualifyCode(disqualifyCode);
        verify(poolDetails).setUserEdtq(login);
        verify(poolDetails).setStatus(disqualifiedJurorStatus);
        verify(jurorStatusRepository).findById(IJurorStatus.DISQUALIFIED);
        verify(poolDetails).setNextDate(null);
        verify(poolRepository).save(poolDetails);

        verify(jurorHistoryService).createDisqualifyHistory(poolDetails, disqualifyCode);
        verify(printDataService).printWithdrawalLetter(poolDetails);
    }

    @Test
    public void disqualifyJuror_unhappy_disqualifyCodeNotValid() throws Exception {
        String disqualifyCode = "A";
        String jurorId = "123456789";
        String login = "login";
        DisqualifyCodeDto disqualifyCodeDto = new DisqualifyCodeDto(1, disqualifyCode, "A code");

        // configure mocks
        DigitalResponse jurorResponse = mock(DigitalResponse.class);
        JurorPool poolDetails = mock(JurorPool.class);

        List<DisqualifiedCode> disqualifyCodeEntityList = new ArrayList<>();
        // Add codes to list, but not the one we are using in this test
        disqualifyCodeEntityList.add(new DisqualifiedCode("B", "Description of code", true));
        disqualifyCodeEntityList.add(new DisqualifiedCode("C", "Description of another code", true));
        given(disqualifyCodeRepository.findAll()).willReturn(disqualifyCodeEntityList);

        // run process
        try {
            responseDisqualifyService.disqualifyJuror(jurorId, disqualifyCodeDto, login);
            fail("Expected RequestedCodeNotValid to be thrown");
        } catch (DisqualifyException e) {
            // assertions
            assertThat(e.getClass()).isEqualTo(DisqualifyException.RequestedCodeNotValid.class);
            verify(jurorResponseRepository, times(0)).findByJurorNumber(any(String.class));
            verify(jurorResponse, times(0)).setProcessingStatus(jurorResponseAuditRepository, ProcessingStatus.CLOSED);
            verify(jurorResponseRepository, times(0)).save(jurorResponse);

            verify(jurorResponseAuditRepository, times(0)).save(any(JurorResponseAuditMod.class));

            verify(poolDetails, times(0)).getJuror();
            verify(poolDetails, times(0)).setUserEdtq(login);
            verify(poolDetails, times(0)).setStatus(any());
            verify(poolDetails, times(0)).setNextDate(null);
            verify(poolRepository, times(0)).save(poolDetails);


            verifyNoInteractions(jurorHistoryService);
            verify(printDataService, times(0)).printWithdrawalLetter(any(JurorPool.class));
        }
    }

    @Test
    public void disqualifyJuror_unhappy_jurorNotFound() throws Exception {
        String disqualifyCode = "A";
        String jurorId = "123456789";
        String login = "login";
        DisqualifyCodeDto disqualifyCodeDto = new DisqualifyCodeDto(1, disqualifyCode, "A code");

        // configure mocks
        DigitalResponse jurorResponse = mock(DigitalResponse.class);
        given(jurorResponseRepository.findByJurorNumber(any(String.class))).willReturn(null);
        JurorPool poolDetails = mock(JurorPool.class);

        List<DisqualifiedCode> disqualifyCodeEntityList = new ArrayList<>();
        disqualifyCodeEntityList.add(new DisqualifiedCode("B", "Description of code", true));
        disqualifyCodeEntityList.add(new DisqualifiedCode(disqualifyCode, "Description of another code", true));
        given(disqualifyCodeRepository.findAll()).willReturn(disqualifyCodeEntityList);

        // run process
        try {
            responseDisqualifyService.disqualifyJuror(jurorId, disqualifyCodeDto, login);
            fail("Expected JurorNotFound to be thrown");
        } catch (DisqualifyException e) {
            // assertions
            assertThat(e.getClass()).isEqualTo(DisqualifyException.JurorNotFound.class);
            verify(jurorResponseRepository, times(1)).findByJurorNumber(any(String.class));
            verify(jurorResponse, times(0)).setProcessingStatus(jurorResponseAuditRepository, ProcessingStatus.CLOSED);
            verify(jurorResponseRepository, times(0)).save(jurorResponse);

            verify(jurorResponseAuditRepository, times(0)).save(any(JurorResponseAuditMod.class));

            verify(poolDetails, times(0)).getJuror();
            verify(poolDetails, times(0)).setUserEdtq(login);
            verify(poolDetails, times(0)).setStatus(any());
            verify(poolDetails, times(0)).setNextDate(null);
            verify(poolRepository, times(0)).save(poolDetails);

            verifyNoInteractions(jurorHistoryService);
            verify(printDataService, times(0)).printWithdrawalLetter(any(JurorPool.class));
        }
    }
}
