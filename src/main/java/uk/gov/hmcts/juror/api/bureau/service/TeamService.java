package uk.gov.hmcts.juror.api.bureau.service;

import uk.gov.hmcts.juror.api.bureau.controller.response.TeamDto;

import java.util.List;

public interface TeamService {
    List<TeamDto> findAllTeams();
}
