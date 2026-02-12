package uk.gov.hmcts.juror.api.jurorer.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "Request to send notifications to local authorities")
public class LaNotificationRequestDto {
    @JsonProperty("la_codes")
    @NotEmpty(message = "At least one LA code must be provided")
@Schema(description = "List of Local Authority codes to send notifications to", requiredMode = Schema.RequiredMode.REQUIRED)
private List<@Size(min = 3, max = 3, message = "LA code must be 3 characters") String> laCodes;
}
