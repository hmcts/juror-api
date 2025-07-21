package uk.gov.hmcts.juror.api.moj.controller.bureaudashboard;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.config.security.IsBureauUser;
import uk.gov.hmcts.juror.api.moj.controller.request.ActivePoolFilterQuery;
import uk.gov.hmcts.juror.api.moj.controller.response.PoolRequestActiveDataDto;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;
import uk.gov.hmcts.juror.api.moj.domain.SortMethod;
import uk.gov.hmcts.juror.api.moj.service.BureauDashboardService;
import uk.gov.hmcts.juror.api.moj.service.PoolRequestService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;
import uk.gov.hmcts.juror.api.validation.CourtLocationCode;


@RestController
@RequestMapping(value = "/api/v1/moj/bureau-dashboard/", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Bureau Dashboard", description = "Bureau Dashboard API")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class BureauDashboardInfoController {

    @NonNull
    private final BureauDashboardService bureauDashboardService;

    @NonNull
    private final PoolRequestService poolRequestService;

    @IsBureauUser
    @GetMapping("/notification-management/")
    @Operation(summary = "Retrieves notification management information for bureau")
    public ResponseEntity<BureauNotificationManagementInfoDto> getBureauNotificationManagementInfo() {
        BureauNotificationManagementInfoDto dto = bureauDashboardService.getBureauNotificationManagementInfo(
            SecurityUtil.getActiveOwner());
        return ResponseEntity.ok().body(dto);
    }

    @IsBureauUser
    @GetMapping("/summons-management/")
    @Operation(summary = "Retrieves summons management information for bureau")
    public ResponseEntity<BureauSummonsManagementInfoDto> getBureauSummonsManagementInfo() {
        BureauSummonsManagementInfoDto dto = bureauDashboardService.getBureauSummonsManagementInfo( SecurityUtil.getActiveOwner());
        return ResponseEntity.ok().body(dto);
    }

    @IsBureauUser
    @GetMapping("/pool-management/")
    @Operation(summary = "Retrieves pool management information for bureau")
    public ResponseEntity<BureauPoolManagementInfoDto> getBureauPoolManagementInfo() {
        BureauPoolManagementInfoDto dto = bureauDashboardService.getBureauPoolManagementInfo(SecurityUtil.getActiveOwner());
        return ResponseEntity.ok().body(dto);
    }

    @IsBureauUser
    @GetMapping("/pools-under-responded/")
    @Operation(summary = "Retrieves pools under responded information for bureau")
    public ResponseEntity<PaginatedList<PoolRequestActiveDataDto>> getActivePoolRequests(
        @CourtLocationCode @Size(min = 3, max = 3) @Valid String locCode,
        @RequestParam @Valid String tab,
        @RequestParam @Valid @Min(1) Integer pageNumber,
        @RequestParam @Valid @Min(1) Integer pageLimit,
        @RequestParam @Valid ActivePoolFilterQuery.SortField sortBy,
        @RequestParam @Valid SortMethod sortOrder) {

        ActivePoolFilterQuery filterQuery = ActivePoolFilterQuery.builder()
            .locCode(locCode)
            .tab(tab)
            .sortField(sortBy)
            .sortMethod(sortOrder)
            .pageLimit(pageLimit)
            .pageNumber(pageNumber)
            .build();
        return ResponseEntity.ok().body(poolRequestService.getActivePoolRequests(filterQuery));
    }
}



