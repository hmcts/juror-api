package uk.gov.hmcts.juror.api.moj.service.poolmanagement;

import com.querydsl.core.Tuple;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.response.SummoningProgressResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.poolmanagement.AvailablePoolsInCourtLocationDto;
import uk.gov.hmcts.juror.api.moj.enumeration.PoolUtilisationDescription;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolStatisticsRepository;
import uk.gov.hmcts.juror.api.moj.utils.DateUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SuppressWarnings("PMD.TooManyMethods")
public class ManagePoolsServiceImplTest extends TestCase {
    public static final String COURT_LOCATION_CODE = "415";
    public static final String POOL_TYPE = "CRO";
    public static final int NUMBER_OF_WEEKS = 8;
    public static final String POOL_NUMBER = "415230601";

    @Mock
    PoolStatisticsRepository poolStatisticsRepository;
    @Mock
    CourtLocationRepository courtLocationRepository;
    @Mock
    PoolRequestRepository poolRequestRepository;

    @InjectMocks
    ManagePoolsServiceImpl managePoolsService;

    @Before
    public void setupMocks() {
        final String partialPoolNumber = "4152306";
        BureauJwtPayload bureauPayload = TestUtils.createJwt("400", "BUREAU_USER");
        List<Tuple> results = getStatisticsByCourtLocationAndPoolTypeResults(8, partialPoolNumber);
        List<Tuple> nilPoolResults = getNilPools();

        doReturn(results).when(poolStatisticsRepository)
            .getStatisticsByCourtLocationAndPoolType(bureauPayload.getOwner(), COURT_LOCATION_CODE, POOL_TYPE,
                NUMBER_OF_WEEKS);

        doReturn(nilPoolResults)
            .when(poolStatisticsRepository)
            .getNilPools(bureauPayload.getOwner(), COURT_LOCATION_CODE, POOL_TYPE, NUMBER_OF_WEEKS);
    }

    //Tests related to service method: findAvailablePools
    @Test
    public void findAvailablePools_courtUser_happy() {
        doReturn(createCourtLocations()).when(courtLocationRepository).findByOwner(any());
        doReturn(createActivePools()).when(poolRequestRepository).findActivePoolsForDateRange(any(), any(), any(),
            any());

        BureauJwtPayload payload = TestUtils.createJwt("404", "COURT_USER", "99");
        AvailablePoolsInCourtLocationDto availablePools = managePoolsService.findAvailablePools("404", payload);

        assertThat(availablePools.getAvailablePools()).hasSize(1);
        assertThat(availablePools.getAvailablePools().get(0).getPoolNumber()).isEqualTo("111111111");
        assertThat(availablePools.getAvailablePools().get(0).getUtilisationDescription()).isEqualTo(
            PoolUtilisationDescription.CONFIRMED);
        assertThat(availablePools.getAvailablePools().get(0).getUtilisation()).isEqualTo(1);
        assertThat(availablePools.getAvailablePools().get(0).getServiceStartDate()).isEqualTo("2023-06-22");
        mockitoVerificationFindAvailablePools(1);
    }

    @Test
    public void findAvailablePools_courtUser_noCourtsFoundForOwner() {
        doReturn(new ArrayList<>()).when(courtLocationRepository).findByOwner(any());

        BureauJwtPayload payload = TestUtils.createJwt("404", "COURT_USER", "99");
        AvailablePoolsInCourtLocationDto availablePools = new AvailablePoolsInCourtLocationDto();

        assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(
            () -> managePoolsService.findAvailablePools("404", payload));
        assertThat(availablePools.getAvailablePools()).isEmpty();
        mockitoVerificationFindAvailablePools(0);
    }

    @Test
    public void findAvailablePools_courtUser_noCourtsFoundForLocCode() {
        doReturn(createCourtLocations()).when(courtLocationRepository).findByOwner(any());

        BureauJwtPayload payload = TestUtils.createJwt("404", "COURT_USER", "99");
        AvailablePoolsInCourtLocationDto availablePools = new AvailablePoolsInCourtLocationDto();

        assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(
            () -> managePoolsService.findAvailablePools("999", payload));
        assertThat(availablePools.getAvailablePools()).isEmpty();
        mockitoVerificationFindAvailablePools(0);
    }

