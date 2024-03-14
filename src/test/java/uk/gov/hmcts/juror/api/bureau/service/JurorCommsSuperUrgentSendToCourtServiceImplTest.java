package uk.gov.hmcts.juror.api.bureau.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.juror.api.bureau.notify.JurorCommsNotifyTemplateType;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.validation.ResponseInspector;

import java.time.LocalDate;

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

    private JurorPool poolDetails1;
    private DigitalResponse jurorResponse1;
    private LocalDate birthDate;
    private LocalDate hearingDate;

    private Juror juror;

    @Mock
    private JurorPoolRepository poolRepository;

    @Mock
    private JurorDigitalResponseRepositoryMod jurorResponseRepository;

    @Mock
    private JurorCommsNotificationService jurorCommsNotificationService;

    @Mock
    private ResponseInspector responseInspector;

    @InjectMocks
    private JurorCommsSuperUrgentSendToCourtServiceImpl service;

    @Before
    public void setUp() {

        poolDetails1 = new JurorPool();
        juror = new Juror();
        poolDetails1.setJuror(juror);
        jurorResponse1 = new DigitalResponse();

        given(responseInspector.getYoungestJurorAgeAllowed()).willReturn(18);
        given(responseInspector.getTooOldJurorAge()).willReturn(76);
    }

    @Test
    public void process_HappyPath() {

        JurorPool poolDetails = new JurorPool();
        juror = new Juror();
        poolDetails.setJuror(juror);
        juror.setJurorNumber(JUROR_ID);
        juror.setFirstName(FIRST_NAME);
        juror.setLastName(LAST_NAME);
        poolDetails.setNextDate(hearingDate);
        juror.setWelsh(false);
        juror.setNotifications(0);


        DigitalResponse jurorResponse = new DigitalResponse();
        jurorResponse.setJurorNumber(JUROR_ID);
        jurorResponse.setFirstName(FIRST_NAME);
        jurorResponse.setLastName(LAST_NAME);
        jurorResponse.setDateOfBirth(birthDate);
        jurorResponse.setEmail("abcd@yahoo.com");
        jurorResponse.setPhoneNumber("07442231123");
        jurorResponse.setWelsh(false);


        //given(poolRepository.findOne(any(String.class))).willReturn(poolDetails);
        given(poolRepository.findByJurorJurorNumber(any(String.class))).willReturn(poolDetails);
        given(jurorResponseRepository.findByJurorNumber(any(String.class))).willReturn(jurorResponse);
        given(responseInspector.getJurorAgeAtHearingDate(birthDate,
            hearingDate)).willReturn(18);

        service.processSuperUrgent(JUROR_ID);
        verify(responseInspector, times(1)).getJurorAgeAtHearingDate(birthDate,
            hearingDate);
        verify(jurorCommsNotificationService, times(1)).sendJurorComms(any(JurorPool.class),
            any(JurorCommsNotifyTemplateType.class),
            eq(null), eq(null), anyBoolean());
        verify(poolRepository, times(1)).save(any(JurorPool.class));
    }

    @Test
    public void process_UnHappyPath_Too_Young() {
        given(poolRepository.findByJurorJurorNumber(any(String.class))).willReturn(poolDetails1);
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
        given(poolRepository.findByJurorJurorNumber(any(String.class))).willReturn(poolDetails1);
        given(jurorResponseRepository.findByJurorNumber(any(String.class))).willReturn(jurorResponse1);
        given(responseInspector.getJurorAgeAtHearingDate(birthDate,
            hearingDate)).willReturn(77);
        service.processSuperUrgent(JUROR_ID);
        verify(responseInspector, times(1)).getJurorAgeAtHearingDate(birthDate,
            hearingDate);
        verifyNoInteractions(jurorCommsNotificationService);
    }
}
