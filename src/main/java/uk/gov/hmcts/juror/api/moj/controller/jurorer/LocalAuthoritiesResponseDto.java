package uk.gov.hmcts.juror.api.moj.controller.jurorer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@Schema(description = "List of all local authorities")
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
