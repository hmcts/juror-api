package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@Valid
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ReissueLetterListResponseDto {

    @JsonProperty("headings")
    private List<String> headings;

    @JsonProperty("data_types")
    private List<String> dataTypes;

    @JsonProperty("data")
    private List<List<Object>> data;

}
