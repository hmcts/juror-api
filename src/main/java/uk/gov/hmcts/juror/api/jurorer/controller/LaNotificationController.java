package uk.gov.hmcts.juror.api.jurorer.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.config.security.IsBureauUser;
import uk.gov.hmcts.juror.api.jurorer.controller.dto.LaNotificationRequestDto;
import uk.gov.hmcts.juror.api.jurorer.service.LaNotificationService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;



@Slf4j
@RestController
@Validated
@RequestMapping(value = "/api/v1/moj/notification", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "LaNotification")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@IsBureauUser
public class LaNotificationController {

    private final LaNotificationService laNotificationService;

    @PostMapping("/send-la-reminder")
    @Operation(summary = "Send reminder notifications to selected Local Authorities")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(SecurityUtil.IS_BUREAU)
    public ResponseEntity<Void> sendNotification(
        @RequestBody @Valid @NotNull LaNotificationRequestDto request) {

        log.info("Sending notifications to {} Local Authorities", request.getLaCodes().size());
        laNotificationService.sendNotificationsToLocalAuthorities(request.getLaCodes());

        return ResponseEntity.noContent().build();
    }
}



