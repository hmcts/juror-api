package uk.gov.hmcts.juror.api.moj.domain;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.QReasonableAdjustments;
import uk.gov.hmcts.juror.api.moj.enumeration.trial.TrialType;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class CodeTypeTest {


    @Test
    void positiveConstructorDisqualified() {
        CodeType codeType = CodeType.DISQUALIFIED;
        assertThat(codeType.getEntityPathBase())
            .isEqualTo(QDisqualifiedCode.disqualifiedCode1);
        assertThat(codeType.getValues()).isEmpty();
    }

    @Test
    void positiveConstructorJurorStatus() {
        CodeType codeType = CodeType.JUROR_STATUS;
        assertThat(codeType.getEntityPathBase())
            .isEqualTo(QJurorStatus.jurorStatus);
        assertThat(codeType.getValues()).isEmpty();
    }

    @Test
    void positiveConstructorTrialType() {
        CodeType codeType = CodeType.TRIAL_TYPE;
        assertThat(codeType.getEntityPathBase()).isNull();
        assertThat(codeType.getValues()).isEqualTo(TrialType.values());
    }

    @Test
    void positiveConstructorIdCheck() {
        CodeType codeType = CodeType.ID_CHECK;
        assertThat(codeType.getEntityPathBase())
            .isEqualTo(QIdCheckCode.idCheckCode);
        assertThat(codeType.getValues()).isEmpty();
    }

    @Test
    void positiveConstructorExcusalAndDeferral() {
        CodeType codeType = CodeType.EXCUSAL_AND_DEFERRAL;
        assertThat(codeType.getEntityPathBase())
            .isEqualTo(QExcusalCode.excusalCode);
        assertThat(codeType.getValues()).isEmpty();
    }

    @Test
    void positiveConstructorPhoneLog() {
        CodeType codeType = CodeType.PHONE_LOG;
        assertThat(codeType.getEntityPathBase())
            .isEqualTo(QContactCode.contactCode);
        assertThat(codeType.getValues()).isEmpty();
    }

    @Test
    void positiveConstructorReasonableAdjustments() {
        CodeType codeType = CodeType.REASONABLE_ADJUSTMENTS;
        assertThat(codeType.getEntityPathBase())
            .isEqualTo(QReasonableAdjustments.reasonableAdjustments);
        assertThat(codeType.getValues()).isEmpty();
    }

}
