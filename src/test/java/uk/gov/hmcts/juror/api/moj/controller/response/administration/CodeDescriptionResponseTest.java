package uk.gov.hmcts.juror.api.moj.controller.response.administration;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.moj.domain.system.HasActive;
import uk.gov.hmcts.juror.api.moj.domain.system.HasCodeAndDescription;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@SuppressWarnings("unchecked")
class CodeDescriptionResponseTest {
    @Test
    void positiveConstructorNotHasActive() {
        HasCodeAndDescription<String> testClass = mock(HasCodeAndDescription.class);
        when(testClass.getCode()).thenReturn("SOME_CODE");
        when(testClass.getDescription()).thenReturn("SOME_DESCRIPTION");

        CodeDescriptionResponse codeDescriptionResponse = new CodeDescriptionResponse(testClass);
        assertThat(codeDescriptionResponse.getCode()).isEqualTo("SOME_CODE");
        assertThat(codeDescriptionResponse.getDescription()).isEqualTo("SOME_DESCRIPTION");
        assertThat(codeDescriptionResponse.getIsActive()).isNull();
    }

    @Test
    void positiveConstructorWithHasActive() {
        HasCodeAndDescriptionAndActiveTest testClass = mock(HasCodeAndDescriptionAndActiveTest.class);
        when(testClass.getCode()).thenReturn("SOME_CODE");
        when(testClass.getDescription()).thenReturn("SOME_DESCRIPTION");
        when(testClass.getActive()).thenReturn(true);

        CodeDescriptionResponse codeDescriptionResponse = new CodeDescriptionResponse(testClass);
        assertThat(codeDescriptionResponse.getCode()).isEqualTo("SOME_CODE");
        assertThat(codeDescriptionResponse.getDescription()).isEqualTo("SOME_DESCRIPTION");
        assertThat(codeDescriptionResponse.getIsActive()).isTrue();


    }

    interface HasCodeAndDescriptionAndActiveTest extends HasCodeAndDescription<String>, HasActive {

    }
}
