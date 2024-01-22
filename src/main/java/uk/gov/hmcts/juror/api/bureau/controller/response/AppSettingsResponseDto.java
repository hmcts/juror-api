package uk.gov.hmcts.juror.api.bureau.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.juror.api.bureau.domain.AppSetting;

import java.io.Serializable;
import java.util.List;


@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(description = "Application settings response")
public class AppSettingsResponseDto implements Serializable {
    @Schema(description = "Application settings", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<AppSetting> data;
}
