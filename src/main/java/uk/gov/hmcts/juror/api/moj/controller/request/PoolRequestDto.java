package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.juror.api.validation.NumericString;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO for inbound Pool Request operations (create or update) .
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Schema(description = "Pool request DTO for create and update operations")
public class PoolRequestDto {

    @JsonProperty("poolNumber")
    @NotBlank
    @Size(min = 9, max = 9)
    @NumericString
    @Schema(description = "Unique pool number")
    private String poolNumber;

    @JsonProperty("courtCode")
    @NotBlank
    @Size(min = 3, max = 3)
    @NumericString
    @Schema(description = "Unique 3 digit code to identify a court location")
    private String locationCode;

    @JsonProperty("attendanceDate")
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "The date this pool is being created for, when the pool members are first expected to "
        + "attend court")
    private LocalDate attendanceDate;


    @JsonProperty("numberRequested")
    @NotNull
    @Min(0)
    @Max(3000)
    @Schema(description = "Total number of jurors requested for this pool")
    private int numberRequested;

    @JsonProperty("poolType")
    @NotBlank
    @Size(min = 3, max = 3)
    @Schema(description = "What type of court is the pool being created for")
    private String poolType;

    @JsonProperty("attendanceTime")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    @Schema(description = "The time this pool is first due to attend court (in 24-hour format)")
    private LocalTime attendanceTime;

    @JsonProperty(value = "deferralsUsed", defaultValue = "0")
    @NotNull
    @Min(0)
    @Schema(description = "How many of the available deferrals have been used in this pool")
    private int deferralsUsed;

    @JsonProperty(value = "courtOnly", defaultValue = "false")
    @Schema(description = "Flag the pool as being requested for court use only - this is to support court-only work "
        + "flow management and does not require the Bureau to summon any jurors")
    private boolean courtOnly;

}
