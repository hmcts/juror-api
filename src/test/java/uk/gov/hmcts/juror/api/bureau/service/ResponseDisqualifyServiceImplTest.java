package uk.gov.hmcts.juror.api.bureau.service;

import jakarta.persistence.EntityManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.juror.api.bureau.controller.ResponseDisqualifyController.DisqualifyCodeDto;
import uk.gov.hmcts.juror.api.bureau.domain.DisqualifyCodeEntity;
import uk.gov.hmcts.juror.api.bureau.domain.DisqualifyCodeRepository;
import uk.gov.hmcts.juror.api.bureau.domain.IPoolStatus;
import uk.gov.hmcts.juror.api.bureau.domain.JurorResponseAudit;
import uk.gov.hmcts.juror.api.bureau.domain.JurorResponseAuditRepository;
import uk.gov.hmcts.juror.api.bureau.domain.PartHist;
import uk.gov.hmcts.juror.api.bureau.domain.PartHistRepository;
import uk.gov.hmcts.juror.api.bureau.exception.DisqualifyException;
import uk.gov.hmcts.juror.api.juror.domain.DisqualificationLetter;
import uk.gov.hmcts.juror.api.juror.domain.DisqualificationLetterRepository;
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
public class ResponseDisqualifyServiceImplTest {

    @Mock
    private JurorResponseRepository jurorResponseRepository;

    @Mock
    private JurorResponseAuditRepository jurorResponseAuditRepository;

    @Mock
    private PoolRepository poolRepository;

    @Mock
    private PartHistRepository partHistRepository;

    @Mock
    private DisqualifyCodeRepository disqualifyCodeRepository;

    @Mock
    private DisqualificationLetterRepository disqualificationLetterRepository;

    @Mock
    private ResponseMergeService mergeService;

    @Mock
    private EntityManager entityManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AssignOnUpdateService assignOnUpdateService;

    @InjectMocks
    private ResponseDisqualifyServiceImpl responseDisqualifyService;

    @Test
    public void getDisqualifyReasons_happy() throws Exception {
        List<DisqualifyCodeEntity> disqualifyReasonsList = new ArrayList<>();
        disqualifyReasonsList.add(new DisqualifyCodeEntity("A", "Description"));
        disqualifyReasonsList.add(new DisqualifyCodeEntity("B", "Description"));
        disqualifyReasonsList.add(new DisqualifyCodeEntity("C", "Description"));
        given(disqualifyCodeRepository.findAll()).willReturn(disqualifyReasonsList);

        List<DisqualifyCodeDto> disqualifyReasonsListDto = new ArrayList<>();
        for (DisqualifyCodeEntity disqualifyCodeEntity : disqualifyReasonsList) {
            disqualifyReasonsListDto.add(new DisqualifyCodeDto(1,
                disqualifyCodeEntity.getDisqualifyCode(), disqualifyCodeEntity.getDescription()));
        }

        List<DisqualifyCodeDto> retrievedDisqualifyReasonsList = responseDisqualifyService.getDisqualifyReasons();

        assertThat(retrievedDisqualifyReasonsList.size()).isEqualTo(disqualifyReasonsListDto.size());
    }

    @Test
    public void disqualifyJuror_happy() throws Exception {
        String disqualifyCode = "B";
        String jurorId = "123456789";
        String login = "login";
        DisqualifyCodeDto disqualifyCodeDto = new DisqualifyCodeDto(1, disqualifyCode, "A code");

        // configure mocks
        JurorResponse jurorResponse = mock(JurorResponse.class);
        given(jurorResponse.getJurorNumber()).willReturn(jurorId);
        given(jurorResponseRepository.findByJurorNumber(any(String.class))).willReturn(jurorResponse);

        Pool poolDetails = mock(Pool.class);
        given(poolRepository.findByJurorNumber(any(String.class))).willReturn(poolDetails);

        List<DisqualifyCodeEntity> disqualifyCodeEntityList = new ArrayList<>();
        disqualifyCodeEntityList.add(new DisqualifyCodeEntity("B", "Description of code"));
        disqualifyCodeEntityList.add(new DisqualifyCodeEntity(disqualifyCode, "Description of another code"));
        given(disqualifyCodeRepository.findAll()).willReturn(disqualifyCodeEntityList);

        // run process
        boolean result = responseDisqualifyService.disqualifyJuror(jurorId, disqualifyCodeDto, login);

        // assertions
        assertThat(result).isTrue();
        verify(jurorResponseRepository).findByJurorNumber(any(String.class));
        verify(jurorResponse).setProcessingStatus(ProcessingStatus.CLOSED);
        verify(mergeService).mergeResponse(jurorResponse, login);

        verify(jurorResponseAuditRepository).save(any(JurorResponseAudit.class));

        verify(poolDetails).setResponded(Pool.RESPONDED);
        verify(poolDetails).setDisqualifyDate(any(Date.class));
        verify(poolDetails).setDisqualifyCode(disqualifyCode);
        verify(poolDetails).setUserEdtq(login);
        verify(poolDetails).setStatus(IPoolStatus.DISQUALIFIED);
        verify(poolDetails).setHearingDate(null);
        verify(poolRepository).save(poolDetails);

        verify(partHistRepository).save(any(PartHist.class));
        verify(disqualificationLetterRepository).save(any(DisqualificationLetter.class));
    }

