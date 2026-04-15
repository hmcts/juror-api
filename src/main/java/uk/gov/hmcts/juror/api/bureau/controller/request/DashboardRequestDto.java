package uk.gov.hmcts.juror.api.bureau.controller.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * Request DTO for the juror dashboard.
 */
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Builder
@Data
@Schema(description = "Request body for the juror dashboard. Given Period.")
public class DashboardRequestDto implements Serializable {

    @Schema(description = "Start date for reporting statistics", requiredMode = Schema.RequiredMode.REQUIRED)
    private Date startDate;

    @Schema(description = "End date for reporting statistics", requiredMode = Schema.RequiredMode.REQUIRED)
    private Date endDate;

}
