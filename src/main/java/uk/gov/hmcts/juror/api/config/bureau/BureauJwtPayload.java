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
import uk.gov.hmcts.juror.api.moj.domain.Permission;
import uk.gov.hmcts.juror.api.moj.domain.Role;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.domain.UserType;
import uk.gov.hmcts.juror.api.moj.enumeration.CourtType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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
    private String email;
    private String owner;
    private String locCode;
    private String login;
    @Deprecated(forRemoval = true)
    private String userLevel;
    private Staff staff;
    private UserType userType;
    private UserType activeUserType;

    private Collection<Role> roles;
    private Collection<Permission> permissions;

    public BureauJwtPayload(User user, String locCode, List<CourtLocation> courtLocations) {
        this(user, user.getUserType(), locCode, courtLocations);
    }

    public BureauJwtPayload(User user, UserType activeType, String locCode, List<CourtLocation> courtLocations) {
        this.owner = courtLocations.stream()
            .filter(courtLocation -> CourtType.MAIN.equals(courtLocation.getType()))
            .toList().get(0).getOwner();
        this.locCode = locCode;
        this.email = user.getEmail();
        this.login = user.getUsername();
        this.userLevel = String.valueOf(user.getLevel());
        this.userType = user.getUserType();
        this.activeUserType = activeType;

        if (UserType.ADMINISTRATOR.equals(user.getUserType())) {
            this.roles = List.of(Role.values());
        } else {
            this.roles = user.getRoles()
                .stream()
                .distinct()
                .sorted()
                .toList();
        }

        this.permissions = user.getPermissions()
            .stream()
            .distinct()
            .sorted()
            .toList();

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


    public BureauJwtPayload(String owner, String login, String userLevel,
                            Staff staff) {
        this(null, owner, null, login, userLevel, staff, null, null, null, null);
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
        Map<String, Object> data = new HashMap<>();
        data.put("owner", owner);
        data.put("locCode", locCode);
        data.put("email", email);
        data.put("login", login);
        data.put("userLevel", userLevel);
        data.put("staff", staff.toClaims());
        data.put("roles", roles);
        data.put("permissions", permissions);
        data.put("userType", userType);
        data.put("activeUserType", activeUserType);
        return data;
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

        final List<String> permissionString =
            claims.containsKey("permissions") ? claims.get("permissions", List.class) : Collections.emptyList();

        final List<Permission> permissions = permissionString
            .stream()
            .map(o -> Permission.valueOf(String.valueOf(o)))
            .toList();

        UserType userType = claims.containsKey("userType")
            ? UserType.valueOf(claims.get("userType", String.class))
            : null;

        UserType activeUserType = claims.containsKey("activeUserType")
            ? UserType.valueOf(claims.get("activeUserType", String.class))
            : null;

        return BureauJwtPayload.builder()
            .login(claims.get("login", String.class))
            .email(claims.get("email", String.class))
            .owner(claims.get("owner", String.class))
            .locCode(claims.get("locCode", String.class))
            .userLevel(claims.get("userLevel", String.class))
            .activeUserType(activeUserType)
            .staff(staff)
            .roles(roles)
            .permissions(permissions)
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
