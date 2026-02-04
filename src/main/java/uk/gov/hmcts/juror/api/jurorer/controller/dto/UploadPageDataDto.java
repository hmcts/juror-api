package uk.gov.hmcts.juror.api.jurorer.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
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
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "Complete upload page data including dashboard and account details")
public class UploadPageDataDto {

    @JsonProperty("dashboard")
    @Schema(description = "Dashboard information with deadline and upload status")
    private DashboardInfoDto dashboard;

    @JsonProperty("account_details")
    @Schema(description = "User account details")
    private AccountDetailsDto accountDetails;

    @JsonProperty("upload_history")
    @Schema(description = "Recent upload history for this LA")
    private UploadHistoryDto uploadHistory;
}
