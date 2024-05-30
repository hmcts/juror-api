package uk.gov.hmcts.juror.api.moj.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.JurorDigitalApplication;
import uk.gov.hmcts.juror.api.bureau.controller.response.BureauJurorDetailDto;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtAuthentication;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.config.security.IsCourtUser;
import uk.gov.hmcts.juror.api.config.security.IsSeniorCourtUser;
import uk.gov.hmcts.juror.api.moj.controller.request.ConfirmIdentityDto;
import uk.gov.hmcts.juror.api.moj.controller.request.ContactLogRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.EditJurorRecordRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.FilterableJurorDetailsRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorCreateRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorNameDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorNotesRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorNumberAndPoolNumberDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorOpticRefRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorRecordFilterRequestQuery;
import uk.gov.hmcts.juror.api.moj.controller.request.PoliceCheckStatusDto;
import uk.gov.hmcts.juror.api.moj.controller.request.ProcessNameChangeRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.ProcessPendingJurorRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.RequestBankDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.UpdateAttendanceRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.ContactEnquiryTypeListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.ContactLogListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.FilterableJurorDetailsResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorAttendanceDetailsResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorBankDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorDetailsResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorNotesDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorOverviewResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorRecordSearchDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorSummonsReplyResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.PendingJurorsResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.juror.JurorHistoryResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.juror.JurorPaymentsResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.FilterJurorRecord;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;
import uk.gov.hmcts.juror.api.moj.domain.PendingJurorStatus;
import uk.gov.hmcts.juror.api.moj.enumeration.PendingJurorStatusEnum;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.service.BulkService;
import uk.gov.hmcts.juror.api.moj.service.JurorRecordService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;
import uk.gov.hmcts.juror.api.validation.CourtLocationCode;
import uk.gov.hmcts.juror.api.validation.JurorNumber;

import java.util.List;

@RestController
@Validated
@RequestMapping(value = "/api/v1/moj/juror-record", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Juror Management")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.TooManyMethods"})
public class JurorRecordController {

    @NonNull
    private final JurorRecordService jurorRecordService;

    private final BulkService bulkService;

    @GetMapping("/detail/{jurorNumber}/{locCode}")
    @Operation(summary = "Get juror details by juror number and location code",
        description = "Retrieve details of a single juror by his/her juror number")
    public ResponseEntity<JurorDetailsResponseDto> getJurorDetails(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJwtPayload payload,
        @Parameter(description = "Valid juror number",
            required = true)
        @Size(min = 9, max = 9) @PathVariable("jurorNumber")
        @Valid String jurorNumber,
        @Parameter(description = "Valid location Code",
            required = true)
        @Size(min = 3, max = 3) @PathVariable("locCode")
        @Valid String locCode) {
        JurorDetailsResponseDto jurorDetails = jurorRecordService.getJurorDetails(payload, jurorNumber, locCode);
        return ResponseEntity.ok().body(jurorDetails);
    }

    @PostMapping("/details")
    @Operation(summary = "Get juror details for a juror number",
        description = "Retrieve details of a single juror by his/her juror number")
    @IsCourtUser
    public ResponseEntity<List<FilterableJurorDetailsResponseDto>> getJurorDetailsBulkFilterable(
        @Valid
        @NotNull
        @Size(min = 1, max = 20)
        @RequestBody
        List<@NotNull FilterableJurorDetailsRequestDto> request
    ) {
        return ResponseEntity.ok().body(
            bulkService.process(request, jurorRecordService::getJurorDetails));
    }


    @GetMapping("/overview/{jurorNumber}/{locCode}")
    @Operation(summary = "Get juror overview by juror number and location code",
        description = "Retrieve overview of a single juror by his/her juror number")
    public ResponseEntity<JurorOverviewResponseDto> getJurorOverview(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJwtPayload payload,
        @Parameter(description = "Valid juror number",
            required = true)
        @Size(min = 9, max = 9) @PathVariable(
            "jurorNumber")
        @Valid String jurorNumber,
        @Parameter(description = "Valid location Code",
            required = true)
        @Size(min = 3, max = 3) @PathVariable("locCode")
        @Valid String locCode) {
        JurorOverviewResponseDto jurorOverview = jurorRecordService.getJurorOverview(payload, jurorNumber, locCode);
        return ResponseEntity.ok().body(jurorOverview);
    }

