package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
public class DeferralDatesRequestDto {

    @JsonProperty("deferralDates")
    @Schema(name = "Deferral Dates", description = "List of preferred dates to defer jury service to in the format of"
        + " yyyy-MM-dd",
        example = "[\"2023-05-16\", \"2023-07-25\"]")
    @Size(min = 1, max = 3)
    private List<LocalDate> deferralDates = new ArrayList<>();
}
