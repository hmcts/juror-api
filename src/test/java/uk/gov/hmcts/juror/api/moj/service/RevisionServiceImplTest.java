package uk.gov.hmcts.juror.api.moj.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@SuppressWarnings("unchecked")
class RevisionServiceImplTest {

    private CourtLocationRepository courtLocationRepository;
    private JurorRepository jurorRepository;
    private RevisionServiceImpl revisionService;

    @BeforeEach
    void beforeEach() {
        this.courtLocationRepository = mock(CourtLocationRepository.class);
        this.jurorRepository = mock(JurorRepository.class);
        this.revisionService = new RevisionServiceImpl(
            this.courtLocationRepository,
            this.jurorRepository
        );
    }

    @Test
    void positiveGetLatestCourtRevision() {
        long revision = 123L;
        doReturn(revision).when(courtLocationRepository)
            .getLatestRevision(TestConstants.VALID_COURT_LOCATION);

        assertThat(
            revisionService.getLatestCourtRevisionNumber(TestConstants.VALID_COURT_LOCATION)
        ).isEqualTo(revision);

        verify(courtLocationRepository, times(1))
            .getLatestRevision(TestConstants.VALID_COURT_LOCATION);
        verifyNoMoreInteractions(courtLocationRepository);
    }


    @Test
    void positiveGetLatestJurorRevision() {
        long revision = 123L;
        doReturn(revision).when(jurorRepository)
            .getLatestRevision(TestConstants.VALID_JUROR_NUMBER);

        assertThat(
            revisionService.getLatestJurorRevisionNumber(TestConstants.VALID_JUROR_NUMBER)
        ).isEqualTo(revision);

        verify(jurorRepository, times(1))
            .getLatestRevision(TestConstants.VALID_JUROR_NUMBER);
        verifyNoMoreInteractions(jurorRepository);
    }
}
