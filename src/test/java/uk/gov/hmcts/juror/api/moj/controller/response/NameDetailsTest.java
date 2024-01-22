package uk.gov.hmcts.juror.api.moj.controller.response;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.moj.AbstractValidatorTest;
import uk.gov.hmcts.juror.api.moj.domain.Juror;

import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class NameDetailsTest extends AbstractValidatorTest {


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

    class AbstractNameDetailsTest extends AbstractValidatorTest.AbstractValidationFieldTestString<NameDetails> {

        private final BiConsumer<NameDetails, String> setFieldConsumer;

        protected AbstractNameDetailsTest(String fieldName, BiConsumer<NameDetails, String> setFieldConsumer) {
            super(fieldName);
            this.setFieldConsumer = setFieldConsumer;
        }

        @Override
        protected void setField(NameDetails baseObject, String value) {
            setFieldConsumer.accept(baseObject, value);
        }

        @Override
        protected NameDetails createValidObject() {
            return createValidNameDetailsDto();
        }

    }

    @Nested
    class Title extends AbstractNameDetailsTest {
        protected Title() {
            super("title", NameDetails::setTitle);
            addLengthTest(0, 10, null);
            addContainsPipesTest(null);
        }
    }

    @Nested
    class FirstName extends AbstractNameDetailsTest {
        protected FirstName() {
            super("firstName", NameDetails::setFirstName);
            addLengthTest(0, 20, null);
            addContainsPipesTest(null);
            addNotBlankTest(null);
        }
    }

    @Nested
    class LastName extends AbstractNameDetailsTest {
        protected LastName() {
            super("lastName", NameDetails::setLastName);
            addLengthTest(0, 20, null);
            addContainsPipesTest(null);
            addNotBlankTest(null);
        }
    }
}

