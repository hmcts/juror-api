package uk.gov.hmcts.juror.api.moj.controller.request;

import org.junit.jupiter.api.Nested;
import uk.gov.hmcts.juror.api.moj.AbstractValidatorTest;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.JurorDetailRequestDto;
import uk.gov.hmcts.juror.api.moj.enumeration.trial.PanelResult;

class JurorDetailRequestDtoTest extends AbstractValidatorTest<JurorDetailRequestDto> {


    @Override
    protected JurorDetailRequestDto createValidObject() {
        JurorDetailRequestDto detailDto = new JurorDetailRequestDto();
        detailDto.setFirstName("FNAME");
        detailDto.setLastName("LNAME");
        detailDto.setJurorNumber("415000001");
        detailDto.setResult(PanelResult.JUROR);
        return detailDto;
    }


    @Nested
    class FirstName extends AbstractValidationFieldTestString {
        protected FirstName() {
            super("firstName", JurorDetailRequestDto::setFirstName);
            addNotBlankTest(null);
            addMaxLengthTest(20, null);
            addContainsPipesTest(null);
        }
    }

    @Nested
    class LastName extends AbstractValidationFieldTestString {
        protected LastName() {
            super("lastName", JurorDetailRequestDto::setLastName);
            addNotBlankTest(null);
            addMaxLengthTest(25, null);
            addContainsPipesTest(null);
        }
    }

}
