package uk.gov.hmcts.juror.api.moj.controller.jurorer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@Schema(description = "ER Dashboard stats information DTO")
public class ErDashboardStatsResponseDto {

    @Schema(description = "Deadline date for uploading ER data")
    private LocalDate deadlineDate;

    @Schema(description = "Number of days remaining to the deadline")
    private long daysRemaining;

    @Schema(description = "Total Number of local authorities must upload ER data")
    private long totalNumberOfLocalAuthorities;

    @Schema(description = "Number of local authorities that have not yet uploaded their ER data")
    private long notUploadedCount;

    @Schema(description = "Number of local authorities that have uploaded ER data")
    private long uploadedCount;

}
