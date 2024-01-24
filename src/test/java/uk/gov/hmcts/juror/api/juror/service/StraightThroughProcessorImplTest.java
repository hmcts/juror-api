package uk.gov.hmcts.juror.api.juror.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.juror.api.bureau.domain.DisCode;
import uk.gov.hmcts.juror.api.bureau.domain.IPoolStatus;
import uk.gov.hmcts.juror.api.bureau.domain.JurorResponseAudit;
import uk.gov.hmcts.juror.api.bureau.domain.JurorResponseAuditRepository;
import uk.gov.hmcts.juror.api.bureau.domain.PartHist;
import uk.gov.hmcts.juror.api.bureau.domain.PartHistRepository;
import uk.gov.hmcts.juror.api.bureau.service.ResponseMergeService;
import uk.gov.hmcts.juror.api.juror.domain.DisqualificationLetter;
import uk.gov.hmcts.juror.api.juror.domain.DisqualificationLetterRepository;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponse;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponseRepository;
import uk.gov.hmcts.juror.api.juror.domain.Pool;
import uk.gov.hmcts.juror.api.juror.domain.PoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.UserRepository;
import uk.gov.hmcts.juror.api.validation.ResponseInspectorImpl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static org.assertj.core.api.Assertions.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.juror.api.JurorDigitalApplication.AUTO_USER;

/**
 * Unit test of {@link StraightThroughProcessorImpl}.
 */
@SuppressWarnings("Duplicates")
@RunWith(MockitoJUnitRunner.class)
public class StraightThroughProcessorImplTest {

    @Mock
    private JurorResponseRepository jurorResponseRepository;

    @Mock
    private JurorResponseAuditRepository jurorResponseAuditRepository;

    @Mock
    private PoolRepository poolRepository;

    @Mock
    private ResponseMergeService responseMergeService;

    @Mock
    private PartHistRepository partHistRepository;

    @Mock
    private DisqualificationLetterRepository disqualificationLetterRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ResponseInspectorImpl responseInspector;

    @InjectMocks
    private StraightThroughProcessorImpl straightThroughProcessor;

    private static final String TEST_JUROR_NUMBER = "209092530";

    private JurorResponse jurorResponse;
    private Pool jurorPool;

    private static final int TOO_OLD_JUROR_AGE = 76;
    private static final int YOUNGEST_JUROR_AGE_ALLOWED = 18;

    @Before
    public void setup() {
        jurorResponse = mock(JurorResponse.class);
        given(jurorResponseRepository.findByJurorNumber(TEST_JUROR_NUMBER)).willReturn(jurorResponse);

        jurorPool = mock(Pool.class);
        given(poolRepository.findByJurorNumber(TEST_JUROR_NUMBER)).willReturn(jurorPool);

        given(responseInspector.getYoungestJurorAgeAllowed()).willReturn(18);
        given(responseInspector.getTooOldJurorAge()).willReturn(76);
        given(responseInspector.getJurorAgeAtHearingDate(any(), any())).willCallRealMethod();
    }

