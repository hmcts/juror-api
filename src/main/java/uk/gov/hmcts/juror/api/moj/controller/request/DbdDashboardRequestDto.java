package uk.gov.hmcts.juror.api.moj.controller.request;


import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * Request DTO for the Digital by Default pilot dashboard.
 *
 * Unlike DashboardRequestDto, this supports:
 *  - one or more caller-defined court groups (arbitrary, per-request groupings of pilot courts)
 *  - a mandatory comparison period (dateRangeA) plus an optional second period (dateRangeB)
 *  - a toggle for whether each group's locations are summed together or returned individually
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DbdDashboardRequestDto implements Serializable {

    @NotEmpty
    @Valid
    private List<CourtGroupDto> courtGroups;

    @NotNull
    @Valid
    private DateRangeDto dateRangeA;

    @Valid
    private DateRangeDto dateRangeB;

    private boolean sumGroups;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CourtGroupDto implements Serializable {

        @NotEmpty
        private String groupName;

        @NotEmpty
        private List<Integer> groupLocations;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DateRangeDto implements Serializable {

        @NotNull
        @JsonFormat(pattern = "yyyyMMdd")
        private LocalDate startDate;

        @NotNull
        @JsonFormat(pattern = "yyyyMMdd")
        private LocalDate endDate;
    }
}

