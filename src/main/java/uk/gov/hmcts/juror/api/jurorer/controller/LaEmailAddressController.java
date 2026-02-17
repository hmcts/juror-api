package uk.gov.hmcts.juror.api.jurorer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.jurorer.controller.dto.ExportLaEmailAddressResponseDto;
import uk.gov.hmcts.juror.api.jurorer.service.LaUserService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

@RestController
@RequestMapping(value = "/api/v1/moj/LaExport", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "LaExport")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@PreAuthorize(SecurityUtil.IS_BUREAU)
public class LaEmailAddressController {

    private final LaUserService userService;

    @GetMapping("/email-addresses")
    @Operation(summary = "Get a list of all email addresses associated with all Local Authorities")
    @PreAuthorize(SecurityUtil.IS_BUREAU)
    public ResponseEntity<ExportLaEmailAddressResponseDto> exportEmailAddresses() {
        ExportLaEmailAddressResponseDto response = userService.getAllLaEmailAddresses();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
