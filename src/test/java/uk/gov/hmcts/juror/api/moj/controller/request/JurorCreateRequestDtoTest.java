package uk.gov.hmcts.juror.api.moj.controller.request;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.moj.AbstractValidatorTest;
import uk.gov.hmcts.juror.api.validation.ValidationConstants;

import java.time.LocalDate;
import java.util.function.BiConsumer;

public class JurorCreateRequestDtoTest extends AbstractValidatorTest {


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
        expectNoViolations(dto);
    }


    class AbstractJurorCreateRequestDtoTest extends AbstractValidationFieldTestString<JurorCreateRequestDto> {

        private final BiConsumer<JurorCreateRequestDto, String> setFieldConsumer;

        protected AbstractJurorCreateRequestDtoTest(String fieldName,
                                                    BiConsumer<JurorCreateRequestDto, String> setFieldConsumer) {
            super(fieldName);
            this.setFieldConsumer = setFieldConsumer;
        }

        @Override
        protected void setField(JurorCreateRequestDto baseObject, String value) {
            setFieldConsumer.accept(baseObject, value);
        }

        @Override
        protected JurorCreateRequestDto createValidObject() {
            return createValidJurorCreateRequestDto();
        }
    }

    @Nested
    class Title extends AbstractJurorCreateRequestDtoTest {
        protected Title() {
            super("title", JurorCreateRequestDto::setTitle);
            addAllowBlankTest("ABC");
            addMaxLengthTest(10, null);
            addContainsPipesTest(null);
        }
    }

    @Nested
    class FirstName extends AbstractJurorCreateRequestDtoTest {
        protected FirstName() {
            super("firstName", JurorCreateRequestDto::setFirstName);
            addNotBlankTest(null);
            addMaxLengthTest(20, null);
            addContainsPipesTest(null);
        }
    }

    @Nested
    class LastName extends AbstractJurorCreateRequestDtoTest {
        protected LastName() {
            super("lastName", JurorCreateRequestDto::setLastName);
            addNotBlankTest(null);
            addMaxLengthTest(20, null);
            addContainsPipesTest(null);
        }
    }

    @Nested
    class Address extends AbstractValidationFieldTestBase<JurorCreateRequestDto, JurorAddressDto> {
        protected Address() {
            super("address");
            addRequiredTest(null);
        }

        @Override
        protected void setField(JurorCreateRequestDto baseObject, JurorAddressDto value) {
            baseObject.setAddress(value);
        }

        @Override
        protected JurorCreateRequestDto createValidObject() {
            return createValidJurorCreateRequestDto();
        }
    }

    @Nested
    class DateOfBirth extends AbstractValidationFieldTestLocalDate<JurorCreateRequestDto> {
        protected DateOfBirth() {
            super("dateOfBirth");
            addRequiredTest(null);
            addDateRangeTest(LocalDate.now().minusYears(125), LocalDate.now().minusDays(1), new FieldTestSupport(
                "{uk.gov.hmcts.juror.api.validation.LocalDateOfBirth.message}"));
        }


        @Override
        protected void setField(JurorCreateRequestDto baseObject, LocalDate value) {
            baseObject.setDateOfBirth(value);
        }

        @Override
        protected JurorCreateRequestDto createValidObject() {
            return createValidJurorCreateRequestDto();
        }

    }

    @Nested
    class PrimaryPhone extends AbstractJurorCreateRequestDtoTest {
        protected PrimaryPhone() {
            super("primaryPhone", JurorCreateRequestDto::setPrimaryPhone);
            addNotRequiredTest("012345678");
            addInvalidPatternTest("INVALID", ValidationConstants.PHONE_NO_REGEX, null);
        }
    }

    @Nested
    class AlternativePhone extends AbstractJurorCreateRequestDtoTest {
        protected AlternativePhone() {
            super("alternativePhone", JurorCreateRequestDto::setAlternativePhone);
            addNotRequiredTest("012345678");
            addInvalidPatternTest("INVALID", ValidationConstants.PHONE_NO_REGEX, null);
        }
    }

    @Nested
    class EmailAddress extends AbstractJurorCreateRequestDtoTest {
        protected EmailAddress() {
            super("emailAddress", JurorCreateRequestDto::setEmailAddress);
            addAllowBlankTest("test@email.com");
            addMaxLengthTest(RandomStringUtils.randomAlphabetic(245) + "@email.com", 254, null);
            addInvalidPatternTest("INVALID", ValidationConstants.EMAIL_ADDRESS_REGEX, null);
        }
    }

    @Nested
    class Notes extends AbstractJurorCreateRequestDtoTest {
        protected Notes() {
            super("notes", JurorCreateRequestDto::setNotes);
            addAllowBlankTest("ABC");
            addMaxLengthTest(2000, null);
        }
    }

    @Nested
    class PoolNumber extends AbstractJurorCreateRequestDtoTest {
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

            JurorCreateRequestDtoTest.this.expectViolations(jurorCreateRequestDto, new Violation(
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

            JurorCreateRequestDtoTest.this.expectViolations(jurorCreateRequestDto, new Violation(
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

            JurorCreateRequestDtoTest.this.expectViolations(jurorCreateRequestDto, new Violation(
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

            JurorCreateRequestDtoTest.this.expectViolations(jurorCreateRequestDto, new Violation(
                "poolNumber",
                "Field poolNumber should be excluded if any of the following fields are present: [poolType, startDate]"
            ));
        }
    }

    @Nested
    class StartDate extends AbstractValidationFieldTestLocalDate<JurorCreateRequestDto> {
        protected StartDate() {
            super("startDate");
            addNotRequiredTest(LocalDate.now());
        }

        @Override
        protected void setField(JurorCreateRequestDto baseObject, LocalDate value) {
            baseObject.setStartDate(value);
        }

        @Override
        protected JurorCreateRequestDto createValidObject() {
            return createValidJurorCreateRequestDto();
        }


        @Test
        void negativeExcludeIfHasPoolNumber() {
            JurorCreateRequestDto jurorCreateRequestDto = createValidJurorCreateRequestDto();
            jurorCreateRequestDto.setPoolNumber(null);
            jurorCreateRequestDto.setStartDate(null);
            jurorCreateRequestDto.setPoolType("ABC");

            JurorCreateRequestDtoTest.this.expectViolations(jurorCreateRequestDto, new Violation(
                "startDate",
                "Field startDate is required if none of the following fields are present: [poolNumber]"
            ));
        }
    }

    @Nested
    class PoolType extends AbstractJurorCreateRequestDtoTest {
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

            JurorCreateRequestDtoTest.this.expectViolations(jurorCreateRequestDto, new Violation(
                "poolType",
                "Field poolType is required if none of the following fields are present: [poolNumber]"
            ));
        }
    }

    @Nested
    class LocationCode extends AbstractJurorCreateRequestDtoTest {
        protected LocationCode() {
            super("locationCode", JurorCreateRequestDto::setLocationCode);
            addNotRequiredTest("400");
            addLengthTest("40", "4000", 3, 3, null);
        }
    }
}