    @Test
    public void findAvailablePools_courtUser_noActivePools() {
        doReturn(createCourtLocations()).when(courtLocationRepository).findByOwner(any());
        doReturn(new ArrayList<>()).when(poolRequestRepository).findActivePoolsForDateRange(any(), any(), any(), any());

        BureauJwtPayload payload = TestUtils.createJwt("404", "COURT_USER", "99");
        AvailablePoolsInCourtLocationDto availablePools = managePoolsService.findAvailablePools("404", payload);

        assertThat(availablePools.getAvailablePools()).isEmpty();
        mockitoVerificationFindAvailablePools(1);
    }

    private void mockitoVerificationFindAvailablePools(
        int poolRequestFindActivePoolsForDateRange) {
        verify(courtLocationRepository, times(0)).findById(anyString());
        verify(courtLocationRepository, times(1)).findByOwner(anyString());
        verify(poolRequestRepository, times(poolRequestFindActivePoolsForDateRange)).findActivePoolsForDateRange(any(),
            any(), any(), any());
    }

    private List<Tuple> createActivePools() {
        List<Tuple> activePools = new ArrayList<>();
        com.querydsl.core.Tuple tuple = Mockito.mock(Tuple.class);
        setUpMockQueryResult(tuple, "111111111", LocalDate.of(2023, 6, 22), 5, 1);
        activePools.add(tuple);

        return activePools;
    }

    private List<CourtLocation> createCourtLocations() {
        List<CourtLocation> courtLocationList = new ArrayList<>();

        CourtLocation courtLocation1 = createCourtLocation("404", "404", "COURT LOC 404");
        courtLocationList.add(courtLocation1);

        CourtLocation courtLocation2 = createCourtLocation("404", "501", "COURT LOC 501");
        courtLocationList.add(courtLocation2);

        return courtLocationList;
    }

    private CourtLocation createCourtLocation(String owner, String locCode, String name) {
        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setOwner(owner);
        courtLocation.setLocCode(locCode);
        courtLocation.setName(name);

        return courtLocation;
    }


    public static void setUpMockQueryResult(Tuple deferralOption,
                                            String poolNumber,
                                            LocalDate serviceStartDate,
                                            int poolMembersRequested,
                                            int activePoolMemberCount) {
        doReturn(poolNumber).when(deferralOption).get(0, String.class);
        doReturn(serviceStartDate).when(deferralOption).get(1, LocalDate.class);
        doReturn(poolMembersRequested).when(deferralOption).get(2, Integer.class);
        doReturn(activePoolMemberCount).when(deferralOption).get(3, Integer.class);
    }

    @Test
    public void test_getPoolMonitoringStats_validateGetStatisticsByCourtLocationAndPoolType() {
        BureauJwtPayload bureauPayload = TestUtils.createJwt("400", "BUREAU_USER");
        List<Tuple> poolStatistics = poolStatisticsRepository
            .getStatisticsByCourtLocationAndPoolType(bureauPayload.getOwner(), COURT_LOCATION_CODE, POOL_TYPE,
                NUMBER_OF_WEEKS);
        assertThat(poolStatistics.size()).as("Expected list size of nine").isEqualTo(9);

        Tuple stats = poolStatistics.get(0);
        assertThat(stats.get(0, LocalDate.class)).isExactlyInstanceOf(LocalDate.class);
        assertThat(stats.get(1, String.class)).isExactlyInstanceOf(String.class);
        assertThat(stats.get(6, int.class)).isExactlyInstanceOf(Integer.class);
        assertThat(stats.get(4, int.class)).isExactlyInstanceOf(Integer.class);
        assertThat(stats.get(5, int.class)).isExactlyInstanceOf(Integer.class);
        assertThat(stats.get(7, int.class)).isExactlyInstanceOf(Integer.class);

        assertThat(stats.get(0, LocalDate.class))
            .as(stringLogMessage(), LocalDate.now())
            .isEqualTo(LocalDate.now());
        assertThat(stats.get(1, String.class))
            .as(stringLogMessage(), POOL_NUMBER)
            .isEqualTo(POOL_NUMBER);
        assertThat(stats.get(6, int.class)).as(numericLogMessage(), 300).isEqualTo(300);
        assertThat(stats.get(4, int.class)).as(numericLogMessage(), 40).isEqualTo(40);
        assertThat(stats.get(5, int.class)).as(numericLogMessage(), 10).isEqualTo(10);
        assertThat(stats.get(7, int.class)).as(numericLogMessage(), 50).isEqualTo(50);
    }

