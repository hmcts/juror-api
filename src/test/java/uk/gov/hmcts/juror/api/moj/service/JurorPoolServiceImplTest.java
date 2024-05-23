package uk.gov.hmcts.juror.api.moj.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class JurorPoolServiceImplTest {

    @Mock
    private PoolRequestRepository poolRequestRepository;
    @Mock
    private JurorPoolRepository jurorPoolRepository;


    @InjectMocks
    private JurorPoolServiceImpl jurorPoolService;

    @Test
    void positiveGetJurorFromJurorNumber() {
        PoolRequest poolRequest = mock(PoolRequest.class);
        when(poolRequestRepository.findByPoolNumber(TestConstants.VALID_POOL_NUMBER))
            .thenReturn(Optional.of(poolRequest));
        assertThat(jurorPoolService.getPoolRequest(TestConstants.VALID_POOL_NUMBER))
            .isEqualTo(poolRequest);
        verify(poolRequestRepository, times(1))
            .findByPoolNumber(TestConstants.VALID_POOL_NUMBER);
    }

    @Test
    void negativeGetJurorFromJurorNumberNotFound() {
        when(poolRequestRepository.findByPoolNumber(TestConstants.VALID_POOL_NUMBER))
            .thenReturn(Optional.empty());

        MojException.NotFound exception =
            assertThrows(
                MojException.NotFound.class,
                () -> jurorPoolService.getPoolRequest(TestConstants.VALID_POOL_NUMBER),
                "Should throw an error when pool is not found"
            );

        assertThat(exception).isNotNull();
        assertThat(exception.getMessage())
            .isEqualTo("Pool not found: " + TestConstants.VALID_POOL_NUMBER);
        assertThat(exception.getCause()).isNull();

        verify(poolRequestRepository, times(1))
            .findByPoolNumber(TestConstants.VALID_JUROR_NUMBER);
    }

    @Test
    void positiveHasPoolWithLocCodeTrue() {
        List<String> locCodes = List.of(TestConstants.VALID_COURT_LOCATION, "414");
        when(jurorPoolRepository.hasPoolWithLocCode(TestConstants.VALID_JUROR_NUMBER, locCodes))
            .thenReturn(true);

        assertThat(jurorPoolService.hasPoolWithLocCode(TestConstants.VALID_JUROR_NUMBER, locCodes)).isTrue();

        verify(jurorPoolRepository, times(1))
            .hasPoolWithLocCode(TestConstants.VALID_JUROR_NUMBER, locCodes);

    }

    @Test
    void positiveHasPoolWithLocCodeFalse() {
        List<String> locCodes = List.of(TestConstants.VALID_COURT_LOCATION, "414");
        when(jurorPoolRepository.hasPoolWithLocCode(TestConstants.VALID_JUROR_NUMBER, locCodes))
            .thenReturn(false);

        assertThat(jurorPoolService.hasPoolWithLocCode(TestConstants.VALID_JUROR_NUMBER, locCodes)).isFalse();

        verify(jurorPoolRepository, times(1))
            .hasPoolWithLocCode(TestConstants.VALID_JUROR_NUMBER, locCodes);

    }
}
