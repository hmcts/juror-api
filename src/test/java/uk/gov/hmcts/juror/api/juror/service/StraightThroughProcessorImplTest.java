package uk.gov.hmcts.juror.api.juror.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.juror.api.bureau.domain.DisCode;
import uk.gov.hmcts.juror.api.bureau.service.ResponseMergeService;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.JurorResponseAuditMod;
import uk.gov.hmcts.juror.api.moj.domain.letter.DisqualificationLetterMod;
import uk.gov.hmcts.juror.api.moj.repository.DisqualifyLetterModRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorStatusRepository;
import uk.gov.hmcts.juror.api.moj.repository.UserRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorResponseAuditRepositoryMod;
import uk.gov.hmcts.juror.api.validation.ResponseInspectorImpl;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.juror.api.JurorDigitalApplication.AUTO_USER;

/**
 * Unit test of {@link StraightThroughProcessorImpl}.
 */
@SuppressWarnings("Duplicates")
@RunWith(MockitoJUnitRunner.class)
public class StraightThroughProcessorImplTest {

    @Mock
    private JurorDigitalResponseRepositoryMod jurorResponseRepository;

    @Mock
    private JurorResponseAuditRepositoryMod jurorResponseAuditRepository;

    @Mock
    private JurorPoolRepository poolRepository;
    @Mock
    private JurorStatusRepository jurorStatusRepository;

    @Mock
    private ResponseMergeService mergeService;

    @Mock
    private JurorHistoryRepository partHistRepository;

    @Mock
    private DisqualifyLetterModRepository disqualificationLetterRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ResponseInspectorImpl responseInspector;

    @InjectMocks
    private StraightThroughProcessorImpl straightThroughProcessor;

    private static final String TEST_JUROR_NUMBER = "209092530";

    private DigitalResponse jurorResponse;
    private JurorPool jurorPool;
    private Juror juror;

    private static final int TOO_OLD_JUROR_AGE = 76;
    private static final int YOUNGEST_JUROR_AGE_ALLOWED = 18;

    @Before
    public void setup() {
        jurorResponse = mock(DigitalResponse.class);
        given(jurorResponseRepository.findByJurorNumber(TEST_JUROR_NUMBER)).willReturn(jurorResponse);

        jurorPool = mock(JurorPool.class);
        juror = mock(Juror.class);
        given(jurorPool.getJuror()).willReturn(juror);
        given(poolRepository.findByJurorJurorNumber(TEST_JUROR_NUMBER)).willReturn(jurorPool);

        given(responseInspector.getYoungestJurorAgeAllowed()).willReturn(18);
        given(responseInspector.getTooOldJurorAge()).willReturn(76);
        given(responseInspector.getJurorAgeAtHearingDate(any(), any())).willCallRealMethod();
    }

    @Test
    public void processDeceasedExcusal_happyPath_jurorSuccessfullyExcused()
        throws StraightThroughProcessingServiceException {
        JurorStatus excusedJurorStatus = mock(JurorStatus.class);
        when(jurorStatusRepository.findById(IJurorStatus.EXCUSED)).thenReturn(Optional.ofNullable(excusedJurorStatus));
        // configure jurorResponse status to get through Deceased-Excusal logic successfully
        given(jurorResponse.getJurorNumber()).willReturn(TEST_JUROR_NUMBER);
        given(jurorResponse.getRelationship()).willReturn("Relationship");
        given(jurorResponse.getThirdPartyReason()).willReturn("Deceased");
        given(jurorResponse.getSuperUrgent()).willReturn(false);
        JurorStatus jurorStatus = mock(JurorStatus.class);
        when(jurorStatus.getStatus()).thenReturn(IJurorStatus.SUMMONED);
        given(jurorPool.getStatus()).willReturn(jurorStatus);

        // process response
        straightThroughProcessor.processDeceasedExcusal(jurorResponse);

        // check excusal was successful
        verify(mergeService).mergeResponse(any(DigitalResponse.class), eq(AUTO_USER));
        verify(jurorResponseAuditRepository).save(any(JurorResponseAuditMod.class));

        verify(jurorPool, times(3)).getJuror();
        verify(juror, times(1)).setResponded(true);
        verify(juror, times(1)).setExcusalDate(any(LocalDate.class));
        verify(juror, times(1)).setExcusalCode("D");
        verify(jurorPool).setUserEdtq(AUTO_USER);
        verify(jurorPool, times(1)).getStatus();
        verify(jurorPool, times(1)).setStatus(excusedJurorStatus);
        verify(jurorStatusRepository, times(1)).findById(IJurorStatus.EXCUSED);
        verify(jurorPool).setNextDate(null);

        verify(partHistRepository).save(any(JurorHistory.class));

        //verify(staffRepository).findOne(StaffQueries.byLogin(AUTO_USER));
        verify(userRepository).findByUsername(AUTO_USER);
    }

