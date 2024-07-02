package uk.gov.hmcts.juror.api.bureau.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.bureau.controller.request.BureauBacklogAllocateRequestDto;
import uk.gov.hmcts.juror.api.bureau.service.BureauBacklogAllocateService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/bureau/backlogAllocate", produces = MediaType.APPLICATION_JSON_VALUE)
public class BureauBacklogAllocateController {

    private final BureauBacklogAllocateService bureauBacklogAllocateService;

    @Autowired
    public BureauBacklogAllocateController(
        final BureauBacklogAllocateService bureauBacklogAllocateService) {
        Assert.notNull(bureauBacklogAllocateService, "BureauBacklogAllocateService cannot be null!");
        this.bureauBacklogAllocateService = bureauBacklogAllocateService;
    }

    @PostMapping(path = "/replies")
    @Operation(summary = "Allocate Backlog replies to selected staff")
    @PreAuthorize(SecurityUtil.IS_BUREAU_MANAGER)
    public ResponseEntity<Void> allocateBacklogReplies(
        @Validated @RequestBody BureauBacklogAllocateRequestDto bureauBacklogAllocateRequestDto) {
        try {
            bureauBacklogAllocateService.allocateBacklogReplies(
                bureauBacklogAllocateRequestDto,
                SecurityUtil.getUsername()
            );
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            log.error("Failed to allocate replies:", e);
            throw e;
        }
    }

}