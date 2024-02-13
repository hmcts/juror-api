package uk.gov.hmcts.juror.api.moj.domain.messages;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.moj.exception.MojException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings({
    "PMD.LawOfDemeter",
    "PMD.TooManyMethods"
})
class DataTypeTest {


    private static final String STRING_INPUT = "My Value";

    @Test
    void positiveNoneGetFormat() {
        assertThat(DataType.NONE.getFormat())
            .isEqualTo("N/A");
    }

    @Test
    void positiveNoneConvertDataIsWelshTrue() {
        assertThat(DataType.NONE.convertData(STRING_INPUT, true))
            .isEqualTo(STRING_INPUT);
    }

    @Test
    void positiveNoneConvertDtaIsWelshFalse() {
        assertThat(DataType.NONE.convertData(STRING_INPUT, false))
            .isEqualTo(STRING_INPUT);
    }

    @Test
    void positiveStringGetFormat() {
        assertThat(DataType.STRING.getFormat())
            .isEqualTo("N/A");
    }

    @Test
    void positiveStringConvertDtaIsWelshTrue() {
        assertThat(DataType.STRING.convertData(STRING_INPUT, true))
            .isEqualTo(STRING_INPUT);
    }

    @Test
    void positiveStringConvertDtaIsWelshFalse() {
        assertThat(DataType.STRING.convertData(STRING_INPUT, false))
            .isEqualTo(STRING_INPUT);
    }


    @Test
    void positiveDateGetFormat() {
        assertThat(DataType.DATE.getFormat())
            .isEqualTo("yyyy-MM-dd");

    }

    @Test
    void positiveDateConvertDtaIsWelshTrue() {
        assertThat(DataType.DATE.convertData("2023-01-01", true))
            .isEqualTo("01/01/2023");
    }

    @Test
    void positiveDateConvertDtaIsWelshFalse() {
        assertThat(DataType.DATE.convertData("2023-01-01", false))
            .isEqualTo("01/01/2023");
    }

    @Test
    void positiveTimeGetFormat() {
        assertThat(DataType.TIME.getFormat())
            .isEqualTo("HH:mm");
    }

    @Test
    void positiveTimeAmWelshTrue() {
        assertThat(DataType.TIME.convertData("10:30", true))
            .isEqualTo("10:30");
    }

    @Test
    void positiveTimePmWelshTrue() {
        assertThat(DataType.TIME.convertData("16:37", true))
            .isEqualTo("16:37");
    }

    @Test
    void positiveTimeAmWelshFalse() {
        assertThat(DataType.TIME.convertData("10:30", false))
            .isEqualTo("10:30");
    }

    @Test
    void positiveTimePmWelshFalse() {
        assertThat(DataType.TIME.convertData("16:37", false))
            .isEqualTo("16:37");
    }


    @Test
    void negativeConvertDataBadFormat() {
        MojException.BusinessRuleViolation exception =
            assertThrows(MojException.BusinessRuleViolation.class,
                () -> DataType.DATE.convertData("INVALID", false),
                "Exception should be thrown when an invalid date format is provided");
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo("Invalid must be in format 'yyyy-MM-dd'");
        assertThat(exception.getCause()).isNotNull();
    }


    @Test
    void negativeBadTimeFormat() {
        MojException.BusinessRuleViolation exception =
            assertThrows(MojException.BusinessRuleViolation.class,
                () -> DataType.TIME.convertData("INVALID", false),
                "Exception should be thrown when an invalid time format is provided");
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo("Invalid must be in format 'HH:mm'");
        assertThat(exception.getCause()).isNotNull();
    }
}