    @Test
    public void processDeceasedExcusal_unhappyPath_notThirdParty() {
        // configure single jurorResponse property to fail processing
        given(jurorResponse.getJurorNumber()).willReturn(TEST_JUROR_NUMBER);
        given(jurorResponse.getRelationship()).willReturn(null);

        given(jurorResponse.getRelationship()).willReturn(null);

        try {
            // process response
            straightThroughProcessor.processDeceasedExcusal(jurorResponse);
            fail("Expected StraightThroughDeceasedExcusalProcessingServiceException not thrown.");
        } catch (StraightThroughProcessingServiceException.DeceasedExcusal expectedException) {
            // verify there was no interaction with the db etc
            verify(jurorResponseRepository, times(0)).save(any(DigitalResponse.class));
            verify(jurorResponseAuditRepository, times(0)).save(any(JurorResponseAuditMod.class));
            verify(poolRepository, times(0)).save(any(JurorPool.class));
            verify(partHistRepository, times(0)).save(any(JurorHistory.class));
        }
    }

    @Test
    public void processDeceasedExcusal_unhappyPath_thirdPartyReasonNotDeceased() {
        // configure single jurorResponse property to fail processing
        given(jurorResponse.getJurorNumber()).willReturn(TEST_JUROR_NUMBER);
        given(jurorResponse.getRelationship()).willReturn("Relationship");

        given(jurorResponse.getThirdPartyReason()).willReturn("NOT Deceased");

        try {
            // process response
            straightThroughProcessor.processDeceasedExcusal(jurorResponse);
            fail("Expected StraightThroughDeceasedExcusalProcessingServiceException not thrown.");
        } catch (StraightThroughProcessingServiceException.DeceasedExcusal expectedException) {
            // verify there was no interaction with the db etc
            verify(jurorResponseRepository, times(0)).save(any(DigitalResponse.class));
            verify(jurorResponseAuditRepository, times(0)).save(any(JurorResponseAuditMod.class));
            verify(poolRepository, times(0)).save(any(JurorPool.class));
            verify(partHistRepository, times(0)).save(any(JurorHistory.class));
        }
    }

    @Test
    public void processDeceasedExcusal_unhappyPath_isSuperUrgent() {
        // configure single jurorResponse property to fail processing
        given(jurorResponse.getJurorNumber()).willReturn(TEST_JUROR_NUMBER);
        given(jurorResponse.getRelationship()).willReturn("Relationship");
        given(jurorResponse.getThirdPartyReason()).willReturn("Deceased");
        JurorStatus jurorStatus = mock(JurorStatus.class);
        when(jurorStatus.getStatus()).thenReturn(IJurorStatus.SUMMONED);
        given(jurorPool.getStatus()).willReturn(jurorStatus);

        when(jurorResponse.getSuperUrgent()).thenReturn(true);

        try {
            // process response
            straightThroughProcessor.processDeceasedExcusal(jurorResponse);
            fail("Expected StraightThroughProcessingServiceException.DeceasedExcusal not thrown.");
        } catch (StraightThroughProcessingServiceException.DeceasedExcusal expectedException) {
            // verify there was no interaction with the db etc
            verify(jurorResponseRepository, times(0)).save(any(DigitalResponse.class));
            verify(jurorResponseAuditRepository, times(0)).save(any(JurorResponseAuditMod.class));
            verify(poolRepository, times(0)).save(any(JurorPool.class));
            verify(partHistRepository, times(0)).save(any(JurorHistory.class));
        }
    }


