package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.api.validation.JurorNumber;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Response DTO for listing Jurors to dismiss.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Schema(description = "Jurors to dismiss response")
public class JurorsToDismissResponseDto {

    @JsonProperty("jurors_to_dismiss_request_data")
    @Schema(description = "List of Jurors to dismiss data")
    private List<JurorsToDismissData> data;

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Builder
    @Schema(description = "Jurors to dismiss data class")
    public static class JurorsToDismissData {

        @NotNull
        @JurorNumber
        @JsonProperty("juror_number")
        @Schema(name = "Juror number", description = "Jurors Number")
        private String jurorNumber;

        @NotNull
        @Length(max = 20)
        @JsonProperty("first_name")
        @Schema(description = "Juror first name", requiredMode = Schema.RequiredMode.REQUIRED)
        private String firstName;

        @NotNull
        @Length(max = 20)
        @JsonProperty("last_name")
        @Schema(description = "Juror last name", requiredMode = Schema.RequiredMode.REQUIRED)
        private String lastName;

        @JsonProperty("attending")
        @Schema(name = "attending", description = "value indicating if juror is attending, on call or not in "
            + "attendance")
        private String attending;

        @JsonProperty("check_in_time")
        @Schema(description = "Check in time of the juror", implementation = String.class, pattern = "HH24:mm")
        private LocalTime checkInTime;

        @JsonProperty("next_due_at_court")
        @JsonFormat(pattern = "yyyy-MM-dd")
        @Schema(name = "Next due at court", description = "The date the juror is next due to attend court or "
            + "on call")
        private String nextDueAtCourt;

        @JsonProperty("service_start_date")
        @JsonFormat(pattern = "yyyy-MM-dd")
        @Schema(name = "Service start date", description = "The date the juror started their service")
        private LocalDate serviceStartDate;

    }

}
