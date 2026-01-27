package uk.gov.hmcts.juror.api.jurorer.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.juror.api.validation.ValidationConstants;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@AllArgsConstructor
@ToString(callSuper = true)
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class LaUserDetailsDto {

    @NotBlank
    private String username;

    @NotBlank
    private String laCode;

    private boolean isActive;

    @JsonFormat(pattern = ValidationConstants.DATETIME_FORMAT)
    private LocalDateTime lastSignIn;


}
