package uk.gov.hmcts.juror.api.moj.enumeration;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.moj.enumeration.jurormanagement.JurorStatusEnum;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class JurorStatusEnumTest {

    @Test
    @SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
    void confirmJurorStatusEnumValues() {
        assertEquals(JurorStatusEnum.POOL, 0);
        assertEquals(JurorStatusEnum.SUMMONED, 1);
        assertEquals(JurorStatusEnum.RESPONDED, 2);
        assertEquals(JurorStatusEnum.PANEL, 3);
        assertEquals(JurorStatusEnum.JUROR, 4);
        assertEquals(JurorStatusEnum.EXCUSED, 5);
        assertEquals(JurorStatusEnum.DISQUALIFIED, 6);
        assertEquals(JurorStatusEnum.DEFERRED, 7);
        assertEquals(JurorStatusEnum.REASSIGNED, 8);
        assertEquals(JurorStatusEnum.UNDELIVERABLE, 9);
        assertEquals(JurorStatusEnum.TRANSFERRED, 10);
        assertEquals(JurorStatusEnum.AWAITINGINFO, 11);
        assertEquals(JurorStatusEnum.FAILEDTOATTEND, 12);
        assertEquals(JurorStatusEnum.COMPLETED, 13);

    }

    private void assertEquals(JurorStatusEnum status, int id) {
        assertThat(status.getStatus())
            .isEqualTo(id);
    }
}
