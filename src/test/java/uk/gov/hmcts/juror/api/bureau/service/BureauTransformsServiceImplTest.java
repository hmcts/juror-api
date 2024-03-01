package uk.gov.hmcts.juror.api.bureau.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.juror.api.bureau.controller.response.TeamDto;
import uk.gov.hmcts.juror.api.bureau.domain.Team;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link BureauTransformsServiceImpl}.
*/
@RunWith(MockitoJUnitRunner.class)
public class BureauTransformsServiceImplTest {

    @Mock
    private UrgencyService urgencyService;

    @InjectMocks
    private BureauTransformsServiceImpl bureauTransforms;

    @Test

    public void testTransformEntityToDto() {
        final long testId = 42L;
        final String testTeamName = "The meaning of life";
        final int testVersion = 101;
        final TeamDto dto =
            bureauTransforms.toTeamDto(
                Team.builder().id(testId).teamName(testTeamName).version(testVersion).build());
        assertThat(dto.getId()).isEqualTo(testId);
        assertThat(dto.getName()).isEqualTo(testTeamName);
        assertThat(dto.getVersion()).isEqualTo(testVersion);
    }
}
