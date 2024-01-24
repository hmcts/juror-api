package uk.gov.hmcts.juror.api.moj.controller.request;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

@SuppressWarnings(
    "PMD.JUnitTestsShouldIncludeAssert"//False positive done via inheritance
)
class CompleteServiceJurorNumberListDtoTest extends JurorNumberListDtoTest {

    @Override
    protected JurorNumberListDto createJurorNumberListDto(List<String> jurorNumbers) {
        return createCompleteServiceJurorNumberListDto(LocalDate.now(), jurorNumbers);
    }


    protected JurorNumberListDto createCompleteServiceJurorNumberListDto(LocalDate completionDate,
                                                                         List<String> jurorNumbers) {
        CompleteServiceJurorNumberListDto jurorNumberListDto = new CompleteServiceJurorNumberListDto();
        jurorNumberListDto.setJurorNumbers(jurorNumbers);
        jurorNumberListDto.setCompletionDate(completionDate);
        return jurorNumberListDto;
    }

    @Test
    void negativeCompletionDateNull() {
        JurorNumberListDto jurorNumberListDto = createCompleteServiceJurorNumberListDto(null,List.of("123456789"));
        expectViolations(jurorNumberListDto,
            new Violation("completionDate", "must not be null")
        );
    }
}
