package uk.gov.hmcts.juror.api.config.public_;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Juror custom JWT data payload.
 *
 * @apiNote This should match the json object "data" supplied from Express inside the JWT body.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PublicJWTPayload {
    private String id;
    private String jurorNumber;
    private String surname;
    private String postcode;
    private String[] roles;
}
