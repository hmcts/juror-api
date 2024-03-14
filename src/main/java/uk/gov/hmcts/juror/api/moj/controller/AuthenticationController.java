package uk.gov.hmcts.juror.api.moj.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
import uk.gov.hmcts.juror.api.moj.domain.authentication.CourtDto;
import uk.gov.hmcts.juror.api.moj.domain.authentication.EmailDto;
import uk.gov.hmcts.juror.api.moj.domain.authentication.JwtDto;
import uk.gov.hmcts.juror.api.moj.service.UserService;
import uk.gov.hmcts.juror.api.validation.CourtLocationCode;

import java.util.List;

@RestController
@Validated
@RequestMapping(value = "/api/v1/auth/moj", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Authentication")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AuthenticationController {


    private final UserService userService;

    @PostMapping("/courts")
    @Operation(summary = "Get a users courts")
    public ResponseEntity<List<CourtDto>> viewCourts(
        @RequestBody @Valid EmailDto emailDto
    ) {
        return ResponseEntity.ok(userService.getCourts(emailDto.getEmail()));
    }

    @PostMapping("/jwt/{loc_code}")
    @Operation(summary = "Creates a jwt for a given user at location")
    public ResponseEntity<JwtDto> createJwt(
        @P("loc_code") @PathVariable("loc_code") @Valid @NotBlank
        @CourtLocationCode String locCode,
        @RequestBody @Valid EmailDto emailDto
    ) {
        return ResponseEntity.ok(userService.createJwt(emailDto.getEmail(), locCode));
    }
}