    @Test
    public void processDeceasedExcusal_unhappyPath_statusNotSummoned() {
        // configure single jurorResponse property to fail processing
        given(jurorResponse.getJurorNumber()).willReturn(TEST_JUROR_NUMBER);
        given(jurorResponse.getRelationship()).willReturn("Relationship");
        given(jurorResponse.getThirdPartyReason()).willReturn("Deceased");
        JurorStatus jurorStatus = mock(JurorStatus.class);
        when(jurorStatus.getStatus()).thenReturn(IJurorStatus.EXCUSED);
        given(jurorPool.getStatus()).willReturn(jurorStatus);

        try {
            // process response
            straightThroughProcessor.processDeceasedExcusal(jurorResponse);
            fail("Expected StraightThroughProcessingServiceException.DeceasedExcusal not thrown.");
        } catch (StraightThroughProcessingServiceException.DeceasedExcusal expectedException) {
            // verify there was no interaction with the db etc
            verify(jurorResponseRepository, times(0)).save(any(DigitalResponse.class));
            verify(jurorResponseAuditRepository, times(0)).save(any(JurorResponseAuditMod.class));
            verify(poolRepository, times(0)).save(any(JurorPool.class));
            verify(partHistRepository, times(0)).save(any(JurorHistory.class));
        }
    }

    @Test
    public void processAgeExcusal_happyPath_jurorSuccessfullyExcused_exactlyTooOld()
        throws StraightThroughProcessingServiceException {
        JurorStatus disquallifiedJurorStatus = mock(JurorStatus.class);
        when(jurorStatusRepository.findById(IJurorStatus.DISQUALIFIED))
            .thenReturn(Optional.ofNullable(disquallifiedJurorStatus));
        // configure jurorResponse status to get through Age-Excusal logic successfully
        given(jurorResponse.getJurorNumber()).willReturn(TEST_JUROR_NUMBER);
        given(jurorResponse.getRelationship()).willReturn(null);
        given(jurorResponse.getSuperUrgent()).willReturn(false);
        JurorStatus jurorStatus = mock(JurorStatus.class);
        when(jurorStatus.getStatus()).thenReturn(IJurorStatus.SUMMONED);
        given(jurorPool.getStatus()).willReturn(jurorStatus);

        LocalDate birthDate = LocalDate.of(1901, 1, 1);

        given(jurorResponse.getDateOfBirth()).willReturn(birthDate);

        // Juror turns too old on first day of hearing
        LocalDate hearingDate = addTime(birthDate, TOO_OLD_JUROR_AGE, 0);
        given(jurorPool.getNextDate()).willReturn(hearingDate);


        // process response
        straightThroughProcessor.processAgeExcusal(jurorResponse);

        // check excusal was successful
        verify(mergeService).mergeResponse(any(DigitalResponse.class), eq(AUTO_USER));
        verify(jurorResponseAuditRepository).save(any(JurorResponseAuditMod.class));

        verify(jurorPool, times(3)).getJuror();
        verify(juror, times(1)).setResponded(true);
        verify(juror, times(1)).setDisqualifyDate(any(LocalDate.class));
        verify(juror, times(1)).setDisqualifyCode(DisCode.AGE);
        verify(jurorPool).setUserEdtq(AUTO_USER);
        verify(jurorPool).setStatus(disquallifiedJurorStatus);
        verify(jurorStatusRepository).findById(IJurorStatus.DISQUALIFIED);
        verify(jurorPool).setNextDate(null);
        verify(poolRepository).save(any(JurorPool.class));

        verify(partHistRepository, times(2)).save(any(JurorHistory.class));

        verify(disqualificationLetterRepository).save(any(DisqualificationLetterMod.class));

        //verify(staffRepository).findOne(StaffQueries.byLogin(AUTO_USER));
        verify(userRepository).findByUsername(AUTO_USER);
    }

