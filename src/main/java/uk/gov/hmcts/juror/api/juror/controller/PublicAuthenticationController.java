package uk.gov.hmcts.juror.api.juror.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.juror.service.PublicAuthenticationService;
import uk.gov.hmcts.juror.api.validation.ValidationConstants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.juror.api.validation.ValidationConstants.NO_PIPES_REGEX;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.POSTCODE_REGEX;

/**
 * Authentication endpoints for Public Juror authentication.
 */
@RestController
@RequestMapping(value = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
@Tag(name = "Auth (Public)", description = "Juror Authentication API")
public class PublicAuthenticationController {
    private final PublicAuthenticationService authService;

    @Autowired
    public PublicAuthenticationController(PublicAuthenticationService authenticationService) {
        Assert.notNull(authenticationService, "PublicAuthenticationService cannot be null");
        this.authService = authenticationService;
    }

    /**
     * Authentication endpoint for Jurors.
     *
     * @param requestDto Credentials
     * @return juror details for minting a JWT
     */
    @PostMapping("/auth/juror")
    @Operation(summary = "Authenticate Public Login", description = "Authenticate Juror credentials to"
        + " allow creation of a JWT")
    public ResponseEntity<PublicAuthenticationResponseDto> authenticationEndpoint(
        @Valid @RequestBody PublicAuthenticationRequestDto requestDto) {
        return ResponseEntity.ok().body(authService.authenticationJuror(requestDto));
    }

    /**
     * Login credentials for Public authentication.
     * Created by jonny on 24/03/17.
     */
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Schema(description = "Public Juror login credentials")
    public static class PublicAuthenticationRequestDto implements Serializable {
        @NotEmpty
        @Length(min = 9, max = 9)
        @Pattern(regexp = ValidationConstants.JUROR_NUMBER)
        @Schema(description = "Juror number", requiredMode = Schema.RequiredMode.REQUIRED)
        private String jurorNumber;


        @NotEmpty
        @Length(max = 20)
        @Pattern(regexp = NO_PIPES_REGEX)
        @Schema(description = "Juror last name", requiredMode = Schema.RequiredMode.REQUIRED)
        private String lastName;

        @NotEmpty
        @Length(max = 8)
        @Pattern(regexp = POSTCODE_REGEX)
        @Schema(description = "Juror post code", requiredMode = Schema.RequiredMode.REQUIRED)
        private String postcode;
    }

    /**
     * Response dto for a successful public authentication request.
     * Created by jonny on 24/03/17.
     */
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Schema(description = "Public Juror authentication response. Used to mint a valid JWT")
    public static class PublicAuthenticationResponseDto {
        @Schema(description = "Juror number", requiredMode = Schema.RequiredMode.REQUIRED)
        private String jurorNumber;

        @Schema(description = "Juror first name", requiredMode = Schema.RequiredMode.REQUIRED)
        private String firstName;

        @Schema(description = "Juror last name", requiredMode = Schema.RequiredMode.REQUIRED)
        private String lastName;

        @Schema(description = "Juror post code", requiredMode = Schema.RequiredMode.REQUIRED)
        private String postcode;

        @Schema(description = "Juror application roles", requiredMode = Schema.RequiredMode.REQUIRED)
        @Builder.Default
        private List<String> roles = new ArrayList<>(1);
    }
}
