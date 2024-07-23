package uk.gov.hmcts.juror.api.moj.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.config.security.IsCourtUser;
import uk.gov.hmcts.juror.api.moj.controller.request.AddAttendanceDayDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorAppearanceDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorsToDismissRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.jurormanagement.JurorNonAttendanceDto;
import uk.gov.hmcts.juror.api.moj.controller.request.jurormanagement.ModifyConfirmedAttendanceDto;
import uk.gov.hmcts.juror.api.moj.controller.request.jurormanagement.RetrieveAttendanceDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.jurormanagement.UpdateAttendanceDateDto;
import uk.gov.hmcts.juror.api.moj.controller.request.jurormanagement.UpdateAttendanceDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorAppearanceResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorsOnTrialResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorsToDismissResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.jurormanagement.AttendanceDetailsResponse;
import uk.gov.hmcts.juror.api.moj.enumeration.jurormanagement.JurorStatusGroup;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.service.jurormanagement.JurorAppearanceService;
import uk.gov.hmcts.juror.api.moj.utils.DataUtils;
import uk.gov.hmcts.juror.api.validation.CourtLocationCode;

import java.time.LocalDate;

import static uk.gov.hmcts.juror.api.JurorDigitalApplication.JUROR_OWNER;

@RestController
@Validated
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.TooManyMethods"})
@RequestMapping(value = "/api/v1/moj/juror-management", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Juror Management")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class JurorManagementController {

    @NonNull
    private final JurorAppearanceService jurorAppearanceService;

    @GetMapping(path = "/appearance")
    @Operation(description = "Retrieve juror's with an appearance record for a given date and location. "
        + "Juror status is filtered by providing a status group (list of valid statuses for each request")
    public ResponseEntity<JurorAppearanceResponseDto> getAppearanceRecords(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJwtPayload payload,
        @RequestParam @CourtLocationCode @Valid String locationCode,
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") @Valid
        LocalDate attendanceDate, @RequestParam @Valid JurorStatusGroup group) {
        final JurorAppearanceResponseDto jurorAppearanceResponseDto = jurorAppearanceService
            .getAppearanceRecords(locationCode, attendanceDate, payload, group);
        return ResponseEntity.ok(jurorAppearanceResponseDto);
    }

    @PutMapping("/appearance")
    @Operation(description = "Check-in or Check-out a juror")
    public ResponseEntity<JurorAppearanceResponseDto.JurorAppearanceResponseData> processAppearance(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJwtPayload payload,
        @RequestBody @Valid JurorAppearanceDto jurorAppearanceDto) {
        JurorAppearanceResponseDto.JurorAppearanceResponseData appearanceData =
            jurorAppearanceService.processAppearance(payload, jurorAppearanceDto);
        return ResponseEntity.ok(appearanceData);
    }

    @GetMapping("/attendance")
    @Operation(description = "Retrieve a list of juror attendance details based on attendance (appearance) status")
    public ResponseEntity<AttendanceDetailsResponse> retrieveAttendanceDetails(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJwtPayload payload,
        @RequestBody @Valid RetrieveAttendanceDetailsDto request) {
        validateOwner(payload);
        System.out.println("TMP: " + DataUtils.asJsonString(request));
        return ResponseEntity.ok(jurorAppearanceService.retrieveAttendanceDetails(payload, request));
    }

    @PatchMapping("/attendance")
    @Operation(description = "Update the list of jurors based on their attendance (appearance) status")
    public ResponseEntity<AttendanceDetailsResponse> updateAttendance(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJwtPayload payload,
        @RequestBody @Valid UpdateAttendanceDto request) {
        validateOwner(payload);

        return ResponseEntity.ok(jurorAppearanceService.updateAttendance(payload, request));
    }

    @PostMapping("/add-attendance-day")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(description = "Manually add attendance day for a juror")
    @IsCourtUser
    public void addAttendanceDay(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJwtPayload payload,
        @RequestBody @Valid AddAttendanceDayDto addAttendanceDayDto) {
        jurorAppearanceService.addAttendanceDay(payload, addAttendanceDayDto);
    }

    @PatchMapping("/attendance/attendance-date")
    @Operation(description = "Update juror attendance date")
    @IsCourtUser
    public ResponseEntity<String> updateAttendanceDate(
        @RequestBody @Valid UpdateAttendanceDateDto request) {
        return ResponseEntity.ok(jurorAppearanceService.updateAttendanceDate(request));
    }

    @PatchMapping("/attendance/modify-attendance")
    @Operation(description = "Modify a jurors confirmed attendance for a given date")
    @ResponseStatus(HttpStatus.OK)
    @IsCourtUser
    public void modifyAttendance(
        @RequestBody @Valid ModifyConfirmedAttendanceDto request) {
        jurorAppearanceService.modifyConfirmedAttendance(request);
    }

    @PutMapping("/mark-as-absent")
    @Operation(description = "Mark juror as absent")
    public ResponseEntity<Void> markJurorAsAbsent(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJwtPayload payload,
        @RequestBody @Valid UpdateAttendanceDto.CommonData request) {
        jurorAppearanceService.markJurorAsAbsent(payload, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/non-attendance")
    @IsCourtUser
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(description = "Add a non-attendance day for a juror")
    public void addNonAttendance(
        @RequestBody @Valid JurorNonAttendanceDto jurorNonAttendanceDto) {

        jurorAppearanceService.addNonAttendance(jurorNonAttendanceDto);
    }

    @GetMapping("/jurors-to-dismiss")
    @Operation(description = "Retrieve a list of jurors to dismiss based on their attendance (appearance) status")
    @IsCourtUser
    public ResponseEntity<JurorsToDismissResponseDto> retrieveJurorsToDismiss(
        @RequestBody @Valid JurorsToDismissRequestDto request) {

        return ResponseEntity.ok(jurorAppearanceService.retrieveJurorsToDismiss(request));
    }

    @GetMapping("/jurors-on-trial/{location-code}")
    @Operation(description = "Retrieve a list of trials with jury attendance summary and audit reference number")
    @IsCourtUser
    public ResponseEntity<JurorsOnTrialResponseDto> retrieveJurorsToDismiss(
        @P("location-code") @PathVariable("location-code") @CourtLocationCode @Valid String locationCode,
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") @Valid LocalDate attendanceDate) {

        return ResponseEntity.ok(jurorAppearanceService.retrieveJurorsOnTrials(locationCode, attendanceDate));
    }

    @PatchMapping("/confirm-jury-attendance")
    @Operation(description = "Confirm attendance for jurors who are on a trial")
    @IsCourtUser
    @ResponseStatus(HttpStatus.OK)
    public void confirmJuryAttendance(
        @RequestBody @Valid UpdateAttendanceDto request) {
        jurorAppearanceService.confirmJuryAttendance(request);
    }

    private void validateOwner(BureauJwtPayload payload) {
        if (JUROR_OWNER.equalsIgnoreCase(payload.getOwner())) {
            throw new MojException.Forbidden("Bureau users are not allowed to use this service",
                null);
        }
    }
}
