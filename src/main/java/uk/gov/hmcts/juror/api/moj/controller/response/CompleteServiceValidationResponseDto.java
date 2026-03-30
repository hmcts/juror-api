package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class CompleteServiceValidationResponseDto {

    @NotNull
    @JsonProperty("valid")
    private List<@NotNull JurorStatusValidationResponseDto> valid;

    @JsonProperty("invalid_not_responded")
    @NotNull
    private List<@NotNull JurorStatusValidationResponseDto> invalidNotResponded;

    public CompleteServiceValidationResponseDto() {
        this.valid = new ArrayList<>();
        this.invalidNotResponded = new ArrayList<>();
    }

    public void addValid(JurorStatusValidationResponseDto jurorStatusValidationResponseDto) {
        this.valid.add(jurorStatusValidationResponseDto);
    }

    public void addInvalidNotResponded(JurorStatusValidationResponseDto jurorStatusValidationResponseDto) {
        this.invalidNotResponded.add(jurorStatusValidationResponseDto);
    }
}
