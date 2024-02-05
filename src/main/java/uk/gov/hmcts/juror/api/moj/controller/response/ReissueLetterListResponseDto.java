package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@Valid
@NoArgsConstructor
@AllArgsConstructor
public class ReissueLetterListResponseDto {

    @JsonProperty("headings")
    @NotEmpty
    private List<String> headings;

    @JsonProperty("data_types")
    @NotEmpty
    private List<String> dataTypes;

    @JsonProperty("data")
    private List<List<Object>> data;

}
