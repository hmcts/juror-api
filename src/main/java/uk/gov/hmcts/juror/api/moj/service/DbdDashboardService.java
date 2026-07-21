package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.moj.controller.request.DbdDashboardRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.DbdDashboardResponseDto;

public interface DbdDashboardService {

    /**
     * Get take-up statistics for the requested court groups and date range(s).
     *
     * Implementations should resolve the distinct set of locations across all requested
     * groups in a single pass, fetch each underlying stats table once per date range
     * (filtered to that combined location set), and perform group-level aggregation
     * in memory — group membership is caller-defined per request and isn't something
     * to push into per-group SQL calls.
     *
     * @param request - contains the court groups, one or two date ranges, and the
     *                  sumGroups flag controlling whether each group's locations are
     *                  summed or returned individually.
     * @return DbdDashboardResponseDto
     */
    DbdDashboardResponseDto getGroupStatistics(DbdDashboardRequestDto request);
}
