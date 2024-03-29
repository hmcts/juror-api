package uk.gov.hmcts.juror.api.bureau.domain;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import uk.gov.hmcts.juror.api.JurorDigitalApplication;
import uk.gov.hmcts.juror.api.moj.domain.QUser;
import uk.gov.hmcts.juror.api.moj.domain.Role;
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
        return isBureau().and(active())
            .and(USER.roles.contains(Role.TEAM_LEADER).not());
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

}
