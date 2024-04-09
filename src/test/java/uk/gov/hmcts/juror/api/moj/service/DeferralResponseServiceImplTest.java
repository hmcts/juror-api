package uk.gov.hmcts.juror.api.moj.service;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.bureau.domain.ExcusalCodeEntity;
import uk.gov.hmcts.juror.api.bureau.domain.ExcusalCodeRepository;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.request.DeferralRequestDto;
import uk.gov.hmcts.juror.api.moj.domain.DeferralDecision;
import uk.gov.hmcts.juror.api.moj.domain.ExcusalCode;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorStatusRepository;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class DeferralResponseServiceImplTest {

    @Mock
    private ExcusalCodeRepository excusalCodeRepository;
    @Mock
    private JurorRepository jurorRepository;
    @Mock
    private JurorPoolRepository jurorPoolRepository;
    @Mock
    private JurorStatusRepository jurorStatusRepository;
    @Mock
    private JurorHistoryRepository jurorHistoryRepository;
    @Mock
    private PrintDataService printDataService;


    @InjectMocks
    private DeferralResponseServiceImpl deferralResponseService;


    @Before
    public void setUpMocks() {
        ExcusalCode excusalCodeEntity = new ExcusalCode("B", "Description of B",false,true,false,false);
        when(excusalCodeRepository.findAll()).thenReturn(Collections.singletonList(excusalCodeEntity));

        doReturn(Collections.singletonList(createTestJurorPool("400", "123456789"))).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc("123456789", true);
        doReturn(Collections.singletonList(createTestJurorPool("415", "987654321"))).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc("987654321", true);

        doReturn(Optional.of(createJurorStatus(1))).when(jurorStatusRepository).findById(1);
        doReturn(Optional.of(createJurorStatus(2))).when(jurorStatusRepository).findById(2);

        doReturn(null).when(jurorHistoryRepository).save(any());
    }


    @Test
    public void test_denyDeferralRequest_happyPath_courtUser_courtOwner() {
        String jurorNumber = "987654321";
        BureauJwtPayload payload = TestUtils.createJwt("415", "SOME_USER");

        DeferralRequestDto deferralRequestDto = createTestDeferralRequestDto(jurorNumber);

        deferralResponseService.respondToDeferralRequest(payload, deferralRequestDto);

        verify(jurorPoolRepository, times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), anyBoolean());

        verify(jurorPoolRepository, times(1)).save(any());
        verify(jurorHistoryRepository, times(1)).save(any());
        verify(printDataService, never()).printDeferralDeniedLetter(any());

    }

    @Test
    public void test_denyDeferralRequest_happyPath_bureauUser_bureauOwner() {
        String jurorNumber = "123456789";
        BureauJwtPayload payload = TestUtils.createJwt("400", "BUREAU_USER");

        DeferralRequestDto deferralRequestDto = createTestDeferralRequestDto(jurorNumber);

        deferralResponseService.respondToDeferralRequest(payload, deferralRequestDto);

        verify(jurorPoolRepository, times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), anyBoolean());

        verify(jurorPoolRepository, times(1)).save(any());
        verify(jurorHistoryRepository, times(2)).save(any());
        verify(printDataService, times(1)).printDeferralDeniedLetter(any());
    }

    @Test
    public void test_denyDeferralRequest_bureauUser_courtOwner() {
        String jurorNumber = "987654321";
        BureauJwtPayload payload = TestUtils.createJwt("400", "SOME_USER");

        DeferralRequestDto deferralRequestDto = createTestDeferralRequestDto(jurorNumber);

        Assertions.assertThatExceptionOfType(MojException.Forbidden.class)
            .isThrownBy(() -> {
                deferralResponseService.respondToDeferralRequest(payload, deferralRequestDto);
            });

        verify(jurorPoolRepository, times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), anyBoolean());

        verify(jurorPoolRepository, never()).save(any());
        verify(jurorHistoryRepository, never()).save(any());
        verify(printDataService, never()).printDeferralDeniedLetter(any());
    }

    @Test
    public void test_denyDeferralRequest_courtUser_bureauOwner() {
        String jurorNumber = "123456789";
        BureauJwtPayload payload = TestUtils.createJwt("415", "SOME_USER");

        DeferralRequestDto deferralRequestDto = createTestDeferralRequestDto(jurorNumber);

        Assertions.assertThatExceptionOfType(MojException.Forbidden.class)
            .isThrownBy(() -> deferralResponseService.respondToDeferralRequest(payload, deferralRequestDto));

        verify(jurorPoolRepository, times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), anyBoolean());


        verify(jurorPoolRepository, never()).save(any());
        verify(jurorHistoryRepository, never()).save(any());
        verify(printDataService, never()).printDeferralDeniedLetter(any());

    }

    @Test
    public void test_denyDeferralRequest_noJurorRecord() {
        String jurorNumber = "111111111";
        BureauJwtPayload payload = TestUtils.createJwt("400", "SOME_USER");

        DeferralRequestDto deferralRequestDto = createTestDeferralRequestDto(jurorNumber);

        Assertions.assertThatExceptionOfType(MojException.NotFound.class)
            .isThrownBy(() -> deferralResponseService.respondToDeferralRequest(payload, deferralRequestDto));

        verify(jurorPoolRepository, times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), anyBoolean());


        verify(jurorPoolRepository, never()).save(any());
        verify(jurorHistoryRepository, never()).save(any());
        verify(printDataService, never()).printDeferralDeniedLetter(any());

    }

    private DeferralRequestDto createTestDeferralRequestDto(String jurorNumber) {
        DeferralRequestDto deferralRequestDto = new DeferralRequestDto();
        deferralRequestDto.setJurorNumber(jurorNumber);
        deferralRequestDto.setDeferralReason("B");
        deferralRequestDto.setDeferralDecision(DeferralDecision.REFUSE);
        return deferralRequestDto;
    }

    private JurorPool createTestJurorPool(String owner, String jurorNumber) {
        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCourtName("CHESTER");
        courtLocation.setLocCode("415");

        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber("415230101");
        poolRequest.setCourtLocation(courtLocation);

        Juror juror = new Juror();
        juror.setJurorNumber(jurorNumber);
        juror.setFirstName("jurorPool1");
        juror.setLastName("jurorPool1L");
        juror.setPostcode("M24 4GT");
        juror.setNoDefPos(1);

        JurorStatus jurorStatus = new JurorStatus();
        jurorStatus.setStatus(1);
        jurorStatus.setStatusDesc("Responded");
        jurorStatus.setActive(true);

        JurorPool jurorPool = new JurorPool();
        jurorPool.setOwner(owner);
        jurorPool.setStatus(jurorStatus);
        jurorPool.setPool(poolRequest);

        juror.setAssociatedPools(Set.of(jurorPool));
        jurorPool.setJuror(juror);

        return jurorPool;
    }

    private JurorStatus createJurorStatus(int statusCode) {
        JurorStatus jurorStatus = new JurorStatus();
        jurorStatus.setStatus(statusCode);
        return jurorStatus;
    }
}
