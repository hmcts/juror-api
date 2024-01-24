package uk.gov.hmcts.juror.api.bureau.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.juror.api.bureau.notify.JurorCommsNotifyTemplateType;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponse;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponseRepository;
import uk.gov.hmcts.juror.api.juror.domain.Pool;
import uk.gov.hmcts.juror.api.juror.domain.PoolRepository;
import uk.gov.hmcts.juror.api.validation.ResponseInspector;

import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;


/**
 * Tests for {@link private JurorCommsSuperUrgentSendToCourtServiceImpl service;}.
 */
@SuppressWarnings("Duplicates")
@RunWith(MockitoJUnitRunner.class)
public class JurorCommsSuperUrgentSendToCourtServiceImplTest {

    private static final String JUROR_ID = "123456789";
    private static final String FIRST_NAME = "Watts";
    private static final String LAST_NAME = "John";

    private Pool poolDetails1;
    private JurorResponse jurorResponse1;
    private Date birthDate;
    private Date hearingDate;

    @Mock
    private PoolRepository poolRepository;

    @Mock
    private JurorResponseRepository jurorResponseRepository;

    @Mock
    private JurorCommsNotificationService jurorCommsNotificationService;

    @Mock
    private ResponseInspector responseInspector;

    @InjectMocks
    private JurorCommsSuperUrgentSendToCourtServiceImpl service;

    @Before
    public void setUp() {

        poolDetails1 = new Pool();
        jurorResponse1 = new JurorResponse();

        given(responseInspector.getYoungestJurorAgeAllowed()).willReturn(18);
        given(responseInspector.getTooOldJurorAge()).willReturn(76);
    }

    @Test
    public void process_HappyPath() {

        Pool poolDetails = Pool.builder()
            .jurorNumber(JUROR_ID)
            .firstName(FIRST_NAME)
            .lastName(LAST_NAME)
            .hearingDate(hearingDate)
            .welsh(false)
            .notifications(0)
            .build();

        JurorResponse jurorResponse = JurorResponse.builder()
            .jurorNumber(JUROR_ID)
            .firstName(FIRST_NAME)
            .lastName(LAST_NAME)
            .dateOfBirth(birthDate)
            .email("abcd@yahoo.com")
            .phoneNumber("07442231123")
            .welsh(false)
            .build();

        //given(poolRepository.findOne(any(String.class))).willReturn(poolDetails);
        given(poolRepository.findByJurorNumber(any(String.class))).willReturn(poolDetails);
        given(jurorResponseRepository.findByJurorNumber(any(String.class))).willReturn(jurorResponse);
        given(responseInspector.getJurorAgeAtHearingDate(birthDate,
            hearingDate)).willReturn(18);

        service.processSuperUrgent(JUROR_ID);
        verify(responseInspector, times(1)).getJurorAgeAtHearingDate(birthDate,
            hearingDate);
        verify(jurorCommsNotificationService, times(1)).sendJurorComms(any(Pool.class),
            any(JurorCommsNotifyTemplateType.class),
            eq(null), eq(null), anyBoolean());
        verify(poolRepository, times(1)).save(any(Pool.class));
    }

    @Test
    public void process_UnHappyPath_Too_Young() {
        given(poolRepository.findByJurorNumber(any(String.class))).willReturn(poolDetails1);
        given(jurorResponseRepository.findByJurorNumber(any(String.class))).willReturn(jurorResponse1);
        given(responseInspector.getJurorAgeAtHearingDate(birthDate,
            hearingDate)).willReturn(10);
        service.processSuperUrgent(JUROR_ID);
        verify(responseInspector, times(1)).getJurorAgeAtHearingDate(birthDate,
            hearingDate);
        verifyNoInteractions(jurorCommsNotificationService);
    }

    @Test
    public void process_UnHappyPath_Too_Old() {
        given(poolRepository.findByJurorNumber(any(String.class))).willReturn(poolDetails1);
        given(jurorResponseRepository.findByJurorNumber(any(String.class))).willReturn(jurorResponse1);
        given(responseInspector.getJurorAgeAtHearingDate(birthDate,
            hearingDate)).willReturn(77);
        service.processSuperUrgent(JUROR_ID);
        verify(responseInspector, times(1)).getJurorAgeAtHearingDate(birthDate,
            hearingDate);
        verifyNoInteractions(jurorCommsNotificationService);
    }
}
