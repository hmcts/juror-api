package uk.gov.hmcts.juror.api.moj.controller.request.jurormanagement;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.juror.api.validation.JurorNumber;
import uk.gov.hmcts.juror.api.validation.NumericString;
import uk.gov.hmcts.juror.api.validation.PoolNumber;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Schema(description = "Juror Non attendance request DTO")
public class JurorNonAttendanceDto {

    @JsonProperty("juror_number")
    @JurorNumber
    @NotBlank
    @Schema(description = "Juror number", requiredMode = Schema.RequiredMode.REQUIRED)
    private String jurorNumber;

    @JsonProperty("location_code")
    @NotBlank
    @Size(min = 3, max = 3)
    @NumericString
    @Schema(description = "Unique 3 digit code to identify a court location",
        requiredMode = Schema.RequiredMode.REQUIRED)
    private String locationCode;

    @JsonProperty("pool_number")
    @NotBlank
    @PoolNumber
    @Schema(description = "Numerical string to identify a pool at a court location",
        requiredMode = Schema.RequiredMode.REQUIRED)
    private String poolNumber;

    @JsonProperty("non_attendance_date")
    @NotNull
    @Schema(description = "Non attendance date of the juror",
        requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate nonAttendanceDate;

}
