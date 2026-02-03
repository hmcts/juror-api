package uk.gov.hmcts.juror.api.moj.controller.jurorer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.juror.api.jurorer.domain.UploadStatus;

import java.util.List;

@Getter
@Setter
@Builder
@Schema(description = "Local authority status information request DTO")
public class ErLocalAuthorityStatusRequestDto {

    @Schema(description = "The Local Authority Code")
    private String localAuthorityCode;

    @Schema(description = "The upload status to filter by")
    private List<UploadStatus> uploadStatus;

}
