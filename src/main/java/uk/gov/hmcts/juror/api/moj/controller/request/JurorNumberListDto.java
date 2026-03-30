package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.juror.api.validation.JurorNumber;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class JurorNumberListDto {

    @NotEmpty
    @JsonProperty("juror_numbers")
    private List<@JurorNumber String> jurorNumbers;
}
