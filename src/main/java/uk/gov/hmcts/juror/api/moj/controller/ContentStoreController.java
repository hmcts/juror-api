package uk.gov.hmcts.juror.api.moj.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.moj.client.contracts.PncCheckServiceClient;
import uk.gov.hmcts.juror.api.validation.JurorNumber;

@RestController
@Validated
@RequestMapping(value = "/api/v1/moj/contentstore", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Content Store")
public class ContentStoreController {

    @PatchMapping("/generate")
    @Operation(summary = "Manually generate content store files ")
    public ResponseEntity<Void> generateFiles() {



        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }
}
