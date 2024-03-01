package uk.gov.hmcts.juror.api.moj.controller.response.administration;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.juror.api.moj.domain.system.HasActive;
import uk.gov.hmcts.juror.api.moj.domain.system.HasCodeAndDescription;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CodeDescriptionResponse {
    @NotBlank
    private String code;
    @NotBlank
    private String description;
    private Boolean isActive;

    public CodeDescriptionResponse(HasCodeAndDescription<?> hasCodeAndDescription) {
        this.code = String.valueOf(hasCodeAndDescription.getCode());
        this.description = hasCodeAndDescription.getDescription();

        if (hasCodeAndDescription instanceof HasActive isActive) {
            this.isActive = isActive.getActive();
        }
    }
}
