package uk.gov.hmcts.juror.api.moj.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.response.PoolSummaryResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.PoolStatistics;
import uk.gov.hmcts.juror.api.moj.domain.PoolType;
import uk.gov.hmcts.juror.api.moj.exception.PoolRequestException;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolStatisticsRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@RunWith(SpringRunner.class)
public class PoolStatisticsServiceTest {

    @Mock
    PoolRequestRepository poolRequestRepository;

    @Mock
    PoolStatisticsRepository poolStatisticsRepository;

    @InjectMocks
    PoolStatisticsServiceImpl poolStatisticsService;

    private PoolRequest initPoolRequest(String poolNumber, int numberRequested, int totalNumberRequired) {
        return initPoolRequest(poolNumber, numberRequested, totalNumberRequired, false);
    }

    private PoolRequest initPoolRequest(String poolNumber, int numberRequested, int totalNumberRequired,
                                        boolean isNilPool) {
        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber(poolNumber);
        poolRequest.setNumberRequested(numberRequested);
        poolRequest.setReturnDate(LocalDate.now());
        poolRequest.setTotalNoRequired(totalNumberRequired);

        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCode("123");
        courtLocation.setLocCourtName("The Crown Court at Test Location");
        poolRequest.setCourtLocation(courtLocation);
        poolRequest.setNilPool(isNilPool);
        poolRequest.setPoolType(new PoolType("CIV", "Civil Court"));
        return poolRequest;
    }

    private PoolStatistics initPoolStats(String poolNumber) {
        return new PoolStatistics(poolNumber, 15, 3, 2, 3, 7);
    }

