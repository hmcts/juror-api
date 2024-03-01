package uk.gov.hmcts.juror.api.moj.dto;

import org.junit.jupiter.api.Nested;
import uk.gov.hmcts.juror.api.moj.AbstractValidatorTest;
import uk.gov.hmcts.juror.api.moj.domain.administration.JudgeUpdateDto;

public class JudgeUpdateDtoTest extends AbstractValidatorTest<JudgeUpdateDto> {
    @Override
    protected JudgeUpdateDto createValidObject() {
        return JudgeUpdateDto.builder()
            .judgeCode("CDE")
            .judgeName("Judge Name")
            .isActive(true)
            .build();
    }

    @Nested
    class JudgeCodeTest extends AbstractValidationFieldTestString {
        protected JudgeCodeTest() {
            super("judgeCode", JudgeUpdateDto::setJudgeCode);
            ignoreAdditionalFailures();
            addNotBlankTest(null);
            addLengthTest(1, 4, null);
        }
    }

    @Nested
    class JudgeNameTest extends AbstractValidationFieldTestString {
        protected JudgeNameTest() {
            super("judgeName", JudgeUpdateDto::setJudgeName);
            ignoreAdditionalFailures();
            addNotBlankTest(null);
            addLengthTest(1, 30, null);
        }
    }

    @Nested
    class IsActiveTest extends AbstractValidationFieldTestBoolean {
        protected IsActiveTest() {
            super("isActive", JudgeUpdateDto::setIsActive);
            addRequiredTest(null);
        }
    }
}
