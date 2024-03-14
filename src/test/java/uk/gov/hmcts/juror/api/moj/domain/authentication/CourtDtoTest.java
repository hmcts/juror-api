package uk.gov.hmcts.juror.api.moj.domain.authentication;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.AbstractValidatorTest;
import uk.gov.hmcts.juror.api.moj.enumeration.CourtType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CourtDtoTest extends AbstractValidatorTest<CourtDto> {

    @Override
    protected CourtDto createValidObject() {
        return getValidObject();
    }

    public static CourtDto getValidObject() {
        return CourtDto.builder()
            .name("courtName")
            .locCode("123")
            .courtType(CourtType.MAIN)
            .build();
    }

    @Nested
    class NameTest extends AbstractValidationFieldTestString {
        protected NameTest() {
            super("name", CourtDto::setName);
            addLengthTest(0, 40, null);
            addNotBlankTest(null);
        }
    }

    @Nested
    class LocCodeTest extends AbstractValidationFieldTestString {
        protected LocCodeTest() {
            super("locCode", CourtDto::setLocCode);
            ignoreAdditionalFailures();
            addNotBlankTest(null);
            addInvalidPatternTest("invalid", "^\\d{3}$", null);
        }
    }

    @Nested
    class CourtTypeTest extends AbstractValidationFieldTestBase<CourtType> {
        protected CourtTypeTest() {
            super("courtType", CourtDto::setCourtType);
            addRequiredTest(null);
        }

    }

    @Test
    void positiveConstructorTest() {
        CourtLocation courtLocation = mock(CourtLocation.class);
        when(courtLocation.getLocCode()).thenReturn("123");
        when(courtLocation.getName()).thenReturn("courtName");
        when(courtLocation.getType()).thenReturn(CourtType.MAIN);

        assertThat(new CourtDto(courtLocation))
            .isEqualTo(CourtDto.builder()
                .locCode("123")
                .name("courtName")
                .courtType(CourtType.MAIN)
                .build());
    }
}
