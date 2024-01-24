package uk.gov.hmcts.juror.api.moj.domain;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
public class PoolHistoryTest {

    @Test
    public void test_constructor_allDataPresent() {
        String poolNumber = "415221201";
        HistoryCode historyCode = HistoryCode.PHSI;
        String userId = "BUREAU_USER";
        String otherInformation = "BUREAU_USER";

        PoolHistory poolHistory = new PoolHistory(poolNumber, LocalDateTime.now(), historyCode, userId,
            otherInformation);

        assertThat(poolHistory.getPoolNumber())
            .as("Embedded Pool History Id pool number value should be mapped from the pool number value"
                + " in the constructor")
            .isEqualTo(poolNumber);
        assertThat(poolHistory.getHistoryDate())
            .as("Embedded Pool History Id history date value should be defaulted (not passed in "
                + "the constructor)")
            .isNotNull();
        assertThat(poolHistory.getHistoryCode())
            .as("Embedded Pool History Id history code value should be mapped from the history code value "
                + "in the constructor")
            .isEqualTo(historyCode);
        assertThat(poolHistory.getUserId())
            .as("Embedded Pool History ID user ID value should be mapped from the user ID value in"
                + " the constructor")
            .isEqualTo(userId);
        assertThat(poolHistory.getOtherInformation())
            .as("Embedded Pool History Id other information value should be mapped from the other "
                + "information value in the constructor")
            .isEqualTo(otherInformation);
    }

}
