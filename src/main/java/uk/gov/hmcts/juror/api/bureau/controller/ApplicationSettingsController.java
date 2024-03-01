package uk.gov.hmcts.juror.api.bureau.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.bureau.controller.response.AppSettingsResponseDto;
import uk.gov.hmcts.juror.api.bureau.domain.AppSetting;
import uk.gov.hmcts.juror.api.bureau.service.AppSettingService;

import java.util.List;

@RestController
@Tag(name = "Bureau Frontend Settings API", description = "Bureau Frontend settings API")
@Slf4j
@RequestMapping(value = "/api/v1/auth", produces = MediaType.APPLICATION_JSON_VALUE)
public class ApplicationSettingsController {
    private final AppSettingService appSettingService;

    @Autowired
    public ApplicationSettingsController(final AppSettingService appSettingService) {
        Assert.notNull(appSettingService, "AppSettingService cannot be null.");
        this.appSettingService = appSettingService;
    }

    /**
     * Expose all of the application settings via a single endpoint.
     *
     * @return Application settings
     */
    @GetMapping(path = "/settings")
    @Operation(summary = "Application settings",
        description = "Expose the application settings via the API")
    public ResponseEntity<AppSettingsResponseDto> applicationSettings() {
        try {
            List<AppSetting> settings = appSettingService.findAllSettings();
            return ResponseEntity.ok()
                .body(AppSettingsResponseDto.builder()
                    .data(settings)
                    .build()
                );
        } catch (Exception e) {
            log.error("Failed to retrieve application settings: {}", e.getMessage());
            throw e;
        }
    }
}
