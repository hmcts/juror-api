package uk.gov.hmcts.juror.api.moj.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtAuthentication;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.config.public1.PublicJwtAuthentication;
import uk.gov.hmcts.juror.api.moj.domain.Role;
import uk.gov.hmcts.juror.api.moj.domain.UserType;
import uk.gov.hmcts.juror.api.moj.exception.MojException;

import java.util.List;

public final class SecurityUtil {

    public static final String BUREAU_OWNER = "400";

    public static final String IS_MANAGER = "hasRole('ROLE_MANAGER')";
    public static final String IS_SJO = "hasRole('ROLE_SENIOR_JUROR_OFFICER')";
    public static final String IS_BUREAU = "principal.activeUserType.name() == 'BUREAU'";
    public static final String IS_COURT = "principal.activeUserType.name() == 'COURT'";
    public static final String IS_ADMINISTRATOR = "principal.activeUserType.name() == 'ADMINISTRATOR'";

    public static final String IS_BUREAU_MANAGER = IS_BUREAU + " && " + IS_MANAGER;
    public static final String IS_COURT_MANAGER = IS_COURT + " && " + IS_MANAGER;

    public static final String IS_COURT_SJO = IS_COURT + " && " + IS_SJO;


    public static final String LOC_CODE_AUTH = "isAuthenticated() && principal.staff.courts.contains(#loc_code)";

    public static final String LOC_CODE_AUTH_COURT_ONLY = "isAuthenticated() && " + IS_COURT + " && "
        + "principal.staff.courts.contains(#loc_code)";
    public static final String LOC_CODE_AUTH_OR_BUREAU = "isAuthenticated() "
        + "&& (" + IS_BUREAU + " || principal.staff.courts.contains(#loc_code))";


    private SecurityUtil() {
        throw new IllegalStateException("Utility class");
    }


    public static boolean hasBureauJwtPayload() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        if (securityContext != null) {
            // Authentication authentication = ;
            return securityContext.getAuthentication() instanceof BureauJwtAuthentication;
        }
        return false;
    }

    public static boolean hasPublicJwtPayload() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        if (securityContext != null) {
            // Authentication authentication = ;
            return securityContext.getAuthentication() instanceof PublicJwtAuthentication;
        }
        return false;
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
        return UserType.BUREAU.equals(getUserType());
    }

    public static boolean isCourt() {
        return UserType.COURT.equals(getUserType());
    }

    public static boolean isSatellite() {
        return isCourt() && !getActiveOwner().equals(getLocCode());
    }

    public static void validateCanAccessOwner(String owner) {
        if (!getActiveOwner().equals(owner)) {
            throw new MojException.Forbidden("User does not have access", null);
        }
    }

    public static void validateIsLocCode(String locCode) {
        if (!getLocCode().equals(locCode)) {
            throw new MojException.Forbidden("User does not have access", null);
        }
    }

    public static void validateCanAccessRole(Role role) {
        if (!hasRole(role)) {
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
        return getActiveUsersBureauPayload().getActiveUserType();
    }


    public static String getLocCode() {
        return getActiveUsersBureauPayload().getLocCode();
    }

    public static boolean isManager() {
        return getActiveUsersBureauPayload().getRoles().contains(Role.MANAGER);
    }

    public static boolean isBureauManager() {
        return isBureau() && isManager();
    }

    public static boolean hasRole(Role role) {
        return getActiveUsersBureauPayload().getRoles().contains(role);
    }

}
