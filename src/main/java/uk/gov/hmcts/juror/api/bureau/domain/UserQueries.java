package uk.gov.hmcts.juror.api.bureau.domain;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import uk.gov.hmcts.juror.api.JurorDigitalApplication;
import uk.gov.hmcts.juror.api.moj.domain.QUser;
import uk.gov.hmcts.juror.api.moj.domain.UserType;

/**
 * QueryDSL queries for {@link uk.gov.hmcts.juror.api.moj.domain.User}.
 */
@Deprecated(forRemoval = true)
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
        return isBureau().and(active());
    }

    public static BooleanExpression isBureau() {
        return USER.userType.eq(UserType.BUREAU);
    }

    public static BooleanExpression active() {
        return USER.active.eq(true)
            .and(USER.userType.ne(UserType.SYSTEM));
    }

    public static BooleanExpression owner(String owner) {
        return USER.courts.any().owner.eq(owner);
    }

    public static BooleanExpression inactive() {
        return USER.active.notIn(true)
            .and(USER.username.notEqualsIgnoreCase(JurorDigitalApplication.AUTO_USER));
    }

    public static OrderSpecifier<String> sortNameAsc() {
        return USER.name.asc();
    }

}