    @GetMapping("/summons-reply/{jurorNumber}/{locCode}")
    @Operation(summary = "Get summons reply information of a juror",
        description = "Retrieve summons reply information of a juror by juror number and location code")
    public ResponseEntity<JurorSummonsReplyResponseDto> getJurorSummonsReply(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJwtPayload payload,
        @Parameter(description = "Valid juror number", required = true)
        @Size(min = 9, max = 9) @PathVariable("jurorNumber")
        @Valid String jurorNumber,
        @Parameter(description = "Valid location Code", required = true)
        @Size(min = 3, max = 3) @PathVariable("locCode")
        @Valid String locCode) {
        JurorSummonsReplyResponseDto jurorSummonsReply = jurorRecordService.getJurorSummonsReply(
            payload,
            jurorNumber,
            locCode
        );
        return ResponseEntity.ok().body(jurorSummonsReply);
    }

    @GetMapping("/single-search")
    @Operation(summary = "Search for juror record by juror number")
    public ResponseEntity<JurorRecordSearchDto> searchJurorRecord(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJwtPayload payload,
        @Parameter(description = "Valid juror number",
            required = true)
        @RequestParam @Size(min = 9, max = 9)
        @Valid String jurorNumber) {
        JurorRecordSearchDto jurorRecords = jurorRecordService.searchJurorRecord(payload, jurorNumber);
        return ResponseEntity.ok().body(jurorRecords);
    }

    @GetMapping("/contact-log/{jurorNumber}")
    @Operation(summary = "Get all logged contact information relating to a given juror")
    public ResponseEntity<ContactLogListDto> getJurorContactLogs(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJwtPayload payload,
        @Valid @JurorNumber @Parameter(description = "Valid juror number", required = true)
        @PathVariable String jurorNumber) {
        ContactLogListDto contactLogs = jurorRecordService.getJurorContactLogs(payload, jurorNumber);
        return ResponseEntity.ok().body(contactLogs);
    }

    @PostMapping("/create-juror")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Send a payload to create a new Juror Record")
    public void createJurorRecord(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJwtPayload payload,
        @Valid @RequestBody JurorCreateRequestDto jurorCreateRequestDto) {
        jurorRecordService.createJurorRecord(payload, jurorCreateRequestDto);
    }

    @GetMapping("/pending-jurors/{loc_code}")
    @Operation(summary = "Get a list of pending jurors for a given location code")
    @IsCourtUser
    public ResponseEntity<PendingJurorsResponseDto> getPendingJurors(
        @Parameter(description = "Valid location Code", required = true)
        @Size(min = 3, max = 3) @PathVariable("loc_code") @Valid String locCode,
        @Parameter(description = "Pending juror status filter")
        @RequestParam(name = "status", required = false) PendingJurorStatusEnum status) {

        PendingJurorStatus pendingJurorStatus = null;
        if (status != null) {
            pendingJurorStatus = new PendingJurorStatus();
            pendingJurorStatus.setCode(status.getCode());
        }

        PendingJurorsResponseDto pendingJurorsResponse =
            jurorRecordService.getPendingJurors(locCode, pendingJurorStatus);
        return ResponseEntity.ok().body(pendingJurorsResponse);
    }

    @PostMapping("/process-pending-juror")
    @ResponseStatus(HttpStatus.OK)
    @IsSeniorCourtUser
    @Operation(summary = "Send a payload to approve/reject a pending Juror Record")
    public void approveJurorRecord(
        @Valid @RequestBody ProcessPendingJurorRequestDto processPendingJurorRequestDto) {
        jurorRecordService.processPendingJuror(processPendingJurorRequestDto);
    }