    @Test
    public void processAgeExcusal_happyPath_jurorSuccessfullyExcused_exactlyTooYoung()
        throws StraightThroughProcessingServiceException {
        JurorStatus disquallifiedJurorStatus = mock(JurorStatus.class);
        when(jurorStatusRepository.findById(IJurorStatus.DISQUALIFIED))
            .thenReturn(Optional.ofNullable(disquallifiedJurorStatus));

        // configure jurorResponse status to get through Age-Excusal logic successfully
        given(jurorResponse.getJurorNumber()).willReturn(TEST_JUROR_NUMBER);
        given(jurorResponse.getRelationship()).willReturn(null);
        given(jurorResponse.getSuperUrgent()).willReturn(false);
        JurorStatus jurorStatus = mock(JurorStatus.class);
        when(jurorStatus.getStatus()).thenReturn(IJurorStatus.SUMMONED);
        given(jurorPool.getStatus()).willReturn(jurorStatus);

        LocalDate birthDate = LocalDate.of(1901, 1, 1);
        given(jurorResponse.getDateOfBirth()).willReturn(birthDate);

        // set Juror to be one day too young on first day of hearing
        LocalDate hearingDate = addTime(birthDate, YOUNGEST_JUROR_AGE_ALLOWED - 1, 364);
        given(jurorPool.getNextDate()).willReturn(hearingDate);

        // process response
        straightThroughProcessor.processAgeExcusal(jurorResponse);

        // check excusal was successful
        verify(mergeService).mergeResponse(any(DigitalResponse.class), eq(AUTO_USER));
        verify(jurorResponseAuditRepository).save(any(JurorResponseAuditMod.class));

        verify(jurorPool, times(3)).getJuror();
        verify(juror).setResponded(true);
        verify(juror).setDisqualifyDate(any(LocalDate.class));
        verify(juror).setDisqualifyCode(DisCode.AGE);
        verify(jurorPool).setUserEdtq(AUTO_USER);
        verify(jurorPool).setStatus(disquallifiedJurorStatus);
        verify(jurorPool).setNextDate(null);
        verify(poolRepository).save(any(JurorPool.class));

        verify(partHistRepository, times(2)).save(any(JurorHistory.class));

        verify(disqualificationLetterRepository).save(any(DisqualificationLetterMod.class));

        //verify(staffRepository).findOne(StaffQueries.byLogin(AUTO_USER));
        verify(userRepository).findByUsername(AUTO_USER);
    }

    @Test
    public void processAgeExcusal_unhappyPath_jurorExactlyMinimumAge()
        throws StraightThroughProcessingServiceException {

        // configure jurorResponse
        given(jurorResponse.getJurorNumber()).willReturn(TEST_JUROR_NUMBER);
        given(jurorResponse.getRelationship()).willReturn(null);
        given(jurorResponse.getSuperUrgent()).willReturn(false);
        JurorStatus jurorStatus = mock(JurorStatus.class);
        when(jurorStatus.getStatus()).thenReturn(IJurorStatus.SUMMONED);
        given(jurorPool.getStatus()).willReturn(jurorStatus);

        LocalDate birthDate = LocalDate.of(1901, 1, 1);
        given(jurorResponse.getDateOfBirth()).willReturn(birthDate);

        // Juror is exactly minimum age on first day of hearing so can't be excused
        LocalDate hearingDate = addTime(birthDate, YOUNGEST_JUROR_AGE_ALLOWED, 0);
        given(jurorPool.getNextDate()).willReturn(hearingDate);

        try {
            // process response
            straightThroughProcessor.processAgeExcusal(jurorResponse);
            fail("Expected StraightThroughProcessingServiceException.AgeExcusal not thrown.");
        } catch (StraightThroughProcessingServiceException.AgeExcusal expectedException) {
            // check database was not updated
            verify(jurorResponseRepository, times(0)).save(any(DigitalResponse.class));
            verify(jurorResponseAuditRepository, times(0)).save(any(JurorResponseAuditMod.class));
            verify(poolRepository, times(0)).save(any(JurorPool.class));
            verify(partHistRepository, times(0)).save(any(JurorHistory.class));
            verify(disqualificationLetterRepository, times(0)).save(any(DisqualificationLetterMod.class));
        }
    }

