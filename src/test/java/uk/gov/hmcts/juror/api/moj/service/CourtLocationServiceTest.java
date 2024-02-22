package uk.gov.hmcts.juror.api.moj.service;

import com.querydsl.core.types.Predicate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.data.history.Revision;
import org.springframework.data.history.RevisionSort;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.response.CourtLocationDataDto;
import uk.gov.hmcts.juror.api.moj.controller.response.CourtLocationListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.CourtRates;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.CourtQueriesRepository;
import uk.gov.hmcts.juror.api.moj.utils.RevisionUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.juror.api.moj.domain.CourtLocationQueries.filterByLocCodes;

@ExtendWith(SpringExtension.class)
public class CourtLocationServiceTest {

    @Mock
    private CourtLocationRepository courtLocationRepository;

    @Mock
    private CourtQueriesRepository courtQueriesRepository;


    @InjectMocks
    CourtLocationServiceImpl courtLocationService;

    List<CourtLocation> courtLocationList;

    @BeforeEach
    public void setUpCourts() {
        CourtLocation chester = buidCourtLocation("415", "415", "CHESTER");
        CourtLocation manchester = buidCourtLocation("435", "435", "MANCHESTER");
        CourtLocation satellite = buidCourtLocation("415", "462", "WARRINGTON");
        CourtLocation bureau = buidCourtLocation("400", "400", "Central Summonsing Bureau");
        courtLocationList = Arrays.asList(chester, manchester, bureau, satellite);

        doReturn(courtLocationList).when(courtLocationRepository).findAll();
    }

    @Test
    public void test_buildCourtLocationDataResponse_bureauUser() {
        BureauJWTPayload payload = TestUtils.createJwt("400", "BUREAU_USER");
        CourtLocationListDto dto = courtLocationService.buildCourtLocationDataResponse(payload);

        verify(courtLocationRepository, Mockito.times(1)).findAll();
        List<CourtLocationDataDto> courtLocationDataList = dto.getData();
        assertThat(courtLocationDataList.size()).isEqualTo(4);

        verifyCourtLocationDataMapping(courtLocationList, courtLocationDataList);
    }

    @Test
    public void test_buildCourtLocationDataResponse_courtUser_noSatellites() {
        BureauJWTPayload payload = TestUtils.createJwt("435", "COURT_USER");
        List<String> staffCourts = Collections.singletonList("435");
        payload.setStaff(TestUtils.staffBuilder("Test Staff", 1, staffCourts));
        doReturn(courtLocationList.stream().filter(courtLocation ->
            courtLocation.getOwner().equalsIgnoreCase(payload.getOwner())).collect(Collectors.toList()))
            .when(courtLocationRepository).findAll(filterByLocCodes(staffCourts));

        CourtLocationListDto dto = courtLocationService.buildCourtLocationDataResponse(payload);

        verify(courtLocationRepository, Mockito.times(1)).findAll(Mockito.any(Predicate.class));

        List<CourtLocationDataDto> courtLocationDataList = dto.getData();
        assertThat(courtLocationDataList.size()).isEqualTo(1);

        CourtLocationDataDto courtLocationData =
            courtLocationDataList.stream().findFirst().orElse(null);
        assert courtLocationData != null;

        assertThat(courtLocationData.getLocationName()).isEqualTo("MANCHESTER");
        assertThat(courtLocationData.getLocationCode()).isEqualTo("435");
        assertThat(courtLocationData.getAttendanceTime()).isEqualTo("09:15");
    }

    @Test
    public void test_buildCourtLocationDataResponse_courtUser_withSatellites() {
        BureauJWTPayload payload = TestUtils.createJwt("415", "COURT_USER");
        List<String> staffCourts = Arrays.asList("415", "462");
        payload.setStaff(TestUtils.staffBuilder("Test Staff", 1, staffCourts));
        List<CourtLocation> courtLocations = courtLocationList.stream().filter(courtLocation ->
            courtLocation.getOwner().equalsIgnoreCase(payload.getOwner())).collect(Collectors.toList());

        doReturn(courtLocations).when(courtLocationRepository).findAll(filterByLocCodes(staffCourts));

        CourtLocationListDto dto = courtLocationService.buildCourtLocationDataResponse(payload);

        verify(courtLocationRepository, Mockito.times(1)).findAll(Mockito.any(Predicate.class));

        List<CourtLocationDataDto> courtLocationDataList = dto.getData();
        assertThat(courtLocationDataList.size()).isEqualTo(2);

        verifyCourtLocationDataMapping(courtLocations, courtLocationDataList);
    }


