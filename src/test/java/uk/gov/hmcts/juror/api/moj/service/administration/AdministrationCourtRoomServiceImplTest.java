package uk.gov.hmcts.juror.api.moj.service.administration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.domain.administration.CourtRoomDto;
import uk.gov.hmcts.juror.api.moj.domain.trial.Courtroom;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.trial.CourtroomRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@DisplayName("AdministrationCourtRoomServiceImpl")
class AdministrationCourtRoomServiceImplTest {

    private AdministrationCourtRoomServiceImpl administrationCourtRoomService;
    private CourtroomRepository courtroomRepository;
    private CourtLocationRepository courtLocationRepository;

    @BeforeEach
    void beforeEach() {
        this.courtLocationRepository = mock(CourtLocationRepository.class);
        this.courtroomRepository = mock(CourtroomRepository.class);
        this.administrationCourtRoomService =
            spy(new AdministrationCourtRoomServiceImpl(courtroomRepository, courtLocationRepository));
    }

    @Nested
    @DisplayName("public void createCourtRoom(String locCode, CourtRoomDto courtRoomDto)")
    class CreateCourtRoomTest {
        @Test
        void positiveTypical() {
            CourtLocation courtLocation = mock(CourtLocation.class);
            doReturn(courtLocation).when(administrationCourtRoomService)
                .getCourtLocation(anyString());

            administrationCourtRoomService.createCourtRoom(TestConstants.VALID_COURT_LOCATION,
                CourtRoomDto.builder()
                    .roomName(TestConstants.VALID_COURT_ROOM_NAME)
                    .roomDescription(TestConstants.VALID_COURT_ROOM_DESC)
                    .build());

            verify(courtroomRepository, times(1)).save(Courtroom.builder()
                .courtLocation(courtLocation)
                .roomNumber(TestConstants.VALID_COURT_ROOM_NAME)
                .description(TestConstants.VALID_COURT_ROOM_DESC)
                .courtLocation(courtLocation)
                .build());
            verify(administrationCourtRoomService, times(1))
                .getCourtLocation(TestConstants.VALID_COURT_LOCATION);
        }
    }

    @Nested
    @DisplayName("public List<CourtRoomDto> viewCourtRooms(String locCode)")
    class ViewCourtRoomsTest {

        @Test
        void positiveTypical() {
            doReturn(List.of(
                Courtroom.builder()
                    .roomNumber(TestConstants.VALID_COURT_ROOM_NAME + "1")
                    .description(TestConstants.VALID_COURT_ROOM_DESC + " 1")
                    .build(),
                Courtroom.builder()
                    .roomNumber(TestConstants.VALID_COURT_ROOM_NAME + "2")
                    .description(TestConstants.VALID_COURT_ROOM_DESC + " 2")
                    .build(),
                Courtroom.builder()
                    .roomNumber(TestConstants.VALID_COURT_ROOM_NAME + "3")
                    .description(TestConstants.VALID_COURT_ROOM_DESC + " 3")
                    .build()
            )).when(courtroomRepository).findByCourtLocationLocCode(TestConstants.VALID_COURT_LOCATION);

            assertThat(administrationCourtRoomService.viewCourtRooms(TestConstants.VALID_COURT_LOCATION))
                .containsExactlyInAnyOrder(
                    CourtRoomDto.builder()
                        .roomName(TestConstants.VALID_COURT_ROOM_NAME + "1")
                        .roomDescription(TestConstants.VALID_COURT_ROOM_DESC + " 1")
                        .build(),
                    CourtRoomDto.builder()
                        .roomName(TestConstants.VALID_COURT_ROOM_NAME + "2")
                        .roomDescription(TestConstants.VALID_COURT_ROOM_DESC + " 2")
                        .build(),
                    CourtRoomDto.builder()
                        .roomName(TestConstants.VALID_COURT_ROOM_NAME + "3")
                        .roomDescription(TestConstants.VALID_COURT_ROOM_DESC + " 3")
                        .build()
                );
            verify(courtroomRepository, times(1))
                .findByCourtLocationLocCode(TestConstants.VALID_COURT_LOCATION);
        }

        @Test
        void positiveNoData() {
            doReturn(List.of()).when(courtroomRepository)
                .findByCourtLocationLocCode(TestConstants.VALID_COURT_LOCATION);

            assertThat(administrationCourtRoomService.viewCourtRooms(TestConstants.VALID_COURT_LOCATION)).isEmpty();
            verify(courtroomRepository, times(1))
                .findByCourtLocationLocCode(TestConstants.VALID_COURT_LOCATION);
        }
    }

    @Nested
    @DisplayName("public CourtRoomDto viewCourtRoom(String locCode, Long id)")
    class ViewCourtRoomTest {

