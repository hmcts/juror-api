package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Schema(description = "Summons form request")
public class SummonsFormRequestDto implements Serializable {

    @JsonProperty("poolNumber")
    @Schema(name = "Pool number", description = "Pool Request number")
    private String poolNumber;

    @JsonProperty("catchmentArea")
    @Schema(name = "Catchment Area", description = "Summons Catchment Area (location code)")
    private String catchmentArea;

    @JsonProperty("attendTime")
    @Schema(name = "Attend Time", description = "Pool Request Attend Time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime attendTime;

    @JsonProperty("noRequested")
    @Schema(name = "Number Requested", description = "Pool Request Number Requested")
    private Integer noRequested;

    @JsonProperty("nextDate")
    @Schema(name = "Next Date", description = "Pool Request Next Date")
    private LocalDate nextDate;

}