    @Test
    public void calculatePoolStatistics_allDataPresent() {
        String poolNumber = "123456789";
        int totalNumberRequired = 15;
        int numberRequestFromBureau = 12;

        PoolRequest poolRequest = initPoolRequest(poolNumber, numberRequestFromBureau, totalNumberRequired);
        CourtLocation courtLocation = poolRequest.getCourtLocation();
        PoolStatistics poolStatistics = initPoolStats(poolNumber);

        Mockito.doReturn(Optional.of(poolRequest))
            .when(poolRequestRepository).findByPoolNumber(poolNumber);
        Mockito.doReturn(Optional.of(poolStatistics)).when(poolStatisticsRepository).findById(poolNumber);
        Mockito.doReturn(true).when(poolRequestRepository).isActive(poolNumber);

        PoolSummaryResponseDto poolSummaryResponseDto = poolStatisticsService.calculatePoolStatistics(poolNumber);
        PoolSummaryResponseDto.PoolDetails poolDetails = poolSummaryResponseDto.getPoolDetails();
        PoolSummaryResponseDto.BureauSummoning bureauSummoning = poolSummaryResponseDto.getBureauSummoning();
        PoolSummaryResponseDto.PoolSummary poolSummary = poolSummaryResponseDto.getPoolSummary();
        PoolSummaryResponseDto.AdditionalStatistics additionalStatistics =
            poolSummaryResponseDto.getAdditionalStatistics();

        assertThat(poolDetails.getPoolNumber())
            .as("Pool number should be mapped from the POOL_NO value in the POOL_REQUEST view")
            .isEqualTo(poolRequest.getPoolNumber());
        assertThat(poolDetails.getCourtLocationCode())
            .as("Court location code should be mapped from the COURT_LOCATION associated with this POOL_REQUEST record")
            .isEqualTo(courtLocation.getLocCode());
        assertThat(poolDetails.getCourtName())
            .as("Court name should be mapped from the COURT_LOCATION associated with this POOL_REQUEST record")
            .isEqualToIgnoringCase(courtLocation.getName());
        assertThat(poolDetails.getPoolType()).isEqualTo("Civil Court");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE dd MMM yyyy");
        assertThat(poolDetails.getCourtStartDate())
            .as("Court start date should be mapped from the RETURN_DATE value in the POOL_REQUEST view")
            .isEqualToIgnoringCase(poolRequest.getReturnDate().format(formatter));
        assertThat(poolDetails.getAdditionalRequirements())
            .as("Additional requirements should be set to 'Special pool' if the Pool was requested with this "
                + "requirement")
            .isNull();
        assertThat(poolDetails.getIsActive())
            .as("Is active should be true if any copy of this request has a NEW_REQUEST value equal to 'N'")
            .isTrue();
        assertThat(poolDetails.isNilPool())
            .as("Is nil pool should be mapped from the nil_pool value in the POOL_REQUEST view")
            .isFalse();
        assertThat(bureauSummoning.getTotalSummoned())
            .as("Total summoned should be mapped from the TOTAL_SUMMONED value in the POOL_STATS view and represents "
                + "the total number of bureau owned members in a pool (regardless of status)")
            .isEqualTo(poolStatistics.getTotalSummoned());
        assertThat(bureauSummoning.getConfirmedFromBureau())
            .as("Confirmed from bureau should be mapped from the AVAILABLE value in the POOL_STATS view and "
                + "represents the total number of bureau owned members in a pool wih a status of 'Responded'")
            .isEqualTo(poolStatistics.getAvailable());
        assertThat(bureauSummoning.getRequestedFromBureau())
            .as("Requested from bureau should be mapped from the NO_REQUESTED value in the POOL_REQUEST view and "
                + "represents the number of jurors requested from the Bureau (total required - court supply)")
            .isEqualTo(poolRequest.getNumberRequested());
        assertThat(bureauSummoning.getUnavailable())
            .as("Unavailable should be mapped from the UNAVAILABLE value in the POOL_STATS view and represents the "
                + "number of bureau owned members in  a pool with a status that is NOT: 'Responded', 'Summoned', or "
                + "'Awaiting Info'")
            .isEqualTo(poolStatistics.getUnavailable());
        assertThat(bureauSummoning.getUnresolved())
            .as("Unresolved should be mapped from the UNRESOLVED value in the POOL_STATS view and represents the "
                + "number of bureau owned members in  a pool with a status of: 'Summoned' or 'Awaiting Info'")
            .isEqualTo(poolStatistics.getUnresolved());
        assertThat(poolSummary.getCurrentPoolSize())
            .as("Current pool size should be calculated as COURT_SUPPLY plus AVAILABLE")
            .isEqualTo(poolStatistics.getAvailable() + poolStatistics.getCourtSupply());
        assertThat(poolSummary.getRequiredPoolSize())
            .as("Required pool size should be mapped from TOTAL_NUMBER_REQUIRED value in the POOL_REQUEST_EXT view")
            .isEqualTo(totalNumberRequired);
        assertThat(additionalStatistics.getCourtSupply())
            .as("Court Supply should be mapped from COURT_SUPPLY value in the POOL_STATS view and represent active, "
                + "court owned members in a pool")
            .isEqualTo(poolStatistics.getCourtSupply());
    }