    @Test
    public void test_buildAllCourtLocationDataResponse() {
        CourtLocationListDto dto = courtLocationService.buildAllCourtLocationDataResponse();

        verify(courtLocationRepository, Mockito.times(1)).findAll();
        List<CourtLocationDataDto> courtLocationDataList = dto.getData();
        assertThat(courtLocationDataList.size()).isEqualTo(4);

        verifyCourtLocationDataMapping(courtLocationList, courtLocationDataList);
    }

    private void verifyCourtLocationDataMapping(List<CourtLocation> courtLocations,
                                                List<CourtLocationDataDto> courtLocationDataList) {

        for (CourtLocation courtLocation : courtLocations) {
            CourtLocationDataDto courtLocationData =
                courtLocationDataList.stream().filter(courtLocationDataDto ->
                        courtLocationDataDto.getLocationCode().equalsIgnoreCase(courtLocation.getLocCode())).findFirst()
                    .orElse(null);
            assert courtLocationData != null;

            assertThat(courtLocationData.getLocationName()).isEqualTo(courtLocation.getName());
            assertThat(courtLocationData.getLocationCode()).isEqualTo(courtLocation.getLocCode());
            assertThat(courtLocationData.getAttendanceTime()).isEqualTo(courtLocation.getCourtAttendTime());
        }

    }

    private CourtLocation buidCourtLocation(String owner, String locCode, String locationName) {
        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setOwner(owner);
        courtLocation.setLocCode(locCode);
        courtLocation.setName(locationName);
        courtLocation.setCourtAttendTime("09:15");

        return courtLocation;
    }

    //Tests related to service method: getCourtLocationsByPostcode()
    @Test
    public void getCourtLocationsByPostcode_happy_postcodeLength7() {
        final ArgumentCaptor<String> firstHalfOfPostcodeCaptor = ArgumentCaptor.forClass(String.class);

        doReturn(getCourtDetailsFilteredByPostcode()).when(courtQueriesRepository)
            .getCourtDetailsFilteredByPostcode(any(String.class));

        List<CourtLocationDataDto> courtLocationsDto = courtLocationService.getCourtLocationsByPostcode("MK49");

        verify(courtQueriesRepository, times(1)).getCourtDetailsFilteredByPostcode(
            firstHalfOfPostcodeCaptor.capture());
        assertThat(firstHalfOfPostcodeCaptor.getValue()).isEqualTo("MK49");

        assertThat(courtLocationsDto).hasSize(1);
        assertThat(courtLocationsDto.get(0).getLocationCode()).isEqualTo("440");
        assertThat(courtLocationsDto.get(0).getLocationName()).isEqualTo("Inner London");
        assertThat(courtLocationsDto.get(0).getAttendanceTime()).isEqualTo("");
    }

    @Test
    public void getDisqualifyReasons_noMatchInDatabase() {
        final ArgumentCaptor<String> firstHalfOfPostcodeCaptor = ArgumentCaptor.forClass(String.class);


        doReturn(List.of()).when(courtQueriesRepository)
            .getCourtDetailsFilteredByPostcode(any(String.class));

        List<CourtLocationDataDto> courtLocationsDto = courtLocationService.getCourtLocationsByPostcode("AB12");

        verify(courtQueriesRepository, times(1)).getCourtDetailsFilteredByPostcode(
            firstHalfOfPostcodeCaptor.capture());
        assertThat(firstHalfOfPostcodeCaptor.getValue()).isEqualTo("AB12");

        assertThat(courtLocationsDto).isEmpty();
    }

    private List<CourtLocationDataDto> getCourtDetailsFilteredByPostcode() {
        return List.of(new CourtLocationDataDto("440", "Inner London", ""));
    }


