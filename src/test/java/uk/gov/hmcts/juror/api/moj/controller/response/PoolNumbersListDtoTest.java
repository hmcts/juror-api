package uk.gov.hmcts.juror.api.moj.controller.response;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
public class PoolNumbersListDtoTest {

    private CourtLocation createCourtLocation(String locationCode, String name, String attendanceTime) {
        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCode(locationCode);
        courtLocation.setName(name);
        courtLocation.setCourtAttendTime(LocalTime.parse(attendanceTime));

        return courtLocation;
    }

    @Test
    public void test_PoolNumbersListDto_PoolNumbersDataDto() {
        CourtLocation courtLocation = createCourtLocation("410",
            "CHESTER", "09:15");
        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setCourtLocation(courtLocation);
        poolRequest.setPoolNumber("410230101");
        poolRequest.setReturnDate(LocalDate.of(2023, 1, 1));

        PoolNumbersListDto.PoolNumbersDataDto activePoolsDataDto =
            new PoolNumbersListDto.PoolNumbersDataDto(poolRequest.getPoolNumber(), poolRequest.getReturnDate());

        assertThat(activePoolsDataDto.getPoolNumber())
            .as("DTO Court Number should be mapped from the Pool Request's Pool Number")
            .isEqualTo(poolRequest.getPoolNumber());
        assertThat(activePoolsDataDto.getAttendanceDate())
            .as("DTO Attendance Date should be mapped from the Pool Request's Return Date")
            .hasYear(2023).hasMonth(Month.JANUARY).hasDayOfMonth(1);
    }

    @Test
    public void test_PoolNumbersListDto() {
        CourtLocation courtLocation = createCourtLocation("410",
            "CHESTER", "09:15");

        PoolRequest poolRequestOne = new PoolRequest();
        poolRequestOne.setCourtLocation(courtLocation);
        poolRequestOne.setPoolNumber("410230101");
        poolRequestOne.setReturnDate(LocalDate.of(2023, 1, 1));

        final PoolNumbersListDto.PoolNumbersDataDto activePoolsDataDtoOne =
            new PoolNumbersListDto.PoolNumbersDataDto(poolRequestOne.getPoolNumber(), poolRequestOne.getReturnDate());

        PoolRequest poolRequestTwo = new PoolRequest();
        poolRequestTwo.setCourtLocation(courtLocation);
        poolRequestTwo.setPoolNumber("410230102");
        poolRequestTwo.setReturnDate(LocalDate.of(2023, 1, 1));

        PoolNumbersListDto.PoolNumbersDataDto activePoolsDataDtoTwo =
            new PoolNumbersListDto.PoolNumbersDataDto(poolRequestTwo.getPoolNumber(), poolRequestTwo.getReturnDate());

        List<PoolNumbersListDto.PoolNumbersDataDto> activePoolsList = new ArrayList<>();
        activePoolsList.add(activePoolsDataDtoOne);
        activePoolsList.add(activePoolsDataDtoTwo);

        PoolNumbersListDto activePoolsListDto = new PoolNumbersListDto(activePoolsList);

        assertThat(activePoolsListDto.getData().size())
            .as("DTO Should be initialised with 2 data objects")
            .isEqualTo(2);
        assertThat(activePoolsListDto.getData())
            .as("Data list should contain the two test data object")
            .contains(activePoolsDataDtoOne, activePoolsDataDtoTwo);
    }

}
