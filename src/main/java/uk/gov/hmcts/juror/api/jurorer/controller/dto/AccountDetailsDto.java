package uk.gov.hmcts.juror.api.jurorer.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * User account details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "User account information")
public class AccountDetailsDto {

    @JsonProperty("username")
    @Schema(description = "User's email/username", example = "user@birmingham.gov.uk")
    private String username;

    @JsonProperty("la_code")
    @Schema(description = "Local Authority code", example = "314")
    private String laCode;

    @JsonProperty("la_name")
    @Schema(description = "Local Authority name", example = "Birmingham")
    private String laName;

    @JsonProperty("active")
    @Schema(description = "Whether account is active", example = "true")
    private Boolean active;

    @JsonProperty("last_logged_in")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Last login timestamp")
    private LocalDateTime lastLoggedIn;
}
