package uk.gov.hmcts.juror.api.juror.controller.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Builder
@Data
@Schema(description = "DBD information")
public class DbdInformationResponseDto {

    @Schema(description = "Name of the court the juror has been summoned to attend")
    @JsonProperty("courtName")
    private String courtName;

    @Schema(description = "Date the juror is due to start their service")
    @JsonProperty("serviceStartDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate serviceStartDate;

    @Schema(description = "The time the juror should attend court")
    @JsonProperty("courtAttendTime")
    private String courtAttendTime;

    @Schema(description = "Court address line 1")
    @JsonProperty("courtAddress1")
    private String courtAddress1;

    @Schema(description = "Court address line 2")
    @JsonProperty("courtAddress2")
    private String courtAddress2;

    @Schema(description = "Court address line 3")
    @JsonProperty("courtAddress3")
    private String courtAddress3;

    @Schema(description = "Court address line 4")
    @JsonProperty("courtAddress4")
    private String courtAddress4;

    @Schema(description = "Court address line 5")
    @JsonProperty("courtAddress5")
    private String courtAddress5;

    @Schema(description = "Court postcode")
    @JsonProperty("courtPostcode")
    private String courtPostcode;
}
