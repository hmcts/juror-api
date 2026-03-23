package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.juror.api.moj.enumeration.IdCheckCodeEnum;

@Getter
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ConfirmIdentityDto {

    @NotNull
    @JsonProperty("juror_number")
    @Schema(name = "Juror number", description = "Jurors Number")
    private String jurorNumber;

    @JsonProperty("confirm_code")
    @Schema(name = "Confirmation Code", description = "The ID for a type of Identity confirmation evidence")
    @Enumerated(EnumType.STRING)
    private IdCheckCodeEnum idCheckCode;

}