    @Test
    public void test_getPoolMonitoringStats_nilPool() {
        BureauJwtPayload bureauPayload = TestUtils.createJwt("400", "BUREAU_USER");
        List<Tuple> poolStatistics = poolStatisticsRepository
            .getNilPools(bureauPayload.getOwner(), COURT_LOCATION_CODE, POOL_TYPE, NUMBER_OF_WEEKS);
        assertThat(poolStatistics.size()).as("Expected list size of two").isEqualTo(2);

        Tuple stats = poolStatistics.get(0);
        assertThat(stats.get(0, String.class))
            .as(stringLogMessage(), "111111111")
            .isEqualTo("111111111");
        assertThat(stats.get(1, int.class))
            .as(stringLogMessage(), "0")
            .isEqualTo(0);
        assertThat(stats.get(2, LocalDate.class))
            .as(stringLogMessage(), LocalDate.now())
            .isEqualTo(LocalDate.now());
        verify(poolStatisticsRepository, times(0))
            .getStatisticsByCourtLocationAndPoolType(anyString(), anyString(), anyString(),
                anyInt());
        verify(poolStatisticsRepository, times(1))
            .getNilPools(anyString(), anyString(), anyString(), anyInt());
    }

    @Test
    public void test_getPoolMonitoringStats_happyPath() {
        BureauJwtPayload bureauPayload = TestUtils.createJwt("400", "BUREAU_USER");
        SummoningProgressResponseDto dto = managePoolsService
            .getPoolMonitoringStats(bureauPayload, COURT_LOCATION_CODE, POOL_TYPE);

        verify(poolStatisticsRepository, times(1))
            .getStatisticsByCourtLocationAndPoolType(
                anyString(), anyString(), anyString(), anyInt());
        verify(poolStatisticsRepository, times(1)).getNilPools(
            anyString(), anyString(), anyString(), anyInt());

        assertThat(dto.getStatsByWeek().size())
            .as("Expected size to be eight")
            .isEqualTo(8);
        assertThat(dto.getStatsByWeek().get(0).getStartOfWeek())
            .as("Expected start of week date to be %s", DateUtils.getStartOfWeekFromDate(LocalDate.now()))
            .isEqualTo(DateUtils.getStartOfWeekFromDate(LocalDate.now()));
        assertThat(dto.getStatsByWeek().get(0).getStats().size())
            .as("Expected size to be ten")
            .isEqualTo(10);
        assertThat(dto.getStatsByWeek().get(0).getStats().get(0).summoned)
            .as("Expected summon amount to be greater than zero")
            .isGreaterThanOrEqualTo(0);
        assertThat(dto.getStatsByWeek().get(0).getStats().get(0).requested)
            .as("Expected requested amount to be greater than zero")
            .isGreaterThanOrEqualTo(0);
        assertThat(dto.getStatsByWeek().get(0).getStats().get(0).confirmed)
            .as("Expected confirmed amount to be greater than zero")
            .isGreaterThanOrEqualTo(0);
        assertThat(dto.getStatsByWeek().get(0).getStats().get(0).unavailable)
            .as("Expected unavailable amount to be greater than zero")
            .isGreaterThanOrEqualTo(0);

    }

