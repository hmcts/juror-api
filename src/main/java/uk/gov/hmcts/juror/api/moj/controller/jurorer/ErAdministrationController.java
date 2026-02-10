package uk.gov.hmcts.juror.api.moj.controller.jurorer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.config.security.IsBureauUser;
import uk.gov.hmcts.juror.api.moj.service.jurorer.ErAdministrationService;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/moj/er-administration", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Validated
@Tag(name = "ER Portal Administration")
public class ErAdministrationController {

    private final ErAdministrationService erAdministrationService;

    @PutMapping(path = "/deactivate-la")
    @Operation(summary = "Deactivate a Local Authority in the ER Portal")
    @IsBureauUser
    public ResponseEntity<Void> deactivateLa(
        @RequestBody @Valid DeactiveLaRequestDto deactiveLaRequest) {
        erAdministrationService.deactivateLa(deactiveLaRequest);
        return ResponseEntity.ok().build();
    }

}
