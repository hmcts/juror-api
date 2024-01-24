package uk.gov.hmcts.juror.api.moj.service.trial;

import com.querydsl.core.Tuple;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.CourtroomsListDto;
import uk.gov.hmcts.juror.api.moj.repository.trial.CourtroomRepository;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
public class CourtroomServiceImplTest {

    @Mock
    private CourtroomRepository courtroomRepository;

    @InjectMocks
    private CourtroomServiceImpl courtroomService;

    @Test
    public void courtroomsForLocationHappy() {
        List<String> courts = List.of("767");
        doReturn(createCourtsList()).when(courtroomRepository).getCourtroomsForLocation(courts);

        List<CourtroomsListDto> courtrooms = courtroomService.getCourtroomsForLocation(courts);

        verify(courtroomRepository, times(1)).getCourtroomsForLocation(courts);
        assertThat(courtrooms).hasSize(1);

        assertThat(courtrooms.get(0).getCourtLocation()).isEqualTo("STOKE_ON_TRENT");
        assertThat(courtrooms.get(0).getCourtRooms().get(0).getId()).isEqualTo(Long.valueOf(9002));
        assertThat(courtrooms.get(0).getCourtRooms().get(0).getOwner()).isEqualTo("001");
        assertThat(courtrooms.get(0).getCourtRooms().get(0).getRoomNumber()).isEqualTo("RM1");
        assertThat(courtrooms.get(0).getCourtRooms().get(0).getDescription()).isEqualTo("JURY ROOM 1");
    }

    private List<Tuple> createCourtsList() {
        Tuple mockedTupleResult = mock(Tuple.class);
        createMockQueryDslResult(mockedTupleResult, 9002, "001", "JURY ROOM 1",
            "RM1", "STOKE_ON_TRENT");

        List<Tuple> firstRoom = new ArrayList<>();
        firstRoom.add(mockedTupleResult);

        return firstRoom;
    }

    private void createMockQueryDslResult(Tuple mockedTuple, long id,
                                          String owner,
                                          String description,
                                          String roomNumber,
                                          String courtroom) {
        doReturn(id).when(mockedTuple).get(0, Long.class);
        doReturn(owner).when(mockedTuple).get(1, String.class);
        doReturn(description).when(mockedTuple).get(2, String.class);
        doReturn(roomNumber).when(mockedTuple).get(3, String.class);
        doReturn(courtroom).when(mockedTuple).get(4, String.class);
    }
}