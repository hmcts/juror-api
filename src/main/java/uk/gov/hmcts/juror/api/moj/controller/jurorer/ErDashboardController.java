package uk.gov.hmcts.juror.api.moj.controller.jurorer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.config.security.IsBureauUser;
import uk.gov.hmcts.juror.api.moj.service.jurorer.ErDashboardService;

@RestController
@RequestMapping(value = "/api/v1/moj/er-dashboard/", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "ER Dashboard", description = "Bureau ER Dashboard")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ErDashboardController {

    private final ErDashboardService erDashboardService;

    @IsBureauUser
    @GetMapping("/upload-stats")
    @Operation(summary = "Retrieves dashboard stats on Juror ER uploads for bureau")
    public ResponseEntity<ErDashboardStatsResponseDto> getErDashboardStats() {
        ErDashboardStatsResponseDto dto = erDashboardService.getErDashboardStats();
        return ResponseEntity.ok().body(dto);
    }

    @IsBureauUser
    @PostMapping("/local-authority-status")
    @Operation(summary = "Retrieves ER upload status of local authorities for bureau")
    public ResponseEntity<ErLocalAuthorityStatusResponseDto> getLocalAuthorityStatus(
        @RequestBody ErLocalAuthorityStatusRequestDto requestDto) {
        ErLocalAuthorityStatusResponseDto dto = erDashboardService.getLocalAuthorityStatus(requestDto);
        return ResponseEntity.ok().body(dto);
    }

    @IsBureauUser
    @GetMapping("/local-authorities")
    @Operation(summary = "Retrieves a list of all local authorities for bureau, can be filtered by active only")
    public ResponseEntity<LocalAuthoritiesResponseDto> getLocalAuthorities(
        @RequestParam(value = "active_only", required = false, defaultValue = "false")
        @Parameter(description = "active_only")
        @Valid Boolean activeOnly
    ) {
        LocalAuthoritiesResponseDto dto = erDashboardService.getLocalAuthorities(activeOnly);
        return ResponseEntity.ok().body(dto);
    }

    @IsBureauUser
    @GetMapping("/local-authority-info/{laCode}")
    @Operation(summary = "Retrieves detailed info on a local authority for bureau")
    public ResponseEntity<LocalAuthorityInfoResponseDto> getLocalAuthorityInfo(
        @Parameter(description = "laCode", required = true) @PathVariable("laCode") String laCode
    ) {
        LocalAuthorityInfoResponseDto dto = erDashboardService.getLocalAuthorityInfo(laCode);
        return ResponseEntity.ok().body(dto);
    }

}


