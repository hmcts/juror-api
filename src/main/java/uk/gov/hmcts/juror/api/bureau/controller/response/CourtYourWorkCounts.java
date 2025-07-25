package uk.gov.hmcts.juror.api.bureau.controller.response;


import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CourtYourWorkCounts {
    long todoCourtCount;
    long workCourtCount;
}
