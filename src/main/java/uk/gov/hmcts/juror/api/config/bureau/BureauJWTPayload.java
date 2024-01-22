package uk.gov.hmcts.juror.api.config.bureau;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Bureau authentication Json Web Token payload.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BureauJWTPayload {
    private String owner;
    private String login;
    private String userLevel;
    private Boolean passwordWarning;
    private Integer daysToExpire;
    private Staff staff;

    @Builder
    @Data
    @EqualsAndHashCode
    public static class Staff {
        private String name;
        private Integer rank;
        private Integer active;
        @Builder.Default
        private List<String> courts = new ArrayList<>();
    }
}