    @Test
    public void processAgeExcusal_unhappyPath_jurorOneDayUnderTooOld()
        throws StraightThroughProcessingServiceException {

        // configure jurorResponse
        given(jurorResponse.getJurorNumber()).willReturn(TEST_JUROR_NUMBER);
        given(jurorResponse.getRelationship()).willReturn(null);
        given(jurorResponse.getSuperUrgent()).willReturn(false);
        JurorStatus jurorStatus = mock(JurorStatus.class);
        when(jurorStatus.getStatus()).thenReturn(IJurorStatus.SUMMONED);
        given(jurorPool.getStatus()).willReturn(jurorStatus);

        LocalDate birthDate = LocalDate.of(1901, 1, 1);
        given(jurorResponse.getDateOfBirth()).willReturn(birthDate);

        // Juror is one day away from being excused
        LocalDate hearingDate = addTime(birthDate, TOO_OLD_JUROR_AGE - 1, 364);
        given(jurorPool.getNextDate()).willReturn(hearingDate);

        try {
            // process response
            straightThroughProcessor.processAgeExcusal(jurorResponse);
            fail("Expected StraightThroughProcessingServiceException.AgeExcusal not thrown.");
        } catch (StraightThroughProcessingServiceException.AgeExcusal expectedException) {
            // check database was not updated
            verify(jurorResponseRepository, times(0)).save(any(DigitalResponse.class));
            verify(jurorResponseAuditRepository, times(0)).save(any(JurorResponseAuditMod.class));
            verify(poolRepository, times(0)).save(any(JurorPool.class));
            verify(partHistRepository, times(0)).save(any(JurorHistory.class));
            verify(disqualificationLetterRepository, times(0)).save(any(DisqualificationLetterMod.class));
        }
    }

    @Test
    public void processAgeExcusal_unhappyPath_thirdParty() throws StraightThroughProcessingServiceException {
        // configure jurorResponse status to fail validation
        given(jurorResponse.getJurorNumber()).willReturn(TEST_JUROR_NUMBER);
        given(jurorResponse.getRelationship()).willReturn("BFFs");

        try {
            // process response
            straightThroughProcessor.processAgeExcusal(jurorResponse);
            fail("Expected StraightThroughProcessingServiceException.AgeExcusal not thrown.");
        } catch (StraightThroughProcessingServiceException.AgeExcusal expectedException) {
            // check database was not updated
            verify(jurorResponseRepository, times(0)).save(any(DigitalResponse.class));
            verify(jurorResponseAuditRepository, times(0)).save(any(JurorResponseAuditMod.class));
            verify(poolRepository, times(0)).save(any(JurorPool.class));
            verify(partHistRepository, times(0)).save(any(JurorHistory.class));
            verify(disqualificationLetterRepository, times(0)).save(any(DisqualificationLetterMod.class));
        }
    }

    @Test
    public void processAgeExcusal_unhappyPath_isSuperUrgent() throws StraightThroughProcessingServiceException {
        // configure jurorResponse status to fail validation
        given(jurorResponse.getJurorNumber()).willReturn(TEST_JUROR_NUMBER);
        given(jurorResponse.getRelationship()).willReturn(null);
        given(jurorResponse.getSuperUrgent()).willReturn(true);
        JurorStatus jurorStatus = mock(JurorStatus.class);
        when(jurorStatus.getStatus()).thenReturn(IJurorStatus.SUMMONED);
        given(jurorPool.getStatus()).willReturn(jurorStatus);

        try {
            // process response
            straightThroughProcessor.processAgeExcusal(jurorResponse);
            fail("Expected StraightThroughProcessingServiceException.AgeExcusal not thrown.");
        } catch (StraightThroughProcessingServiceException.AgeExcusal expectedException) {
            // check database was not updated
            verify(jurorResponseRepository, times(0)).save(any(DigitalResponse.class));
            verify(jurorResponseAuditRepository, times(0)).save(any(JurorResponseAuditMod.class));
            verify(poolRepository, times(0)).save(any(JurorPool.class));
            verify(partHistRepository, times(0)).save(any(JurorHistory.class));
            verify(disqualificationLetterRepository, times(0)).save(any(DisqualificationLetterMod.class));
        }
    }

