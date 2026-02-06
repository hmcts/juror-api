package uk.gov.hmcts.juror.api.moj.controller.jurorer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.juror.api.jurorer.domain.UploadStatus;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@Schema(description = "Local authority status information DTO")
public class ErLocalAuthorityStatusResponseDto {

    @Schema(description = "List of local authorities and their ER upload status")
    private List<ErLocalAuthorityStatus> localAuthorityStatuses;

    @Builder
    @Getter
    public static class ErLocalAuthorityStatus {

        private String localAuthorityCode;

        private String localAuthorityName;

        private UploadStatus uploadStatus;

        private LocalDateTime lastUploadDate;
    }

}
