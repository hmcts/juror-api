package uk.gov.hmcts.juror.api.moj.controller.jurorer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "Request to update the deadline date")
public class UpdateDeadlineRequestDto {

    @NotNull(message = "Deadline date is required")
    @Future(message = "Deadline date must be in the future")
    @JsonProperty("deadline_date")
    @Schema(description = "New deadline date", example = "2025-03-31", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate deadlineDate;
}
