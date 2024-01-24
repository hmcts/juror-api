package uk.gov.hmcts.juror.api.moj.controller.request;

import org.junit.jupiter.api.Nested;
import uk.gov.hmcts.juror.api.moj.AbstractValidatorTest;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.JurorDetailRequestDto;
import uk.gov.hmcts.juror.api.moj.enumeration.trial.PanelResult;

import java.util.function.BiConsumer;

public class JurorDetailRequestDtoTest extends AbstractValidatorTest {
    JurorDetailRequestDto createDto() {
        JurorDetailRequestDto detailDto = new JurorDetailRequestDto();
        detailDto.setFirstName("FNAME");
        detailDto.setLastName("LNAME");
        detailDto.setJurorNumber("415000001");
        detailDto.setResult(PanelResult.JUROR);
        return detailDto;
    }

    class AbstractEmpanelDetailRequestDtoTest extends AbstractValidationFieldTestString<JurorDetailRequestDto> {

        private final BiConsumer<JurorDetailRequestDto, String> setFieldConsumer;

        protected AbstractEmpanelDetailRequestDtoTest(String fieldName,
                                                      BiConsumer<JurorDetailRequestDto, String> setFieldConsumer) {
            super(fieldName);
            this.setFieldConsumer = setFieldConsumer;
        }

        @Override
        protected void setField(JurorDetailRequestDto baseObject, String value) {
            setFieldConsumer.accept(baseObject, value);
        }

        @Override
        protected JurorDetailRequestDto createValidObject() {
            return createDto();
        }
    }

    @Nested
    class FirstName extends AbstractEmpanelDetailRequestDtoTest {
        protected FirstName() {
            super("firstName", JurorDetailRequestDto::setFirstName);
            addNotBlankTest(null);
            addMaxLengthTest(20, null);
            addContainsPipesTest(null);
        }
    }

    @Nested
    class LastName extends AbstractEmpanelDetailRequestDtoTest {
        protected LastName() {
            super("lastName", JurorDetailRequestDto::setLastName);
            addNotBlankTest(null);
            addMaxLengthTest(20, null);
            addContainsPipesTest(null);
        }
    }

}
