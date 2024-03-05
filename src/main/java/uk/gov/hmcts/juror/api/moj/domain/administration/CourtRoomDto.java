package uk.gov.hmcts.juror.api.moj.domain.administration;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.api.moj.domain.trial.Courtroom;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CourtRoomDto {
    @NotBlank
    @Length(min = 1, max = 6)
    private String roomName;
    @NotBlank
    @Length(min = 1, max = 30)
    private String roomDescription;

    public CourtRoomDto(Courtroom courtRoom) {
        this(courtRoom.getRoomNumber(), courtRoom.getDescription());
    }
}
