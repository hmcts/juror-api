package uk.gov.hmcts.juror.api.moj.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.moj.domain.administration.JudgeCreateDto;
import uk.gov.hmcts.juror.api.moj.domain.administration.JudgeDetailsDto;
import uk.gov.hmcts.juror.api.moj.domain.administration.JudgeUpdateDto;
import uk.gov.hmcts.juror.api.moj.service.administration.AdministrationJudgeService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.util.List;

@RestController
@Validated
@RequestMapping(value = "/api/v1/moj/administration/judges", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Administration - Judges")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@PreAuthorize(SecurityUtil.COURT_AUTH)
public class AdministrationJudgeController {

    private final AdministrationJudgeService administrationJudgeService;

    @GetMapping("/{judge_id}")
    @Operation(summary = "View a a judges details")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<JudgeDetailsDto> viewJudgeDetails(
        @PathVariable("judge_id")
        @Parameter(description = "judge_id", required = true)
        @Valid Long judgeId
    ) {
        return ResponseEntity.ok(administrationJudgeService.viewJudge(judgeId));
    }

    @DeleteMapping("/{judge_id}")
    @Operation(summary = "Delete a judge")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<JudgeDetailsDto> deleteJudge(
        @PathVariable("judge_id")
        @Parameter(description = "judge_id", required = true)
        @Valid Long judgeId
    ) {
        administrationJudgeService.deleteJudge(judgeId);
        return ResponseEntity.accepted().build();
    }

    @GetMapping
    @Operation(summary = "View all judges details")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<JudgeDetailsDto>> viewAllJudgeDetails(
        @RequestParam(value = "is_active", required = false)
        @Parameter(description = "is_active")
        @Valid Boolean isActive
    ) {
        return ResponseEntity.ok(administrationJudgeService.viewAllJudges(isActive));
    }

    @PostMapping
    @Operation(summary = "Create a judge")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<Void> createJudgeDetails(
        @RequestBody @Valid JudgeCreateDto judgeCreateDto
    ) {
        administrationJudgeService.createJudge(judgeCreateDto);
        return ResponseEntity.accepted().build();
    }

    @PutMapping("/{judge_id}")
    @Operation(summary = "Update a judges details")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<Void> updateJudgeDetails(
        @PathVariable("judge_id")
        @Parameter(description = "judge_id", required = true)
        @Valid Long judgeId,
        @RequestBody @Valid JudgeUpdateDto judgeUpdateDto
    ) {
        administrationJudgeService.updateJudge(judgeId, judgeUpdateDto);
        return ResponseEntity.accepted().build();
    }
}