    @Test
    public void processDeceasedExcusal_happyPath_jurorSuccessfullyExcused()
        throws StraightThroughProcessingServiceException {
        // configure jurorResponse status to get through Deceased-Excusal logic successfully
        given(jurorResponse.getJurorNumber()).willReturn(TEST_JUROR_NUMBER);
        given(jurorResponse.getRelationship()).willReturn("Relationship");
        given(jurorResponse.getThirdPartyReason()).willReturn("Deceased");
        given(jurorResponse.getSuperUrgent()).willReturn(false);
        given(jurorPool.getStatus()).willReturn(IPoolStatus.SUMMONED);

        // process response
        straightThroughProcessor.processDeceasedExcusal(jurorResponse);

        // check excusal was successful
        verify(responseMergeService).mergeResponse(any(JurorResponse.class), eq(AUTO_USER));
        verify(jurorResponseAuditRepository).save(any(JurorResponseAudit.class));

        verify(jurorPool).setResponded(Pool.RESPONDED);
        verify(jurorPool).setExcusalDate(any(Date.class));
        verify(jurorPool).setExcusalCode("D");
        verify(jurorPool).setUserEdtq(AUTO_USER);
        verify(jurorPool).setStatus(IPoolStatus.EXCUSED);
        verify(jurorPool).setHearingDate(null);

        verify(partHistRepository).save(any(PartHist.class));

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
            verify(jurorResponseRepository, times(0)).save(any(JurorResponse.class));
            verify(jurorResponseAuditRepository, times(0)).save(any(JurorResponseAudit.class));
            verify(poolRepository, times(0)).save(any(Pool.class));
            verify(partHistRepository, times(0)).save(any(PartHist.class));
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
            verify(jurorResponseRepository, times(0)).save(any(JurorResponse.class));
            verify(jurorResponseAuditRepository, times(0)).save(any(JurorResponseAudit.class));
            verify(poolRepository, times(0)).save(any(Pool.class));
            verify(partHistRepository, times(0)).save(any(PartHist.class));
        }
    }

    @Test
    public void processDeceasedExcusal_unhappyPath_isSuperUrgent() {
        // configure single jurorResponse property to fail processing
        given(jurorResponse.getJurorNumber()).willReturn(TEST_JUROR_NUMBER);
        given(jurorResponse.getRelationship()).willReturn("Relationship");
        given(jurorResponse.getThirdPartyReason()).willReturn("Deceased");
        given(jurorPool.getStatus()).willReturn(IPoolStatus.SUMMONED);

        given(jurorResponse.getSuperUrgent()).willReturn(true);

        try {
            // process response
            straightThroughProcessor.processDeceasedExcusal(jurorResponse);
            fail("Expected StraightThroughProcessingServiceException.DeceasedExcusal not thrown.");
        } catch (StraightThroughProcessingServiceException.DeceasedExcusal expectedException) {
            // verify there was no interaction with the db etc
            verify(jurorResponseRepository, times(0)).save(any(JurorResponse.class));
            verify(jurorResponseAuditRepository, times(0)).save(any(JurorResponseAudit.class));
            verify(poolRepository, times(0)).save(any(Pool.class));
            verify(partHistRepository, times(0)).save(any(PartHist.class));
        }
    }


    @Test
    public void processDeceasedExcusal_unhappyPath_statusNotSummoned() {
        // configure single jurorResponse property to fail processing
        given(jurorResponse.getJurorNumber()).willReturn(TEST_JUROR_NUMBER);
        given(jurorResponse.getRelationship()).willReturn("Relationship");
        given(jurorResponse.getThirdPartyReason()).willReturn("Deceased");

        given(jurorPool.getStatus()).willReturn(IPoolStatus.EXCUSED);

        try {
            // process response
            straightThroughProcessor.processDeceasedExcusal(jurorResponse);
            fail("Expected StraightThroughProcessingServiceException.DeceasedExcusal not thrown.");
        } catch (StraightThroughProcessingServiceException.DeceasedExcusal expectedException) {
            // verify there was no interaction with the db etc
            verify(jurorResponseRepository, times(0)).save(any(JurorResponse.class));
            verify(jurorResponseAuditRepository, times(0)).save(any(JurorResponseAudit.class));
            verify(poolRepository, times(0)).save(any(Pool.class));
            verify(partHistRepository, times(0)).save(any(PartHist.class));
        }
    }

    @Test
    public void processAgeExcusal_happyPath_jurorSuccessfullyExcused_exactlyTooOld()
        throws StraightThroughProcessingServiceException, ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

        // configure jurorResponse status to get through Age-Excusal logic successfully
        given(jurorResponse.getJurorNumber()).willReturn(TEST_JUROR_NUMBER);
        given(jurorResponse.getRelationship()).willReturn(null);
        given(jurorResponse.getSuperUrgent()).willReturn(false);
        given(jurorPool.getStatus()).willReturn(IPoolStatus.SUMMONED);

        Date birthDate = simpleDateFormat.parse("01/01/1901");
        given(jurorResponse.getDateOfBirth()).willReturn(birthDate);

        // Juror turns too old on first day of hearing
        Date hearingDate = addTime(birthDate, TOO_OLD_JUROR_AGE, 0);
        given(jurorPool.getHearingDate()).willReturn(hearingDate);

        // process response
        straightThroughProcessor.processAgeExcusal(jurorResponse);

        // check excusal was successful
        verify(responseMergeService).mergeResponse(any(JurorResponse.class), eq(AUTO_USER));
        verify(jurorResponseAuditRepository).save(any(JurorResponseAudit.class));

        verify(jurorPool).setResponded(Pool.RESPONDED);
        verify(jurorPool).setDisqualifyDate(any(Date.class));
        verify(jurorPool).setDisqualifyCode(DisCode.AGE);
        verify(jurorPool).setUserEdtq(AUTO_USER);
        verify(jurorPool).setStatus(IPoolStatus.DISQUALIFIED);
        verify(jurorPool).setHearingDate(null);
        verify(poolRepository).save(any(Pool.class));

        verify(partHistRepository, times(2)).save(any(PartHist.class));

        verify(disqualificationLetterRepository).save(any(DisqualificationLetter.class));

        //verify(staffRepository).findOne(StaffQueries.byLogin(AUTO_USER));
        verify(userRepository).findByUsername(AUTO_USER);
    }

    @Test
    public void processAgeExcusal_happyPath_jurorSuccessfullyExcused_exactlyTooYoung()
        throws StraightThroughProcessingServiceException, ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

        // configure jurorResponse status to get through Age-Excusal logic successfully
        given(jurorResponse.getJurorNumber()).willReturn(TEST_JUROR_NUMBER);
        given(jurorResponse.getRelationship()).willReturn(null);
        given(jurorResponse.getSuperUrgent()).willReturn(false);
        given(jurorPool.getStatus()).willReturn(IPoolStatus.SUMMONED);

        Date birthDate = simpleDateFormat.parse("01/01/1901");
        given(jurorResponse.getDateOfBirth()).willReturn(birthDate);

        // set Juror to be one day too young on first day of hearing
        Date hearingDate = addTime(birthDate, YOUNGEST_JUROR_AGE_ALLOWED - 1, 364);
        given(jurorPool.getHearingDate()).willReturn(hearingDate);

        // process response
        straightThroughProcessor.processAgeExcusal(jurorResponse);

        // check excusal was successful
        verify(responseMergeService).mergeResponse(any(JurorResponse.class), eq(AUTO_USER));
        verify(jurorResponseAuditRepository).save(any(JurorResponseAudit.class));

        verify(jurorPool).setResponded(Pool.RESPONDED);
        verify(jurorPool).setDisqualifyDate(any(Date.class));
        verify(jurorPool).setDisqualifyCode(DisCode.AGE);
        verify(jurorPool).setUserEdtq(AUTO_USER);
        verify(jurorPool).setStatus(IPoolStatus.DISQUALIFIED);
        verify(jurorPool).setHearingDate(null);
        verify(poolRepository).save(any(Pool.class));

        verify(partHistRepository, times(2)).save(any(PartHist.class));

        verify(disqualificationLetterRepository).save(any(DisqualificationLetter.class));

        //verify(staffRepository).findOne(StaffQueries.byLogin(AUTO_USER));
        verify(userRepository).findByUsername(AUTO_USER);
    }

    @Test
    public void processAgeExcusal_unhappyPath_jurorExactlyMinimumAge()
        throws StraightThroughProcessingServiceException, ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

        // configure jurorResponse
        given(jurorResponse.getJurorNumber()).willReturn(TEST_JUROR_NUMBER);
        given(jurorResponse.getRelationship()).willReturn(null);
        given(jurorResponse.getSuperUrgent()).willReturn(false);
        given(jurorPool.getStatus()).willReturn(IPoolStatus.SUMMONED);

        Date birthDate = simpleDateFormat.parse("01/01/1901");
        given(jurorResponse.getDateOfBirth()).willReturn(birthDate);

        // Juror is exactly minimum age on first day of hearing so can't be excused
        Date hearingDate = addTime(birthDate, YOUNGEST_JUROR_AGE_ALLOWED, 0);
        given(jurorPool.getHearingDate()).willReturn(hearingDate);

        try {
            // process response
            straightThroughProcessor.processAgeExcusal(jurorResponse);
            fail("Expected StraightThroughProcessingServiceException.AgeExcusal not thrown.");
        } catch (StraightThroughProcessingServiceException.AgeExcusal expectedException) {
            // check database was not updated
            verify(jurorResponseRepository, times(0)).save(any(JurorResponse.class));
            verify(jurorResponseAuditRepository, times(0)).save(any(JurorResponseAudit.class));
            verify(poolRepository, times(0)).save(any(Pool.class));
            verify(partHistRepository, times(0)).save(any(PartHist.class));
            verify(disqualificationLetterRepository, times(0)).save(any(DisqualificationLetter.class));
        }
    }

    @Test
    public void processAgeExcusal_unhappyPath_jurorOneDayUnderTooOld()
        throws StraightThroughProcessingServiceException, ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

        // configure jurorResponse
        given(jurorResponse.getJurorNumber()).willReturn(TEST_JUROR_NUMBER);
        given(jurorResponse.getRelationship()).willReturn(null);
        given(jurorResponse.getSuperUrgent()).willReturn(false);
        given(jurorPool.getStatus()).willReturn(IPoolStatus.SUMMONED);

        Date birthDate = simpleDateFormat.parse("01/01/1901");
        given(jurorResponse.getDateOfBirth()).willReturn(birthDate);

        // Juror is one day away from being excused
        Date hearingDate = addTime(birthDate, TOO_OLD_JUROR_AGE - 1, 364);
        given(jurorPool.getHearingDate()).willReturn(hearingDate);

        try {
            // process response
            straightThroughProcessor.processAgeExcusal(jurorResponse);
            fail("Expected StraightThroughProcessingServiceException.AgeExcusal not thrown.");
        } catch (StraightThroughProcessingServiceException.AgeExcusal expectedException) {
            // check database was not updated
            verify(jurorResponseRepository, times(0)).save(any(JurorResponse.class));
            verify(jurorResponseAuditRepository, times(0)).save(any(JurorResponseAudit.class));
            verify(poolRepository, times(0)).save(any(Pool.class));
            verify(partHistRepository, times(0)).save(any(PartHist.class));
            verify(disqualificationLetterRepository, times(0)).save(any(DisqualificationLetter.class));
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
            verify(jurorResponseRepository, times(0)).save(any(JurorResponse.class));
            verify(jurorResponseAuditRepository, times(0)).save(any(JurorResponseAudit.class));
            verify(poolRepository, times(0)).save(any(Pool.class));
            verify(partHistRepository, times(0)).save(any(PartHist.class));
            verify(disqualificationLetterRepository, times(0)).save(any(DisqualificationLetter.class));
        }
    }

    @Test
    public void processAgeExcusal_unhappyPath_isSuperUrgent() throws StraightThroughProcessingServiceException {
        // configure jurorResponse status to fail validation
        given(jurorResponse.getJurorNumber()).willReturn(TEST_JUROR_NUMBER);
        given(jurorResponse.getRelationship()).willReturn(null);
        given(jurorResponse.getSuperUrgent()).willReturn(true);
        given(jurorPool.getStatus()).willReturn(IPoolStatus.SUMMONED);

        try {
            // process response
            straightThroughProcessor.processAgeExcusal(jurorResponse);
            fail("Expected StraightThroughProcessingServiceException.AgeExcusal not thrown.");
        } catch (StraightThroughProcessingServiceException.AgeExcusal expectedException) {
            // check database was not updated
            verify(jurorResponseRepository, times(0)).save(any(JurorResponse.class));
            verify(jurorResponseAuditRepository, times(0)).save(any(JurorResponseAudit.class));
            verify(poolRepository, times(0)).save(any(Pool.class));
            verify(partHistRepository, times(0)).save(any(PartHist.class));
            verify(disqualificationLetterRepository, times(0)).save(any(DisqualificationLetter.class));
        }
    }

    @Test
    public void processAgeExcusal_unhappyPath_statusNotSummoned() throws StraightThroughProcessingServiceException {
        // configure jurorResponse status to fail validation
        given(jurorResponse.getJurorNumber()).willReturn(TEST_JUROR_NUMBER);
        given(jurorResponse.getRelationship()).willReturn(null);
        given(jurorPool.getStatus()).willReturn(IPoolStatus.EXCUSED);

        try {
            // process response
            straightThroughProcessor.processAgeExcusal(jurorResponse);
            fail("Expected StraightThroughProcessingServiceException.AgeExcusal not thrown.");
        } catch (StraightThroughProcessingServiceException.AgeExcusal expectedException) {
            // check database was not updated
            verify(jurorResponseRepository, times(0)).save(any(JurorResponse.class));
            verify(jurorResponseAuditRepository, times(0)).save(any(JurorResponseAudit.class));
            verify(poolRepository, times(0)).save(any(Pool.class));
            verify(partHistRepository, times(0)).save(any(PartHist.class));
            verify(disqualificationLetterRepository, times(0)).save(any(DisqualificationLetter.class));
        }
    }

    @Test
    public void processAcceptance_unhappyPath_FirstNameChanged() {
        // configure single jurorResponse property to fail processing
        given(jurorResponse.getJurorNumber()).willReturn(TEST_JUROR_NUMBER);
        given(jurorResponse.getTitle()).willReturn("Mr");
        given(jurorResponse.getFirstName()).willReturn("David");
        given(jurorPool.getTitle()).willReturn("Mr");
        given(jurorPool.getFirstName()).willReturn("Matt");

        try {
            // process response
            straightThroughProcessor.processAcceptance(jurorResponse);
            fail("Firstname does not match with saved response");
        } catch (StraightThroughProcessingServiceException expectedException) {
            // verify there was no interaction with the db etc
            verify(jurorResponseRepository, times(0)).save(any(JurorResponse.class));
            verify(jurorResponseAuditRepository, times(0)).save(any(JurorResponseAudit.class));
            verify(poolRepository, times(0)).save(any(Pool.class));
            verify(partHistRepository, times(0)).save(any(PartHist.class));
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
        given(jurorPool.getTitle()).willReturn("Mr");
        given(jurorPool.getFirstName()).willReturn("David");
        given(jurorPool.getLastName()).willReturn("Gardener");
        given(jurorPool.getPostcode()).willReturn("RG1 8HG");


        try {
            // process response
            straightThroughProcessor.processAcceptance(jurorResponse);
            fail("Postcode does not coincide with saved response");
        } catch (StraightThroughProcessingServiceException expectedException) {
            // verify there was no interaction with the db etc
            verify(jurorResponseRepository, times(0)).save(any(JurorResponse.class));
            verify(jurorResponseAuditRepository, times(0)).save(any(JurorResponseAudit.class));
            verify(poolRepository, times(0)).save(any(Pool.class));
            verify(partHistRepository, times(0)).save(any(PartHist.class));
        }
    }


    @Test
    public void processAcceptance_unhappyPath_AddressChanged() {
        // configure single jurorResponse property to fail processing
        given(jurorResponse.getJurorNumber()).willReturn(TEST_JUROR_NUMBER);
        given(jurorResponse.getTitle()).willReturn("Mr");
        given(jurorResponse.getFirstName()).willReturn("David");
        given(jurorResponse.getLastName()).willReturn("Gardener");

        given(jurorPool.getTitle()).willReturn("Mr");
        given(jurorPool.getFirstName()).willReturn("David");
        given(jurorPool.getLastName()).willReturn("Gardener");

        given(jurorResponse.getPostcode()).willReturn("RG1 7HG");
        given(jurorPool.getPostcode()).willReturn("RG1 7HG");
        given(jurorResponse.getAddress()).willReturn("Green Park, Reading");
        given(jurorPool.getAddress()).willReturn("250 Brooks Drive");


        try {
            // process response
            straightThroughProcessor.processAcceptance(jurorResponse);
            fail("Address  does not coincide with saved response");
        } catch (StraightThroughProcessingServiceException expectedException) {
            // verify there was no interaction with the db etc
            verify(jurorResponseRepository, times(0)).save(any(JurorResponse.class));
            verify(jurorResponseAuditRepository, times(0)).save(any(JurorResponseAudit.class));
            verify(poolRepository, times(0)).save(any(Pool.class));
            verify(partHistRepository, times(0)).save(any(PartHist.class));
        }
    }


    @Test
    public void processAcceptance_unhappyPath_Address2Changed() {
        // configure single jurorResponse property to fail processing
        given(jurorResponse.getJurorNumber()).willReturn(TEST_JUROR_NUMBER);
        given(jurorResponse.getTitle()).willReturn("Mr");
        given(jurorResponse.getFirstName()).willReturn("David");
        given(jurorResponse.getLastName()).willReturn("Gardener");

        given(jurorPool.getTitle()).willReturn("Mr");
        given(jurorPool.getFirstName()).willReturn("David");
        given(jurorPool.getLastName()).willReturn("Gardener");

        given(jurorResponse.getPostcode()).willReturn("RG1 7HG");
        given(jurorPool.getPostcode()).willReturn("RG1 7HG");
        given(jurorResponse.getAddress2()).willReturn("Green Park, Reading");
        given(jurorPool.getAddress2()).willReturn("250 Brooks Drive");


        try {
            // process response
            straightThroughProcessor.processAcceptance(jurorResponse);
            fail("Address  does not coincide with saved response");
        } catch (StraightThroughProcessingServiceException expectedException) {
            // verify there was no interaction with the db etc
            verify(jurorResponseRepository, times(0)).save(any(JurorResponse.class));
            verify(jurorResponseAuditRepository, times(0)).save(any(JurorResponseAudit.class));
            verify(poolRepository, times(0)).save(any(Pool.class));
            verify(partHistRepository, times(0)).save(any(PartHist.class));
        }
    }


    @Test
    public void processAcceptance_unhappyPath_NullAddress2Changed() {
        // configure single jurorResponse property to fail processing
        given(jurorResponse.getJurorNumber()).willReturn(TEST_JUROR_NUMBER);
        given(jurorResponse.getTitle()).willReturn("Mr");
        given(jurorResponse.getFirstName()).willReturn("David");
        given(jurorResponse.getLastName()).willReturn("Gardener");

        given(jurorPool.getTitle()).willReturn("Mr");
        given(jurorPool.getFirstName()).willReturn("David");
        given(jurorPool.getLastName()).willReturn("Gardener");

        given(jurorResponse.getPostcode()).willReturn("RG1 7HG");
        given(jurorPool.getPostcode()).willReturn("RG1 7HG");
        given(jurorResponse.getAddress2()).willReturn("Green Park, Reading");
        given(jurorPool.getAddress2()).willReturn(null);

        try {
            // process response
            straightThroughProcessor.processAcceptance(jurorResponse);
            fail("Address  does not coincide with saved response");
        } catch (StraightThroughProcessingServiceException expectedException) {
            // verify there was no interaction with the db etc
            verify(jurorResponseRepository, times(0)).save(any(JurorResponse.class));
            verify(jurorResponseAuditRepository, times(0)).save(any(JurorResponseAudit.class));
            verify(poolRepository, times(0)).save(any(Pool.class));
            verify(partHistRepository, times(0)).save(any(PartHist.class));
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
            verify(jurorResponseRepository, times(0)).save(any(JurorResponse.class));
            verify(jurorResponseAuditRepository, times(0)).save(any(JurorResponseAudit.class));
            verify(poolRepository, times(0)).save(any(Pool.class));
            verify(partHistRepository, times(0)).save(any(PartHist.class));
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
            verify(jurorResponseRepository, times(0)).save(any(JurorResponse.class));
            verify(jurorResponseAuditRepository, times(0)).save(any(JurorResponseAudit.class));
            verify(poolRepository, times(0)).save(any(Pool.class));
            verify(partHistRepository, times(0)).save(any(PartHist.class));
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
            verify(jurorResponseRepository, times(0)).save(any(JurorResponse.class));
            verify(jurorResponseAuditRepository, times(0)).save(any(JurorResponseAudit.class));
            verify(poolRepository, times(0)).save(any(Pool.class));
            verify(partHistRepository, times(0)).save(any(PartHist.class));
        }
    }

    private void setupMock_UrgentSuperUrgent() {
        given(jurorResponse.getJurorNumber()).willReturn(TEST_JUROR_NUMBER);
        given(jurorResponse.getJurorNumber()).willReturn(TEST_JUROR_NUMBER);
        given(jurorResponse.getTitle()).willReturn("Mr");
        given(jurorResponse.getFirstName()).willReturn("David");
        given(jurorResponse.getLastName()).willReturn("Gardener");

        given(jurorPool.getTitle()).willReturn("Mr");
        given(jurorPool.getFirstName()).willReturn("David");
        given(jurorPool.getLastName()).willReturn("Gardener");

        given(jurorResponse.getPostcode()).willReturn("RG1 7HG");
        given(jurorPool.getPostcode()).willReturn("RG1 7HG");
        given(jurorResponse.getAddress()).willReturn("Green Park, Reading");
        given(jurorPool.getAddress()).willReturn("Green Park, Reading");
        given(jurorResponse.getAddress2()).willReturn("250 Brooks Drive");
        given(jurorPool.getAddress2()).willReturn("250 Brooks Drive");
    }


    private Date addTime(Date date, int years, int days) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.YEAR, years);
        c.add(Calendar.DAY_OF_YEAR, days);
        return c.getTime();
    }

}
