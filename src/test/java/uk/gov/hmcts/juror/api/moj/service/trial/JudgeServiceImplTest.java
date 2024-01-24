package uk.gov.hmcts.juror.api.moj.service.trial;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.JudgeListDto;
import uk.gov.hmcts.juror.api.moj.domain.trial.Judge;
import uk.gov.hmcts.juror.api.moj.repository.trial.JudgeRepository;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
public class JudgeServiceImplTest {
    private static final String OWNER_415 = "415";

    @Mock
    private JudgeRepository judgeRepository;

    @InjectMocks
    private JudgeServiceImpl judgeService;

    @Test
    public void judgeForCourtLocationHappy() {
        final ArgumentCaptor<String> ownerArg = ArgumentCaptor.forClass(String.class);

        doReturn(createJudgeListFindByOwner415()).when(judgeRepository).findByOwner(OWNER_415);

        JudgeListDto judgeForCourtLocation = judgeService.getJudgeForCourtLocation(OWNER_415);

        verify(judgeRepository, times(1)).findByOwner(ownerArg.capture());
        assertThat(ownerArg.getValue()).isEqualTo(OWNER_415);
        assertThat(judgeForCourtLocation.getJudges().size()).isEqualTo(2);
    }

    @Test
    public void judgeForCourtLocationNoJudgesExistForGivenOwner() {
        final ArgumentCaptor<String> ownerArg = ArgumentCaptor.forClass(String.class);

        List<Judge> judges = new ArrayList<>();
        doReturn(judges).when(judgeRepository).findByOwner(OWNER_415);

        JudgeListDto judgeForCourtLocation = judgeService.getJudgeForCourtLocation(OWNER_415);

        verify(judgeRepository, times(1)).findByOwner(ownerArg.capture());
        assertThat(ownerArg.getValue()).isEqualTo(OWNER_415);
        assertThat(judgeForCourtLocation.getJudges().size()).isEqualTo(0);
    }

    private List<Judge> createJudgeListFindByOwner415() {
        Judge judge1 = new Judge();
        judge1.setOwner("415");
        judge1.setCode("DRED");
        judge1.setDescription("DREDD");

        Judge judge2 = new Judge();
        judge2.setOwner("415");
        judge2.setCode("JUDD");
        judge2.setDescription("LAWSON");

        List<Judge> judges = new ArrayList<>();
        judges.add(judge1);
        judges.add(judge2);

        return judges;
    }
}