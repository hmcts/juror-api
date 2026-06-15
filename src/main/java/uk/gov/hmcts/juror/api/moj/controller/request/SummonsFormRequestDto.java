package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Getter
@Schema(description = "Summons form request")
public class SummonsFormRequestDto implements Serializable {

    @Schema(name = "Pool number", description = "Pool Request number")
    private String poolNumber;

    @Schema(name = "Catchment Area", description = "Summons Catchment Area (location code)")
    private String catchmentArea;

    @Schema(name = "Attend Time", description = "Pool Request Attend Time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime attendTime;

    @Schema(name = "Number Requested", description = "Pool Request Number Requested")
    private Integer noRequested;

    @Schema(name = "Next Date", description = "Pool Request Next Date")
    private LocalDate nextDate;

}