    @PostMapping("/create/contact-log")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Send a payload containing the necessary data items to create and "
        + "populate a new log of contact information relating to a given juror")
    public void createJurorContactLog(@Parameter(hidden = true) @AuthenticationPrincipal BureauJwtPayload payload,
                                      @Valid @RequestBody ContactLogRequestDto contactLogRequestDto) {
        jurorRecordService.createJurorContactLog(payload, contactLogRequestDto);
    }


    @GetMapping("/contact-log/enquiry-types")
    @Operation(summary = "Get all available contact enquiry types")
    public ResponseEntity<ContactEnquiryTypeListDto> getContactEnquiryTypes(
        @Parameter(hidden = true) BureauJwtAuthentication auth) {
        ContactEnquiryTypeListDto contactEnquiryTypes = jurorRecordService.getContactEnquiryTypes();
        return ResponseEntity.ok().body(contactEnquiryTypes);
    }


    @GetMapping("/notes/{jurorNumber}")
    @Operation(summary = "Get the latest Notes data for a specific Juror record")
    public ResponseEntity<JurorNotesDto> getJurorNotes(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJwtPayload payload,
        @Valid @Parameter(description = "Valid juror number",
            required = true)
        @JurorNumber @PathVariable String jurorNumber) {
        JurorNotesDto notes = jurorRecordService.getJurorNotes(jurorNumber, payload.getOwner());

        return ResponseEntity.ok().body(notes);
    }

    @PatchMapping("/notes/{jurorNumber}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Update the Notes property for a specific Juror record")
    public void updateJurorNotes(@Parameter(hidden = true) @AuthenticationPrincipal BureauJwtPayload payload,
                                 @Valid @Parameter(description = "Valid juror number", required = true) @JurorNumber
                                 @PathVariable String jurorNumber,
                                 @Valid @RequestBody JurorNotesRequestDto notes) {
        jurorRecordService.setJurorNotes(jurorNumber, notes.getNotes(), payload.getOwner());
    }

    @PostMapping("/create/optic-reference")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Send a payload containing Optic Reference to add it to the Juror Record")
    public void createJurorOpticReference(@Parameter(hidden = true) @AuthenticationPrincipal BureauJwtPayload payload,
                                          @Valid @RequestBody JurorOpticRefRequestDto opticRefRequestDto) {
        jurorRecordService.createJurorOpticReference(payload, opticRefRequestDto);
    }

    @GetMapping("/optic-reference/{jurorNumber}/{poolNumber}")
    @Operation(summary = "Retrieve an Optic Reference for a specific Juror")
    public ResponseEntity<String> getJurorOpticReference(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJwtPayload payload,
        @PathVariable @Size(min = 9, max = 9)
        @Parameter(description = "Juror number", required = true)
        @Valid String jurorNumber,
        @PathVariable @Size(min = 9, max = 9)
        @Parameter(description = "Pool number", required = true)
        @Valid String poolNumber) {
        String opticRef = jurorRecordService.getJurorOpticReference(jurorNumber, poolNumber, payload);
        return ResponseEntity.ok().body(opticRef);
    }

    /**
     * Get the DTO for a single juror response in the Bureau context (active juror record owned by the Bureau)
     * including digital summons reply data.
     *
     * @param jurorId Juror number of the response to view
     * @return Fully populated DTO for juror records with a digital summons reply
     */
    @GetMapping(path = "/digital-detail/{jurorId}")
    @Operation(summary = "Get juror details, including digital response data, by juror number",
        description = "Retrieve details of a single juror by his/her juror number")
    public ResponseEntity<BureauJurorDetailDto> retrieveJurorDetailsById(
        @Parameter(hidden = true) @AuthenticationPrincipal
        BureauJwtPayload payload,
        @PathVariable @Valid @JurorNumber
        @Parameter(description = "Valid juror number",
            required = true) String jurorId) {
        final BureauJurorDetailDto details =
            jurorRecordService.getBureauDetailsByJurorNumber(jurorId, payload.getOwner());

        return ResponseEntity.ok().body(details);
    }

    /**
     * Perform a change of name, bypassing the approval process. This is intended to only be used for small "fixes" to
     * a juror's name to help them pass a police check, for example, removing special characters.
     * <p/>
     * This can only be performed by court users and bureau team leaders
     *
     * @param payload             JSON Web Token containing user authentication context
     * @param jurorNameDetailsDto Update juror name details to persist on the juror record
     * @param jurorNumber         The juror number to update the name details for
     */
    @PatchMapping(path = "/fix-name/{jurorNumber}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Schema(description = "Update the name details on a juror record without an approval process")
    public void fixJurorName(@Parameter(hidden = true) @AuthenticationPrincipal BureauJwtPayload payload,
                             @RequestBody @Valid JurorNameDetailsDto jurorNameDetailsDto,
                             @Parameter(description = "Valid juror number", required = true)
                             @Size(min = 9, max = 9)
                             @PathVariable("jurorNumber")
                             @Valid @JurorNumber String jurorNumber) {
        boolean isBureauUser = SecurityUtil.isBureau();
        boolean isTeamLeader = SecurityUtil.isBureauManager();

        if (isBureauUser && !isTeamLeader) {
            throw new MojException.Forbidden("User has insufficient permission to perform "
                + "the fix juror name action", null);
        }

        jurorRecordService.fixErrorInJurorName(payload, jurorNumber, jurorNameDetailsDto);
    }

    /**
     * Process a pending change of name, either approving the name change and allowing the juror record's
     * primary name values to be updated or reject the name change.
     *
     * <p>This can only be performed by court users
     *
     * @param payload     JSON Web Token containing user authentication context
     * @param requestDto  Decision and reason for processing the pending name change
     * @param jurorNumber The juror number to uniquely identify the juror record to process
     */
    @PatchMapping(path = "/change-name/{jurorNumber}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Schema(description = "Approve or reject changes to the name details on a juror record")
    public void processNameChangeApproval(@Parameter(hidden = true) @AuthenticationPrincipal BureauJwtPayload payload,
                                          @RequestBody @Valid ProcessNameChangeRequestDto requestDto,
                                          @Parameter(description = "Valid juror number", required = true)
                                          @JurorNumber @PathVariable("jurorNumber")
                                          @Valid String jurorNumber) {
        boolean isBureauUser = JurorDigitalApplication.JUROR_OWNER.equalsIgnoreCase(payload.getOwner());

        if (isBureauUser) {
            throw new MojException.Forbidden("User has insufficient permission to perform "
                + "the process pending name change action", null);
        }

        jurorRecordService.processPendingNameChange(payload, jurorNumber, requestDto);
    }

    /**
     * Update Juror record details for a given Juror.
     *
     * @param payload     JSON Web Token containing user authentication context
     * @param requestDto  Response information to persist
     * @param jurorNumber The juror number to update the personal details for
     */
    @PatchMapping(path = "/edit-juror/{jurorNumber}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Schema(description = "Edit personal details of an existing juror")
    public void editJurorDetails(@Parameter(hidden = true) @AuthenticationPrincipal BureauJwtPayload payload,
                                 @RequestBody @Valid EditJurorRecordRequestDto requestDto,
                                 @Parameter(description = "Valid juror number", required = true)
                                 @JurorNumber @PathVariable("jurorNumber")
                                 @Valid String jurorNumber) {
        jurorRecordService.editJurorDetails(payload, requestDto, jurorNumber);
    }

    @PatchMapping("/pnc/{jurorNumber}")
    @Operation(summary = "Updates the juror police national computer check status",
        description = "Updates the juror police national computer check status")
    public ResponseEntity<PoliceCheckStatusDto> updatePncCheckStatus(
        @Parameter(description = "Valid juror number",
            required = true)
        @JurorNumber
        @PathVariable(
            "jurorNumber")
        @Valid String jurorNumber,
        @Valid @RequestBody PoliceCheckStatusDto request
    ) {
        return ResponseEntity.accepted().body(jurorRecordService.updatePncStatus(jurorNumber, request.getStatus()));
    }

    @PatchMapping("/failed-to-attend")
    @Operation(summary = "Updates the status to be failed to attend",
        description = "Updates the juror status to be failed to attend")
    @IsCourtUser
    public ResponseEntity<Void> updateJurorToFailedToAttend(
        @Valid @RequestBody JurorNumberAndPoolNumberDto request
    ) {
        jurorRecordService.updateJurorToFailedToAttend(
            request.getJurorNumber(),
            request.getPoolNumber());
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @PatchMapping("/failed-to-attend/undo")
    @Operation(summary = "Revert failed to attend status back to a responded status",
        description = "Undo a previous failed to attend update")
    @IsSeniorCourtUser
    public ResponseEntity<Void> undoUpdateJurorToFailedToAttend(
        @Valid @RequestBody JurorNumberAndPoolNumberDto request
    ) {
        jurorRecordService.undoUpdateJurorToFailedToAttend(
            request.getJurorNumber(),
            request.getPoolNumber());
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    /**
     * Get the Attendance details for a given Juror in a given court.
     *
     * @param jurorNumber Unique Juror number of the juror
     * @param locCode  a court location code to retrieve attendance details for
     * @return Fully populated DTO of a juror's attendance details in a pool
     */
    @GetMapping(path = "/attendance-detail/{locCode}/{jurorNumber}")
    @Operation(summary = "Get attendance details for juror number in a pool")
    public ResponseEntity<JurorAttendanceDetailsResponseDto> getJurorAttendanceDetails(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJwtPayload payload,
        @PathVariable("jurorNumber") @Valid @JurorNumber
        @Parameter(description = "Valid juror number", required = true) String jurorNumber,
        @Valid @PathVariable("locCode") @CourtLocationCode String locCode) {
        final JurorAttendanceDetailsResponseDto details =
            jurorRecordService.getJurorAttendanceDetails(locCode, jurorNumber, payload);
        return ResponseEntity.ok().body(details);
    }


    /**
     * Get the attendance and payments log details for a given Juror.
     *
     * @param jurorNumber Unique Juror number of the juror
     * @return Fully populated DTO of the juror's attendance and payments log
     */
    @GetMapping(path = "/{jurorNumber}/payments")
    @Operation(summary = "Get attendance and payments log for a given juror")
    public ResponseEntity<JurorPaymentsResponseDto> getJurorPaymentsHistory(
        @PathVariable("jurorNumber") @Valid @JurorNumber
        @Parameter(description = "Valid juror number", required = true) String jurorNumber) {

        return ResponseEntity.ok().body(jurorRecordService.getJurorPayments(jurorNumber));
    }

    /**
     * Get the history log for a given Juror.
     *
     * @param jurorNumber Unique Juror number of the juror
     * @return Fully populated DTO of the juror's history entries
     */
    @GetMapping(path = "/{jurorNumber}/payments")
    @Operation(summary = "Get attendance and payments log for a given juror")
    public ResponseEntity<JurorHistoryResponseDto> getJurorHistory(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJwtPayload payload,
        @PathVariable("jurorNumber") @Valid @JurorNumber
        @Parameter(description = "Valid juror number", required = true) String jurorNumber) {
        final JurorHistoryResponseDto details =
            jurorRecordService.getJurorHistory(jurorNumber, payload);

        return ResponseEntity.ok().body(details);
    }

    @PatchMapping("/update-attendance")
    @IsCourtUser
    @Operation(summary = "Update a jurors next date to be on call",
        description = "set juror to on call")
    public ResponseEntity<Void> updateJurorAttendance(
        @Valid @RequestBody UpdateAttendanceRequestDto dto) {
        jurorRecordService.updateAttendance(dto);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @GetMapping(path = "/{juror_number}/bank-details")
    @IsCourtUser
    @Operation(summary = "get juror bank details",
        description = "get juror bank details for editing jurors bank details")
    public ResponseEntity<JurorBankDetailsDto> getJurorBankDetails(
        @Valid @JurorNumber @P("juror_number") @PathVariable("juror_number")
        @Parameter(description = "jurorNumber", required = true) String jurorNumber) {
        return ResponseEntity.ok().body(jurorRecordService.getJurorBankDetails(
            jurorNumber));
    }

    @PatchMapping("/update-bank-details")
    @Operation(summary = "edit a jurors bank details")
    @ResponseStatus(HttpStatus.OK)
    @IsCourtUser
    public ResponseEntity<Void> editJurorsBankDetails(@Valid @RequestBody RequestBankDetailsDto dto) {
        jurorRecordService.editJurorsBankDetails(dto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PatchMapping("/confirm-identity")
    @Operation(summary = "Confirm a jurors identity")
    @ResponseStatus(HttpStatus.OK)
    @IsCourtUser
    public void confirmJurorsIdentity(@Valid @RequestBody ConfirmIdentityDto dto) {
        jurorRecordService.confirmIdentity(dto);
    }

    @PatchMapping("/mark-responded/{juror_number}")
    @Operation(summary = "Mark a juror as responded")
    @ResponseStatus(HttpStatus.OK)
    public void markResponded(@Valid @JurorNumber @P("juror_number") @PathVariable("juror_number")
                                  @Parameter(description = "jurorNumber", required = true) String jurorNumber) {
        jurorRecordService.markResponded(jurorNumber);
    }

    @PostMapping("/search")
    @Operation(summary = "GET With Body", description = "Retrieve a list of all jurors, filtered by the "
        + "provided search criteria")
    public ResponseEntity<PaginatedList<FilterJurorRecord>> searchForJurorRecord(
        @RequestBody @Valid JurorRecordFilterRequestQuery query) {

        PaginatedList<FilterJurorRecord> jurorRecords = jurorRecordService.searchForJurorRecords(query);
        if (null == jurorRecords || jurorRecords.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok().body(jurorRecords);
    }
}
