package uk.gov.hmcts.juror.api.moj.controller.reports.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.juror.api.moj.report.AbstractReport;
import uk.gov.hmcts.juror.api.validation.PoolNumber;
import uk.gov.hmcts.juror.api.validation.ValidationConstants;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class StandardReportRequest {

    @NotBlank(groups = AbstractReport.Validators.AbstractRequestValidator.class)
    @NotBlank
    @Schema(allowableValues = {
        //Standard
        "CurrentPoolStatusReport",
        "DeferredListByDateReport",
        "NextAttendanceDayReport",
        "NonRespondedReport",
        "PostponedListByPoolReport",
        "UndeliverableListReport",
        //Grouped
        "PostponedListByDateReport"
    })
    private String reportType;

    @PoolNumber(groups = AbstractReport.Validators.RequirePoolNumber.class)
    @NotNull(groups = AbstractReport.Validators.RequirePoolNumber.class)
    private String poolNumber;

    @NotNull(groups = AbstractReport.Validators.RequireFromDate.class)
    @JsonFormat(pattern = ValidationConstants.DATE_FORMAT)
    private LocalDate fromDate;

    @NotNull(groups = AbstractReport.Validators.RequireToDate.class)
    @JsonFormat(pattern = ValidationConstants.DATE_FORMAT)
    private LocalDate toDate;
}
