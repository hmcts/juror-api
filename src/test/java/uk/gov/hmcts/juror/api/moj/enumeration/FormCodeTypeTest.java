package uk.gov.hmcts.juror.api.moj.enumeration;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.moj.domain.FormCode;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SuppressWarnings({
    "PMD.TooManyMethods",
    "PMD.JUnitAssertionsShouldIncludeMessage"
})
class FormCodeTypeTest {

    @Test
    void englishExcusal() {
        assertEquals(FormCode.ENG_EXCUSAL, "5225", List.of(IJurorStatus.EXCUSED));
    }

    @Test
    void welshExcusal() {
        assertEquals(FormCode.BI_EXCUSAL, "5225C", List.of(IJurorStatus.EXCUSED));
    }

    @Test
    void englishExcusalDenied() {
        assertEquals(FormCode.ENG_EXCUSALDENIED, "5226", List.of(IJurorStatus.SUMMONED, IJurorStatus.RESPONDED));
    }

    @Test
    void welshExcusalDenied() {
        assertEquals(FormCode.BI_EXCUSALDENIED, "5226C", List.of(IJurorStatus.SUMMONED, IJurorStatus.RESPONDED));
    }

    @Test
    void englishSummons() {
        assertEquals(FormCode.ENG_SUMMONS, "5221", List.of(IJurorStatus.SUMMONED));
    }

    @Test
    void welshSummons() {
        assertEquals(FormCode.BI_SUMMONS, "5221C", List.of(IJurorStatus.SUMMONED));
    }

    @Test
    void englishConfirmation() {
        assertEquals(FormCode.ENG_CONFIRMATION, "5224A", List.of(IJurorStatus.RESPONDED));
    }

    @Test
    void welshConfirmation() {
        assertEquals(FormCode.BI_CONFIRMATION, "5224AC", List.of(IJurorStatus.RESPONDED));
    }

    @Test
    void englishDeferral() {
        assertEquals(FormCode.ENG_DEFERRAL, "5229A", List.of(IJurorStatus.DEFERRED));
    }

    @Test
    void welshDeferral() {
        assertEquals(FormCode.BI_DEFERRAL, "5229AC", List.of(IJurorStatus.DEFERRED));
    }

    @Test
    void englishDeferralDenied() {
        assertEquals(FormCode.ENG_DEFERRALDENIED, "5226A", List.of(IJurorStatus.RESPONDED));
    }

    @Test
    void welshDeferralDenied() {
        assertEquals(FormCode.BI_DEFERRALDENIED, "5226AC", List.of(IJurorStatus.RESPONDED));
    }

    @Test
    void englishPostpone() {
        assertEquals(FormCode.ENG_POSTPONE, "5229", List.of(IJurorStatus.DEFERRED));
    }

    @Test
    void welshPostpone() {
        assertEquals(FormCode.BI_POSTPONE, "5229C", List.of(IJurorStatus.DEFERRED));
    }

    @Test
    void englishRequestInfo() {
        assertEquals(FormCode.ENG_REQUESTINFO, "5227", List.of(IJurorStatus.SUMMONED));
    }

    @Test
    void welshRequestInfo() {
        assertEquals(FormCode.BI_REQUESTINFO, "5227C", List.of(IJurorStatus.SUMMONED));
    }

    @Test
    void englishWithdrawal() {
        assertEquals(FormCode.ENG_WITHDRAWAL, "5224", List.of(IJurorStatus.DISQUALIFIED));
    }

    @Test
    void welshWithdrawal() {
        assertEquals(FormCode.BI_WITHDRAWAL, "5224C", List.of(IJurorStatus.DISQUALIFIED));
    }

    private void assertEquals(FormCode type, String code, List<Integer> jurorStatus) {
        assertThat(type.getCode())
            .isEqualTo(code);
        assertThat(type.getJurorStatus()).isEqualTo(jurorStatus);
    }
}
