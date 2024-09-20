package uk.gov.hmcts.juror.api.moj.controller.response.jurormanagement;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Schema(description = "Data containing the details of unconfirmed jurors")
public class UnconfirmedJurorDataDto {

    @JsonProperty("juror_number")
    @Schema(description = "Juror number of juror", requiredMode = Schema.RequiredMode.REQUIRED)
    private String jurorNumber;

    @JsonProperty("first_name")
    @Schema(description = "The Juror's first name", requiredMode = Schema.RequiredMode.REQUIRED)
    private String firstName;

    @JsonProperty("last_name")
    @Schema(description = "The Juror's last name", requiredMode = Schema.RequiredMode.REQUIRED)
    private String lastName;

    @JsonProperty("status")
    @Schema(description = "The Juror's status", requiredMode = Schema.RequiredMode.REQUIRED)
    private int status;

    @JsonProperty("check_in_time")
    @Schema(description = "Check in time of the juror", implementation = String.class, pattern = "HH24:mm")
    private LocalTime checkInTime;

    @JsonProperty("check_out_time")
    @Schema(description = "Check out time of the juror", implementation = String.class, pattern = "HH24:mm")
    private LocalTime checkOutTime;

}
