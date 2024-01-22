package uk.gov.hmcts.juror.api.bureau.domain;

import com.querydsl.core.types.dsl.BooleanExpression;

public abstract class BureauJurorSpecialNeedQueries {

    private BureauJurorSpecialNeedQueries() {
        throw new IllegalStateException("BureauJurorSpecialNeedQueries should not be instantiated.");
    }

    public static BooleanExpression byJurorNumberAndCode(final String jurorNumber, final String code) {
        final QBureauJurorSpecialNeed specialNeed = QBureauJurorSpecialNeed.bureauJurorSpecialNeed;
        return specialNeed.jurorNumber.eq(jurorNumber).and(specialNeed.specialNeed.code.eq(code));
    }
}
