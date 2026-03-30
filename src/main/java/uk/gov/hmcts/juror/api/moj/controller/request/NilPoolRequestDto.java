package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.juror.api.validation.NumericString;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Schema(description = "Nil Pool request")
public class NilPoolRequestDto implements Serializable {

    @JsonProperty("courtCode")
    @Size(min = 3, max = 3)
    @NumericString
    @Schema(description = "Unique 3 digit code to identify a court location")
    private String locationCode;

    @JsonProperty("locationName")
    @Schema(name = "Location name", description = "The court name")
    private String locationName;

    @JsonProperty("attendanceDate")
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "The date this pool is being created for")
    private LocalDate attendanceDate;

    @JsonProperty("attendanceTime")
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    @Schema(description = "The time this pool is first due to attend court (in 24-hour format)")
    private LocalTime attendanceTime;

    @JsonProperty("poolType")
    @NotBlank
    @Size(min = 3, max = 3)
    @Schema(description = "What type of court is the pool being created for")
    private String poolType;

    @JsonProperty("poolNumber")
    @Schema(name = "Pool number", description = "The unique number for a pool request")
    private String poolNumber;

}