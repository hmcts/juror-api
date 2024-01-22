package uk.gov.hmcts.juror.api.moj.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorManagementRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.PoolEditRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorManagementResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.PoolSummaryResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.SummoningProgressResponseDTO;
import uk.gov.hmcts.juror.api.moj.controller.response.poolmanagement.AvailablePoolsInCourtLocationDto;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.service.DeletePoolService;
import uk.gov.hmcts.juror.api.moj.service.EditPoolService;
import uk.gov.hmcts.juror.api.moj.service.PoolStatisticsService;
import uk.gov.hmcts.juror.api.moj.service.poolmanagement.JurorManagementService;
import uk.gov.hmcts.juror.api.moj.service.poolmanagement.ManagePoolsService;

import static uk.gov.hmcts.juror.api.JurorDigitalApplication.JUROR_OWNER;

@RestController
@RequestMapping(value = "/api/v1/moj/manage-pool", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Pool Management")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ManagePoolController {

    @NonNull
    private final PoolStatisticsService poolStatisticsService;
    @NonNull
    private final DeletePoolService deletePoolService;
    @NonNull
    private final EditPoolService editPoolService;
    @NonNull
    private final ManagePoolsService managePoolsService;
    @NonNull
    private final JurorManagementService jurorManagementService;

    @GetMapping("/summary")
    @Operation(summary = "Retrieve summary information about a given pool")
    public ResponseEntity<PoolSummaryResponseDto> getPoolStatistics(@RequestParam() @Size(min = 9, max = 9)
                                                                    @Valid String poolNumber) {
        PoolSummaryResponseDto poolSummary = poolStatisticsService.calculatePoolStatistics(poolNumber);
        return ResponseEntity.ok().body(poolSummary);
    }

    /**
     * Delete request operation to complete the deletion of a pool.
     *
     * @param payload    authentication object principal
     * @param poolNumber a complete (min 9 digit) pool number that identifies a unique pool entry
     */
    @DeleteMapping(path = "/delete")
    @Operation(summary = "Delete a pool record from the database")
    public ResponseEntity<?> deletePool(@Parameter(hidden = true) @AuthenticationPrincipal BureauJWTPayload payload,
                                        @Valid @RequestParam() @Size(min = 9, max = 9) String poolNumber) {
        deletePoolService.deletePool(payload, poolNumber);

        return ResponseEntity.ok().build();
    }

    /**
     * Edit Pool to update a pool request record.
     *
     * @param payload            authentication object principal
     * @param poolEditRequestDto a DTO containing the information to update the pool request
     */
    @PutMapping(path = "/edit-pool")
    @Operation(summary = "Edit a pool request record")
    public ResponseEntity<?> editPool(@Parameter(hidden = true) @AuthenticationPrincipal BureauJWTPayload payload,
                                      @RequestBody @Valid PoolEditRequestDto poolEditRequestDto) {
        editPoolService.editPool(payload, poolEditRequestDto);
        return ResponseEntity.ok().build();
    }

    /**
     * Transfer one (or more) jurors from one pool/court location to a new pool at a different court location.
     *
     * @param payload    authentication object principal
     * @param requestDto request body (payload) containing information on jurors to be transferred,
     *                   source pool/court location and target court location
     *
     * @return an integer representing the number of jurors successfully transferred
     */
    @PutMapping(path = "/transfer")
    @Operation(summary = "Transfer one (or more) jurors from one pool/court location to a new pool at a different "
        + "court location")
    public ResponseEntity<Integer> transferJurorsToNewCourt(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJWTPayload payload,
        @RequestBody @Valid @Parameter(description = "Transfer request data", required = true)
        JurorManagementRequestDto requestDto) {

        int transferCount = jurorManagementService.transferPoolMembers(payload, requestDto);

        return ResponseEntity.accepted().body(transferCount);
    }

    @PutMapping(path = "/movement/validation")
    @Operation(summary = "Perform basic validation on pool members to ensure they meet the criteria to be moved")
    public ResponseEntity<JurorManagementResponseDto> validatePoolMembers(
        @Parameter(hidden = true) @AuthenticationPrincipal
        BureauJWTPayload payload,
        @RequestBody @Valid
        @Parameter(description =
            "Transfer request data",
            required = true)
        JurorManagementRequestDto requestDto) {
        JurorManagementResponseDto responseDto = jurorManagementService.validatePoolMembers(
            payload,
            requestDto
        );

        return ResponseEntity.accepted().body(responseDto);
    }

    @GetMapping("/available-pools/{locCode}")
    @Operation(summary = "Retrieve active pools, including required Jurors, for a given court location")
    public ResponseEntity<AvailablePoolsInCourtLocationDto> getAvailablePoolsInCourtLocation(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJWTPayload payload,
        @Parameter(description = "3-digit numeric string to identify the court") @PathVariable(name = "locCode")
        @Size(min = 3, max = 3) @Valid String locCode) {
        AvailablePoolsInCourtLocationDto responseBody = managePoolsService.findAvailablePools(locCode, payload);
        return ResponseEntity.ok().body(responseBody);
    }

    /**
     * Reassign a list of Jurors to another pool.
     *
     * @param payload                   authentication object principal
     * @param jurorManagementRequestDto a DTO containing the information to reassign Jurors
     */
    @PutMapping(path = "/reassign-jurors")
    @Operation(summary = "Reassign Jurors from current pool to another pool")
    public ResponseEntity<Integer> reassignJurors(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJWTPayload payload,
        @RequestBody @Valid JurorManagementRequestDto jurorManagementRequestDto) {
        int jurorsReassigned = jurorManagementService.reassignJurors(payload, jurorManagementRequestDto);
        return ResponseEntity.ok().body(jurorsReassigned);
    }

    @GetMapping(path = "/summoning-progress/{courtLocCode}/{poolType}")
    @Operation(summary = "Get pool monitoring stats")
    public ResponseEntity<SummoningProgressResponseDTO> getPoolMonitoringStats(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJWTPayload payload,
        @Parameter @PathVariable("courtLocCode") String courtLocationCode,
        @Parameter @PathVariable("poolType") String poolType) {

        if (!payload.getOwner().equals(JUROR_OWNER)) {
            throw new MojException.Forbidden("Authorisation access denied, bureau user only",
                null);
        }

        return ResponseEntity.ok()
            .body(managePoolsService.getPoolMonitoringStats(payload, courtLocationCode, poolType));
    }
}
