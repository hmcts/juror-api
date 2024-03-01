package uk.gov.hmcts.juror.api.moj.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CourtDetailsDto {
    private boolean isWelsh;
    private String courtCode;
    private String englishCourtName;
    private Address englishAddress;
    private String welshCourtName;
    private Address welshAddress;
    private String mainPhone;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime attendanceTime;
    private String costCentre;
    private String signature;
    private String assemblyRoom;
}
