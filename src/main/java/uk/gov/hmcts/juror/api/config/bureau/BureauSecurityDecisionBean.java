package uk.gov.hmcts.juror.api.config.bureau;

import org.springframework.stereotype.Component;

/**
 * Spring expression language referencable bean for making.
 * {@link org.springframework.security.access.prepost.PreAuthorize} decisions on a custom.
 * {@link java.security.Principal} object, namely {@link BureauJwtAuthentication}.
 */
@Component
public class BureauSecurityDecisionBean {
    /**
     * Constant for team leader rank.
     */
    public static final Integer TEAM_LEADER_RANK = 1;

    /**
     * Is the current user principal a team leader?.
     *
     * @param principal Principal object (JWT payload)
     * @return
     */
    public static boolean isTeamLeader(Object principal) {
        return TEAM_LEADER_RANK.compareTo(((BureauJWTPayload) principal).getStaff().getRank()) == 0;
    }
}
