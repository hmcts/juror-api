package uk.gov.hmcts.juror.api.moj.domain;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.api.validation.ValidationConstants;

import java.time.LocalTime;

@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UpdateCourtDetailsDto {

    @Pattern(regexp = ValidationConstants.PHONE_PRIMARY_REGEX)
    @NotBlank
    private String mainPhoneNumber;

    @NotNull
    private LocalTime defaultAttendanceTime;

    @Min(0)
    @NotNull
    private Long assemblyRoomId;

    @Length(min = 1, max = 9)
    @NotBlank
    private String costCentre;

    @NotBlank
    @Length(min = 1, max = 30)
    private String signature;
}