    @Test
    void getCourtRatesTypical() {
        final String locCode = "123";
        final LocalDate date = LocalDate.now();
        courtLocationService = spy(courtLocationService);

        CourtLocation courtLocation = new CourtLocation();

        courtLocation.setCarMileageRatePerMile0Passengers(new BigDecimal("1.01000"));
        courtLocation.setCarMileageRatePerMile1Passengers(new BigDecimal("2.02000"));
        courtLocation.setCarMileageRatePerMile2OrMorePassengers(new BigDecimal("3.03000"));

        courtLocation.setMotorcycleMileageRatePerMile0Passengers(new BigDecimal("4.04000"));
        courtLocation.setMotorcycleMileageRatePerMile1Passengers(new BigDecimal("5.05000"));

        courtLocation.setBikeRate(new BigDecimal("6.06000"));
        courtLocation.setLimitFinancialLossHalfDay(new BigDecimal("7.07000"));
        courtLocation.setLimitFinancialLossFullDay(new BigDecimal("8.08000"));
        courtLocation.setLimitFinancialLossHalfDayLongTrial(new BigDecimal("9.09000"));
        courtLocation.setLimitFinancialLossFullDayLongTrial(new BigDecimal("10.01000"));
        courtLocation.setSubsistenceRateStandard(new BigDecimal("11.01100"));
        courtLocation.setSubsistenceRateLongDay(new BigDecimal("12.01200"));
        courtLocation.setPublicTransportSoftLimit(new BigDecimal("13.01300"));

        doReturn(courtLocation).when(courtLocationService)
            .getCourtLocationFromEffectiveFromDate(locCode, date);

        CourtRates courtRates = courtLocationService.getCourtRates(locCode, date);

        assertThat(courtRates.getCarRate0Passengers()).isEqualTo(new BigDecimal("1.01000"));
        assertThat(courtRates.getCarRate1Passenger()).isEqualTo(new BigDecimal("2.02000"));
        assertThat(courtRates.getCarRate2OrMorePassenger()).isEqualTo(new BigDecimal("3.03000"));

        assertThat(courtRates.getMotorcycleRate0Passenger()).isEqualTo(new BigDecimal("4.04000"));
        assertThat(courtRates.getMotorcycleRate1OrMorePassenger()).isEqualTo(new BigDecimal("5.05000"));

        assertThat(courtRates.getBicycleRate0OrMorePassenger()).isEqualTo(new BigDecimal("6.06000"));

        assertThat(courtRates.getFinancialLossHalfDayLimit()).isEqualTo(new BigDecimal("7.07000"));
        assertThat(courtRates.getFinancialLossFullDayLimit()).isEqualTo(new BigDecimal("8.08000"));
        assertThat(courtRates.getFinancialLossHalfDayLongTrialLimit()).isEqualTo(new BigDecimal("9.09000"));
        assertThat(courtRates.getFinancialLossFullDayLongTrialLimit()).isEqualTo(new BigDecimal("10.01000"));

        assertThat(courtRates.getSubsistenceRateStandard()).isEqualTo(new BigDecimal("11.01100"));
        assertThat(courtRates.getSubsistenceRateLongDay()).isEqualTo(new BigDecimal("12.01200"));
        assertThat(courtRates.getPublicTransportSoftLimit()).isEqualTo(new BigDecimal("13.01300"));
    }

    @Nested
    @DisplayName(" CourtLocation getCourtLocationFromEffectiveFromDate(String locCode, LocalDate date)")
    class GetCourtLocationFromEffectiveFromDate {

        private final String validLocCode = "415";
        private MockedStatic<RevisionUtil> mockRevisionUtil;

        @SuppressWarnings("unchecked")
        private Revision<Long, CourtLocation> mockRevision(LocalDate date) {
            CourtLocation courtLocation = mock(CourtLocation.class);
            when(courtLocation.getRatesEffectiveFrom()).thenReturn(date);

            Revision<Long, CourtLocation> revision = mock(Revision.class);
            when(revision.getEntity()).thenReturn(courtLocation);
            return revision;
        }

        @BeforeEach
        void beforeEach() {
            mockRevisionUtil = mockStatic(RevisionUtil.class);
        }

        @AfterEach
        void afterEach() {
            if (mockRevisionUtil != null) {
                mockRevisionUtil.close();
            }

        }

        @Test
        void positiveEqualToEffectiveDate() {
            LocalDate date = LocalDate.of(2023, 2, 5);
            Revision<Long, CourtLocation> courtLocation1 = mockRevision(LocalDate.of(2023, 4, 1));
            Revision<Long, CourtLocation> courtLocation2 = mockRevision(date);
            Revision<Long, CourtLocation> courtLocation3 = mockRevision(LocalDate.of(2023, 3, 1));


            List<Revision<Long, CourtLocation>> revisions = List.of(courtLocation1, courtLocation2, courtLocation3);

            mockRevisionUtil.when(() -> RevisionUtil.findRevisionsSorted(
                    courtLocationRepository, validLocCode, RevisionSort.desc()))
                .thenReturn(revisions.stream());


            CourtLocation foundCourtLocation =
                courtLocationService.getCourtLocationFromEffectiveFromDate(validLocCode, date);

            assertThat(foundCourtLocation).isNotNull().isEqualTo(courtLocation2.getEntity());
        }

