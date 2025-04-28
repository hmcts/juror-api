package uk.gov.hmcts.juror.api.moj.controller;

import groovyjarjarantlr4.v4.runtime.misc.NotNull;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.moj.service.ContentStoreService;

@RestController
@Validated
@RequestMapping(value = "/api/v1/moj/contentstore", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Tag(name = "Content Store")
public class ContentStoreController {

    @NotNull
    private final ContentStoreService contentStoreService;

    @PostMapping("/generate")
    @Operation(summary = "Manually generate content store files ")
    public ResponseEntity<Void> generateFiles() {

        // Call the service to generate files
        contentStoreService.generateFiles();
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }
}
