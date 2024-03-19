package uk.gov.hmcts.juror.api.moj.controller.reports.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.juror.api.moj.report.AbstractReport;
import uk.gov.hmcts.juror.api.validation.PoolNumber;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class StandardReportRequest {

    @NotBlank(groups = AbstractReport.Validators.AbstractRequestValidator.class)
    @NotBlank
    private String reportType;

    @PoolNumber(groups = AbstractReport.Validators.RequirePoolNumber.class)
    @NotNull(groups = AbstractReport.Validators.RequirePoolNumber.class)
    private String poolNumber;
}
