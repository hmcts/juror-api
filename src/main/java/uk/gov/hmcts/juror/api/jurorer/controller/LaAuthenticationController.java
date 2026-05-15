package uk.gov.hmcts.juror.api.jurorer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.jurorer.controller.dto.LaEmailDto;
import uk.gov.hmcts.juror.api.jurorer.controller.dto.LaJwtDto;
import uk.gov.hmcts.juror.api.jurorer.domain.LocalAuthority;
import uk.gov.hmcts.juror.api.jurorer.service.LaUserService;
import uk.gov.hmcts.juror.api.moj.domain.authentication.EmailDto;

import java.util.List;

@RestController
@Validated
@RequestMapping(value = "/api/v1/auth/juror-er", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Authentication")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class LaAuthenticationController {

    private final LaUserService userService;

    @PostMapping("/local-authorities")
    @Operation(summary = "Get a users local authorities")
    public ResponseEntity<List<LocalAuthority>> viewLocalAuthorities(
        @RequestBody @Valid EmailDto emailDto
    ) {
        return ResponseEntity.ok(userService.getLocalAuthorities(emailDto.getEmail()));
    }

    @PostMapping("/jwt/{la_code}")
    @Operation(summary = "Creates a jwt for a given user at local authority")
    public ResponseEntity<LaJwtDto> createJwt(
        @P("la_code") @PathVariable("la_code") @Valid @NotBlank
        @Pattern(regexp = "^\\d{3}$") String laCode,
        @RequestBody @Valid LaEmailDto emailDto
    ) {
        return ResponseEntity.ok(userService.createJwt(emailDto.getEmail(), laCode));
    }
}
