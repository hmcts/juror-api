package uk.gov.hmcts.juror.api.config.bureau;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import uk.gov.hmcts.juror.api.moj.domain.Role;
import uk.gov.hmcts.juror.api.moj.domain.UserType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
    private UserType userType;

    private Set<Role> roles;

    public BureauJWTPayload(String owner, String login, String userLevel, Boolean passwordWarning, Integer daysToExpire,
                            Staff staff) {
        this(owner, login, userLevel, passwordWarning, daysToExpire, staff, null, null);
    }

    public List<GrantedAuthority> getGrantedAuthority() {
        List<String> authorities = new ArrayList<>();
        if (roles != null) {
            roles.forEach(role -> authorities.add("ROLE_" + role.name()));
        }
        authorities.add(getUserLevel());
        return AuthorityUtils.createAuthorityList(authorities);
    }

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
