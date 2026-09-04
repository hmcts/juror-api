package uk.gov.hmcts.juror.api.moj.utils.converters;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.moj.exception.MojException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConversionUtilsTest {

    @Test
    void toProperCaseEmptyStringThrowsInternalServerError() {
        assertThatThrownBy(() -> ConversionUtils.toProperCase(""))
            .isInstanceOf(MojException.InternalServerError.class);
    }
}
