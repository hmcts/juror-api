package uk.gov.hmcts.juror.api.moj.service;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.JurorReasonableAdjustment;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.JurorResponseCjsEmployment;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.PaperResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.ReasonableAdjustments;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorStatusRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorPaperResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorReasonableAdjustmentRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorResponseCjsEmploymentRepositoryMod;
import uk.gov.hmcts.juror.api.validation.ResponseInspector;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;

@RunWith(SpringRunner.class)
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.TooManyMethods"})
public class StraightThroughProcessorServiceImplTest {

    @Mock
    private JurorPaperResponseRepositoryMod jurorPaperResponseRepository;
    @Mock
    private JurorResponseCjsEmploymentRepositoryMod jurorResponseCjsEmploymentRepository;
    @Mock
    private JurorReasonableAdjustmentRepository jurorReasonableAdjustmentRepository;
    @Mock
    private JurorDigitalResponseRepositoryMod jurorDigitalResponseRepository;

    @Mock
    private JurorRepository jurorRepository;
    @Mock
    private JurorPoolRepository jurorPoolRepository;
    @Mock
    private JurorHistoryService jurorHistoryService;
    @Mock
    private JurorStatusRepository jurorStatusRepository;
    @Mock
    private PrintDataService printDataService;
    @Mock
    private ResponseInspector responseInspector;
    @Mock
    private SummonsReplyMergeService mergeService;

    @InjectMocks
    private StraightThroughProcessorServiceImpl straightThroughProcessorService;

    @Before
    public void setUpMocks() {
        Mockito.doReturn(18).when(responseInspector).getYoungestJurorAgeAllowed();
        Mockito.doReturn(76).when(responseInspector).getTooOldJurorAge();

        Mockito.doReturn(Optional.of(createJurorStatus(1))).when(jurorStatusRepository).findById(1);
        Mockito.doReturn(Optional.of(createJurorStatus(6))).when(jurorStatusRepository).findById(6);
        Mockito.doReturn(Optional.of(createJurorStatus(7))).when(jurorStatusRepository).findById(7);

        Mockito.doNothing().when(mergeService).mergePaperResponse(any(PaperResponse.class), any(String.class));
        Mockito.doNothing().when(mergeService).mergeDigitalResponse(any(DigitalResponse.class), any(String.class));

        Mockito.doNothing().when(printDataService).printWithdrawalLetter(any());

        Mockito.doReturn(null).when(jurorPoolRepository).save(any());
        Mockito.doReturn(null).when(jurorPaperResponseRepository).save(any());
        Mockito.doReturn(null).when(jurorDigitalResponseRepository).save(any());

        Mockito.doReturn(new ArrayList<>()).when(jurorResponseCjsEmploymentRepository)
            .findByJurorNumber(Mockito.anyString());
        Mockito.doReturn(new ArrayList<>()).when(jurorReasonableAdjustmentRepository)
            .findByJurorNumber(Mockito.anyString());
    }

    //Interface method: isValidResponseForStraightThroughProcessing
    @Test
    public void test_paper_isValidResponseForStraightThroughProcessing_valid_tooYoung() {
        LocalDate serviceStartDate = LocalDate.now().plusWeeks(8);
        LocalDate dateOfBirth = serviceStartDate.minusYears(18).plusDays(1);

        JurorPool jurorPool = createJurorPool(1);

        PaperResponse paperResponse = createPaperResponse(dateOfBirth);

        Assertions.assertThat(straightThroughProcessorService
                .isValidForStraightThroughAgeDisqualification(paperResponse, serviceStartDate, jurorPool))
            .isTrue();
    }

    @Test
    public void test_digital_isValidResponseForStraightThroughProcessing_valid_tooYoung() {
        LocalDate serviceStartDate = LocalDate.now().plusWeeks(8);
        LocalDate dateOfBirth = serviceStartDate.minusYears(18).plusDays(1);

        JurorPool jurorPool = createJurorPool(1);

        DigitalResponse jurorDigitalResponse = createDigitalResponse(dateOfBirth);

        Assertions.assertThat(
            straightThroughProcessorService.isValidForStraightThroughAgeDisqualification(jurorDigitalResponse,
                serviceStartDate, jurorPool)).isTrue();
    }

    @Test
    public void test_paper_isValidResponseForStraightThroughProcessing_valid_tooOld() {
        LocalDate serviceStartDate = LocalDate.now().plusWeeks(8);
        LocalDate dateOfBirth = serviceStartDate.minusYears(76);

        JurorPool jurorPool = createJurorPool(1);

        PaperResponse paperResponse = createPaperResponse(dateOfBirth);

        Assertions.assertThat(
                straightThroughProcessorService.isValidForStraightThroughAgeDisqualification(paperResponse,
                    serviceStartDate, jurorPool))
            .isTrue();
    }

    @Test
    public void test_digital_isValidResponseForStraightThroughProcessing_valid_tooOld() {
        LocalDate serviceStartDate = LocalDate.now().plusWeeks(8);
        LocalDate dateOfBirth = serviceStartDate.minusYears(76);

        JurorPool jurorPool = createJurorPool(1);

        DigitalResponse jurorDigitalResponse = createDigitalResponse(dateOfBirth);

        Assertions.assertThat(
            straightThroughProcessorService.isValidForStraightThroughAgeDisqualification(jurorDigitalResponse,
                serviceStartDate, jurorPool)).isTrue();
    }

    @Test
    public void test_paper_isValidResponseForStraightThroughProcessing_invalid_maxAgeBoundary() {
        LocalDate serviceStartDate = LocalDate.now().plusWeeks(8);
        LocalDate dateOfBirth = serviceStartDate.minusYears(76).plusDays(1);

        JurorPool jurorPool = createJurorPool(1);

        PaperResponse paperResponse = createPaperResponse(dateOfBirth);

        Assertions.assertThat(
            straightThroughProcessorService.isValidForStraightThroughAgeDisqualification(paperResponse,
                serviceStartDate, jurorPool)).isFalse();
    }

    @Test
    public void test_digital_isValidResponseForStraightThroughProcessing_invalid_maxAgeBoundary() {
        LocalDate serviceStartDate = LocalDate.now().plusWeeks(8);
        LocalDate dateOfBirth = serviceStartDate.minusYears(76).plusDays(1);

        JurorPool jurorPool = createJurorPool(1);

        DigitalResponse jurorDigitalResponse = createDigitalResponse(dateOfBirth);

        Assertions.assertThat(
            straightThroughProcessorService.isValidForStraightThroughAgeDisqualification(jurorDigitalResponse,
                serviceStartDate, jurorPool)).isFalse();
    }

