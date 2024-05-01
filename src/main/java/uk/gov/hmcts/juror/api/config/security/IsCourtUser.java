package uk.gov.hmcts.juror.api.config.security;

import org.springframework.security.access.prepost.PreAuthorize;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize(SecurityUtil.IS_COURT)
public @interface IsCourtUser {
}
