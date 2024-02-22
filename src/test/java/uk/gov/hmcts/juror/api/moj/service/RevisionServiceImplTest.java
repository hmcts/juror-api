package uk.gov.hmcts.juror.api.moj.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.history.Revision;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.domain.AppearanceId;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.AppearanceRepository;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@SuppressWarnings("unchecked")
class RevisionServiceImplTest {

    private CourtLocationRepository courtLocationRepository;
    private JurorRepository jurorRepository;
    private AppearanceRepository appearanceRepository;
    private RevisionServiceImpl revisionService;

    @BeforeEach
    void beforeEach() {
        this.courtLocationRepository = mock(CourtLocationRepository.class);
        this.jurorRepository = mock(JurorRepository.class);
        this.appearanceRepository = mock(AppearanceRepository.class);
        this.revisionService = new RevisionServiceImpl(
            this.courtLocationRepository,
            this.jurorRepository,
            this.appearanceRepository
        );
    }

    @Test
    void positiveGetLatestCourtRevision() {
        Revision<Long, CourtLocation> revision = mock(Revision.class);
        doReturn(Optional.of(revision)).when(courtLocationRepository)
            .findLastChangeRevision(TestConstants.VALID_COURT_LOCATION);

        assertThat(
            revisionService.getLatestCourtRevision(TestConstants.VALID_COURT_LOCATION)
        ).isEqualTo(revision);

        verify(courtLocationRepository, times(1))
            .findLastChangeRevision(TestConstants.VALID_COURT_LOCATION);
        verifyNoMoreInteractions(courtLocationRepository);
    }

    @Test
    void negativeGetLatestCourtRevisionNotFound() {
        doReturn(Optional.empty()).when(courtLocationRepository)
            .findLastChangeRevision(TestConstants.VALID_COURT_LOCATION);

        MojException.NotFound exception = assertThrows(MojException.NotFound.class,
            () -> revisionService.getLatestCourtRevision(TestConstants.VALID_COURT_LOCATION),
            "Expected exception to be thrown when court revision is not found");

        assertThat(exception.getMessage())
            .isEqualTo("Court revision: " + TestConstants.VALID_COURT_LOCATION + " not found");
        assertThat(exception.getCause()).isNull();
    }

    @Test
    void positiveGetLatestJurorRevision() {
        Revision<Long, Juror> revision = mock(Revision.class);
        doReturn(Optional.of(revision)).when(jurorRepository)
            .findLastChangeRevision(TestConstants.VALID_JUROR_NUMBER);

        assertThat(
            revisionService.getLatestJurorRevision(TestConstants.VALID_JUROR_NUMBER)
        ).isEqualTo(revision);

        verify(jurorRepository, times(1))
            .findLastChangeRevision(TestConstants.VALID_JUROR_NUMBER);
        verifyNoMoreInteractions(jurorRepository);
    }

    @Test
    void negativeGetLatestJurorRevisionNotFound() {
        doReturn(Optional.empty()).when(jurorRepository)
            .findLastChangeRevision(TestConstants.VALID_JUROR_NUMBER);

        MojException.NotFound exception = assertThrows(MojException.NotFound.class,
            () -> revisionService.getLatestJurorRevision(TestConstants.VALID_JUROR_NUMBER),
            "Expected exception to be thrown when juror revision is not found");

        assertThat(exception.getMessage())
            .isEqualTo("Juror revision: " + TestConstants.VALID_JUROR_NUMBER + " not found");
        assertThat(exception.getCause()).isNull();
    }


    @Test
    void positiveGetLatestAppearanceRevision() {
        AppearanceId appearanceId = mock(AppearanceId.class);
        Revision<Long, CourtLocation> revision = mock(Revision.class);
        doReturn(Optional.of(revision)).when(appearanceRepository)
            .findLastChangeRevision(appearanceId);

        assertThat(
            revisionService.getLatestAppearanceRevision(appearanceId)
        ).isEqualTo(revision);

        verify(appearanceRepository, times(1))
            .findLastChangeRevision(appearanceId);
        verifyNoMoreInteractions(appearanceRepository);
    }

    @Test
    void negativeGetLatestAppearanceRevisionNotFound() {
        AppearanceId appearanceId = mock(AppearanceId.class);
        doReturn(Optional.empty()).when(appearanceRepository)
            .findLastChangeRevision(appearanceId);

        MojException.NotFound exception = assertThrows(MojException.NotFound.class,
            () -> revisionService.getLatestAppearanceRevision(appearanceId),
            "Expected exception to be thrown when appearance revision is not found");

        assertThat(exception.getMessage())
            .isEqualTo("Appearance revision not found");
        assertThat(exception.getCause()).isNull();
    }
}