    @Test
    public void disqualifyJuror_unhappy_disqualifyCodeNotValid() throws Exception {
        String disqualifyCode = "A";
        String jurorId = "123456789";
        String login = "login";
        DisqualifyCodeDto disqualifyCodeDto = new DisqualifyCodeDto(1, disqualifyCode, "A code");

        // configure mocks
        JurorResponse jurorResponse = mock(JurorResponse.class);
        Pool poolDetails = mock(Pool.class);

        List<DisqualifyCodeEntity> disqualifyCodeEntityList = new ArrayList<>();
        // Add codes to list, but not the one we are using in this test
        disqualifyCodeEntityList.add(new DisqualifyCodeEntity("B", "Description of code"));
        disqualifyCodeEntityList.add(new DisqualifyCodeEntity("C", "Description of another code"));
        given(disqualifyCodeRepository.findAll()).willReturn(disqualifyCodeEntityList);

        // run process
        try {
            responseDisqualifyService.disqualifyJuror(jurorId, disqualifyCodeDto, login);
            fail("Expected RequestedCodeNotValid to be thrown");
        } catch (DisqualifyException e) {
            // assertions
            assertThat(e.getClass()).isEqualTo(DisqualifyException.RequestedCodeNotValid.class);
            verify(jurorResponseRepository, times(0)).findByJurorNumber(any(String.class));
            verify(jurorResponse, times(0)).setProcessingStatus(ProcessingStatus.CLOSED);
            verify(jurorResponseRepository, times(0)).save(jurorResponse);

            verify(jurorResponseAuditRepository, times(0)).save(any(JurorResponseAudit.class));

            verify(poolDetails, times(0)).setResponded(Pool.RESPONDED);
            verify(poolDetails, times(0)).setDisqualifyDate(any(Date.class));
            verify(poolDetails, times(0)).setExcusalCode(disqualifyCode);
            verify(poolDetails, times(0)).setUserEdtq(login);
            verify(poolDetails, times(0)).setStatus(IPoolStatus.DISQUALIFIED);
            verify(poolDetails, times(0)).setHearingDate(null);
            verify(poolRepository, times(0)).save(poolDetails);

            verify(partHistRepository, times(0)).save(any(PartHist.class));
            verify(disqualificationLetterRepository, times(0)).save(any(DisqualificationLetter.class));
        }
    }

    @Test
    public void disqualifyJuror_unhappy_jurorNotFound() throws Exception {
        String disqualifyCode = "A";
        String jurorId = "123456789";
        String login = "login";
        DisqualifyCodeDto disqualifyCodeDto = new DisqualifyCodeDto(1, disqualifyCode, "A code");

        // configure mocks
        JurorResponse jurorResponse = mock(JurorResponse.class);
        given(jurorResponseRepository.findByJurorNumber(any(String.class))).willReturn(null);
        Pool poolDetails = mock(Pool.class);

        List<DisqualifyCodeEntity> disqualifyCodeEntityList = new ArrayList<>();
        disqualifyCodeEntityList.add(new DisqualifyCodeEntity("B", "Description of code"));
        disqualifyCodeEntityList.add(new DisqualifyCodeEntity(disqualifyCode, "Description of another code"));
        given(disqualifyCodeRepository.findAll()).willReturn(disqualifyCodeEntityList);

        // run process
        try {
            responseDisqualifyService.disqualifyJuror(jurorId, disqualifyCodeDto, login);
            fail("Expected JurorNotFound to be thrown");
        } catch (DisqualifyException e) {
            // assertions
            assertThat(e.getClass()).isEqualTo(DisqualifyException.JurorNotFound.class);
            verify(jurorResponseRepository, times(1)).findByJurorNumber(any(String.class));
            verify(jurorResponse, times(0)).setProcessingStatus(ProcessingStatus.CLOSED);
            verify(jurorResponseRepository, times(0)).save(jurorResponse);

            verify(jurorResponseAuditRepository, times(0)).save(any(JurorResponseAudit.class));

            verify(poolDetails, times(0)).setResponded(Pool.RESPONDED);
            verify(poolDetails, times(0)).setDisqualifyDate(any(Date.class));
            verify(poolDetails, times(0)).setExcusalCode(disqualifyCode);
            verify(poolDetails, times(0)).setUserEdtq(login);
            verify(poolDetails, times(0)).setStatus(IPoolStatus.DISQUALIFIED);
            verify(poolDetails, times(0)).setHearingDate(null);
            verify(poolRepository, times(0)).save(poolDetails);

            verify(partHistRepository, times(0)).save(any(PartHist.class));
            verify(disqualificationLetterRepository, times(0)).save(any(DisqualificationLetter.class));
        }
    }
}