    @Test
    public void calculatePoolStatistics_poolStatsNotFound() {
        String poolNumber = "123456789";
        int numberRequestFromBureau = 12;
        int totalNumberRequired = 15;

        PoolRequest poolRequest = initPoolRequest(poolNumber, numberRequestFromBureau, totalNumberRequired);
        CourtLocation courtLocation = poolRequest.getCourtLocation();

        Mockito.doReturn(Optional.of(poolRequest))
            .when(poolRequestRepository).findByPoolNumber(poolNumber);
        Mockito.doReturn(Optional.empty()).when(poolStatisticsRepository).findById(poolNumber);
        Mockito.doReturn(false).when(poolRequestRepository).isActive(poolNumber);

        PoolSummaryResponseDto poolSummaryResponseDto = poolStatisticsService.calculatePoolStatistics(poolNumber);
        PoolSummaryResponseDto.PoolDetails poolDetails = poolSummaryResponseDto.getPoolDetails();
        PoolSummaryResponseDto.BureauSummoning bureauSummoning = poolSummaryResponseDto.getBureauSummoning();
        PoolSummaryResponseDto.PoolSummary poolSummary = poolSummaryResponseDto.getPoolSummary();
        PoolSummaryResponseDto.AdditionalStatistics additionalStatistics =
            poolSummaryResponseDto.getAdditionalStatistics();

        // assert all pool stats related items are defaulted to 0
        assertThat(bureauSummoning.getTotalSummoned())
            .as("Total summoned should be defaulted to 0 where no Pool Stats record exists")
            .isEqualTo(0);
        assertThat(bureauSummoning.getConfirmedFromBureau())
            .as("Confirmed from bureau should be defaulted to 0 where no Pool Stats record exists")
            .isEqualTo(0);
        assertThat(bureauSummoning.getUnavailable())
            .as("Unavailable should be defaulted to 0 where no Pool Stats record exists")
            .isEqualTo(0);
        assertThat(bureauSummoning.getUnresolved())
            .as("Unresolved should be defaulted to 0 where no Pool Stats record exists")
            .isEqualTo(0);
        assertThat(poolSummary.getCurrentPoolSize())
            .as("Current pool size should be defaulted to 0 where no Pool Stats record exists")
            .isEqualTo(0);
        assertThat(additionalStatistics.getCourtSupply())
            .as("Court Supply should be defaulted to 0 where no Pool Stats record exists")
            .isEqualTo(0);

        // regression test non-pool stats related data items are not impacted
        assertThat(poolDetails.getPoolNumber())
            .as("Pool number should be mapped from the POOL_NO value in the POOL_REQUEST view")
            .isEqualTo(poolRequest.getPoolNumber());
        assertThat(poolDetails.getCourtLocationCode())
            .as("Court location code should be mapped from the COURT_LOCATION associated with this POOL_REQUEST record")
            .isEqualTo(courtLocation.getLocCode());
        assertThat(poolDetails.getCourtName())
            .as("Court name should be mapped from the COURT_LOCATION associated with this POOL_REQUEST record")
            .isEqualToIgnoringCase(courtLocation.getName());
        assertThat(poolDetails.getPoolType()).isEqualTo("Civil Court");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE dd MMM yyyy");
        assertThat(poolDetails.getCourtStartDate())
            .as("Court start date should be mapped from the RETURN_DATE value in the POOL_REQUEST view")
            .isEqualToIgnoringCase(poolRequest.getReturnDate().format(formatter));
        assertThat(poolDetails.getAdditionalRequirements())
            .as("Additional requirements should be empty if this was not requested as a Special Pool")
            .isNull();
        assertThat(poolDetails.getIsActive())
            .as("Is active should be false if no copies of this request has a NEW_REQUEST value equal to 'N'")
            .isFalse();
        assertThat(poolDetails.isNilPool())
            .as("Is nil pool should be false be mapped from the nil_pool value in the POOL_REQUEST view")
            .isFalse();

        assertThat(bureauSummoning.getRequestedFromBureau())
            .as("Requested from bureau should be mapped from the NO_REQUESTED value in the POOL_REQUEST view and "
                + "represents the number of jurors requested from the Bureau (total required - court supply)")
            .isEqualTo(poolRequest.getNumberRequested());
    }

