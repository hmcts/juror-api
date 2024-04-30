package uk.gov.hmcts.juror.api.bureau.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.bureau.controller.response.TeamDto;
import uk.gov.hmcts.juror.api.bureau.service.BureauAuthenticationService;
import uk.gov.hmcts.juror.api.bureau.service.TeamService;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtAuthentication;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/bureau/team", produces = MediaType.APPLICATION_JSON_VALUE)
public class BureauTeamController {

    private final BureauAuthenticationService bureauAuthService;
    private final TeamService teamService;

    @Autowired
    public BureauTeamController(final BureauAuthenticationService bureauAuthService,
                                final TeamService teamService) {
        Assert.notNull(bureauAuthService, "BureauAuthenticationService cannot be null");
        Assert.notNull(teamService, "TeamService cannot be null");
        this.bureauAuthService = bureauAuthService;
        this.teamService = teamService;
    }

    @GetMapping
    @Operation(summary = "team list",
        description = "Retrieve a list of all teams")
    public ResponseEntity<List<TeamDto>> getAllTeams(@Parameter(hidden = true) BureauJwtAuthentication principal) {
        if (!SecurityUtil.isBureauManager()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return ResponseEntity.ok(teamService.findAllTeams());
    }

}
