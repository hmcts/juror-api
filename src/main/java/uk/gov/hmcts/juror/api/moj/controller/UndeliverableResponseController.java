package uk.gov.hmcts.juror.api.moj.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.bureau.exception.ExcusalException;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorNumberListDto;
import uk.gov.hmcts.juror.api.moj.service.UndeliverableResponseService;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/moj/undeliverable-response", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Validated
@Tag(name = "Summons Management")
public class UndeliverableResponseController {
    private final UndeliverableResponseService undeliverableResponseService;

    @Operation(summary = "Mark a Juror as undeliverable with information provided")
    @PatchMapping
    public ResponseEntity<Void> markJurorAsUndeliverable(
        @RequestBody @Valid JurorNumberListDto jurorNumbers) throws ExcusalException {
        undeliverableResponseService.markAsUndeliverable(jurorNumbers.getJurorNumbers());
        return ResponseEntity.ok().build();
    }
}
