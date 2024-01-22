package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.juror.api.validation.JurorNumber;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Schema(description = "Information required to put a juror on call")
public class UpdateAttendanceRequestDto {

    @NotNull
    @Column(name = "juror_number")
    @JurorNumber
    private String jurorNumber;

    @JsonProperty("on_call")
    @Schema(description = "check to see if juror has been set to on call")
    private boolean onCall;

    @JsonProperty("next_date")
    @Schema(description = "Next date due at court")
    private LocalDate nextDate;
}
