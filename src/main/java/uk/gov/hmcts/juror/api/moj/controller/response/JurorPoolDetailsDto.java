package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;

@Builder
@Data
@EqualsAndHashCode
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class JurorPoolDetailsDto {

    @JsonProperty("pool_number")
    private String poolNumber;

    @JsonProperty("status")
    private String status;

    @JsonProperty("court_name")
    private String courtName;

    public static JurorPoolDetailsDto from(JurorPool jurorPool) {
        return JurorPoolDetailsDto.builder()
            .poolNumber(jurorPool.getPoolNumber())
            .status(jurorPool.getStatus().getStatusDesc())
            .courtName(jurorPool.getCourt().getName())
            .build();
    }
}
