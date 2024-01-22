package uk.gov.hmcts.juror.api.bureau.domain;

import com.querydsl.core.types.dsl.BooleanExpression;

public abstract class BureauJurorCjsQueries {

    private BureauJurorCjsQueries() {
        throw new IllegalStateException("BureauJurorCjsQueries should not be instantiated.");
    }

    public static BooleanExpression byJurorNumberAndEmployer(final String jurorNumber, final String employer) {
        final QBureauJurorCJS bureauJurorCJS = QBureauJurorCJS.bureauJurorCJS;
        return bureauJurorCJS.jurorNumber.eq(jurorNumber).and(bureauJurorCJS.employer.eq(employer));
    }
}
