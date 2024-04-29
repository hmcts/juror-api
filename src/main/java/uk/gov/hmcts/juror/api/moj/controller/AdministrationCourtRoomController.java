package uk.gov.hmcts.juror.api.moj.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.moj.domain.administration.CourtRoomDto;
import uk.gov.hmcts.juror.api.moj.domain.administration.CourtRoomWithIdDto;
import uk.gov.hmcts.juror.api.moj.service.administration.AdministrationCourtRoomService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;
import uk.gov.hmcts.juror.api.validation.CourtLocationCode;

import java.util.List;

@RestController
@Validated
@RequestMapping(value = "/api/v1/moj/administration/court-rooms", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Administration - Court Rooms")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AdministrationCourtRoomController {

    private final AdministrationCourtRoomService administrationCourtRoomService;

    @GetMapping("/{loc_code}")
    @Operation(summary = "View a court room")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("(" + SecurityUtil.LOC_CODE_AUTH_COURT_ONLY +  ") or "
        + SecurityUtil.IS_ADMINISTRATOR)
    public ResponseEntity<List<CourtRoomWithIdDto>> viewCourtRoomsDetails(
        @P("loc_code")
        @PathVariable("loc_code")
        @CourtLocationCode
        @Parameter(description = "locCode", required = true)
        @Valid String locCode) {
        return ResponseEntity.ok(administrationCourtRoomService.viewCourtRooms(locCode));
    }

    @PostMapping("/{loc_code}")
    @Operation(summary = "Create a court room")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize(SecurityUtil.LOC_CODE_AUTH_COURT_ONLY)
    public ResponseEntity<Void> createCourtRoom(
        @P("loc_code")
        @PathVariable("loc_code")
        @CourtLocationCode
        @Parameter(description = "locCode", required = true)
        @Valid String locCode,
        @RequestBody @Valid CourtRoomDto courtRoomDto) {
        administrationCourtRoomService.createCourtRoom(locCode, courtRoomDto);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/{loc_code}/{id}")
    @Operation(summary = "View a court room")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize(SecurityUtil.LOC_CODE_AUTH_COURT_ONLY)
    public ResponseEntity<CourtRoomWithIdDto> viewCourtRoomDetails(
        @P("loc_code")
        @PathVariable("loc_code")
        @CourtLocationCode
        @Parameter(description = "locCode", required = true)
        @Valid String locCode,
        @PathVariable("id")
        @Parameter(description = "id", required = true)
        @Valid Long id
    ) {
        return ResponseEntity.ok(administrationCourtRoomService.viewCourtRoom(locCode, id));
    }


    @PutMapping("/{loc_code}/{id}")
    @Operation(summary = "Update a court room")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize(SecurityUtil.LOC_CODE_AUTH_COURT_ONLY)
    public ResponseEntity<Void> updateCourtRoom(
        @P("loc_code")
        @PathVariable("loc_code")
        @CourtLocationCode
        @Parameter(description = "locCode", required = true)
        @Valid String locCode,
        @PathVariable("id")
        @Parameter(description = "id", required = true)
        @Valid Long id,
        @RequestBody @Valid CourtRoomDto courtRoomDto) {
        administrationCourtRoomService.updateCourtRoom(locCode, id, courtRoomDto);
        return ResponseEntity.accepted().build();
    }

}