    @Test
    public void test_getPoolMonitoringStats_unhappyPath() {
        BureauJwtPayload bureauPayload = TestUtils.createJwt("400", "BUREAU_USER");
        assertThatExceptionOfType(MojException.NotFound.class)
            .isThrownBy(() -> managePoolsService.getPoolMonitoringStats(bureauPayload, COURT_LOCATION_CODE, "HGH"));
    }

    @Test
    public void test_getPoolMonitoringStats_serviceStartDateBeforeCurrentWeek() {
        BureauJwtPayload bureauPayload = TestUtils.createJwt("400", "BUREAU_USER");
        doReturn(getStatisticsBeforeCurrentWeek()).when(poolStatisticsRepository)
            .getStatisticsByCourtLocationAndPoolType(
                bureauPayload.getOwner(), COURT_LOCATION_CODE, POOL_TYPE, NUMBER_OF_WEEKS);
        doReturn(null).when(poolStatisticsRepository).getNilPools(
            bureauPayload.getOwner(), COURT_LOCATION_CODE, POOL_TYPE, NUMBER_OF_WEEKS);
        SummoningProgressResponseDto dto = managePoolsService
            .getPoolMonitoringStats(bureauPayload, COURT_LOCATION_CODE, POOL_TYPE);
        assertThat(dto.getStatsByWeek().size()).isEqualTo(8);
        assertThat(dto.getStatsByWeek().get(0).getStats().size()).isEqualTo(0);
    }

    @Test
    public void test_getPoolMonitoringStats_serviceStartDateAfterCurrentWeek() {
        BureauJwtPayload bureauPayload = TestUtils.createJwt("400", "BUREAU_USER");
        doReturn(getStatisticsAfterCurrentWeek()).when(poolStatisticsRepository)
            .getStatisticsByCourtLocationAndPoolType(
                bureauPayload.getOwner(), COURT_LOCATION_CODE, POOL_TYPE, NUMBER_OF_WEEKS);
        doReturn(null).when(poolStatisticsRepository).getNilPools(
            bureauPayload.getOwner(), COURT_LOCATION_CODE, POOL_TYPE, NUMBER_OF_WEEKS);
        SummoningProgressResponseDto dto = managePoolsService
            .getPoolMonitoringStats(bureauPayload, COURT_LOCATION_CODE, POOL_TYPE);
        assertThat(dto.getStatsByWeek().size()).isEqualTo(8);
        assertThat(dto.getStatsByWeek().get(0).getStats().size()).isEqualTo(0);
        assertThat(dto.getStatsByWeek().get(1).getStats().size()).isEqualTo(1);
    }

    @Test
    public void test_getPoolMonitoringStats_nilPoolServiceStartDateAfterCurrentWeek() {
        BureauJwtPayload bureauPayload = TestUtils.createJwt("400", "BUREAU_USER");
        doReturn(null).when(poolStatisticsRepository).getStatisticsByCourtLocationAndPoolType(
            bureauPayload.getOwner(), COURT_LOCATION_CODE, POOL_TYPE, NUMBER_OF_WEEKS);
        doReturn(getNilPoolAfterCurrentWeek()).when(poolStatisticsRepository).getNilPools(
            bureauPayload.getOwner(), COURT_LOCATION_CODE, POOL_TYPE, NUMBER_OF_WEEKS);
        SummoningProgressResponseDto dto = managePoolsService
            .getPoolMonitoringStats(bureauPayload, COURT_LOCATION_CODE, POOL_TYPE);
        assertThat(dto.getStatsByWeek().size()).isEqualTo(8);
        assertThat(dto.getStatsByWeek().get(0).getStats().size()).isEqualTo(0);
        assertThat(dto.getStatsByWeek().get(1).getStats().size()).isEqualTo(1);
    }


    @Test
    public void test_getPoolMonitoringStats_nilPoolsOnly() {
        BureauJwtPayload bureauPayload = TestUtils.createJwt("400", "BUREAU_USER");
        doReturn(null).when(poolStatisticsRepository).getStatisticsByCourtLocationAndPoolType(
            bureauPayload.getOwner(), COURT_LOCATION_CODE, POOL_TYPE, NUMBER_OF_WEEKS);
        SummoningProgressResponseDto dto = managePoolsService
            .getPoolMonitoringStats(bureauPayload, COURT_LOCATION_CODE, POOL_TYPE);
        assertThat(dto.getStatsByWeek().size()).isEqualTo(8);
        assertThat(dto.getStatsByWeek().get(0).getStats().size()).isEqualTo(2);
    }

