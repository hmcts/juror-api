package uk.gov.hmcts.juror.api.moj.controller.request;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.moj.AbstractValidatorTest;
import uk.gov.hmcts.juror.api.validation.ValidationConstants;

import java.time.LocalDate;

@SuppressWarnings("PMD.JUnit5TestShouldBePackagePrivate")
public class JurorCreateRequestDtoTest extends AbstractValidatorTest<JurorCreateRequestDto> {

    @Override
    protected JurorCreateRequestDto createValidObject() {
        return createValidJurorCreateRequestDto();
    }

    public static JurorCreateRequestDto createValidJurorCreateRequestExistingPoolDto() {
        JurorCreateRequestDto jurorCreateRequestDto = createValidJurorCreateRequestDto();
        jurorCreateRequestDto.setStartDate(null);
        jurorCreateRequestDto.setPoolType(null);
        jurorCreateRequestDto.setPoolNumber(TestConstants.VALID_POOL_NUMBER);
        return jurorCreateRequestDto;
    }

    public static JurorCreateRequestDto createValidJurorCreateRequestNewPoolDto() {
        JurorCreateRequestDto jurorCreateRequestDto = createValidJurorCreateRequestDto();
        jurorCreateRequestDto.setStartDate(LocalDate.now());
        jurorCreateRequestDto.setPoolType("CRO");
        jurorCreateRequestDto.setPoolNumber(null);
        return jurorCreateRequestDto;

    }

    public static JurorCreateRequestDto createValidJurorCreateRequestDto() {
        JurorCreateRequestDto jurorCreateRequestDto = new JurorCreateRequestDto();
        jurorCreateRequestDto.setTitle("title");
        jurorCreateRequestDto.setFirstName("abc");
        jurorCreateRequestDto.setLastName("def");
        jurorCreateRequestDto.setAddress(JurorAddressDtoTest.createValidJurorAddressDto());
        jurorCreateRequestDto.setDateOfBirth(LocalDate.now().minusYears(18));
        jurorCreateRequestDto.setPrimaryPhone("012345678");
        jurorCreateRequestDto.setAlternativePhone("0123456789");
        jurorCreateRequestDto.setEmailAddress("email@email.com");
        jurorCreateRequestDto.setNotes("Some notes");
        jurorCreateRequestDto.setStartDate(LocalDate.now());
        jurorCreateRequestDto.setPoolType("CRO");
        jurorCreateRequestDto.setLocationCode("415");
        return jurorCreateRequestDto;
    }

    @Test
    void positiveTypical() {
        JurorCreateRequestDto dto = createValidJurorCreateRequestDto();
        assertExpectNoViolations(dto);
    }


    @Nested
    class Title extends AbstractValidationFieldTestString {
        protected Title() {
            super("title", JurorCreateRequestDto::setTitle);
            addAllowBlankTest("ABC");
            addMaxLengthTest(10, null);
            addContainsPipesTest(null);
        }
    }

    @Nested
    class FirstName extends AbstractValidationFieldTestString {
        protected FirstName() {
            super("firstName", JurorCreateRequestDto::setFirstName);
            addNotBlankTest(null);
            addMaxLengthTest(20, null);
            addContainsPipesTest(null);
        }
    }

    @Nested
    class LastName extends AbstractValidationFieldTestString {
        protected LastName() {
            super("lastName", JurorCreateRequestDto::setLastName);
            addNotBlankTest(null);
            addMaxLengthTest(20, null);
            addContainsPipesTest(null);
        }
    }

    @Nested
    class Address extends AbstractValidationFieldTestBase<JurorAddressDto> {
        protected Address() {
            super("address", JurorCreateRequestDto::setAddress);
            addRequiredTest(null);
        }
    }

    @Nested
    class DateOfBirth extends AbstractValidationFieldTestLocalDate {
        protected DateOfBirth() {
            super("dateOfBirth", JurorCreateRequestDto::setDateOfBirth);
            addRequiredTest(null);
            addDateRangeTest(LocalDate.now().minusYears(125), LocalDate.now().minusDays(1),
                new FieldTestSupport().setMessage(
                    "{uk.gov.hmcts.juror.api.validation.LocalDateOfBirth.message}"));
        }
    }

    @Nested
    class PrimaryPhone extends AbstractValidationFieldTestString {
        protected PrimaryPhone() {
            super("primaryPhone", JurorCreateRequestDto::setPrimaryPhone);
            addNotRequiredTest("012345678");
            addInvalidPatternTest("INVALID", ValidationConstants.PHONE_NO_REGEX, null);
        }
    }

    @Nested
    class AlternativePhone extends AbstractValidationFieldTestString {
        protected AlternativePhone() {
            super("alternativePhone", JurorCreateRequestDto::setAlternativePhone);
            addNotRequiredTest("012345678");
            addInvalidPatternTest("INVALID", ValidationConstants.PHONE_NO_REGEX, null);
        }
    }

    @Nested
    class EmailAddress extends AbstractValidationFieldTestString {
        protected EmailAddress() {
            super("emailAddress", JurorCreateRequestDto::setEmailAddress);
            addAllowBlankTest("test@email.com");
            addMaxLengthTest(RandomStringUtils.randomAlphabetic(245) + "@email.com", 254, null);
            addInvalidPatternTest("INVALID", ValidationConstants.EMAIL_ADDRESS_REGEX, null);
        }
    }

