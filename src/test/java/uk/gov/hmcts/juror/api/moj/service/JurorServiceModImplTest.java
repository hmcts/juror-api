package uk.gov.hmcts.juror.api.moj.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class JurorServiceModImplTest {

    @Mock
    private JurorRepository jurorRepository;


    @InjectMocks
    private JurorServiceModImpl jurorService;

    @Test
    void positiveGetJurorFromJurorNumber() {
        Juror juror = mock(Juror.class);
        when(jurorRepository.findById(TestConstants.VALID_JUROR_NUMBER))
            .thenReturn(Optional.of(juror));
        assertThat(jurorService.getJurorFromJurorNumber(TestConstants.VALID_JUROR_NUMBER))
            .isEqualTo(juror);
        verify(jurorRepository, times(1))
            .findById(TestConstants.VALID_JUROR_NUMBER);
    }

    @Test
    void negativeGetJurorFromJurorNumberNotFound() {
        when(jurorRepository.findById(TestConstants.VALID_JUROR_NUMBER))
            .thenReturn(Optional.empty());

        MojException.NotFound exception =
            assertThrows(
                MojException.NotFound.class,
                () -> jurorService.getJurorFromJurorNumber(TestConstants.VALID_JUROR_NUMBER),
                "Should throw an error when juror is not found"
            );

        assertThat(exception).isNotNull();
        assertThat(exception.getMessage())
            .isEqualTo("Juror not found: " + TestConstants.VALID_JUROR_NUMBER);
        assertThat(exception.getCause()).isNull();

        verify(jurorRepository, times(1))
            .findById(TestConstants.VALID_JUROR_NUMBER);

    }
}
