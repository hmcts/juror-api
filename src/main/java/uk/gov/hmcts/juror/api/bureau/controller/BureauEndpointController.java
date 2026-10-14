package uk.gov.hmcts.juror.api.bureau.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.bureau.service.BureauService;
import uk.gov.hmcts.juror.api.bureau.service.ResponseStatusUpdateService;

/**
 * API endpoints controller for Bureau Endpoints.
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/v1/bureau", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Bureau API", description = "Bureau Interface API")
public class BureauEndpointController {
    private final BureauService bureauService;
    private final ResponseStatusUpdateService responseStatusUpdateService;

    @Autowired
    public BureauEndpointController(final ResponseStatusUpdateService responseStatusUpdateService,
                                    final BureauService bureauService) {
        Assert.notNull(bureauService, "BureauService cannot be null!");
        Assert.notNull(responseStatusUpdateService, "ResponseStatusUpdateService cannot be null!");
        this.bureauService = bureauService;
        this.responseStatusUpdateService = responseStatusUpdateService;
    }
}
