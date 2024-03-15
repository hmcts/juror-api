package uk.gov.hmcts.juror.api.moj.domain.authentication;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.enumeration.CourtType;
import uk.gov.hmcts.juror.api.validation.CourtLocationCode;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CourtDto {

    @NotBlank
    @Length(max = 40)
    private String name;
    @CourtLocationCode
    @NotBlank
    private String locCode;
    @NotNull
    private CourtType courtType;

    public CourtDto(CourtLocation court) {
        this.locCode = court.getLocCode();
        this.name = court.getName();
        this.courtType = court.getType();
    }
}
