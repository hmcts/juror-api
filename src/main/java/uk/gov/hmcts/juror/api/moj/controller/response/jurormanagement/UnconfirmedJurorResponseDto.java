package uk.gov.hmcts.juror.api.moj.controller.response.jurormanagement;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(description = "The list containing the details of unconfirmed jurors")
public class UnconfirmedJurorResponseDto {
    @JsonProperty("jurors")
    @NotNull
    private List<UnconfirmedJurorDataDto> jurors;

}
