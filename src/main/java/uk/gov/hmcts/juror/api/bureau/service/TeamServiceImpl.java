package uk.gov.hmcts.juror.api.bureau.service;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import uk.gov.hmcts.juror.api.bureau.controller.response.TeamDto;
import uk.gov.hmcts.juror.api.bureau.domain.Team;
import uk.gov.hmcts.juror.api.bureau.domain.TeamRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;

    @Autowired
    public TeamServiceImpl(final TeamRepository teamRepository) {
        Assert.notNull(teamRepository, "TeamRepository cannot be null");
        this.teamRepository = teamRepository;

    }


    @Override
    public List<TeamDto> findAllTeams() {
        final ArrayList<Team> teams = Lists.newArrayList(teamRepository.findAll());
        return teams.stream().map(team -> new TeamDto(team))
            .collect(Collectors.toList());
    }

}
