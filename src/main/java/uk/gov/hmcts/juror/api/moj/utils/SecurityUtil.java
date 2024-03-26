package uk.gov.hmcts.juror.api.moj.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtAuthentication;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.domain.UserType;
import uk.gov.hmcts.juror.api.moj.exception.MojException;

import java.util.List;

public final class SecurityUtil {
    public static final int STANDARD_USER_LEVEL = 0;
    public static final int TEAM_LEADER_LEVEL = 1;
    public static final int JURY_OFFICER_LEVEL = 1;
    public static final int SENIOR_JUROR_OFFICER_LEVEL = 9;
    public static final String BUREAU_OWNER = "400";
    public static final String BUREAU_AUTH = "isAuthenticated() && principal.owner == '400'";
    public static final String COURT_AUTH = "isAuthenticated() && principal.owner != '400'";
    public static final String SENIOR_COURT_AUTH = COURT_AUTH + " && principal.userLevel == '9'";
    public static final String TEAM_LEADER_AUTH = "principal.userLevel == '" + TEAM_LEADER_LEVEL + "'";
    public static final String BUREAU_TEAM_LEADER = BUREAU_AUTH + " && " + TEAM_LEADER_AUTH;
    public static final String TEAM_LEADER_LEVEL_STR = String.valueOf(TEAM_LEADER_LEVEL);


    public static final String LOC_CODE_AUTH = "isAuthenticated() && principal.staff.courts.contains(#loc_code)";
    public static final String LOC_CODE_AUTH_OR_BUREAU = "isAuthenticated() "
        + "&& (principal.owner == '400' || principal.staff.courts.contains(#loc_code))";

    public static final String IS_MANAGER = "hasRole('ROLE_MANAGER')";

    public static final String USER_TYPE_ADMINISTRATOR = "principal.userType.name() == 'ADMINISTRATOR'";
    public static final String USER_TYPE_COURT = "principal.userType.name() == 'COURT'";


    private SecurityUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static BureauJwtAuthentication getActiveUsersBureauJwtAuthentication() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        if (authentication instanceof BureauJwtAuthentication bureauJwtAuthentication) {
            return bureauJwtAuthentication;
        }
        throw new MojException.Forbidden("User must be authorised with BureauJwtAuthentication", null);
    }

    public static BureauJwtPayload getActiveUsersBureauPayload() {
        Object principal = getActiveUsersBureauJwtAuthentication().getPrincipal();

        if (principal instanceof BureauJwtPayload bureauPayload) {
            return bureauPayload;
        }
        throw new MojException.InternalServerError("Unexpected principal object type", null);
    }

    public static String getActiveLogin() {
        return getActiveUsersBureauPayload().getLogin();
    }

    public static String getActiveOwner() {
        return getActiveUsersBureauPayload().getOwner();
    }

    /**
     * Verify whether the current/active user has permission to access to a specific court location.
     *
     * @param locCode 3-digit numeric string to uniquely identify a court location
     */
    public static void validateCourtLocationPermitted(String locCode) {
        if (!getActiveUsersBureauPayload().getStaff().getCourts().contains(locCode)) {
            throw new MojException.Forbidden(String.format("Current user does not have permissions to Court Location: "
                + "%s", locCode), null);
        }
    }

    public static boolean isBureau() {
        return BUREAU_OWNER.equals(getActiveOwner());
    }

    public static boolean isCourt() {
        return UserType.COURT.equals(getUserType());
    }

    public static void validateCanAccessOwner(String owner) {
        if (!getActiveOwner().equals(owner)) {
            throw new MojException.Forbidden("User does not have access", null);
        }
    }

    public static List<String> getCourts() {
        return getActiveUsersBureauPayload().getStaff().getCourts();
    }

    public static String getUsername() {
        return getActiveUsersBureauPayload().getLogin();
    }

    public static boolean isAdministration() {
        return getUserType().equals(UserType.ADMINISTRATOR);
    }

    public static UserType getUserType() {
        return getActiveUsersBureauPayload().getUserType();
    }

    public static String getLocCode() {
        return getActiveUsersBureauPayload().getLocCode();
    }
}
