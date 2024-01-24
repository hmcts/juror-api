package uk.gov.hmcts.juror.api.moj.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.juror.api.moj.domain.PoliceCheck;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Schema(description = "Police Check Status request")
public class PoliceCheckStatusDto {
    @NotNull
    @Schema(description = "The status of the police check")
    private PoliceCheck status;
}
