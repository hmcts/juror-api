package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;

@Builder
@Data
@EqualsAndHashCode
public class JurorPoolDetailsDto {

    @JsonProperty("pool_number")
    private String poolNumber;


    @JsonProperty("status")
    private String status;

    public static JurorPoolDetailsDto from(JurorPool jurorPool) {
        return JurorPoolDetailsDto.builder()
            .poolNumber(jurorPool.getPoolNumber())
            .status(jurorPool.getStatus().getStatusDesc())
            .build();
    }
}
