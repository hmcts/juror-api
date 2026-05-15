package uk.gov.hmcts.juror.api.moj.controller.response.jurormanagement;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Data
@Schema(description = "The list containing the details of unconfirmed jurors")
public class UnconfirmedJurorResponseDto {
    @JsonProperty("jurors")
    @NotNull
    private List<UnconfirmedJurorDataDto> jurors;

}
