package uk.gov.hmcts.juror.api.moj.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorBankDetailsDto;
import uk.gov.hmcts.juror.api.moj.domain.Juror;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
class JurorBankDetailsDtoTest {

    @Test
    void positiveConstructorTest() {
        Juror juror = new Juror();
        juror.setJurorNumber(TestConstants.VALID_JUROR_NUMBER);
        juror.setBankAccountNumber("12345678");
        juror.setSortCode("115578");
        juror.setAddressLine1("Address Line 1");
        juror.setAddressLine2("Address Line 2");
        juror.setAddressLine3("Address Line 3");
        juror.setAddressLine4("Address Line 4");
        juror.setAddressLine5("Address Line 5");
        juror.setPostcode("M24 4BP");
        juror.setNotes("Some notes");

        JurorBankDetailsDto jurorBankDetailsDto = new JurorBankDetailsDto(juror);

        assertThat(jurorBankDetailsDto.getBankAccountNumber()).isEqualTo("12345678");
        assertThat(jurorBankDetailsDto.getSortCode()).isEqualTo("115578");
        assertThat(jurorBankDetailsDto.getAddressLineOne()).isEqualTo("Address Line 1");
        assertThat(jurorBankDetailsDto.getAddressLineTwo()).isEqualTo("Address Line 2");
        assertThat(jurorBankDetailsDto.getAddressLineThree()).isEqualTo("Address Line 3");
        assertThat(jurorBankDetailsDto.getAddressLineFour()).isEqualTo("Address Line 4");
        assertThat(jurorBankDetailsDto.getAddressLineFive()).isEqualTo("Address Line 5");
        assertThat(jurorBankDetailsDto.getPostCode()).isEqualTo("M24 4BP");
        assertThat(jurorBankDetailsDto.getNotes()).isEqualTo("Some notes");

    }

}