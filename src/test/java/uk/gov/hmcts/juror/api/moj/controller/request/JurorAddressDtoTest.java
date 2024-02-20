package uk.gov.hmcts.juror.api.moj.controller.request;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.moj.AbstractValidatorTest;
import uk.gov.hmcts.juror.api.validation.ValidationConstants;

@SuppressWarnings({
    "PMD.JUnitTestsShouldIncludeAssert",//False positive done via inheritance
    "PMD.JUnit5TestShouldBePackagePrivate"
}
)
public class JurorAddressDtoTest extends AbstractValidatorTest<JurorAddressDto> {


    @SuppressWarnings("PMD.UseObjectForClearerAPI")
    public static JurorAddressDto createJurorAddressDto(String addressLineOne,
                                                        String addressLineTwo,
                                                        String addressLineThree,
                                                        String addressCountry,
                                                        String addressTown,
                                                        String postcode
    ) {
        JurorAddressDto jurorAddressDto = new JurorAddressDto();
        jurorAddressDto.setLineOne(addressLineOne);
        jurorAddressDto.setLineTwo(addressLineTwo);
        jurorAddressDto.setLineThree(addressLineThree);
        jurorAddressDto.setCounty(addressCountry);
        jurorAddressDto.setTown(addressTown);
        jurorAddressDto.setPostcode(postcode);
        return jurorAddressDto;
    }

    public static JurorAddressDto createValidJurorAddressDto() {
        return createJurorAddressDto(
            "line one", "line two", "line three", "town", "county", "SY2 6LU"
        );
    }

    @Override
    public JurorAddressDto createValidObject() {
        return createValidJurorAddressDto();
    }

    @Test
    void positiveTypical() {
        JurorAddressDto dto = createValidJurorAddressDto();
        assertExpectNoViolations(dto);
    }


    @Nested
    class LineOne extends AbstractValidationFieldTestString {
        protected LineOne() {
            super("lineOne", JurorAddressDto::setLineOne);
            addNotBlankTest(new FieldTestSupport().setMessage("Address line 1 cannot be blank"));
            addMaxLengthTest(35, null);
            addContainsPipesTest(null);
        }
    }

    @Nested
    class LineTwo extends AbstractValidationFieldTestString {
        protected LineTwo() {
            super("lineTwo", JurorAddressDto::setLineTwo);
            addAllowBlankTest("ABC");
            addMaxLengthTest(35, null);
            addContainsPipesTest(null);
        }
    }

    @Nested
    class LineThree extends AbstractValidationFieldTestString {
        protected LineThree() {
            super("lineThree", JurorAddressDto::setLineThree);
            addAllowBlankTest("A");
            addMaxLengthTest(35, null);
            addContainsPipesTest(null);
        }
    }

    @Nested
    class TownTest extends AbstractValidationFieldTestString {
        protected TownTest() {
            super("town", JurorAddressDto::setTown);
            addNotBlankTest(new FieldTestSupport().setMessage("Address town/city cannot be blank"));
            addMaxLengthTest(35, null);
            addContainsPipesTest(null);
        }
    }

    @Nested
    class County extends AbstractValidationFieldTestString {
        protected County() {
            super("county", JurorAddressDto::setCounty);
            addAllowBlankTest("ABC");
            addMaxLengthTest(35, null);
            addContainsPipesTest(null);
        }
    }

    @Nested
    class Postcode extends AbstractValidationFieldTestString {
        protected Postcode() {
            super("postcode", JurorAddressDto::setPostcode);
            addNotBlankTest(null);
            addMaxLengthTest("GIr   0AA", 8, null);
            addInvalidPatternTest("A B C D", ValidationConstants.POSTCODE_REGEX, null);
        }
    }
}
