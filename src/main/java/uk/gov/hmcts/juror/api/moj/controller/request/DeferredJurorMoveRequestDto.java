package uk.gov.hmcts.juror.api.moj.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.juror.api.validation.JurorNumber;
import uk.gov.hmcts.juror.api.validation.PoolNumber;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Schema(description = "payload for moving a deferred request")
public class DeferredJurorMoveRequestDto {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
        description = "Array of Juror numbers to move to new pool")
    @Size(min = 1, message = "Request should contain at least one juror number")
    private List<@JurorNumber String> jurorNumbers;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "The unique number for a pool request")
    @NotBlank(message = "Request should contain a valid pool number")
    private @PoolNumber String poolNumber;

}
