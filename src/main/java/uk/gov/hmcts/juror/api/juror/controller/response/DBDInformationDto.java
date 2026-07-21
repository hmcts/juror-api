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
public class DbdInformationDto {

    @Schema(description = "Name of the court the juror has been summoned to attend")
    @JsonProperty("courtName")
    private String courtName;

    @Schema(description = "Date the juror is due to start their service")
    @JsonProperty("serviceStartDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate serviceStartDate;
}
