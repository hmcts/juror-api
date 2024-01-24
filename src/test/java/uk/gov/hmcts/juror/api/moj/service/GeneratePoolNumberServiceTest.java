package uk.gov.hmcts.juror.api.moj.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
public class GeneratePoolNumberServiceTest {

    @Mock
    PoolRequestRepository poolRequestRepository;

    @InjectMocks
    GeneratePoolNumberServiceImpl generatePoolNumberService;

    @Test
    public void test_generatePoolNumber_firstSequence() {
        String courtLocation = "415";
        LocalDate attendanceDate = LocalDate.of(2022, 10, 12);
        Mockito.when(poolRequestRepository.findLatestPoolRequestByPoolNumberPrefix("4152210")).thenReturn(null);

        String poolNumber = generatePoolNumberService.generatePoolNumber(courtLocation, attendanceDate);

        assertThat(poolNumber).as("No previous records exist for the given court location and attendance date, "
            + "expect the generated pool number to have the default sequence start suffix (01)").isEqualTo("415221001");
    }

    @Test
    public void test_generatePoolNumber_secondSequence() {
        String courtLocation = "415";
        LocalDate attendanceDate = LocalDate.of(2022, 10, 12);
        PoolRequest existingPoolRequest = new PoolRequest();
        existingPoolRequest.setPoolNumber("415221001");
        Mockito.when(poolRequestRepository.findLatestPoolRequestByPoolNumberPrefix("4152210"))
            .thenReturn(existingPoolRequest);

        String poolNumber = generatePoolNumberService.generatePoolNumber(courtLocation, attendanceDate);

        assertThat(poolNumber).as("A previous record exist for the given court location and attendance date, "
                + "expect the generated pool number to have the next sequence number (02)")
            .isEqualTo("415221002");
    }

    @Test
    public void test_generatePoolNumber_invalidLastSequence() {
        String courtLocation = "415";
        LocalDate attendanceDate = LocalDate.of(2022, 10, 12);
        PoolRequest existingPoolRequest = new PoolRequest();
        existingPoolRequest.setPoolNumber("415221099");
        Mockito.when(poolRequestRepository.findLatestPoolRequestByPoolNumberPrefix("4152210"))
            .thenReturn(existingPoolRequest);

        String poolNumber = generatePoolNumberService.generatePoolNumber(courtLocation, attendanceDate);

        assertThat(poolNumber).as("The last sequence number is set to 99 (which should never happen), "
                + "expect the generated pool number to be an empty string as there are no more 2 digit sequence "
                + "numbers available")
            .isEqualTo("");
    }

}
