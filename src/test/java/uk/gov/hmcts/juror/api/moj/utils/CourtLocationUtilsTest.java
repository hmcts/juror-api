package uk.gov.hmcts.juror.api.moj.utils;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocationRepository;

import java.util.Optional;

@RunWith(SpringRunner.class)
public class CourtLocationUtilsTest {

    @Mock
    WelshCourtLocationRepository welshCourtLocationRepository;


    @Test
    public void test_isWelshCourtLocation_welshCourt() {
        Mockito.doReturn(Optional.of(new WelshCourtLocation())).when(welshCourtLocationRepository)
            .findById("457");

        Assertions.assertThat(CourtLocationUtils.isWelshCourtLocation(welshCourtLocationRepository, "457"))
            .isTrue();
    }

    @Test
    public void test_isWelshCourtLocation_nonWelshCourt() {
        Mockito.doReturn(Optional.empty()).when(welshCourtLocationRepository)
            .findById("415");

        Assertions.assertThat(CourtLocationUtils.isWelshCourtLocation(welshCourtLocationRepository, "415"))
            .isFalse();
    }

}