    @Test
    public void processAgeExcusal_unhappyPath_statusNotSummoned() throws StraightThroughProcessingServiceException {
        // configure jurorResponse status to fail validation
        given(jurorResponse.getJurorNumber()).willReturn(TEST_JUROR_NUMBER);
        given(jurorResponse.getRelationship()).willReturn(null);
        JurorStatus jurorStatus = mock(JurorStatus.class);
        when(jurorStatus.getStatus()).thenReturn(IJurorStatus.EXCUSED);
        given(jurorPool.getStatus()).willReturn(jurorStatus);

        try {
            // process response
            straightThroughProcessor.processAgeExcusal(jurorResponse);
            fail("Expected StraightThroughProcessingServiceException.AgeExcusal not thrown.");
        } catch (StraightThroughProcessingServiceException.AgeExcusal expectedException) {
            // check database was not updated
            verify(jurorResponseRepository, times(0)).save(any(DigitalResponse.class));
            verify(jurorResponseAuditRepository, times(0)).save(any(JurorResponseAuditMod.class));
            verify(poolRepository, times(0)).save(any(JurorPool.class));
            verify(partHistRepository, times(0)).save(any(JurorHistory.class));
            verify(disqualificationLetterRepository, times(0)).save(any(DisqualificationLetterMod.class));
        }
    }

    @Test
    public void processAcceptance_unhappyPath_FirstNameChanged() {
        // configure single jurorResponse property to fail processing
        given(jurorResponse.getJurorNumber()).willReturn(TEST_JUROR_NUMBER);
        given(jurorResponse.getTitle()).willReturn("Mr");
        given(jurorResponse.getFirstName()).willReturn("David");
        given(jurorPool.getJuror().getTitle()).willReturn("Mr");
        given(jurorPool.getJuror().getFirstName()).willReturn("Matt");

        try {
            // process response
            straightThroughProcessor.processAcceptance(jurorResponse);
            fail("Firstname does not match with saved response");
        } catch (StraightThroughProcessingServiceException expectedException) {
            // verify there was no interaction with the db etc
            verify(jurorResponseRepository, times(0)).save(any(DigitalResponse.class));
            verify(jurorResponseAuditRepository, times(0)).save(any(JurorResponseAuditMod.class));
            verify(poolRepository, times(0)).save(any(JurorPool.class));
            verify(partHistRepository, times(0)).save(any(JurorHistory.class));
        }
    }

    @Test
    public void processAcceptance_unhappyPath_PostCodeChanged() {
        // configure single jurorResponse property to fail processing
        given(jurorResponse.getJurorNumber()).willReturn(TEST_JUROR_NUMBER);
        given(jurorResponse.getTitle()).willReturn("Mr");
        given(jurorResponse.getFirstName()).willReturn("David");
        given(jurorResponse.getLastName()).willReturn("Gardener");
        given(jurorResponse.getPostcode()).willReturn("RG1 7HG");
        given(jurorPool.getJuror().getTitle()).willReturn("Mr");
        given(jurorPool.getJuror().getFirstName()).willReturn("David");
        given(jurorPool.getJuror().getLastName()).willReturn("Gardener");
        given(jurorPool.getJuror().getPostcode()).willReturn("RG1 8HG");


        try {
            // process response
            straightThroughProcessor.processAcceptance(jurorResponse);
            fail("Postcode does not coincide with saved response");
        } catch (StraightThroughProcessingServiceException expectedException) {
            // verify there was no interaction with the db etc
            verify(jurorResponseRepository, times(0)).save(any(DigitalResponse.class));
            verify(jurorResponseAuditRepository, times(0)).save(any(JurorResponseAuditMod.class));
            verify(poolRepository, times(0)).save(any(JurorPool.class));
            verify(partHistRepository, times(0)).save(any(JurorHistory.class));
        }
    }


