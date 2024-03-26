package uk.gov.hmcts.juror.api.bureau.domain;

import com.querydsl.core.types.dsl.BooleanExpression;

public abstract class BureauJurorCjsQueries {

    private BureauJurorCjsQueries() {
        throw new IllegalStateException("BureauJurorCjsQueries should not be instantiated.");
    }

    public static BooleanExpression byJurorNumberAndEmployer(final String jurorNumber, final String employer) {
        final QBureauJurorCjs bureauJurorCjs = QBureauJurorCjs.bureauJurorCjs;
        return bureauJurorCjs.jurorNumber.eq(jurorNumber).and(bureauJurorCjs.employer.eq(employer));
    }
}