        @Test
        void positiveGreaterThanEffectiveDate() {
            LocalDate date = LocalDate.of(2023, 2, 5);
            Revision<Long, CourtLocation> courtLocation1 = mockRevision(LocalDate.of(2023, 4, 1));
            Revision<Long, CourtLocation> courtLocation2 = mockRevision(LocalDate.of(2023, 1, 27));
            Revision<Long, CourtLocation> courtLocation3 = mockRevision(LocalDate.of(2023, 3, 1));


            List<Revision<Long, CourtLocation>> revisions = List.of(courtLocation1, courtLocation2, courtLocation3);

            mockRevisionUtil.when(() -> RevisionUtil.findRevisionsSorted(
                    courtLocationRepository, validLocCode, RevisionSort.desc()))
                .thenReturn(revisions.stream());


            CourtLocation foundCourtLocation =
                courtLocationService.getCourtLocationFromEffectiveFromDate(validLocCode, date);

            assertThat(foundCourtLocation).isNotNull().isEqualTo(courtLocation2.getEntity());

        }

        @Test
        void positiveNullEffectiveDate() {
            LocalDate date = LocalDate.of(2023, 2, 5);
            Revision<Long, CourtLocation> courtLocation1 = mockRevision(LocalDate.of(2023, 4, 1));
            Revision<Long, CourtLocation> courtLocation2 = mockRevision(null);
            Revision<Long, CourtLocation> courtLocation3 = mockRevision(LocalDate.of(2023, 1, 1));


            List<Revision<Long, CourtLocation>> revisions = List.of(courtLocation1, courtLocation2, courtLocation3);

            mockRevisionUtil.when(() -> RevisionUtil.findRevisionsSorted(
                    courtLocationRepository, validLocCode, RevisionSort.desc()))
                .thenReturn(revisions.stream());


            CourtLocation foundCourtLocation =
                courtLocationService.getCourtLocationFromEffectiveFromDate(validLocCode, date);

            assertThat(foundCourtLocation).isNotNull().isEqualTo(courtLocation2.getEntity());

        }

        @Test
        void negativeNotFound() {
            LocalDate date = LocalDate.of(2023, 2, 5);
            MojException.NotFound exception = assertThrows(
                MojException.NotFound.class,
                () -> courtLocationService.getCourtLocationFromEffectiveFromDate(validLocCode, date),
                "Should throw an exception when no dates are found"
            );

            assertThat(exception).isNotNull();
            assertThat(exception.getCause()).isNull();
            assertThat(exception.getMessage()).isNotNull()
                .isEqualTo("No court location rates are active on date: 2023-02-05 for court 415");
        }

        @Test
        void negativeNotFoundEffectiveDateInFuture() {
            LocalDate date = LocalDate.of(2022, 2, 5);
            Revision<Long, CourtLocation> courtLocation1 = mockRevision(LocalDate.of(2023, 4, 1));
            Revision<Long, CourtLocation> courtLocation2 = mockRevision(LocalDate.of(2023, 3, 1));
            Revision<Long, CourtLocation> courtLocation3 = mockRevision(LocalDate.of(2023, 1, 1));


            List<Revision<Long, CourtLocation>> revisions = List.of(courtLocation1, courtLocation2, courtLocation3);

            mockRevisionUtil.when(() -> RevisionUtil.findRevisionsSorted(
                    courtLocationRepository, validLocCode, RevisionSort.desc()))
                .thenReturn(revisions.stream());

            MojException.NotFound exception = assertThrows(
                MojException.NotFound.class,
                () -> courtLocationService.getCourtLocationFromEffectiveFromDate(validLocCode, date),
                "Should throw an exception when no dates are found"
            );

            assertThat(exception).isNotNull();
            assertThat(exception.getCause()).isNull();
            assertThat(exception.getMessage()).isNotNull().isEqualTo("No court location rates are active on date: "
                + "2022-02-05 for court 415");
        }

    }
}
