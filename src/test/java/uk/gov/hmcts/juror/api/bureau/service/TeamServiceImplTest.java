package uk.gov.hmcts.juror.api.bureau.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.juror.api.bureau.controller.response.TeamDto;
import uk.gov.hmcts.juror.api.bureau.domain.Team;
import uk.gov.hmcts.juror.api.bureau.domain.TeamRepository;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@RunWith(SpringJUnit4ClassRunner.class)
public class TeamServiceImplTest {

    @Mock
    private TeamRepository mockTeamRepository;

    @InjectMocks
    TeamServiceImpl teamService;


    @Test
    public void findAllTeams() {

        final List<Team> teamList = new ArrayList<>(3);
        teamList.add(new Team(1L, "London", 1));
        teamList.add(new Team(2L, "Midlands", 1));

        given(mockTeamRepository.findAll()).willReturn(teamList);

        List<TeamDto> teams = teamService.findAllTeams();

        assertThat(teams).hasSize(2).containsExactly(new TeamDto(1L, "London", 1),
            new TeamDto(2L, "Midlands", 1));

    }


}
