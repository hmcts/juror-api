package uk.gov.hmcts.juror.api.config.jurorer;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import uk.gov.hmcts.juror.api.jurorer.domain.LaRoles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Juror ER authentication Json Web Token payload.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JurorErJwtPayload {
    private String username;
    private String laName;
    private String laCode;
    private List<String> roles;

    public List<GrantedAuthority> getGrantedAuthority() {
        roles = List.of(LaRoles.LA_USER.toString()); // there is only one role for LA users
        return AuthorityUtils.createAuthorityList(roles);
    }

    public Map<String, Object> toClaims() {
        Map<String, Object> data = new HashMap<>();
        data.put("username", username);
        data.put("loCode", laCode);
        data.put("laName", laName);
        data.put("role", roles);
        return data;
    }

    @SuppressWarnings("unchecked")
    public static JurorErJwtPayload fromClaims(Claims claims) {

        return JurorErJwtPayload.builder()
            .username(claims.get("username", String.class))
            .laCode(claims.get("laCode", String.class))
            .laName(claims.get("laName", String.class))
            .roles(claims.get("role", List.class))
            .build();
    }

}
