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
        final long TEST_ID = 42L;
        final String TEST_TEAM_NAME = "The meaning of life";
        final int TEST_VERSION = 101;
        final TeamDto dto =
            bureauTransforms.toTeamDto(
                Team.builder().id(TEST_ID).teamName(TEST_TEAM_NAME).version(TEST_VERSION).build());
        assertThat(dto.getId()).isEqualTo(TEST_ID);
        assertThat(dto.getName()).isEqualTo(TEST_TEAM_NAME);
        assertThat(dto.getVersion()).isEqualTo(TEST_VERSION);
    }
}
