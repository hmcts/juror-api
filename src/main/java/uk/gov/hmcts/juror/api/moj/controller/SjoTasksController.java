package uk.gov.hmcts.juror.api.moj.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.config.security.IsSeniorCourtUser;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorAndPoolRequest;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorPoolSearch;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorDetailsDto;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;
import uk.gov.hmcts.juror.api.moj.service.BulkService;
import uk.gov.hmcts.juror.api.moj.service.JurorPoolService;
import uk.gov.hmcts.juror.api.moj.service.SjoTasksService;

import java.util.List;

@RestController
@Validated
@RequestMapping(value = "/api/v1/moj/sjo-tasks", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Validation")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@IsSeniorCourtUser
public class SjoTasksController {

    private final SjoTasksService sjoTasksService;
    private final JurorPoolService jurorPoolService;
    private final BulkService bulkService;

    @PostMapping("/juror/search")
    @Operation(summary = "Get a list of failed to attend jurors based on search criteria")
    @ResponseStatus(HttpStatus.OK)
    public PaginatedList<JurorDetailsDto> getCompleteJurors(
        @Valid @RequestBody JurorPoolSearch request) {
        return jurorPoolService.search(request);
    }

    @PatchMapping("/failed-to-attend/undo")
    @Operation(summary = "Undo failed to attend status for a list of jurors")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void undoFailedToAttendStatus(
        @Valid
        @NotNull
        @Size(min = 1, max = 500)
        @RequestBody List<@Valid @NotNull JurorAndPoolRequest> requestList) {
        bulkService.processVoid(requestList,
            jurorAndPoolRequest -> sjoTasksService.undoFailedToAttendStatus(
                jurorAndPoolRequest.getJurorNumber(),
                jurorAndPoolRequest.getPoolNumber())
        );
    }

}


