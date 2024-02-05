package uk.gov.hmcts.juror.api.moj.controller.response;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.moj.AbstractValidatorTest;
import uk.gov.hmcts.juror.api.moj.domain.Juror;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class NameDetailsTest extends AbstractValidatorTest<NameDetails> {


    @Override
    protected NameDetails createValidObject() {
        return createValidNameDetailsDto();
    }

    @Test
    void positiveFromTest() {
        Juror juror = mock(Juror.class);
        NameDetails expected = createValidNameDetailsDto();

        when(juror.getTitle()).thenReturn(expected.getTitle());
        when(juror.getFirstName()).thenReturn(expected.getFirstName());
        when(juror.getLastName()).thenReturn(expected.getLastName());

        assertEquals(expected,
            NameDetails.from(juror),
            "Name Details should match that from Juror record");

        verify(juror, times(1)).getTitle();
        verify(juror, times(1)).getFirstName();
        verify(juror, times(1)).getLastName();
        verifyNoMoreInteractions(juror);
    }

    public static NameDetails createValidNameDetailsDto() {
        return NameDetails.builder()
            .title("Mr")
            .firstName("FNAME")
            .lastName("FNAME")
            .build();
    }


    @Nested
    class Title extends AbstractValidationFieldTestString {
        protected Title() {
            super("title", NameDetails::setTitle);
            addLengthTest(0, 10, null);
            addContainsPipesTest(null);
        }
    }

    @Nested
    class FirstName extends AbstractValidationFieldTestString {
        protected FirstName() {
            super("firstName", NameDetails::setFirstName);
            addLengthTest(0, 20, null);
            addContainsPipesTest(null);
            addNotBlankTest(null);
        }
    }

    @Nested
    class LastName extends AbstractValidationFieldTestString {
        protected LastName() {
            super("lastName", NameDetails::setLastName);
            addLengthTest(0, 20, null);
            addContainsPipesTest(null);
            addNotBlankTest(null);
        }
    }
}

