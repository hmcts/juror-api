package uk.gov.hmcts.juror.api.moj.controller.request.jurormanagement;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
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
import uk.gov.hmcts.juror.api.validation.PoolNumber;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Schema(description = "Update attendance date")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UpdateAttendanceDateDto {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
        description = "Array of Juror numbers to move to new pool")
    @Size(min = 1, message = "Request should contain at least one juror number")
    private List<@JurorNumber String> jurorNumbers;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "The unique number for a pool request")
    @NotBlank(message = "Request should contain a valid pool number")
    private @PoolNumber String poolNumber;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
        description = "The date the juror is expected to attend the court")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "Request should contain the new attendance date")
    private LocalDate attendanceDate;
}
