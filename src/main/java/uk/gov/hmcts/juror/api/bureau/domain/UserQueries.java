package uk.gov.hmcts.juror.api.bureau.domain;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import uk.gov.hmcts.juror.api.JurorDigitalApplication;
import uk.gov.hmcts.juror.api.moj.domain.QUser;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

/**
 * QueryDSL queries for {@link uk.gov.hmcts.juror.api.moj.domain.User}.
 */
public class UserQueries {

    private static final QUser USER = QUser.user;

    private UserQueries() {
    }

    /**
     * Query to match active bureau officers.
     *
     * @return QueryDSL filter
     */
    public static BooleanExpression activeBureauOfficers() {
        return USER.level
            .eq(SecurityUtil.STANDARD_USER_LEVEL)
            .and(isBureau())
            .and(active());
    }

    public static BooleanExpression isBureau() {
        return USER.owner.eq(SecurityUtil.BUREAU_OWNER);
    }

    public static BooleanExpression active() {
        return USER.active.eq(true)
            .and(USER.username.notEqualsIgnoreCase(JurorDigitalApplication.AUTO_USER));
    }

    public static BooleanExpression owner(String owner) {
        return USER.owner.eq(owner);
    }

    public static BooleanExpression inactive() {
        return USER.active.notIn(true)
            .and(USER.username.notEqualsIgnoreCase(JurorDigitalApplication.AUTO_USER));
    }

    public static OrderSpecifier<String> sortNameAsc() {
        return USER.name.asc();
    }

    public static BooleanExpression byLogin(final String login) {
        return USER.username.eq(login);
    }

    public static BooleanExpression activeStaffMember(final String login) {
        return byLogin(login).and(active());
    }

    public static BooleanExpression loginAllowed(final String login) {
        return USER.loginEnabledYn.isTrue()
            .and(byLogin(login));
    }
}
