package uk.gov.hmcts.juror.api.moj.domain;

import com.querydsl.core.types.dsl.EntityPathBase;
import lombok.Getter;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.QReasonableAdjustments;
import uk.gov.hmcts.juror.api.moj.domain.system.HasCodeAndDescription;
import uk.gov.hmcts.juror.api.moj.enumeration.trial.TrialType;

@Getter
public enum CodeType {
    DISQUALIFIED(QDisqualifiedCode.disqualifiedCode1),
    JUROR_STATUS(QJurorStatus.jurorStatus),
    TRIAL_TYPE(TrialType.values()),
    ID_CHECK(QIdCheckCode.idCheckCode),
    EXCUSAL_AND_DEFERRAL(QExcusalCode.excusalCode),
    PHONE_LOG(QContactCode.contactCode),
    REASONABLE_ADJUSTMENTS(QReasonableAdjustments.reasonableAdjustments);

    final EntityPathBase<? extends HasCodeAndDescription<?>> entityPathBase;
    final HasCodeAndDescription<?>[] values;

    @SuppressWarnings("PMD.ArrayIsStoredDirectly")
    CodeType(
        EntityPathBase<? extends HasCodeAndDescription<?>> entityPathBase, HasCodeAndDescription<?>... values) {
        this.entityPathBase = entityPathBase;
        this.values = values;
    }

    CodeType(HasCodeAndDescription<?>... values) {
        this(null, values);
    }
}
