package uk.gov.hmcts.juror.api.moj.controller.response;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.PoolType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
public class PoolRequestListDtoTest {

    private CourtLocation createCourtLocation(String locationCode, String name, String attendanceTime) {
        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCode(locationCode);
        courtLocation.setName(name);
        courtLocation.setCourtAttendTime(LocalTime.parse(attendanceTime));

        return courtLocation;
    }

    @Test
    public void test_PoolRequestListDto_PoolRequestDataDto() {
        CourtLocation courtLocation = createCourtLocation("401",
            "AYLESBURY", "09:15");
        PoolType poolType = new PoolType("CRO", "CROWN COURT");
        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setCourtLocation(courtLocation);
        poolRequest.setPoolType(poolType);
        poolRequest.setPoolNumber("401220101");
        poolRequest.setNumberRequested(10);
        poolRequest.setReturnDate(LocalDate.of(2022, 4, 1));

        PoolRequestListDto.PoolRequestDataDto poolRequestDataDto =
            new PoolRequestListDto.PoolRequestDataDto(poolRequest);

        assertThat(poolRequestDataDto.getCourtName())
            .as("DTO Court Name should be mapped from the Court Location related to this pool request")
            .isEqualTo(courtLocation.getName());
        assertThat(poolRequestDataDto.getPoolNumber())
            .as("DTO Pool Number should be mapped from the Pool Request's Pool Number")
            .isEqualTo(poolRequest.getPoolNumber());
        assertThat(poolRequestDataDto.getPoolType())
            .as("DTO Pool Type should be mapped from the Pool Type related to this Pool Request")
            .isEqualTo(poolType.getPoolType());
        assertThat(poolRequestDataDto.getNumberRequested())
            .as("DTO Number Requested should be mapped from the Pool Request's Number Requested")
            .isEqualTo(poolRequest.getNumberRequested());
        assertThat(poolRequestDataDto.getAttendanceDate())
            .as("DTO Attendance Date should be mapped from the Pool Request's Return Date")
            .hasYear(2022).hasMonth(Month.APRIL).hasDayOfMonth(1);
    }

    @Test
    public void test_PoolRequestListDto() {
        CourtLocation courtLocationOne = createCourtLocation("401",
            "AYLESBURY", "09:15");


        PoolType poolType = new PoolType("CRO", "CROWN COURT");

        PoolRequest poolRequestOne = new PoolRequest();
        poolRequestOne.setCourtLocation(courtLocationOne);
        poolRequestOne.setPoolNumber("401220101");
        poolRequestOne.setPoolType(poolType);
        poolRequestOne.setNumberRequested(10);
        poolRequestOne.setReturnDate(LocalDate.of(2022, 4, 1));

        final CourtLocation courtLocationTwo = createCourtLocation("799",
            "HOVE", "09:45");

        PoolRequest poolRequestTwo = new PoolRequest();
        poolRequestTwo.setCourtLocation(courtLocationTwo);
        poolRequestTwo.setPoolType(poolType);
        poolRequestTwo.setPoolNumber("799221105");
        poolRequestTwo.setNumberRequested(100);
        poolRequestTwo.setReturnDate(LocalDate.of(2022, 12, 8));

        PoolRequestListDto.PoolRequestDataDto poolRequestDataOne =
            new PoolRequestListDto.PoolRequestDataDto(poolRequestOne);
        PoolRequestListDto.PoolRequestDataDto poolRequestDataTwo =
            new PoolRequestListDto.PoolRequestDataDto(poolRequestTwo);

        List<PoolRequestListDto.PoolRequestDataDto> poolRequestDataList = new ArrayList<>();
        poolRequestDataList.add(poolRequestDataOne);
        poolRequestDataList.add(poolRequestDataTwo);

        PoolRequestListDto poolRequestListDto = new PoolRequestListDto(poolRequestDataList, poolRequestDataList.size());

        assertThat(poolRequestListDto.getData().size())
            .as("DTO Should be initialised with 2 data objects")
            .isEqualTo(2);
        assertThat(poolRequestListDto.getData())
            .as("Data list should contain the two test data object")
            .contains(poolRequestDataOne, poolRequestDataTwo);
    }

}