    private static String stringLogMessage() {
        return "Expected value to be %s";
    }

    private static String numericLogMessage() {
        return "Expected value to be %d";
    }

    private List<Tuple> getStatisticsByCourtLocationAndPoolTypeResults(int numberOfWeeks, String poolNumber) {
        List<Tuple> results = new ArrayList<>();
        for (int i = 0;
             i < numberOfWeeks;
             i++) {
            Tuple t = Mockito.mock(Tuple.class);
            doReturn(LocalDate.now()).when(t).get(0, LocalDate.class);
            doReturn(String.format("%s%02d", poolNumber, i + 1)).when(t).get(1, String.class);
            doReturn(300).when(t).get(6, int.class);
            doReturn(40).when(t).get(4, int.class);
            doReturn(10).when(t).get(5, int.class);
            doReturn(50).when(t).get(7, int.class);
            results.add(t);
        }

        // nil pool
        Tuple t = Mockito.mock(Tuple.class);
        doReturn(LocalDate.now()).when(t).get(0, LocalDate.class);
        doReturn(String.format("%s%02d", poolNumber, 9)).when(t).get(1, String.class);
        doReturn(0).when(t).get(6, int.class);
        doReturn(0).when(t).get(4, int.class);
        doReturn(0).when(t).get(5, int.class);
        doReturn(0).when(t).get(7, int.class);
        results.add(t);

        return results;
    }

    private List<Tuple> getNilPools() {
        Tuple t = Mockito.mock(Tuple.class);
        doReturn(0).when(t).get(1, int.class);
        doReturn(LocalDate.now()).when(t).get(2, LocalDate.class);
        doReturn("111111111").when(t).get(0, String.class);
        List<Tuple> tuples = new ArrayList<>();
        tuples.add(t);

        t = Mockito.mock(Tuple.class);
        doReturn(0).when(t).get(1, int.class);
        doReturn(LocalDate.now()).when(t).get(2, LocalDate.class);
        doReturn(String.format("%s%02d", "4152306", 9)).when(t).get(0, String.class);
        tuples.add(t);
        return tuples;
    }

    private List<Tuple> getStatisticsBeforeCurrentWeek() {
        Tuple t = Mockito.mock(Tuple.class);
        doReturn(LocalDate.now().minusWeeks(1)).when(t).get(0, LocalDate.class);
        doReturn("111111111").when(t).get(1, String.class);
        doReturn(300).when(t).get(6, int.class);
        doReturn(40).when(t).get(4, int.class);
        doReturn(10).when(t).get(5, int.class);
        doReturn(50).when(t).get(7, int.class);
        return Collections.singletonList(t);
    }

    private List<Tuple> getStatisticsAfterCurrentWeek() {
        Tuple t = Mockito.mock(Tuple.class);
        doReturn(LocalDate.now().plusWeeks(1)).when(t).get(0, LocalDate.class);
        doReturn("111111111").when(t).get(1, String.class);
        doReturn(300).when(t).get(6, int.class);
        doReturn(40).when(t).get(4, int.class);
        doReturn(10).when(t).get(5, int.class);
        doReturn(50).when(t).get(7, int.class);
        return Collections.singletonList(t);
    }

    private List<Tuple> getNilPoolAfterCurrentWeek() {
        Tuple t = Mockito.mock(Tuple.class);
        doReturn(LocalDate.now().plusWeeks(1)).when(t).get(0, LocalDate.class);
        doReturn(0).when(t).get(1, int.class);
        doReturn(LocalDate.now().plusWeeks(1)).when(t).get(2, LocalDate.class);
        doReturn("111111111").when(t).get(0, String.class);
        return Collections.singletonList(t);
    }
}
