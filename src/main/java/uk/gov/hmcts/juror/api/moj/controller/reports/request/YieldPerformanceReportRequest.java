package uk.gov.hmcts.juror.api.moj.controller.reports.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.juror.api.validation.ValidationConstants;

import java.time.LocalDate;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class YieldPerformanceReportRequest {

    private List<String> courtLocCodes;

    private boolean allCourts;

    @NotNull
    @JsonFormat(pattern = ValidationConstants.DATE_FORMAT, shape = JsonFormat.Shape.STRING)
    private LocalDate fromDate;

    @NotNull
    @JsonFormat(pattern = ValidationConstants.DATE_FORMAT, shape = JsonFormat.Shape.STRING)
    private LocalDate toDate;

}