    @Test
    public void processAcceptance_unhappyPath_AddressChanged() {
        // configure single jurorResponse property to fail processing
        given(jurorResponse.getJurorNumber()).willReturn(TEST_JUROR_NUMBER);
        given(jurorResponse.getTitle()).willReturn("Mr");
        given(jurorResponse.getFirstName()).willReturn("David");
        given(jurorResponse.getLastName()).willReturn("Gardener");

        given(jurorPool.getJuror().getTitle()).willReturn("Mr");
        given(jurorPool.getJuror().getFirstName()).willReturn("David");
        given(jurorPool.getJuror().getLastName()).willReturn("Gardener");

        given(jurorResponse.getPostcode()).willReturn("RG1 7HG");
        given(jurorPool.getJuror().getPostcode()).willReturn("RG1 7HG");
        given(jurorResponse.getAddressLine1()).willReturn("Green Park, Reading");
        given(jurorPool.getJuror().getAddressLine1()).willReturn("250 Brooks Drive");


        try {
            // process response
            straightThroughProcessor.processAcceptance(jurorResponse);
            fail("Address  does not coincide with saved response");
        } catch (StraightThroughProcessingServiceException expectedException) {
            // verify there was no interaction with the db etc
            verify(jurorResponseRepository, times(0)).save(any(DigitalResponse.class));
            verify(jurorResponseAuditRepository, times(0)).save(any(JurorResponseAuditMod.class));
            verify(poolRepository, times(0)).save(any(JurorPool.class));
            verify(partHistRepository, times(0)).save(any(JurorHistory.class));
        }
    }


    @Test
    public void processAcceptance_unhappyPath_Address2Changed() {
        // configure single jurorResponse property to fail processing
        given(jurorResponse.getJurorNumber()).willReturn(TEST_JUROR_NUMBER);
        given(jurorResponse.getTitle()).willReturn("Mr");
        given(jurorResponse.getFirstName()).willReturn("David");
        given(jurorResponse.getLastName()).willReturn("Gardener");

        given(jurorPool.getJuror().getTitle()).willReturn("Mr");
        given(jurorPool.getJuror().getFirstName()).willReturn("David");
        given(jurorPool.getJuror().getLastName()).willReturn("Gardener");

        given(jurorResponse.getPostcode()).willReturn("RG1 7HG");
        given(jurorPool.getJuror().getPostcode()).willReturn("RG1 7HG");
        given(jurorResponse.getAddressLine2()).willReturn("Green Park, Reading");
        given(jurorPool.getJuror().getAddressLine2()).willReturn("250 Brooks Drive");


        try {
            // process response
            straightThroughProcessor.processAcceptance(jurorResponse);
            fail("Address  does not coincide with saved response");
        } catch (StraightThroughProcessingServiceException expectedException) {
            // verify there was no interaction with the db etc
            verify(jurorResponseRepository, times(0)).save(any(DigitalResponse.class));
            verify(jurorResponseAuditRepository, times(0)).save(any(JurorResponseAuditMod.class));
            verify(poolRepository, times(0)).save(any(JurorPool.class));
            verify(partHistRepository, times(0)).save(any(JurorHistory.class));
        }
    }


    @Test
    public void processAcceptance_unhappyPath_NullAddress2Changed() {
        // configure single jurorResponse property to fail processing
        given(jurorResponse.getJurorNumber()).willReturn(TEST_JUROR_NUMBER);
        given(jurorResponse.getTitle()).willReturn("Mr");
        given(jurorResponse.getFirstName()).willReturn("David");
        given(jurorResponse.getLastName()).willReturn("Gardener");

        given(jurorPool.getJuror().getTitle()).willReturn("Mr");
        given(jurorPool.getJuror().getFirstName()).willReturn("David");
        given(jurorPool.getJuror().getLastName()).willReturn("Gardener");

        given(jurorResponse.getPostcode()).willReturn("RG1 7HG");
        given(jurorPool.getJuror().getPostcode()).willReturn("RG1 7HG");
        given(jurorResponse.getAddressLine2()).willReturn("Green Park, Reading");
        given(jurorPool.getJuror().getAddressLine2()).willReturn(null);

        try {
            // process response
            straightThroughProcessor.processAcceptance(jurorResponse);
            fail("Address  does not coincide with saved response");
        } catch (StraightThroughProcessingServiceException expectedException) {
            // verify there was no interaction with the db etc
            verify(jurorResponseRepository, times(0)).save(any(DigitalResponse.class));
            verify(jurorResponseAuditRepository, times(0)).save(any(JurorResponseAuditMod.class));
            verify(poolRepository, times(0)).save(any(JurorPool.class));
            verify(partHistRepository, times(0)).save(any(JurorHistory.class));
        }
    }


