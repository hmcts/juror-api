package uk.gov.hmcts.juror.api.moj.controller.request;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.moj.AbstractValidatorTest;

@SuppressWarnings({
    "PMD.JUnitTestsShouldIncludeAssert",//False positive done via inheritance
    "PMD.JUnit5TestShouldBePackagePrivate",
    "PMD.TooManyMethods"
}
)
public class RequestBankDetailsDtoTest extends AbstractValidatorTest<RequestBankDetailsDto> {


    @SuppressWarnings("PMD.UseObjectForClearerAPI")
    public static RequestBankDetailsDto createRequestBankDetailsDto(String jurorNumber, String accountNo,
                                                                    String sortCode, String accountName) {
        RequestBankDetailsDto bankDetailsDto = new RequestBankDetailsDto();
        bankDetailsDto.setAccountHolderName(accountName);
        bankDetailsDto.setAccountNumber(accountNo);
        bankDetailsDto.setSortCode(sortCode);
        bankDetailsDto.setJurorNumber(jurorNumber);
        return bankDetailsDto;
    }

    public static RequestBankDetailsDto createValidRequestBankDetailsDto() {
        return createRequestBankDetailsDto("123456789", "12345678", "112233",
            "Mr Fname Lname");
    }

    @Override
    public RequestBankDetailsDto createValidObject() {
        return createValidRequestBankDetailsDto();
    }

    @Test
    void positiveTypical() {
        RequestBankDetailsDto dto = createValidRequestBankDetailsDto();
        assertExpectNoViolations(dto);
    }

    @Test
    void negativeJurorNumberNull() {
        RequestBankDetailsDto dto = createRequestBankDetailsDto(null, "12345678",
            "112233", "Mr Fname Lname");
        assertExpectViolations(dto, new Violation("jurorNumber", "must not be null"));
    }

    @Test
    void negativeJurorNumberInvalid() {
        RequestBankDetailsDto dto = createRequestBankDetailsDto("1234567890", "12345678",
            "112233", "Mr Fname Lname");
        assertExpectViolations(dto, new Violation("jurorNumber", "must match \"^\\d{9}$\""));
    }

    @Test
    void negativeAccountNumberNull() {
        RequestBankDetailsDto dto = createRequestBankDetailsDto("123456789", null,
            "112233", "Mr Fname Lname");
        assertExpectViolations(dto, new Violation("accountNumber", "must not be null"));
    }

    @Test
    void negativeAccountNumberNullWrongLength() {
        RequestBankDetailsDto dto = createRequestBankDetailsDto("123456789", "123456789",
            "112233", "Mr Fname Lname");
        assertExpectViolations(dto, new Violation("accountNumber", "must match \"^\\d{8}$\""));
    }

    @Test
    void negativeAccountNameNull() {
        RequestBankDetailsDto dto = createRequestBankDetailsDto("123456789", "12345678", "112233", null);
        assertExpectViolations(dto, new Violation("accountHolderName", "must not be empty"));
    }

    @Test
    void negativeAccountNameWrongLength() {
        RequestBankDetailsDto dto = createRequestBankDetailsDto("123456789", "12345678",
            "112233", "Mr Fname Lname Too Long");
        assertExpectViolations(dto, new Violation("accountHolderName", "length must be between 0 and 18"));
    }

    @Test
    void negativeSortCodeNull() {
        RequestBankDetailsDto dto = createRequestBankDetailsDto("123456789", "12345678",
            null, "Mr Fname Lname");
        assertExpectViolations(dto, new Violation("sortCode", "must not be null"));
    }

    @Test
    void negativeSortCodeWrongLength() {
        RequestBankDetailsDto dto = createRequestBankDetailsDto("123456789", "12345678",
            "11223344", "Mr Fname Lname");
        assertExpectViolations(dto, new Violation("sortCode", "must match \"^\\d{6}$\""));
    }
}
