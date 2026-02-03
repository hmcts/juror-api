package uk.gov.hmcts.juror.api.jurorer.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Complete page data DTO containing all information for the upload page.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Complete upload page data including dashboard, guidance, and account details")
public class UploadPageDataDto {

    @JsonProperty("dashboard")
    @Schema(description = "Dashboard information with deadline and upload status")
    private DashboardInfoDto dashboard;

    @JsonProperty("upload_guidance")
    @Schema(description = "Guidance and instructions for file upload")
    private UploadGuidanceDto uploadGuidance;

    @JsonProperty("account_details")
    @Schema(description = "User account details")
    private AccountDetailsDto accountDetails;

    @JsonProperty("upload_history")
    @Schema(description = "Recent upload history for this LA")
    private UploadHistoryDto uploadHistory;
}
