package uk.gov.hmcts.juror.api.moj.domain.administration;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.juror.api.moj.domain.trial.Courtroom;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CourtRoomWithIdDto extends CourtRoomDto {

    @NotNull
    private Long id;

    public CourtRoomWithIdDto(Courtroom courtRoom) {
        super(courtRoom);
        this.id = courtRoom.getId();
    }
}
