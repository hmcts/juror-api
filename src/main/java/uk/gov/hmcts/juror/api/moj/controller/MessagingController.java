package uk.gov.hmcts.juror.api.moj.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.config.security.IsCourtUser;
import uk.gov.hmcts.juror.api.moj.controller.request.messages.ExportContactDetailsRequest;
import uk.gov.hmcts.juror.api.moj.controller.request.messages.MessageSendRequest;
import uk.gov.hmcts.juror.api.moj.controller.response.messages.JurorToSendMessageBase;
import uk.gov.hmcts.juror.api.moj.controller.response.messages.ViewMessageTemplateDto;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;
import uk.gov.hmcts.juror.api.moj.domain.messages.MessageSearch;
import uk.gov.hmcts.juror.api.moj.domain.messages.MessageType;
import uk.gov.hmcts.juror.api.moj.service.MessagingService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.util.Map;

@RestController
@Validated
@RequestMapping(value = "/api/v1/moj/messages", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Message")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@IsCourtUser
public class MessagingController {

    private final MessagingService messagingService;

    @GetMapping("/view/{message_type}/{loc_code}")
    @Operation(summary = "returns the message template details"
        + " for the given template type")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize(SecurityUtil.LOC_CODE_AUTH)
    public ResponseEntity<ViewMessageTemplateDto> getMessageDetails(
        @PathVariable("message_type") MessageType messageType,
        @P("loc_code") @Size(min = 3, max = 3) @PathVariable("loc_code") @Valid String locCode
    ) {
        return new ResponseEntity<>(messagingService.getViewMessageTemplateDto(messageType, locCode), HttpStatus.OK);
    }


    @PostMapping("/view/{message_type}/{loc_code}/populated")
    @Operation(summary = "returns the "
        + "populated message template details for the given template type")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize(SecurityUtil.LOC_CODE_AUTH)
    public ResponseEntity<ViewMessageTemplateDto> getMessageDetailsPopulated(
        @PathVariable("message_type") MessageType messageType,
        @P("loc_code") @Size(min = 3, max = 3) @PathVariable("loc_code") @Valid String locCode,
        @RequestBody @Valid @NotNull Map<@NotNull String, @NotNull String> placeholderValues
    ) {
        return new ResponseEntity<>(
            messagingService.getViewMessageTemplateDtoPopulated(messageType, locCode, placeholderValues),
            HttpStatus.OK);
    }

    @PostMapping("/search/{loc_code}")
    @Operation(summary = "Searches for a list of messages that can be sent")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize(SecurityUtil.LOC_CODE_AUTH_OR_BUREAU)
    public ResponseEntity<PaginatedList<? extends JurorToSendMessageBase>> postSearch(
        @RequestBody @Valid @NotNull MessageSearch messageSearch,
        @P("loc_code") @Size(min = 3, max = 3) @PathVariable("loc_code") @Valid String locCode,
        @RequestParam(required = false, defaultValue = "false", value = "simple_response") boolean simpleResponse
    ) {
        return new ResponseEntity<>(messagingService.search(messageSearch, locCode, simpleResponse), HttpStatus.OK);
    }

    @PostMapping("/send/{message_type}/{loc_code}")
    @Operation(summary = "Send messages")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(SecurityUtil.LOC_CODE_AUTH)
    public ResponseEntity<Void> sendMessage(
        @PathVariable("message_type") MessageType messageType,
        @P("loc_code") @Size(min = 3, max = 3) @PathVariable("loc_code") @Valid String locCode,
        @RequestBody @Valid @NotNull MessageSendRequest messageSendRequest
    ) {
        messagingService.send(messageType, locCode, messageSendRequest);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping(value = "/csv/{loc_code}",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = {"text/csv;", MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Convert message to CSV")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize(SecurityUtil.LOC_CODE_AUTH + " or " + SecurityUtil.IS_BUREAU)
    public ResponseEntity<String> toCsv(
        @P("loc_code") @Size(min = 3, max = 3) @PathVariable("loc_code") @Valid String locCode,
        @RequestBody @Valid @NotNull ExportContactDetailsRequest exportContactDetailsRequest
    ) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=juror_export_details.csv");
        return new ResponseEntity<>(
            messagingService.exportContactDetails(locCode, exportContactDetailsRequest),
            httpHeaders,
            HttpStatus.OK
        );
    }
}
