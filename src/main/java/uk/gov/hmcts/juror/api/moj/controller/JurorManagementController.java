package uk.gov.hmcts.juror.api.moj.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.config.security.IsCourtUser;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorAppearanceDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorsToDismissRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.jurormanagement.RetrieveAttendanceDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.jurormanagement.UpdateAttendanceDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorAppearanceResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorsToDismissResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.jurormanagement.AttendanceDetailsResponse;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.service.jurormanagement.JurorAppearanceService;
import uk.gov.hmcts.juror.api.validation.CourtLocationCode;

import java.time.LocalDate;

import static uk.gov.hmcts.juror.api.JurorDigitalApplication.JUROR_OWNER;

@RestController
@Validated
@RequestMapping(value = "/api/v1/moj/juror-management", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Juror Management")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class JurorManagementController {

    @NonNull
    private final JurorAppearanceService jurorAppearanceService;

    @GetMapping(path = "/appearance")
    @Operation(description = "Retrieve juror appearance records for a given date and location ")
    public ResponseEntity<JurorAppearanceResponseDto> getAppearanceRecords(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJWTPayload payload,
        @RequestParam @CourtLocationCode @Valid String locationCode,
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") @Valid
        LocalDate attendanceDate) {
        final JurorAppearanceResponseDto jurorAppearanceResponseDto = jurorAppearanceService
            .getAppearanceRecords(locationCode, attendanceDate, payload);
        return ResponseEntity.ok(jurorAppearanceResponseDto);
    }

    @PutMapping("/appearance")
    @Operation(description = "Check-in or Check-out a juror")
    public ResponseEntity<JurorAppearanceResponseDto.JurorAppearanceResponseData> processAppearance(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJWTPayload payload,
        @RequestBody @Valid JurorAppearanceDto jurorAppearanceDto) {
        JurorAppearanceResponseDto.JurorAppearanceResponseData appearanceData =
            jurorAppearanceService.processAppearance(payload, jurorAppearanceDto);
        return ResponseEntity.ok(appearanceData);
    }

    @GetMapping("/attendance")
    @Operation(description = "Retrieve a list of juror attendance details based on attendance (appearance) status")
    public ResponseEntity<AttendanceDetailsResponse> retrieveAttendanceDetails(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJWTPayload payload,
        @RequestBody @Valid RetrieveAttendanceDetailsDto request) {
        validateOwner(payload);

        return ResponseEntity.ok(jurorAppearanceService.retrieveAttendanceDetails(payload, request));
    }

    @PatchMapping("/attendance")
    @Operation(description = "Update the list of jurors based on their attendance (appearance) status")
    public ResponseEntity<AttendanceDetailsResponse> updateAttendance(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJWTPayload payload,
        @RequestBody @Valid UpdateAttendanceDto request) {
        validateOwner(payload);

        return ResponseEntity.ok(jurorAppearanceService.updateAttendance(payload, request));
    }

    @DeleteMapping("/attendance")
    @Operation(description = "Delete the attendance record for a juror")
    public ResponseEntity<AttendanceDetailsResponse> deleteAttendance(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJWTPayload payload,
        @RequestBody @Valid UpdateAttendanceDto request) {
        validateOwner(payload);

        return ResponseEntity.ok(jurorAppearanceService.deleteAttendance(payload, request));
    }

    @GetMapping("/jurors-to-dismiss")
    @Operation(description = "Retrieve a list of jurors to dismiss based on their attendance (appearance) status")
    @IsCourtUser
    public ResponseEntity<JurorsToDismissResponseDto> retrieveJurorsToDismiss(
        @RequestBody @Valid JurorsToDismissRequestDto request) {

        return ResponseEntity.ok(jurorAppearanceService.retrieveJurorsToDismiss(request));
    }

    private void validateOwner(BureauJWTPayload payload) {
        if (JUROR_OWNER.equalsIgnoreCase(payload.getOwner())) {
            throw new MojException.Forbidden("Bureau users are not allowed to use this service",
                null);
        }
    }
}