    @Test
    public void test_paper_isValidResponseForStraightThroughProcessing_invalid_minAgeBoundary() {
        LocalDate serviceStartDate = LocalDate.now().plusWeeks(8);
        LocalDate dateOfBirth = serviceStartDate.minusYears(18);

        JurorPool jurorPool = createJurorPool(1);

        PaperResponse paperResponse = createPaperResponse(dateOfBirth);

        Assertions.assertThat(
            straightThroughProcessorService.isValidForStraightThroughAgeDisqualification(paperResponse,
                serviceStartDate, jurorPool)).isFalse();
    }

    @Test
    public void test_digital_isValidResponseForStraightThroughProcessing_invalid_minAgeBoundary() {
        LocalDate serviceStartDate = LocalDate.now().plusWeeks(8);
        LocalDate dateOfBirth = serviceStartDate.minusYears(18);

        JurorPool jurorPool = createJurorPool(1);

        DigitalResponse jurorDigitalResponse = createDigitalResponse(dateOfBirth);

        Assertions.assertThat(
            straightThroughProcessorService.isValidForStraightThroughAgeDisqualification(jurorDigitalResponse,
                serviceStartDate, jurorPool)).isFalse();
    }

    @Test
    public void test_paper_isValidResponseForStraightThroughProcessing_invalid_completedByThirdParty() {
        LocalDate serviceStartDate = LocalDate.now().plusWeeks(8);
        LocalDate dateOfBirth = serviceStartDate.minusYears(80);

        JurorPool jurorPool = createJurorPool(1);

        PaperResponse paperResponse = createPaperResponse(dateOfBirth);
        paperResponse.setRelationship("Some Relation");
        paperResponse.setThirdPartyReason("Example Third Party Reason");

        Assertions.assertThat(
            straightThroughProcessorService.isValidForStraightThroughAgeDisqualification(paperResponse,
                serviceStartDate, jurorPool)).isFalse();
    }

    @Test
    public void test_digital_isValidResponseForStraightThroughProcessing_invalid_completedByThirdParty() {
        LocalDate serviceStartDate = LocalDate.now().plusWeeks(8);
        LocalDate dateOfBirth = serviceStartDate.minusYears(80);

        JurorPool jurorPool = createJurorPool(1);

        DigitalResponse jurorDigitalResponse = createDigitalResponse(dateOfBirth);
        jurorDigitalResponse.setRelationship("Some Relation");
        jurorDigitalResponse.setThirdPartyReason("Example Third Party Reason");

        Assertions.assertThat(
            straightThroughProcessorService.isValidForStraightThroughAgeDisqualification(jurorDigitalResponse,
                serviceStartDate, jurorPool)).isFalse();
    }

    @Test
    public void test_paper_isValidResponseForStraightThroughProcessing_invalid_noLongerSummoned() {
        LocalDate serviceStartDate = LocalDate.now().plusWeeks(8);
        LocalDate dateOfBirth = serviceStartDate.minusYears(17);

        JurorPool jurorPool = createJurorPool(7);
        PaperResponse paperResponse = createPaperResponse(dateOfBirth);

        Assertions.assertThat(
            straightThroughProcessorService.isValidForStraightThroughAgeDisqualification(paperResponse,
                serviceStartDate, jurorPool)).isFalse();
    }

    @Test
    public void test_digital_isValidResponseForStraightThroughProcessing_invalid_noLongerSummoned() {
        LocalDate serviceStartDate = LocalDate.now().plusWeeks(8);
        LocalDate dateOfBirth = serviceStartDate.minusYears(17);

        JurorPool jurorPool = createJurorPool(7);
        DigitalResponse jurorDigitalResponse = createDigitalResponse(dateOfBirth);

        Assertions.assertThat(
            straightThroughProcessorService.isValidForStraightThroughAgeDisqualification(jurorDigitalResponse,
                serviceStartDate, jurorPool)).isFalse();
    }

