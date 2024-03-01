package uk.gov.hmcts.juror.api.moj.controller.response;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
public class CourtLocationsListDtoTest {

    private CourtLocation createCourtLocation(String locationCode, String name, String attendacneTime) {
        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCode(locationCode);
        courtLocation.setName(name);
        courtLocation.setCourtAttendTime(LocalTime.parse(attendacneTime));

        return courtLocation;
    }

    @Test
    public void test_CourtLocationListDto_CourtLocationDataDto() {
        CourtLocation courtLocation = createCourtLocation("401",
            "AYLESBURY", "09:15");

        CourtLocationDataDto courtLocationDataDto =
            new CourtLocationDataDto(courtLocation);

        assertThat(courtLocationDataDto.getLocationCode())
            .as("DTO Location Code should be mapped from the Court Location object's Location Code")
            .isEqualTo(courtLocation.getLocCode());
        assertThat(courtLocationDataDto.getLocationName())
            .as("DTO Location Name should be mapped from the Court Location object's Name")
            .isEqualTo(courtLocation.getName());
        assertThat(courtLocationDataDto.getAttendanceTime())
            .as("DTO Attendance Time should be mapped from the Court Location object's Attendance Time")
            .isEqualTo(courtLocation.getCourtAttendTime().format(DateTimeFormatter.ofPattern("HH:mm")));
    }

    @Test
    public void test_CourtLocationListDto() {
        CourtLocation courtLocationOne = createCourtLocation("401",
            "AYLESBURY", "09:15");
        CourtLocation courtLocationTwo = createCourtLocation("799",
            "HOVE", "09:45");

        CourtLocationDataDto courtLocationDataOne =
            new CourtLocationDataDto(courtLocationOne);
        CourtLocationDataDto courtLocationDataTwo =
            new CourtLocationDataDto(courtLocationTwo);

        List<CourtLocationDataDto> courtLocationDataList = new ArrayList<>();
        courtLocationDataList.add(courtLocationDataOne);
        courtLocationDataList.add(courtLocationDataTwo);

        CourtLocationListDto courtLocationListDto = new CourtLocationListDto(courtLocationDataList);

        assertThat(courtLocationListDto.getData().size())
            .as("DTO Should be initialised with 2 data objects")
            .isEqualTo(2);
        assertThat(courtLocationListDto.getData())
            .as("Data list should contain the first test data object")
            .contains(courtLocationDataOne);
        assertThat(courtLocationListDto.getData())
            .as("Data list should contain the second test data object")
            .contains(courtLocationDataTwo);
    }

}
