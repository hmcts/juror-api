package uk.gov.hmcts.juror.api.moj.controller.response.trial;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.api.validation.PoolNumber;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Schema(description = "A List of available jurors for generating a panel")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AvailableJurorsDto {

    @PoolNumber
    private String poolNumber;

    @NotNull
    private Long availableJurors;

    @NotNull
    private LocalDate serviceStartDate;

    private String courtLocation;

    @Length(min = 3, max = 3)
    private String courtLocationCode;
}
