package uk.gov.hmcts.juror.api.config.bureau;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Access to bureau endpoint is for team leader staff members only.
 *
 * @see BureauSecurityDecisionBean
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("@bureauSecurityDecisionBean.isTeamLeader(principal)")
public @interface IsTeamLeader {
}
