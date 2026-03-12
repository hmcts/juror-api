package uk.gov.hmcts.juror.api.moj.controller.jurorer;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@Schema(description = "List of all local authorities")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class LocalAuthoritiesResponseDto {

    @Schema(description = "List of all local authorities")
    private List<LocalAuthorityData> localAuthorities;

    @Builder
    @Getter
    public static class LocalAuthorityData {

        private String localAuthorityCode;

        private String localAuthorityName;

    }



}