    @Test
    public void processAcceptance_response_isUrgent() {
        // configure single jurorResponse property to fail processing
        setupMock_UrgentSuperUrgent();
        given(jurorResponse.getUrgent()).willReturn(true);

        try {
            // process response
            straightThroughProcessor.processAcceptance(jurorResponse);
            fail("Expected StraightThroughProcessingServiceException  is  thrown.");
        } catch (StraightThroughProcessingServiceException expectedException) {
            // verify there was no interaction with the db etc
            verify(jurorResponseRepository, times(0)).save(any(DigitalResponse.class));
            verify(jurorResponseAuditRepository, times(0)).save(any(JurorResponseAuditMod.class));
            verify(poolRepository, times(0)).save(any(JurorPool.class));
            verify(partHistRepository, times(0)).save(any(JurorHistory.class));
        }
    }

    @Test
    public void process_acceptance_response_isSuperUrgent() {
        // configure single jurorResponse property to fail processing
        setupMock_UrgentSuperUrgent();
        given(jurorResponse.getSuperUrgent()).willReturn(true);

        try {
            // process response
            straightThroughProcessor.processAcceptance(jurorResponse);
            fail("Expected StraightThroughProcessingServiceException  is  thrown.");
        } catch (StraightThroughProcessingServiceException expectedException) {
            // verify there was no interaction with the db etc
            verify(jurorResponseRepository, times(0)).save(any(DigitalResponse.class));
            verify(jurorResponseAuditRepository, times(0)).save(any(JurorResponseAuditMod.class));
            verify(poolRepository, times(0)).save(any(JurorPool.class));
            verify(partHistRepository, times(0)).save(any(JurorHistory.class));
        }
    }


    @Test
    public void process_excusal_response_isUrgent() {
        // configure single jurorResponse property to fail processing
        setupMock_UrgentSuperUrgent();

        try {
            // process response
            straightThroughProcessor.processDeceasedExcusal(jurorResponse);
            fail("Expected StraightThroughProcessingServiceException  is  thrown.");
        } catch (StraightThroughProcessingServiceException.DeceasedExcusal expectedException) {
            // verify there was no interaction with the db etc
            verify(jurorResponseRepository, times(0)).save(any(DigitalResponse.class));
            verify(jurorResponseAuditRepository, times(0)).save(any(JurorResponseAuditMod.class));
            verify(poolRepository, times(0)).save(any(JurorPool.class));
            verify(partHistRepository, times(0)).save(any(JurorHistory.class));
        }
    }

    private void setupMock_UrgentSuperUrgent() {
        given(jurorResponse.getJurorNumber()).willReturn(TEST_JUROR_NUMBER);
        given(jurorResponse.getJurorNumber()).willReturn(TEST_JUROR_NUMBER);
        given(jurorResponse.getTitle()).willReturn("Mr");
        given(jurorResponse.getFirstName()).willReturn("David");
        given(jurorResponse.getLastName()).willReturn("Gardener");

        given(jurorPool.getJuror().getTitle()).willReturn("Mr");
        given(jurorPool.getJuror().getFirstName()).willReturn("David");
        given(jurorPool.getJuror().getLastName()).willReturn("Gardener");

        given(jurorResponse.getPostcode()).willReturn("RG1 7HG");
        given(jurorPool.getJuror().getPostcode()).willReturn("RG1 7HG");
        given(jurorResponse.getAddressLine1()).willReturn("Green Park, Reading");
        given(jurorPool.getJuror().getAddressLine1()).willReturn("Green Park, Reading");
        given(jurorResponse.getAddressLine2()).willReturn("250 Brooks Drive");
        given(jurorPool.getJuror().getAddressLine2()).willReturn("250 Brooks Drive");
    }


    private LocalDate addTime(LocalDate date, int years, int days) {
        LocalDate realDate = date.plusYears(years).plusDays(days);
        return realDate;
    }
}
