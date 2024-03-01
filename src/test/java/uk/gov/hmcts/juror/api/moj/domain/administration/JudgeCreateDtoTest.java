package uk.gov.hmcts.juror.api.moj.domain.administration;

import org.junit.jupiter.api.Nested;
import uk.gov.hmcts.juror.api.moj.AbstractValidatorTest;

public class JudgeCreateDtoTest extends AbstractValidatorTest<JudgeCreateDto> {
    @Override
    protected JudgeCreateDto createValidObject() {
        return JudgeCreateDto.builder()
            .judgeCode("CDE")
            .judgeName("Judge Name")
            .build();
    }

    @Nested
    class JudgeCodeTest extends AbstractValidationFieldTestString {
        protected JudgeCodeTest() {
            super("judgeCode", JudgeCreateDto::setJudgeCode);
            ignoreAdditionalFailures();
            addNotBlankTest(null);
            addLengthTest(1, 4, null);
        }
    }

    @Nested
    class JudgeNameTest extends AbstractValidationFieldTestString {
        protected JudgeNameTest() {
            super("judgeName", JudgeCreateDto::setJudgeName);
            ignoreAdditionalFailures();
            addNotBlankTest(null);
            addLengthTest(1, 30, null);
        }
    }
}