package uk.gov.hmcts.juror.api.moj.domain.authentication;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.enumeration.CourtType;

import java.util.Comparator;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserCourtDto {

    @NotNull
    private CourtDto primaryCourt;

    private List<CourtDto> satelliteCourts;

    public UserCourtDto(List<CourtLocation> courts) {
        this.primaryCourt = new CourtDto(
            courts.stream()
                .filter(courtLocation -> CourtType.MAIN.equals(courtLocation.getType()))
                .toList().get(0));

        this.satelliteCourts = courts.stream()
            .filter(courtLocation -> CourtType.SATELLITE.equals(courtLocation.getType()))
            .map(CourtDto::new)
            .sorted(Comparator.comparing(CourtDto::getLocCode))
            .toList();
    }
}