    @Test
    public void test_paper_isValidResponseForStraightThroughProcessing_invalid_noDateOfBirth() {
        LocalDate serviceStartDate = LocalDate.now().plusWeeks(8);

        JurorPool jurorPool = createJurorPool(1);
        PaperResponse paperResponse = new PaperResponse();

        Assertions.assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> straightThroughProcessorService
                .isValidForStraightThroughAgeDisqualification(paperResponse, serviceStartDate, jurorPool));
    }

    @Test
    public void test_digital_isValidResponseForStraightThroughProcessing_invalid_noDateOfBirth() {
        LocalDate serviceStartDate = LocalDate.now().plusWeeks(8);

        JurorPool jurorPool = createJurorPool(1);
        DigitalResponse jurorDigitalResponse = new DigitalResponse();

        Assertions.assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> straightThroughProcessorService
                .isValidForStraightThroughAgeDisqualification(jurorDigitalResponse, serviceStartDate, jurorPool));
    }

    @Test
    public void test_paper_isValidResponseForStraightThroughProcessing_invalid_noServiceStartDate() {
        final LocalDate dateOfBirth = LocalDate.now().minusYears(30);

        JurorPool jurorPool = createJurorPool(1);
        PaperResponse paperResponse = createPaperResponse(dateOfBirth);

        Assertions.assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> straightThroughProcessorService
                .isValidForStraightThroughAgeDisqualification(paperResponse, null, jurorPool));
    }

    @Test
    public void test_digital_isValidResponseForStraightThroughProcessing_invalid_noServiceStartDate() {
        final LocalDate dateOfBirth = LocalDate.now().minusYears(30);

        JurorPool jurorPool = createJurorPool(1);
        DigitalResponse jurorDigitalResponse = createDigitalResponse(dateOfBirth);

        Assertions.assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> straightThroughProcessorService
                .isValidForStraightThroughAgeDisqualification(jurorDigitalResponse, null, jurorPool));
    }

    //Interface method: processAgeDisqualification
    @Test
    public void test_paper_processAgeDisqualification_bureauUser_bureauOwnedJurorRecord() {
        String jurorNumber = "11111111";
        final LocalDate dateOfBirth = LocalDate.now().minusYears(30);

        JurorPool jurorPool = createJurorPool(1);
        Juror juror = jurorPool.getJuror();
        juror.setJurorNumber(jurorNumber);
        jurorPool.setOwner("400");

        PaperResponse paperResponse = createPaperResponse(dateOfBirth);
        paperResponse.setJurorNumber(jurorNumber);

        LocalDate serviceStartDate = LocalDate.now().plusWeeks(8);
        straightThroughProcessorService.processAgeDisqualification(paperResponse, serviceStartDate, jurorPool,
            TestUtils.createJwt("400", "Bureau_User"));

        Mockito.verify(mergeService, Mockito.times(1)).mergePaperResponse(any(PaperResponse.class),
            any(String.class));
        Mockito.verify(jurorHistoryService).createDisqualifyHistory(jurorPool,"A");
        Mockito.verify(jurorHistoryService).createWithdrawHistoryUser(jurorPool,null,"A");
        Mockito.verify(printDataService, Mockito.times(1)).printWithdrawalLetter(any());
        Mockito.verify(jurorPaperResponseRepository, Mockito.times(1)).save(any());
    }

    @Test
    public void test_digital_processAgeDisqualification_bureauUser_bureauOwnedJurorRecord() {
        String jurorNumber = "11111111";
        final LocalDate dateOfBirth = LocalDate.now().minusYears(30);

        JurorPool jurorPool = createJurorPool(1);
        Juror juror = jurorPool.getJuror();
        juror.setJurorNumber(jurorNumber);
        jurorPool.setOwner("400");

        DigitalResponse digitalResponse = createDigitalResponse(dateOfBirth);
        digitalResponse.setJurorNumber(jurorNumber);

        straightThroughProcessorService.processAgeDisqualification(digitalResponse, jurorPool,
            TestUtils.createJwt("400", "Bureau_User"));

        Mockito.verify(mergeService, Mockito.times(1)).mergeDigitalResponse(any(DigitalResponse.class),
            any(String.class));
        Mockito.verify(jurorHistoryService).createDisqualifyHistory(jurorPool,"A");
        Mockito.verify(jurorHistoryService).createWithdrawHistoryUser(jurorPool,null,"A");
        Mockito.verify(printDataService, Mockito.times(1)).printWithdrawalLetter(any());
        Mockito.verify(jurorDigitalResponseRepository, Mockito.times(1)).save(any());
    }

    @Test
    public void test_paper_processAgeDisqualification_bureauUser_courtOwnedJurorRecord() {
        String jurorNumber = "11111111";
        final LocalDate dateOfBirth = LocalDate.now().minusYears(30);

        JurorPool jurorPool = createJurorPool(1);
        Juror juror = jurorPool.getJuror();
        juror.setJurorNumber(jurorNumber);
        jurorPool.setOwner("415");

        PaperResponse paperResponse = createPaperResponse(dateOfBirth);
        paperResponse.setJurorNumber(jurorNumber);

        LocalDate serviceStartDate = LocalDate.now().plusWeeks(8);
        Assertions.assertThatExceptionOfType(MojException.Forbidden.class).isThrownBy(() ->
            straightThroughProcessorService.processAgeDisqualification(paperResponse, serviceStartDate, jurorPool,
                TestUtils.createJwt("400", "Bureau_User")));

        Mockito.verify(mergeService, Mockito.never()).mergePaperResponse(any(PaperResponse.class),
            any(String.class));
        Mockito.verifyNoInteractions(jurorHistoryService);
        Mockito.verify(printDataService, Mockito.never()).printWithdrawalLetter(any());
        Mockito.verify(jurorPaperResponseRepository, Mockito.never()).save(any());
    }

    @Test
    public void test_digital_processAgeDisqualification_bureauUser_courtOwnedJurorRecord() {
        String jurorNumber = "11111111";
        final LocalDate dateOfBirth = LocalDate.now().minusYears(30);

        JurorPool jurorPool = createJurorPool(1);
        Juror juror = jurorPool.getJuror();
        juror.setJurorNumber(jurorNumber);
        jurorPool.setOwner("415");

        DigitalResponse digitalResponse = createDigitalResponse(dateOfBirth);
        digitalResponse.setJurorNumber(jurorNumber);

        Assertions.assertThatExceptionOfType(MojException.Forbidden.class).isThrownBy(() ->
            straightThroughProcessorService.processAgeDisqualification(digitalResponse, jurorPool,
                TestUtils.createJwt("400", "Bureau_User")));
        Mockito.verify(mergeService, Mockito.never())
            .mergeDigitalResponse(any(DigitalResponse.class), any(String.class));
        Mockito.verifyNoInteractions(jurorHistoryService);
        Mockito.verify(printDataService, Mockito.never()).printWithdrawalLetter(Mockito.any());
        Mockito.verify(jurorDigitalResponseRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    public void test_paper_processAgeDisqualification_courtUser_courtOwnedJurorRecord() {
        String jurorNumber = "11111111";
        final LocalDate dateOfBirth = LocalDate.now().minusYears(30);

        JurorPool jurorPool = createJurorPool(1);
        Juror juror = jurorPool.getJuror();
        juror.setJurorNumber(jurorNumber);
        jurorPool.setOwner("415");

        PaperResponse paperResponse = createPaperResponse(dateOfBirth);
        paperResponse.setJurorNumber(jurorNumber);

        LocalDate serviceStartDate = LocalDate.now().plusWeeks(8);
        straightThroughProcessorService.processAgeDisqualification(paperResponse, serviceStartDate, jurorPool,
            TestUtils.createJwt("415", "Bureau_User"));

        Mockito.verify(mergeService, Mockito.times(1)).mergePaperResponse(any(PaperResponse.class), any(String.class));

        Mockito.verify(jurorHistoryService).createDisqualifyHistory(jurorPool,"A");
        Mockito.verify(jurorHistoryService).createWithdrawHistoryUser(jurorPool,null,"A");
        Mockito.verify(printDataService, Mockito.times(1)).printWithdrawalLetter(any());
        Mockito.verify(jurorPaperResponseRepository, Mockito.times(1)).save(any());
    }

    @Test
    public void test_digital_processAgeDisqualification_courtUser_courtOwnedJurorRecord() {
        String jurorNumber = "11111111";
        final LocalDate dateOfBirth = LocalDate.now().minusYears(30);

        JurorPool jurorPool = createJurorPool(1);
        Juror juror = jurorPool.getJuror();
        juror.setJurorNumber(jurorNumber);
        jurorPool.setOwner("415");

        DigitalResponse digitalResponse = createDigitalResponse(dateOfBirth);
        digitalResponse.setJurorNumber(jurorNumber);

        straightThroughProcessorService.processAgeDisqualification(digitalResponse, jurorPool,
            TestUtils.createJwt("415", "Bureau_User"));

        Mockito.verify(mergeService, Mockito.times(1))
            .mergeDigitalResponse(any(DigitalResponse.class), any(String.class));

        Mockito.verify(jurorHistoryService).createDisqualifyHistory(jurorPool,"A");
        Mockito.verify(jurorHistoryService).createWithdrawHistoryUser(jurorPool,null,"A");
        Mockito.verify(printDataService, Mockito.times(1)).printWithdrawalLetter(Mockito.any());
        Mockito.verify(jurorDigitalResponseRepository, Mockito.times(1)).save(Mockito.any());
    }

    @Test
    public void test_paper_processAgeDisqualification_courtUser_bureauOwnedJurorRecord() {
        String jurorNumber = "11111111";
        final LocalDate dateOfBirth = LocalDate.now().minusYears(30);

        JurorPool jurorPool = createJurorPool(1);
        Juror juror = jurorPool.getJuror();
        juror.setJurorNumber(jurorNumber);
        jurorPool.setOwner("400");

        PaperResponse paperResponse = createPaperResponse(dateOfBirth);
        paperResponse.setJurorNumber(jurorNumber);

        LocalDate serviceStartDate = LocalDate.now().plusWeeks(8);
        Assertions.assertThatExceptionOfType(MojException.Forbidden.class).isThrownBy(() ->
            straightThroughProcessorService.processAgeDisqualification(paperResponse, serviceStartDate, jurorPool,
                TestUtils.createJwt("415", "Bureau_User")));

        Mockito.verify(mergeService, Mockito.never()).mergePaperResponse(any(PaperResponse.class),
            any(String.class));
        Mockito.verifyNoInteractions(jurorHistoryService);
        Mockito.verify(printDataService, Mockito.never()).printWithdrawalLetter(any());
        Mockito.verify(jurorPaperResponseRepository, Mockito.never()).save(any());
    }

    @Test
    public void test_digital_processAgeDisqualification_courtUser_bureauOwnedJurorRecord() {
        String jurorNumber = "11111111";
        final LocalDate dateOfBirth = LocalDate.now().minusYears(30);

        JurorPool jurorPool = createJurorPool(1);
        Juror juror = jurorPool.getJuror();
        juror.setJurorNumber(jurorNumber);
        jurorPool.setOwner("400");

        DigitalResponse digitalResponse = createDigitalResponse(dateOfBirth);
        digitalResponse.setJurorNumber(jurorNumber);

        Assertions.assertThatExceptionOfType(MojException.Forbidden.class).isThrownBy(() ->
            straightThroughProcessorService.processAgeDisqualification(digitalResponse, jurorPool,
                TestUtils.createJwt("415", "Bureau_User")));

        Mockito.verify(mergeService, Mockito.never()).mergeDigitalResponse(Mockito.any(DigitalResponse.class),
            Mockito.any(String.class));
        Mockito.verifyNoInteractions(jurorHistoryService);
        Mockito.verify(printDataService, Mockito.never()).printWithdrawalLetter(Mockito.any());
        Mockito.verify(jurorDigitalResponseRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    public void test_isValidForStraightThroughAcceptance_happyPath() {
        String bureauOwner = "400";
        String jurorNumber = "11111111";
        final LocalDate dateOfBirth = LocalDate.now().minusYears(30);

        JurorPool jurorPool = createJurorPool(1);
        Juror juror = jurorPool.getJuror();
        juror.setJurorNumber(jurorNumber);
        jurorPool.setOwner(bureauOwner);
        setJurorPoolPersonalDetails(juror);

        PaperResponse paperResponse = createPaperResponse(dateOfBirth);
        paperResponse.setProcessingStatus(ProcessingStatus.TODO);
        setPaperResponseDetails(paperResponse);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(paperResponse).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);

        Assertions.assertThat(straightThroughProcessorService
            .isValidForStraightThroughAcceptance(jurorNumber, bureauOwner, true)).isTrue();
    }

    @Test
    public void test_isValidForStraightThroughAcceptance_titleChanged() {
        String bureauOwner = "400";
        String jurorNumber = "11111111";
        final LocalDate dateOfBirth = LocalDate.now().minusYears(30);

        JurorPool jurorPool = createJurorPool(1);
        jurorPool.setOwner(bureauOwner);
        Juror juror = jurorPool.getJuror();
        juror.setJurorNumber(jurorNumber);
        setJurorPoolPersonalDetails(juror);
        juror.setTitle(null);

        PaperResponse paperResponse = createPaperResponse(dateOfBirth);
        paperResponse.setProcessingStatus(ProcessingStatus.TODO);
        setPaperResponseDetails(paperResponse);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(paperResponse).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);

        Assertions.assertThat(straightThroughProcessorService
            .isValidForStraightThroughAcceptance(jurorNumber, bureauOwner, true)).isFalse();
    }

    @Test
    public void test_isValidForStraightThroughAcceptance_firstNameChanged() {
        String bureauOwner = "400";
        String jurorNumber = "11111111";
        final LocalDate dateOfBirth = LocalDate.now().minusYears(30);

        JurorPool jurorPool = createJurorPool(1);
        Juror juror = jurorPool.getJuror();
        juror.setJurorNumber(jurorNumber);
        jurorPool.setOwner(bureauOwner);
        setJurorPoolPersonalDetails(juror);

        PaperResponse paperResponse = createPaperResponse(dateOfBirth);
        paperResponse.setProcessingStatus(ProcessingStatus.TODO);
        setPaperResponseDetails(paperResponse);
        paperResponse.setFirstName("");

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(paperResponse).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);

        Assertions.assertThat(straightThroughProcessorService
            .isValidForStraightThroughAcceptance(jurorNumber, bureauOwner, true)).isFalse();
    }

    @Test
    public void test_isValidForStraightThroughAcceptance_lastNameChanged() {
        String bureauOwner = "400";
        String jurorNumber = "11111111";
        final LocalDate dateOfBirth = LocalDate.now().minusYears(30);

        JurorPool jurorPool = createJurorPool(1);
        Juror juror = jurorPool.getJuror();
        juror.setJurorNumber(jurorNumber);
        jurorPool.setOwner(bureauOwner);
        setJurorPoolPersonalDetails(juror);

        PaperResponse paperResponse = createPaperResponse(dateOfBirth);
        paperResponse.setProcessingStatus(ProcessingStatus.TODO);
        setPaperResponseDetails(paperResponse);
        paperResponse.setLastName(null);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(paperResponse).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);

        Assertions.assertThat(straightThroughProcessorService
            .isValidForStraightThroughAcceptance(jurorNumber, bureauOwner, true)).isFalse();
    }

    @Test
    public void test_isValidForStraightThroughAcceptance_address1Changed() {
        String bureauOwner = "400";
        String jurorNumber = "11111111";
        final LocalDate dateOfBirth = LocalDate.now().minusYears(30);

        JurorPool jurorPool = createJurorPool(1);
        Juror juror = jurorPool.getJuror();
        juror.setJurorNumber(jurorNumber);
        jurorPool.setOwner(bureauOwner);
        setJurorPoolPersonalDetails(juror);
        juror.setAddressLine1("Some Address 1");

        PaperResponse paperResponse = createPaperResponse(dateOfBirth);
        paperResponse.setProcessingStatus(ProcessingStatus.TODO);
        setPaperResponseDetails(paperResponse);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(paperResponse).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);

        Assertions.assertThat(straightThroughProcessorService
            .isValidForStraightThroughAcceptance(jurorNumber, bureauOwner, true)).isFalse();
    }

    @Test
    public void test_isValidForStraightThroughAcceptance_address2Changed() {
        String bureauOwner = "400";
        String jurorNumber = "11111111";
        final LocalDate dateOfBirth = LocalDate.now().minusYears(30);

        JurorPool jurorPool = createJurorPool(1);
        Juror juror = jurorPool.getJuror();
        juror.setJurorNumber(jurorNumber);
        jurorPool.setOwner(bureauOwner);
        setJurorPoolPersonalDetails(juror);
        juror.setAddressLine2("Some Address 2");

        PaperResponse paperResponse = createPaperResponse(dateOfBirth);
        paperResponse.setProcessingStatus(ProcessingStatus.TODO);
        setPaperResponseDetails(paperResponse);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(paperResponse).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);

        Assertions.assertThat(straightThroughProcessorService
            .isValidForStraightThroughAcceptance(jurorNumber, bureauOwner, true)).isFalse();
    }

    @Test
    public void test_isValidForStraightThroughAcceptance_address3Changed() {
        String bureauOwner = "400";
        String jurorNumber = "11111111";
        final LocalDate dateOfBirth = LocalDate.now().minusYears(30);

        JurorPool jurorPool = createJurorPool(1);
        Juror juror = jurorPool.getJuror();
        juror.setJurorNumber(jurorNumber);
        jurorPool.setOwner(bureauOwner);
        setJurorPoolPersonalDetails(juror);

        PaperResponse paperResponse = createPaperResponse(dateOfBirth);
        paperResponse.setProcessingStatus(ProcessingStatus.TODO);
        setPaperResponseDetails(paperResponse);
        paperResponse.setAddressLine3("Some Address 3");

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(paperResponse).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);

        Assertions.assertThat(straightThroughProcessorService
            .isValidForStraightThroughAcceptance(jurorNumber, bureauOwner, true)).isFalse();
    }

    @Test
    public void test_isValidForStraightThroughAcceptance_address4Changed() {
        String bureauOwner = "400";
        String jurorNumber = "11111111";
        final LocalDate dateOfBirth = LocalDate.now().minusYears(30);

        JurorPool jurorPool = createJurorPool(1);
        Juror juror = jurorPool.getJuror();
        juror.setJurorNumber(jurorNumber);
        jurorPool.setOwner(bureauOwner);
        setJurorPoolPersonalDetails(juror);

        PaperResponse paperResponse = createPaperResponse(dateOfBirth);
        paperResponse.setProcessingStatus(ProcessingStatus.TODO);
        setPaperResponseDetails(paperResponse);
        paperResponse.setAddressLine4("Some Town/City");

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(paperResponse).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);

        Assertions.assertThat(straightThroughProcessorService
            .isValidForStraightThroughAcceptance(jurorNumber, bureauOwner, true)).isFalse();
    }

    @Test
    public void test_isValidForStraightThroughAcceptance_address5Changed() {
        String bureauOwner = "400";
        String jurorNumber = "11111111";
        final LocalDate dateOfBirth = LocalDate.now().minusYears(30);

        JurorPool jurorPool = createJurorPool(1);
        Juror juror = jurorPool.getJuror();
        juror.setJurorNumber(jurorNumber);
        jurorPool.setOwner(bureauOwner);
        setJurorPoolPersonalDetails(juror);
        juror.setAddressLine5("Some Address 5");

        PaperResponse paperResponse = createPaperResponse(dateOfBirth);
        paperResponse.setProcessingStatus(ProcessingStatus.TODO);
        setPaperResponseDetails(paperResponse);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(paperResponse).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);

        Assertions.assertThat(straightThroughProcessorService
            .isValidForStraightThroughAcceptance(jurorNumber, bureauOwner, true)).isFalse();
    }

    @Test
    public void test_isValidForStraightThroughAcceptance_postcodeChanged() {
        String bureauOwner = "400";
        String jurorNumber = "11111111";
        final LocalDate dateOfBirth = LocalDate.now().minusYears(30);

        JurorPool jurorPool = createJurorPool(1);
        Juror juror = jurorPool.getJuror();
        juror.setJurorNumber(jurorNumber);
        jurorPool.setOwner(bureauOwner);
        setJurorPoolPersonalDetails(juror);

        PaperResponse paperResponse = createPaperResponse(dateOfBirth);
        paperResponse.setProcessingStatus(ProcessingStatus.TODO);
        setPaperResponseDetails(paperResponse);
        paperResponse.setPostcode(null);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(paperResponse).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);

        Assertions.assertThat(straightThroughProcessorService
            .isValidForStraightThroughAcceptance(jurorNumber, bureauOwner, true)).isFalse();
    }

    @Test
    public void test_isValidForStraightThroughAcceptance_noDateOfBirth() {
        String bureauOwner = "400";
        String jurorNumber = "11111111";
        final LocalDate dateOfBirth = LocalDate.now().minusYears(30);

        JurorPool jurorPool = createJurorPool(1);
        Juror juror = jurorPool.getJuror();
        juror.setJurorNumber(jurorNumber);
        jurorPool.setOwner(bureauOwner);
        setJurorPoolPersonalDetails(juror);

        PaperResponse paperResponse = createPaperResponse(dateOfBirth);
        paperResponse.setProcessingStatus(ProcessingStatus.TODO);
        setPaperResponseDetails(paperResponse);
        paperResponse.setDateOfBirth(null);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(paperResponse).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);

        Assertions.assertThat(straightThroughProcessorService
            .isValidForStraightThroughAcceptance(jurorNumber, bureauOwner, true)).isFalse();
    }

    @Test
    public void test_isValidForStraightThroughAcceptance_invalidJurorAge() {
        String bureauOwner = "400";
        String jurorNumber = "11111111";
        final LocalDate dateOfBirth = LocalDate.now().minusYears(77);

        JurorPool jurorPool = createJurorPool(1);
        Juror juror = jurorPool.getJuror();
        juror.setJurorNumber(jurorNumber);
        jurorPool.setOwner(bureauOwner);
        setJurorPoolPersonalDetails(juror);

        PaperResponse paperResponse = createPaperResponse(dateOfBirth);
        paperResponse.setProcessingStatus(ProcessingStatus.TODO);
        setPaperResponseDetails(paperResponse);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(paperResponse).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);

        Assertions.assertThat(straightThroughProcessorService
            .isValidForStraightThroughAcceptance(jurorNumber, bureauOwner, true)).isFalse();
    }

    @Test
    public void test_isValidForStraightThroughAcceptance_thirdParty() {
        String bureauOwner = "400";
        String jurorNumber = "11111111";
        final LocalDate dateOfBirth = LocalDate.now().minusYears(30);

        JurorPool jurorPool = createJurorPool(1);
        Juror juror = jurorPool.getJuror();
        juror.setJurorNumber(jurorNumber);
        jurorPool.setOwner(bureauOwner);
        setJurorPoolPersonalDetails(juror);

        PaperResponse paperResponse = createPaperResponse(dateOfBirth);
        paperResponse.setProcessingStatus(ProcessingStatus.TODO);
        setPaperResponseDetails(paperResponse);
        paperResponse.setRelationship("Brother");

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(paperResponse).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);

        Assertions.assertThat(straightThroughProcessorService
            .isValidForStraightThroughAcceptance(jurorNumber, bureauOwner, true)).isFalse();
    }

    @Test
    public void test_isValidForStraightThroughAcceptance_invalidJurorStatus() {
        String bureauOwner = "400";
        String jurorNumber = "11111111";
        final LocalDate dateOfBirth = LocalDate.now().minusYears(30);

        JurorStatus jurorStatus = new JurorStatus();
        jurorStatus.setStatus(IJurorStatus.RESPONDED);

        JurorPool jurorPool = createJurorPool(1);
        Juror juror = jurorPool.getJuror();
        juror.setJurorNumber(jurorNumber);
        jurorPool.setOwner(bureauOwner);
        setJurorPoolPersonalDetails(juror);
        jurorPool.setStatus(jurorStatus);

        PaperResponse paperResponse = createPaperResponse(dateOfBirth);
        paperResponse.setProcessingStatus(ProcessingStatus.TODO);
        setPaperResponseDetails(paperResponse);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(paperResponse).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);

        Assertions.assertThat(straightThroughProcessorService
            .isValidForStraightThroughAcceptance(jurorNumber, bureauOwner, true)).isFalse();
    }

    @Test
    public void test_isValidForStraightThroughAcceptance_invalidResidency_false() {
        String bureauOwner = "400";
        String jurorNumber = "11111111";
        final LocalDate dateOfBirth = LocalDate.now().minusYears(30);

        JurorPool jurorPool = createJurorPool(1);
        Juror juror = jurorPool.getJuror();
        juror.setJurorNumber(jurorNumber);
        jurorPool.setOwner(bureauOwner);
        setJurorPoolPersonalDetails(juror);

        PaperResponse paperResponse = createPaperResponse(dateOfBirth);
        paperResponse.setProcessingStatus(ProcessingStatus.TODO);
        setPaperResponseDetails(paperResponse);
        paperResponse.setResidency(false);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(paperResponse).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);

        Assertions.assertThat(straightThroughProcessorService
            .isValidForStraightThroughAcceptance(jurorNumber, bureauOwner, true)).isFalse();
    }

    @Test
    public void test_isValidForStraightThroughAcceptance_invalidResidency_null() {
        String bureauOwner = "400";
        String jurorNumber = "11111111";
        final LocalDate dateOfBirth = LocalDate.now().minusYears(30);

        JurorPool jurorPool = createJurorPool(1);
        Juror juror = jurorPool.getJuror();
        juror.setJurorNumber(jurorNumber);
        jurorPool.setOwner(bureauOwner);
        setJurorPoolPersonalDetails(juror);

        PaperResponse paperResponse = createPaperResponse(dateOfBirth);
        paperResponse.setProcessingStatus(ProcessingStatus.TODO);
        setPaperResponseDetails(paperResponse);
        paperResponse.setResidency(null);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(paperResponse).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);

        Assertions.assertThat(straightThroughProcessorService
            .isValidForStraightThroughAcceptance(jurorNumber, bureauOwner, true)).isFalse();
    }

    @Test
    public void test_isValidForStraightThroughAcceptance_invalidMentalHealthAct_true() {
        String bureauOwner = "400";
        String jurorNumber = "11111111";
        final LocalDate dateOfBirth = LocalDate.now().minusYears(30);

        JurorPool jurorPool = createJurorPool(1);
        Juror juror = jurorPool.getJuror();
        juror.setJurorNumber(jurorNumber);
        jurorPool.setOwner(bureauOwner);
        setJurorPoolPersonalDetails(juror);

        PaperResponse paperResponse = createPaperResponse(dateOfBirth);
        paperResponse.setProcessingStatus(ProcessingStatus.TODO);
        setPaperResponseDetails(paperResponse);
        paperResponse.setMentalHealthAct(true);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(paperResponse).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);

        Assertions.assertThat(straightThroughProcessorService
            .isValidForStraightThroughAcceptance(jurorNumber, bureauOwner, true)).isFalse();
    }

    @Test
    public void test_isValidForStraightThroughAcceptance_invalidMentalHealthAct_null() {
        String bureauOwner = "400";
        String jurorNumber = "11111111";
        final LocalDate dateOfBirth = LocalDate.now().minusYears(30);

        JurorPool jurorPool = createJurorPool(1);
        Juror juror = jurorPool.getJuror();
        juror.setJurorNumber(jurorNumber);
        jurorPool.setOwner(bureauOwner);
        setJurorPoolPersonalDetails(juror);

        PaperResponse paperResponse = createPaperResponse(dateOfBirth);
        paperResponse.setProcessingStatus(ProcessingStatus.TODO);
        setPaperResponseDetails(paperResponse);
        paperResponse.setMentalHealthAct(null);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(paperResponse).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);

        Assertions.assertThat(straightThroughProcessorService
            .isValidForStraightThroughAcceptance(jurorNumber, bureauOwner, true)).isFalse();
    }

    @Test
    public void test_isValidForStraightThroughAcceptance_invalidMentalHealthCapacity_true() {
        String bureauOwner = "400";
        String jurorNumber = "11111111";
        final LocalDate dateOfBirth = LocalDate.now().minusYears(30);

        JurorPool jurorPool = createJurorPool(1);
        Juror juror = jurorPool.getJuror();
        juror.setJurorNumber(jurorNumber);
        jurorPool.setOwner(bureauOwner);
        setJurorPoolPersonalDetails(juror);

        PaperResponse paperResponse = createPaperResponse(dateOfBirth);
        paperResponse.setProcessingStatus(ProcessingStatus.TODO);
        setPaperResponseDetails(paperResponse);
        paperResponse.setMentalHealthCapacity(true);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(paperResponse).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);

        Assertions.assertThat(straightThroughProcessorService
            .isValidForStraightThroughAcceptance(jurorNumber, bureauOwner, true)).isFalse();
    }

    @Test
    public void test_isValidForStraightThroughAcceptance_invalidMentalHealthCapacity_null() {
        String bureauOwner = "400";
        String jurorNumber = "11111111";
        final LocalDate dateOfBirth = LocalDate.now().minusYears(30);

        JurorPool jurorPool = createJurorPool(1);
        Juror juror = jurorPool.getJuror();
        juror.setJurorNumber(jurorNumber);
        jurorPool.setOwner(bureauOwner);
        setJurorPoolPersonalDetails(juror);

        PaperResponse paperResponse = createPaperResponse(dateOfBirth);
        paperResponse.setProcessingStatus(ProcessingStatus.TODO);
        setPaperResponseDetails(paperResponse);
        paperResponse.setMentalHealthCapacity(null);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(paperResponse).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);

        Assertions.assertThat(straightThroughProcessorService
            .isValidForStraightThroughAcceptance(jurorNumber, bureauOwner, true)).isFalse();
    }

    @Test
    public void test_isValidForStraightThroughAcceptance_invalidBail_true() {
        String bureauOwner = "400";
        String jurorNumber = "11111111";
        final LocalDate dateOfBirth = LocalDate.now().minusYears(30);

        JurorPool jurorPool = createJurorPool(1);
        Juror juror = jurorPool.getJuror();
        juror.setJurorNumber(jurorNumber);
        jurorPool.setOwner(bureauOwner);
        setJurorPoolPersonalDetails(juror);

        PaperResponse paperResponse = createPaperResponse(dateOfBirth);
        paperResponse.setProcessingStatus(ProcessingStatus.TODO);
        setPaperResponseDetails(paperResponse);
        paperResponse.setBail(true);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(paperResponse).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);

        Assertions.assertThat(straightThroughProcessorService
            .isValidForStraightThroughAcceptance(jurorNumber, bureauOwner, true)).isFalse();
    }

    @Test
    public void test_isValidForStraightThroughAcceptance_invalidBail_null() {
        String bureauOwner = "400";
        String jurorNumber = "11111111";
        final LocalDate dateOfBirth = LocalDate.now().minusYears(30);

        JurorPool jurorPool = createJurorPool(1);
        Juror juror = jurorPool.getJuror();
        juror.setJurorNumber(jurorNumber);
        jurorPool.setOwner(bureauOwner);
        setJurorPoolPersonalDetails(juror);

        PaperResponse paperResponse = createPaperResponse(dateOfBirth);
        paperResponse.setProcessingStatus(ProcessingStatus.TODO);
        setPaperResponseDetails(paperResponse);
        paperResponse.setBail(null);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(paperResponse).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);

        Assertions.assertThat(straightThroughProcessorService
            .isValidForStraightThroughAcceptance(jurorNumber, bureauOwner, true)).isFalse();
    }

    @Test
    public void test_isValidForStraightThroughAcceptance_invalidConvictions() {
        String bureauOwner = "400";
        String jurorNumber = "11111111";
        final LocalDate dateOfBirth = LocalDate.now().minusYears(30);

        JurorPool jurorPool = createJurorPool(1);
        Juror juror = jurorPool.getJuror();
        juror.setJurorNumber(jurorNumber);
        jurorPool.setOwner(bureauOwner);
        setJurorPoolPersonalDetails(juror);

        PaperResponse paperResponse = createPaperResponse(dateOfBirth);
        paperResponse.setProcessingStatus(ProcessingStatus.TODO);
        setPaperResponseDetails(paperResponse);
        paperResponse.setConvictions(true);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(paperResponse).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);

        Assertions.assertThat(straightThroughProcessorService
            .isValidForStraightThroughAcceptance(jurorNumber, bureauOwner, true)).isFalse();
    }

    @Test
    public void test_isValidForStraightThroughAcceptance_invalidConvictions_null() {
        String bureauOwner = "400";
        String jurorNumber = "11111111";
        final LocalDate dateOfBirth = LocalDate.now().minusYears(30);

        JurorPool jurorPool = createJurorPool(1);
        Juror juror = jurorPool.getJuror();
        juror.setJurorNumber(jurorNumber);
        jurorPool.setOwner(bureauOwner);
        setJurorPoolPersonalDetails(juror);

        PaperResponse paperResponse = createPaperResponse(dateOfBirth);
        paperResponse.setProcessingStatus(ProcessingStatus.TODO);
        setPaperResponseDetails(paperResponse);
        paperResponse.setConvictions(null);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(paperResponse).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);

        Assertions.assertThat(straightThroughProcessorService
            .isValidForStraightThroughAcceptance(jurorNumber, bureauOwner, true)).isFalse();
    }

    @Test
    public void test_isValidForStraightThroughAcceptance_invalidSignature_false() {
        String bureauOwner = "400";
        String jurorNumber = "11111111";
        final LocalDate dateOfBirth = LocalDate.now().minusYears(30);

        JurorPool jurorPool = createJurorPool(1);
        Juror juror = jurorPool.getJuror();
        juror.setJurorNumber(jurorNumber);
        jurorPool.setOwner(bureauOwner);
        setJurorPoolPersonalDetails(juror);

        PaperResponse paperResponse = createPaperResponse(dateOfBirth);
        paperResponse.setProcessingStatus(ProcessingStatus.TODO);
        setPaperResponseDetails(paperResponse);
        paperResponse.setSigned(false);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(paperResponse).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);

        Assertions.assertThat(straightThroughProcessorService
            .isValidForStraightThroughAcceptance(jurorNumber, bureauOwner, true)).isFalse();
    }

    @Test
    public void test_isValidForStraightThroughAcceptance_invalidSignature_null() {
        String bureauOwner = "400";
        String jurorNumber = "11111111";
        final LocalDate dateOfBirth = LocalDate.now().minusYears(30);

        JurorPool jurorPool = createJurorPool(1);
        Juror juror = jurorPool.getJuror();
        juror.setJurorNumber(jurorNumber);
        jurorPool.setOwner(bureauOwner);
        setJurorPoolPersonalDetails(juror);

        PaperResponse paperResponse = createPaperResponse(dateOfBirth);
        paperResponse.setProcessingStatus(ProcessingStatus.TODO);
        setPaperResponseDetails(paperResponse);
        paperResponse.setSigned(null);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(paperResponse).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);

        Assertions.assertThat(straightThroughProcessorService
            .isValidForStraightThroughAcceptance(jurorNumber, bureauOwner, true)).isFalse();
    }

    @Test
    public void test_isValidForStraightThroughAcceptance_excusalRequested() {
        String bureauOwner = "400";
        String jurorNumber = "11111111";
        final LocalDate dateOfBirth = LocalDate.now().minusYears(30);

        JurorPool jurorPool = createJurorPool(1);
        Juror juror = jurorPool.getJuror();
        juror.setJurorNumber(jurorNumber);
        jurorPool.setOwner(bureauOwner);
        setJurorPoolPersonalDetails(juror);

        PaperResponse paperResponse = createPaperResponse(dateOfBirth);
        paperResponse.setProcessingStatus(ProcessingStatus.TODO);
        setPaperResponseDetails(paperResponse);
        paperResponse.setExcusal(true);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(paperResponse).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);

        Assertions.assertThat(straightThroughProcessorService
            .isValidForStraightThroughAcceptance(jurorNumber, bureauOwner, false)).isFalse();
    }

    @Test
    public void test_isValidForStraightThroughAcceptance_deferralRequested() {
        String bureauOwner = "400";
        String jurorNumber = "11111111";
        final LocalDate dateOfBirth = LocalDate.now().minusYears(30);

        JurorPool jurorPool = createJurorPool(1);
        Juror juror = jurorPool.getJuror();
        juror.setJurorNumber(jurorNumber);
        jurorPool.setOwner(bureauOwner);
        setJurorPoolPersonalDetails(juror);

        PaperResponse paperResponse = createPaperResponse(dateOfBirth);
        paperResponse.setProcessingStatus(ProcessingStatus.TODO);
        setPaperResponseDetails(paperResponse);
        paperResponse.setDeferral(true);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(paperResponse).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);

        Assertions.assertThat(straightThroughProcessorService
            .isValidForStraightThroughAcceptance(jurorNumber, bureauOwner, false)).isFalse();
    }

    @Test
    public void test_isValidForStraightThroughAcceptance_noReplyTypeSelected() {
        String bureauOwner = "400";
        String jurorNumber = "11111111";
        final LocalDate dateOfBirth = LocalDate.now().minusYears(30);

        JurorPool jurorPool = createJurorPool(1);
        Juror juror = jurorPool.getJuror();
        juror.setJurorNumber(jurorNumber);
        jurorPool.setOwner(bureauOwner);
        setJurorPoolPersonalDetails(juror);

        PaperResponse paperResponse = createPaperResponse(dateOfBirth);
        paperResponse.setProcessingStatus(ProcessingStatus.TODO);
        setPaperResponseDetails(paperResponse);
        paperResponse.setDeferral(null);
        paperResponse.setExcusal(null);

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(paperResponse).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);

        Assertions.assertThat(straightThroughProcessorService
            .isValidForStraightThroughAcceptance(jurorNumber, bureauOwner, false)).isFalse();
    }

    @Test
    public void test_isValidForStraightThroughAcceptance_cjsEmploymentExists() {
        String bureauOwner = "400";
        String jurorNumber = "11111111";
        final LocalDate dateOfBirth = LocalDate.now().minusYears(30);

        JurorPool jurorPool = createJurorPool(1);
        Juror juror = jurorPool.getJuror();
        juror.setJurorNumber(jurorNumber);
        jurorPool.setOwner(bureauOwner);
        setJurorPoolPersonalDetails(juror);

        PaperResponse paperResponse = createPaperResponse(dateOfBirth);
        paperResponse.setProcessingStatus(ProcessingStatus.TODO);
        setPaperResponseDetails(paperResponse);

        JurorResponseCjsEmployment cjsEmployment = new JurorResponseCjsEmployment();
        cjsEmployment.setJurorNumber(jurorNumber);
        cjsEmployment.setCjsEmployer("POLICE");

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(paperResponse).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(Collections.singletonList(cjsEmployment))
            .when(jurorResponseCjsEmploymentRepository).findByJurorNumber(jurorNumber);

        Assertions.assertThat(straightThroughProcessorService
            .isValidForStraightThroughAcceptance(jurorNumber, bureauOwner, true)).isFalse();
    }

    @Test
    public void test_isValidForStraightThroughAcceptance_reasonableAdjustmentRequested() {
        String bureauOwner = "400";
        String jurorNumber = "11111111";
        final LocalDate dateOfBirth = LocalDate.now().minusYears(30);

        JurorPool jurorPool = createJurorPool(1);
        Juror juror = jurorPool.getJuror();
        juror.setJurorNumber(jurorNumber);
        jurorPool.setOwner(bureauOwner);
        setJurorPoolPersonalDetails(juror);

        PaperResponse paperResponse = createPaperResponse(dateOfBirth);
        paperResponse.setProcessingStatus(ProcessingStatus.TODO);
        setPaperResponseDetails(paperResponse);

        JurorReasonableAdjustment adjustment1 = new JurorReasonableAdjustment();
        adjustment1.setJurorNumber(jurorNumber);
        adjustment1.setReasonableAdjustment(new ReasonableAdjustments("V", "Visual Impairment"));

        JurorReasonableAdjustment adjustment2 = new JurorReasonableAdjustment();
        adjustment2.setJurorNumber(jurorNumber);
        adjustment2.setReasonableAdjustment(new ReasonableAdjustments("W", "Wheelchair Access"));

        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.doReturn(paperResponse).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(Arrays.asList(adjustment1, adjustment2))
            .when(jurorReasonableAdjustmentRepository).findByJurorNumber(jurorNumber);

        Assertions.assertThat(straightThroughProcessorService
            .isValidForStraightThroughAcceptance(jurorNumber, bureauOwner, true)).isFalse();
    }

    private JurorPool createJurorPool(int statusCode) {
        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCourtName("CHESTER");
        courtLocation.setLocCode("415");

        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber("415230101");
        poolRequest.setReturnDate(LocalDate.now().plusWeeks(3));

        Juror juror = new Juror();
        juror.setJurorNumber("123456789");

        JurorPool jurorPool = new JurorPool();
        jurorPool.setStatus(createJurorStatus(statusCode));
        jurorPool.setPool(poolRequest);

        juror.setAssociatedPools(Set.of(jurorPool));
        jurorPool.setJuror(juror);

        return jurorPool;
    }

    private PoolRequest mockPoolRequest(String poolNumber, String owner) {
        PoolRequest mockPoolRequest = new PoolRequest();
        mockPoolRequest.setPoolNumber(poolNumber);
        mockPoolRequest.setOwner(owner);
        mockPoolRequest.setReturnDate(LocalDate.now().plusWeeks(3));
        return mockPoolRequest;
    }

    private void setJurorPoolPersonalDetails(Juror juror) {
        juror.setTitle("Mr");
        juror.setFirstName("Test");
        juror.setLastName("Person");

        juror.setAddressLine1("Some Address");
        juror.setAddressLine4("Some Town");
        juror.setPostcode("PS7 0DE");
    }

    private PaperResponse createPaperResponse(LocalDate dateOfBirth) {
        PaperResponse paperResponse = new PaperResponse();
        paperResponse.setDateOfBirth(dateOfBirth);
        return paperResponse;
    }

    private void setPaperResponseDetails(PaperResponse paperResponse) {
        paperResponse.setTitle("Mr");
        paperResponse.setFirstName("Test");
        paperResponse.setLastName("Person");

        paperResponse.setAddressLine1("Some Address");
        paperResponse.setAddressLine4("Some Town");
        paperResponse.setPostcode("PS7 0DE");

        paperResponse.setResidency(true);
        paperResponse.setMentalHealthAct(false);
        paperResponse.setMentalHealthCapacity(false);
        paperResponse.setBail(false);
        paperResponse.setConvictions(false);

        paperResponse.setSigned(true);
    }

    private DigitalResponse createDigitalResponse(LocalDate dateOfBirth) {
        DigitalResponse digitalResponse = new DigitalResponse();
        digitalResponse.setDateOfBirth(dateOfBirth);
        return digitalResponse;
    }

    private JurorStatus createJurorStatus(int statusCode) {
        JurorStatus jurorStatus = new JurorStatus();
        jurorStatus.setStatus(statusCode);

        return jurorStatus;
    }

}