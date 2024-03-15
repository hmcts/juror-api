package uk.gov.hmcts.juror.api.bureau.domain;

import com.querydsl.core.types.dsl.BooleanExpression;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.QJurorReasonableAdjustment;

public abstract class ReasonableAdjustmentQueries {

    private ReasonableAdjustmentQueries() {
        throw new IllegalStateException("ReasonableAdjustmentQueries should not be instantiated.");
    }

    public static BooleanExpression byJurorNumberAndCode(final String jurorNumber, final String code) {
        final QJurorReasonableAdjustment reasonableAdjustment = QJurorReasonableAdjustment.jurorReasonableAdjustment;
        return reasonableAdjustment.jurorNumber.eq(jurorNumber)
            .and(reasonableAdjustment.reasonableAdjustment.code.eq(code));
    }
}
