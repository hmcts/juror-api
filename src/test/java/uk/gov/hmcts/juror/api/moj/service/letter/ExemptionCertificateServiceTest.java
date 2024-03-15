package uk.gov.hmcts.juror.api.moj.service.letter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.JurorForExemptionListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.TrialExemptionListDto;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.trial.Courtroom;
import uk.gov.hmcts.juror.api.moj.domain.trial.Judge;
import uk.gov.hmcts.juror.api.moj.domain.trial.Panel;
import uk.gov.hmcts.juror.api.moj.domain.trial.Trial;
import uk.gov.hmcts.juror.api.moj.enumeration.trial.PanelResult;
import uk.gov.hmcts.juror.api.moj.enumeration.trial.TrialType;
import uk.gov.hmcts.juror.api.moj.repository.trial.PanelRepository;
import uk.gov.hmcts.juror.api.moj.repository.trial.TrialRepository;
import uk.gov.hmcts.juror.api.moj.service.trial.ExemptionCertificateServiceImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;

@ExtendWith(SpringExtension.class)
public class ExemptionCertificateServiceTest {
    @Mock
    private PanelRepository panelRepository;

    @Mock
    private TrialRepository trialRepository;

    @InjectMocks
    private ExemptionCertificateServiceImpl exemptionCertificateService;


    @Test
    void testRetrieveTrialExemptionListHappy() {
        TestUtils.setUpMockAuthentication("415", "TEST_COURT", "1", List.of("415"));

        Mockito.doReturn(setupTrial()).when(trialRepository).getListOfActiveTrials(
            "415");

        List<TrialExemptionListDto> trials = exemptionCertificateService.getTrialExemptionList("415");
        assertThat(trials).isNotNull();
        assertThat(trials.size()).as("Expect size to be one").isEqualTo(1);

        Mockito.verify(trialRepository, times(1))
            .getListOfActiveTrials("415");

        assertThat(trials.get(0).getTrialType())
            .as("Expect trial type to be Civil")
            .isEqualTo("Civil");
        assertThat(trials.get(0).getEndDate())
            .as("Expect trial end date to be null")
            .isNull();
        assertThat(trials.get(0).getTrialNumber())
            .as("Expect trial number to be T10000000")
            .isEqualTo("T10000000");
        assertThat(trials.get(0).getJudge())
            .as("Expect trial judge to be SIR DREDD")
            .isEqualTo("SIR DREDD");
        assertThat(trials.get(0).getStartDate())
            .as("Expect trial start date to be " + LocalDate.now())
            .isEqualTo(LocalDate.now());

    }

    @Test
    void testRetrieveJurorExemptionListHappy() {
        final String caseNumber = "T10000000";
        final String courtLocation = "415";

        TestUtils.setUpMockAuthentication("415", "TEST_COURT", "1", List.of("415"));

        Mockito.doReturn(setupPanel(setupTrial().get(0)))
            .when(panelRepository)
            .findByTrialTrialNumberAndTrialCourtLocationLocCode(caseNumber, courtLocation);

        List<JurorForExemptionListDto> jurors = exemptionCertificateService.getJurorsForExemptionList(caseNumber,
            courtLocation);

        Mockito.verify(panelRepository, times(1))
            .findByTrialTrialNumberAndTrialCourtLocationLocCode(caseNumber,courtLocation);

        assertThat(jurors).isNotNull();
        assertThat(jurors.size())
            .as("Expect single juror only")
            .isEqualTo(1);

        assertThat(jurors.get(0).getJurorNumber())
            .as("Expect juror number to be 111111111")
            .isEqualTo("111111111");
        assertThat(jurors.get(0).getFirstName())
            .as("Expect juror's first name to be FNAME")
            .isEqualTo("FNAME");
        assertThat(jurors.get(0).getLastName())
            .as("Expect juror's first name to be LNAME")
            .isEqualTo("LNAME");
        assertThat(jurors.get(0).getDateEmpanelled())
            .as("Expect date empanelled to be " + LocalDate.now())
            .isEqualTo(LocalDate.now());
    }

    List<Trial> setupTrial() {

        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCourtName("Test location");
        courtLocation.setLocCode("415");

        Judge judge = new Judge();
        judge.setOwner("415");
        judge.setDescription("SIR DREDD");

        Courtroom courtroom = new Courtroom();
        courtroom.setDescription("Test room");
        courtroom.setRoomNumber("1");

        Trial trial = new Trial();
        trial.setTrialNumber("T10000000");
        trial.setTrialType(TrialType.CIV);
        trial.setTrialStartDate(LocalDate.now());
        trial.setCourtLocation(courtLocation);
        trial.setCourtroom(courtroom);
        trial.setJudge(judge);

        return Collections.singletonList(trial);
    }

    List<Panel> setupPanel(Trial trial) {

        Juror juror = new Juror();
        juror.setFirstName("FNAME");
        juror.setLastName("LNAME");
        juror.setJurorNumber("111111111");

        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCourtName("Test location");
        courtLocation.setLocCode("415");

        PoolRequest request = new PoolRequest();
        request.setOwner("415");
        request.setCourtLocation(courtLocation);
        request.setPoolNumber("415000001");

        JurorPool jurorPool = new JurorPool();
        jurorPool.setJuror(juror);
        jurorPool.setPool(request);

        Panel panel = new Panel();
        panel.setJurorPool(jurorPool);
        panel.setResult(PanelResult.JUROR);
        panel.setTrial(trial);
        panel.setDateSelected(LocalDateTime.now());
        return Collections.singletonList(panel);
    }
}