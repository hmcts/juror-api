package uk.gov.hmcts.juror.api.config.bureau;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.domain.Role;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.domain.UserType;
import uk.gov.hmcts.juror.api.moj.enumeration.CourtType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Bureau authentication Json Web Token payload.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BureauJwtPayload {
    private String owner;
    private String locCode;
    private String login;
    @Deprecated(forRemoval = true)
    private String userLevel;
    @Deprecated(forRemoval = true)
    private Boolean passwordWarning;
    @Deprecated(forRemoval = true)
    private Integer daysToExpire;
    private Staff staff;
    private UserType userType;

    private Collection<Role> roles;

    public BureauJwtPayload(User user, String locCode, List<CourtLocation> courtLocations) {
        this.owner = courtLocations.stream()
            .filter(courtLocation -> CourtType.MAIN.equals(courtLocation.getType()))
            .toList().get(0).getOwner();
        this.locCode = locCode;
        this.login = user.getUsername();
        this.userLevel = String.valueOf(user.getLevel());
        this.passwordWarning = false;
        this.daysToExpire = 999;
        this.userType = user.getUserType();


        if (UserType.ADMINISTRATOR.equals(user.getUserType())) {
            this.roles = List.of(Role.values());
        } else {
            this.roles = user.getRoles()
                .stream()
                .distinct()
                .sorted()
                .toList();
        }

        List<String> courts = new ArrayList<>(courtLocations.stream()
            .map(CourtLocation::getLocCode)
            .sorted()
            .toList());

        this.staff = new Staff(
            user.getName(),
            user.getLevel(),
            user.isActive() ? 1 : 0,
            courts);
    }


    public BureauJwtPayload(String owner, String login, String userLevel, Boolean passwordWarning, Integer daysToExpire,
                            Staff staff) {
        this(owner, null, login, userLevel, passwordWarning, daysToExpire, staff, null, null);
    }

    public List<GrantedAuthority> getGrantedAuthority() {
        List<String> authorities = new ArrayList<>();
        if (roles != null) {
            roles.forEach(role -> authorities.add("ROLE_" + role.name()));
        }
        if (getUserLevel() != null) {
            authorities.add(getUserLevel());
        }
        return AuthorityUtils.createAuthorityList(authorities);
    }

    public Map<String, Object> toClaims() {
        return Map.of(
            "owner", owner,
            "locCode", locCode,
            "login", login,
            "userLevel", userLevel,
            "passwordWarning", passwordWarning,
            "daysToExpire", daysToExpire,
            "staff", staff.toClaims(),
            "roles", roles,
            "userType", userType
        );
    }

    @SuppressWarnings("unchecked")
    public static BureauJwtPayload fromClaims(Claims claims) {
        // parse the staff object
        final Map<String, Object> staffMap = claims.get("staff", Map.class);

        final BureauJwtPayload.Staff staff = BureauJwtPayload.Staff.fromClaims(staffMap);


        final List<String> roleString =
            claims.containsKey("roles") ? claims.get("roles", List.class) : Collections.emptyList();

        final List<Role> roles = roleString
            .stream()
            .map(o -> Role.valueOf(String.valueOf(o)))
            .toList();

        UserType userType = claims.containsKey("userType")
            ? UserType.valueOf(claims.get("userType", String.class))
            : null;

        return BureauJwtPayload.builder()
            .daysToExpire(claims.get("daysToExpire", Integer.class))
            .login(claims.get("login", String.class))
            .owner(claims.get("owner", String.class))
            .locCode(claims.get("locCode", String.class))
            .passwordWarning(claims.get("passwordWarning", Boolean.class))
            .userLevel(claims.get("userLevel", String.class))
            .staff(staff)
            .roles(roles)
            .userType(userType)
            .build();
    }

    @Builder
    @Data
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Staff {
        private String name;
        @Deprecated(forRemoval = true)
        private Integer rank;
        private Integer active;
        @Builder.Default
        private List<String> courts = new ArrayList<>();

        @SuppressWarnings("unchecked")
        public static Staff fromClaims(Map<String, Object> staffMap) {
            StaffBuilder staffBuilder = Staff.builder();
            if (!ObjectUtils.isEmpty(staffMap)) {
                staffBuilder.name(String.valueOf(staffMap.getOrDefault("name", "")));
                staffBuilder.rank((Integer) staffMap.getOrDefault("rank", Integer.MIN_VALUE));
                staffBuilder.active((Integer) staffMap.getOrDefault("active", 0));
                staffBuilder.courts((List<String>) staffMap.getOrDefault("courts", Collections.emptyList()));
            }
            return staffBuilder
                .build();
        }

        public Map<String, Object> toClaims() {
            return Map.of(
                "name", name,
                "rank", rank,
                "active", active,
                "courts", courts
            );
        }
    }
}
