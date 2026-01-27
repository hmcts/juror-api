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
import uk.gov.hmcts.juror.api.jurorer.service.LaUserService;

@RestController
@Validated
@RequestMapping(value = "/api/v1/auth/juror-er", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Authentication")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class LaAuthenticationController {

    LaUserService userService;

    @PostMapping("/jwt")
    @Operation(summary = "Creates a jwt for a given user at local authority")
    public ResponseEntity<LaJwtDto> createJwt(
        @RequestBody @Valid LaEmailDto emailDto
    ) {
        return ResponseEntity.ok(userService.createJwt(emailDto.toString()));
    }
}
