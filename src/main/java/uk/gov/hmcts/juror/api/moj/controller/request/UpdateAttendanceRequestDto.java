package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Schema(description = "Information required to put a juror on call")
public class UpdateAttendanceRequestDto {

    @JsonProperty("juror_numbers")
    @Size(min = 1)
    @Schema(name = "Juror numbers", requiredMode = Schema.RequiredMode.REQUIRED,
        description = "Array of Juror numbers to move to update")
    private List<String> jurorNumbers;

    @JsonProperty("on_call")
    @Schema(description = "check to see if juror has been set to on call")
    private boolean onCall;

    @JsonProperty("next_date")
    @Schema(description = "Next date due at court")
    private LocalDate nextDate;
}
