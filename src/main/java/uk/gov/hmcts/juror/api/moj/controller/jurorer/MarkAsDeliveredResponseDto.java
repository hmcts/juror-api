package uk.gov.hmcts.juror.api.moj.controller.jurorer;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "Response for mark as delivered operation")
public class MarkAsDeliveredResponseDto {

    @Schema(description = "LA codes successfully updated to SENT")
    private List<String> updated;

    @Schema(description = "LA codes already marked as SENT - skipped")
    private List<String> alreadySent;

    @Schema(description = "LA codes that failed to update")
    private List<MarkAsDeliveredErrorDto> errors;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class MarkAsDeliveredErrorDto {
        private String laCode;
        private String reason;
    }
}