    @Test
    public void calculatePoolStatistics_nilPool() {
        String poolNumber = "123456789";
        int totalNumberRequired = 0;
        int numberRequestFromBureau = 0;

        PoolRequest poolRequest = initPoolRequest(poolNumber, numberRequestFromBureau, totalNumberRequired, true);
        CourtLocation courtLocation = poolRequest.getCourtLocation();

        Mockito.doReturn(Optional.of(poolRequest))
            .when(poolRequestRepository).findByPoolNumber(poolNumber);
        Mockito.doReturn(Optional.empty()).when(poolStatisticsRepository).findById(poolNumber);
        Mockito.doReturn(true).when(poolRequestRepository).isActive(poolNumber);

        PoolSummaryResponseDto poolSummaryResponseDto = poolStatisticsService.calculatePoolStatistics(poolNumber);
        PoolSummaryResponseDto.PoolDetails poolDetails = poolSummaryResponseDto.getPoolDetails();
        PoolSummaryResponseDto.BureauSummoning bureauSummoning = poolSummaryResponseDto.getBureauSummoning();
        PoolSummaryResponseDto.PoolSummary poolSummary = poolSummaryResponseDto.getPoolSummary();
        PoolSummaryResponseDto.AdditionalStatistics additionalStatistics =
            poolSummaryResponseDto.getAdditionalStatistics();

        assertThat(poolDetails.getPoolNumber())
            .as("Pool number should be mapped from the POOL_NO value in the POOL_REQUEST view")
            .isEqualTo(poolRequest.getPoolNumber());
        assertThat(poolDetails.getCourtLocationCode())
            .as("Court location code should be mapped from the COURT_LOCATION associated with this POOL_REQUEST record")
            .isEqualTo(courtLocation.getLocCode());
        assertThat(poolDetails.getCourtName())
            .as("Court name should be mapped from the COURT_LOCATION associated with this POOL_REQUEST record")
            .isEqualToIgnoringCase(courtLocation.getName());
        assertThat(poolDetails.getPoolType()).isEqualTo("Civil Court");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE dd MMM yyyy");
        assertThat(poolDetails.getCourtStartDate())
            .as("Court start date should be mapped from the RETURN_DATE value in the POOL_REQUEST view")
            .isEqualToIgnoringCase(poolRequest.getReturnDate().format(formatter));
        assertThat(poolDetails.getAdditionalRequirements())
            .as("Additional requirements should be empty if this was not requested as a Special Pool")
            .isNull();
        assertThat(poolDetails.getIsActive())
            .as("Is active should be true if any copy of this request has a NEW_REQUEST value equal to 'N'")
            .isTrue();
        assertThat(poolDetails.isNilPool())
            .as("Is nil pool should be mapped from the nil_pool value in the POOL_REQUEST view")
            .isTrue();

        assertThat(bureauSummoning.getTotalSummoned())
            .as("Total summoned should be 0 for a nil pool")
            .isEqualTo(0);
        assertThat(bureauSummoning.getConfirmedFromBureau())
            .as("Confirmed from bureau should be 0 for a nil pool")
            .isEqualTo(0);
        assertThat(bureauSummoning.getRequestedFromBureau())
            .as("Requested from bureau should be 0 for a nil pool")
            .isEqualTo(0);
        assertThat(bureauSummoning.getUnavailable())
            .as("Unavailable should be 0 for a nil pool")
            .isEqualTo(0);
        assertThat(bureauSummoning.getUnresolved())
            .as("Unresolved should be 0 for a nil pool")
            .isEqualTo(0);

        assertThat(poolSummary.getCurrentPoolSize())
            .as("Current pool size should be 0 for a nil pool")
            .isEqualTo(0);
        assertThat(poolSummary.getRequiredPoolSize())
            .as("Required pool size should be 0 for a nil pool")
            .isEqualTo(0);

        assertThat(additionalStatistics.getCourtSupply())
            .as("Court Supply should be 0 for a nil pool")
            .isEqualTo(0);
    }

    @Test(expected = PoolRequestException.PoolRequestNotFound.class)
    public void calculatePoolStatistics_invalidPoolNumber() {
        String poolNumber = "123456789";
        Mockito.doReturn(Optional.empty())
            .when(poolRequestRepository).findByPoolNumber(poolNumber);
        poolStatisticsService.calculatePoolStatistics(poolNumber);
    }

}
