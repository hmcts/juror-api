package uk.gov.hmcts.juror.api.juror.controller.request;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Request DTO for Juror request for dates.
 */

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Schema(description = "Request body for selected juror dates.")
public class JurorHolidaysRequestDto implements Serializable {
    @Schema(description = "Juror selected dates", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Date> holidaysDate;

}