        @Test
        void positiveTypical() {
            Courtroom courtroom = Courtroom.builder()
                .roomNumber(TestConstants.VALID_COURT_ROOM_NAME)
                .description(TestConstants.VALID_COURT_ROOM_DESC)
                .build();
            long id = 123L;
            doReturn(courtroom).when(administrationCourtRoomService)
                .getCourtRoom(TestConstants.VALID_COURT_LOCATION, id);
            assertThat(administrationCourtRoomService.viewCourtRoom(TestConstants.VALID_COURT_LOCATION, id))
                .isEqualTo(CourtRoomDto.builder()
                    .roomName(TestConstants.VALID_COURT_ROOM_NAME)
                    .roomDescription(TestConstants.VALID_COURT_ROOM_DESC)
                    .build());
            verify(administrationCourtRoomService, times(1))
                .getCourtRoom(TestConstants.VALID_COURT_LOCATION, id);
        }
    }

    @Nested
    @DisplayName("public void updateCourtRoom(String locCode, Long id, CourtRoomDto courtRoomDto)")
    class UpdateCourtRoomTest {
        @Test
        void positiveTypical() {
            Courtroom courtroom = Courtroom.builder()
                .roomNumber(TestConstants.VALID_COURT_ROOM_NAME)
                .description(TestConstants.VALID_COURT_ROOM_DESC)
                .build();
            long id = 123L;
            doReturn(courtroom).when(administrationCourtRoomService)
                .getCourtRoom(TestConstants.VALID_COURT_LOCATION, id);
            administrationCourtRoomService.updateCourtRoom(TestConstants.VALID_COURT_LOCATION, id,
                CourtRoomDto.builder()
                    .roomName(TestConstants.VALID_COURT_ROOM_NAME + "1")
                    .roomDescription(TestConstants.VALID_COURT_ROOM_DESC + " 1")
                    .build());
            verify(administrationCourtRoomService, times(1))
                .getCourtRoom(TestConstants.VALID_COURT_LOCATION, id);
            verify(courtroomRepository, times(1)).save(courtroom);
            assertThat(courtroom.getRoomNumber()).isEqualTo(TestConstants.VALID_COURT_ROOM_NAME + "1");
            assertThat(courtroom.getDescription()).isEqualTo(TestConstants.VALID_COURT_ROOM_DESC + " 1");
        }

    }

    @Nested
    @DisplayName("CourtLocation getCourtLocation(String locCode)")
    class GetCourtLocationTest {
        @Test
        void positiveFound() {
            CourtLocation courtLocation = mock(CourtLocation.class);
            doReturn(Optional.of(courtLocation)).when(courtLocationRepository)
                .findByLocCode(TestConstants.VALID_COURT_LOCATION);
            assertThat(administrationCourtRoomService.getCourtLocation(TestConstants.VALID_COURT_LOCATION))
                .isEqualTo(courtLocation);
            verify(courtLocationRepository).findByLocCode(TestConstants.VALID_COURT_LOCATION);
        }

        @Test
        void negativeNotFound() {
            doReturn(Optional.empty()).when(courtLocationRepository)
                .findByLocCode(TestConstants.VALID_COURT_LOCATION);
            MojException.NotFound exception =
                assertThrows(MojException.NotFound.class,
                    () -> administrationCourtRoomService.getCourtLocation(TestConstants.VALID_COURT_LOCATION),
                    "Exception should be thrown when court location is not found");
            assertThat(exception).isNotNull();
            assertThat(exception.getMessage())
                .isEqualTo("Court location not found");
            assertThat(exception.getCause()).isNull();

        }
    }

    @Nested
    @DisplayName("Courtroom getCourtRoom(String locCode, Long id)")
    class GetCourtRoomTest {
        @Test
        void positiveFound() {
            Courtroom courtroom = mock(Courtroom.class);
            long id = 123L;
            doReturn(Optional.of(courtroom)).when(courtroomRepository)
                .findByCourtLocationLocCodeAndId(TestConstants.VALID_COURT_LOCATION, id);
            assertThat(administrationCourtRoomService.getCourtRoom(TestConstants.VALID_COURT_LOCATION, id))
                .isEqualTo(courtroom);
            verify(courtroomRepository).findByCourtLocationLocCodeAndId(TestConstants.VALID_COURT_LOCATION, id);
        }

        @Test
        void negativeNotFound() {
            long id = 123L;
            doReturn(Optional.empty()).when(courtroomRepository)
                .findByCourtLocationLocCodeAndId(TestConstants.VALID_COURT_LOCATION, id);
            MojException.NotFound exception =
                assertThrows(MojException.NotFound.class,
                    () -> administrationCourtRoomService.getCourtRoom(TestConstants.VALID_COURT_LOCATION, id),
                    "Exception should be thrown when court room is not found");
            assertThat(exception).isNotNull();
            assertThat(exception.getMessage())
                .isEqualTo("Court room not found");
            assertThat(exception.getCause()).isNull();
        }
    }
}
