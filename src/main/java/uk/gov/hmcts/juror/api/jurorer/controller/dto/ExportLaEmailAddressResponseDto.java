package uk.gov.hmcts.juror.api.jurorer.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
@Schema(description = "Export response containing all LA email addresses grouped by Local Authority")
public class ExportLaEmailAddressResponseDto {

    @JsonProperty("local_authorities")
    @Schema(description = "List of local authorities with their email addresses")
    private List<LocalAuthorityEmailsDto> localAuthorities;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "Local Authority with associated email addresses")
    public static class LocalAuthorityEmailsDto {

        @JsonProperty("la_code")
        @Schema(description = "Local Authority code", example = "314")
        private String laCode;

        @JsonProperty("la_name")
        @Schema(description = "Local Authority name", example = "Birmingham")
        private String laName;

        @JsonProperty("is_active")
        @Schema(description = "Whether LA is active", example = "true")
        private Boolean isActive;

        @JsonProperty("email_addresses")
        @Schema(description = "List of email addresses for this LA")
        private List<EmailAddressDto> emailAddresses;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "Email address details")
    public static class EmailAddressDto {

        @JsonProperty("username")
        @Schema(description = "User's email/username", example = "user@birmingham.gov.uk")
        private String username;

        @JsonProperty("active")
        @Schema(description = "Whether user account is active", example = "true")
        private Boolean active;
    }
}