    @Nested
    class Notes extends AbstractValidationFieldTestString {
        protected Notes() {
            super("notes", JurorCreateRequestDto::setNotes);
            addAllowBlankTest("ABC");
            addMaxLengthTest(2000, null);
        }
    }

    @Nested
    class PoolNumber extends AbstractValidationFieldTestString {
        protected PoolNumber() {
            super("poolNumber", JurorCreateRequestDto::setPoolNumber);
            addNotRequiredTest("123456789");
            addInvalidPatternTest("INVALID", ValidationConstants.POOL_NUMBER, null);
        }

        @Override
        protected JurorCreateRequestDto createValidObject() {
            JurorCreateRequestDto jurorCreateRequestDto = createValidJurorCreateRequestDto();
            jurorCreateRequestDto.setPoolNumber(TestConstants.VALID_POOL_NUMBER);
            jurorCreateRequestDto.setStartDate(null);
            jurorCreateRequestDto.setPoolType(null);
            return jurorCreateRequestDto;
        }

        @Test
        void negativeRequiredIfNoStartDateAndPoolType() {
            JurorCreateRequestDto jurorCreateRequestDto = createValidJurorCreateRequestDto();
            jurorCreateRequestDto.setStartDate(null);
            jurorCreateRequestDto.setPoolType(null);

            assertExpectViolations(jurorCreateRequestDto, new Violation(
                "poolNumber",
                "Field poolNumber is required if none of the following fields are present: [poolType, startDate]"
            ));
        }

        @Test
        void negativeExcludeIfHasStartDate() {
            JurorCreateRequestDto jurorCreateRequestDto = createValidJurorCreateRequestDto();
            jurorCreateRequestDto.setPoolNumber(TestConstants.VALID_POOL_NUMBER);
            jurorCreateRequestDto.setStartDate(LocalDate.now());
            jurorCreateRequestDto.setPoolType(null);

            assertExpectViolations(jurorCreateRequestDto, new Violation(
                "poolNumber",
                "Field poolNumber should be excluded if any of the following fields are present: [poolType, startDate]"
            ));
        }

        @Test
        void negativeExcludeIfHasPoolType() {
            JurorCreateRequestDto jurorCreateRequestDto = createValidJurorCreateRequestDto();
            jurorCreateRequestDto.setPoolNumber(TestConstants.VALID_POOL_NUMBER);
            jurorCreateRequestDto.setStartDate(null);
            jurorCreateRequestDto.setPoolType("ABC");

            assertExpectViolations(jurorCreateRequestDto, new Violation(
                "poolNumber",
                "Field poolNumber should be excluded if any of the following fields are present: [poolType, startDate]"
            ));
        }

        @Test
        void negativeExcludeIfHasPoolTypeAndStartDate() {
            JurorCreateRequestDto jurorCreateRequestDto = createValidJurorCreateRequestDto();
            jurorCreateRequestDto.setPoolNumber(TestConstants.VALID_POOL_NUMBER);
            jurorCreateRequestDto.setStartDate(LocalDate.now());
            jurorCreateRequestDto.setPoolType("ABC");

            assertExpectViolations(jurorCreateRequestDto, new Violation(
                "poolNumber",
                "Field poolNumber should be excluded if any of the following fields are present: [poolType, startDate]"
            ));
        }
    }

    @Nested
    class StartDate extends AbstractValidationFieldTestLocalDate {
        protected StartDate() {
            super("startDate", JurorCreateRequestDto::setStartDate);
            addNotRequiredTest(LocalDate.now());
        }

        @Test
        void negativeExcludeIfHasPoolNumber() {
            JurorCreateRequestDto jurorCreateRequestDto = createValidJurorCreateRequestDto();
            jurorCreateRequestDto.setPoolNumber(null);
            jurorCreateRequestDto.setStartDate(null);
            jurorCreateRequestDto.setPoolType("ABC");

            assertExpectViolations(jurorCreateRequestDto, new Violation(
                "startDate",
                "Field startDate is required if none of the following fields are present: [poolNumber]"
            ));
        }
    }

    @Nested
    class PoolType extends AbstractValidationFieldTestString {
        protected PoolType() {
            super("poolType", JurorCreateRequestDto::setPoolType);
            addNotRequiredTest("ABC");
            addLengthTest(3, 3, null);
        }

        @Test
        void negativeExcludeIfHasPoolNumber() {
            JurorCreateRequestDto jurorCreateRequestDto = createValidJurorCreateRequestDto();
            jurorCreateRequestDto.setPoolNumber(null);
            jurorCreateRequestDto.setStartDate(LocalDate.now());
            jurorCreateRequestDto.setPoolType(null);

            assertExpectViolations(jurorCreateRequestDto, new Violation(
                "poolType",
                "Field poolType is required if none of the following fields are present: [poolNumber]"
            ));
        }
    }

    @Nested
    class LocationCode extends AbstractValidationFieldTestString {
        protected LocationCode() {
            super("locationCode", JurorCreateRequestDto::setLocationCode);
            addNotRequiredTest("400");
            addLengthTest("40", "4000", 3, 3, null);
        }
    }
}
