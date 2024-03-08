package uk.gov.hmcts.juror.api.moj.service;

import com.querydsl.core.types.Predicate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.response.CourtLocationDataDto;
import uk.gov.hmcts.juror.api.moj.controller.response.CourtLocationListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.CourtRates;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.CourtQueriesRepository;
import uk.gov.hmcts.juror.api.moj.service.expense.JurorExpenseService;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.juror.api.moj.domain.CourtLocationQueries.filterByLocCodes;

@ExtendWith(SpringExtension.class)
@SuppressWarnings({
    "PMD.ExcessiveImports",
    "PMD.TooManyMethods"
})
class CourtLocationServiceTest {

    @Mock
    private CourtLocationRepository courtLocationRepository;

    @Mock
    private CourtQueriesRepository courtQueriesRepository;

    @Mock
    private JurorExpenseService expenseService;


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
    void testBuildCourtLocationDataResponseBureauUser() {
        BureauJWTPayload payload = TestUtils.createJwt("400", "BUREAU_USER");
        CourtLocationListDto dto = courtLocationService.buildCourtLocationDataResponse(payload);

        verify(courtLocationRepository, times(1)).findAll();
        List<CourtLocationDataDto> courtLocationDataList = dto.getData();
        assertThat(courtLocationDataList.size()).isEqualTo(4);

        verifyCourtLocationDataMapping(courtLocationList, courtLocationDataList);
    }

    @Test
    void testBuildCourtLocationDataResponseCourtUserNoSatellites() {
        BureauJWTPayload payload = TestUtils.createJwt("435", "COURT_USER");
        List<String> staffCourts = Collections.singletonList("435");
        payload.setStaff(TestUtils.staffBuilder("Test Staff", 1, staffCourts));
        doReturn(courtLocationList.stream().filter(courtLocation ->
            courtLocation.getOwner().equalsIgnoreCase(payload.getOwner())).collect(Collectors.toList()))
            .when(courtLocationRepository).findAll(filterByLocCodes(staffCourts));

        CourtLocationListDto dto = courtLocationService.buildCourtLocationDataResponse(payload);

        verify(courtLocationRepository, times(1)).findAll(Mockito.any(Predicate.class));

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
    void testBuildCourtLocationDataResponseCourtUserWithSatellites() {
        BureauJWTPayload payload = TestUtils.createJwt("415", "COURT_USER");
        List<String> staffCourts = Arrays.asList("415", "462");
        payload.setStaff(TestUtils.staffBuilder("Test Staff", 1, staffCourts));
        List<CourtLocation> courtLocations = courtLocationList.stream().filter(courtLocation ->
            courtLocation.getOwner().equalsIgnoreCase(payload.getOwner())).collect(Collectors.toList());

        doReturn(courtLocations).when(courtLocationRepository).findAll(filterByLocCodes(staffCourts));

        CourtLocationListDto dto = courtLocationService.buildCourtLocationDataResponse(payload);

        verify(courtLocationRepository, times(1)).findAll(Mockito.any(Predicate.class));

        List<CourtLocationDataDto> courtLocationDataList = dto.getData();
        assertThat(courtLocationDataList.size()).isEqualTo(2);

        verifyCourtLocationDataMapping(courtLocations, courtLocationDataList);
    }


    @Test
    void testBuildAllCourtLocationDataResponse() {
        CourtLocationListDto dto = courtLocationService.buildAllCourtLocationDataResponse();

        verify(courtLocationRepository, times(1)).findAll();
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
            assertThat(courtLocationData.getAttendanceTime()).isEqualTo(courtLocation.getCourtAttendTime().format(
                DateTimeFormatter.ofPattern("HH:mm")));
        }

    }

    private CourtLocation buidCourtLocation(String owner, String locCode, String locationName) {
        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setOwner(owner);
        courtLocation.setLocCode(locCode);
        courtLocation.setName(locationName);
        courtLocation.setCourtAttendTime(LocalTime.of(9, 15));

        return courtLocation;
    }

    //Tests related to service method: getCourtLocationsByPostcode()
    @Test
    void positiveGetCourtLocationsByPostcodeHappyPostcodeLength7() {
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
    void positiveGetDisqualifyReasonsNoMatchInDatabase() {
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
    void positiveGetCourtRatesTypical() {
        final String locCode = "123";
        courtLocationService = spy(courtLocationService);


        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setPublicTransportSoftLimit(new BigDecimal("13.01300"));
        courtLocation.setTaxiSoftLimit(new BigDecimal("14.01400"));

        doReturn(courtLocation).when(courtLocationService)
            .getCourtLocation(locCode);

        CourtRates courtRates = courtLocationService.getCourtRates(locCode);
        assertThat(courtRates.getPublicTransportSoftLimit()).isEqualTo(new BigDecimal("13.01300"));
        assertThat(courtRates.getTaxiSoftLimit()).isEqualTo(new BigDecimal("14.01400"));
    }

    @Test
    void negativeGetCourtRatesCourtNotFound() {
        courtLocationService = spy(courtLocationService);
        doReturn(null).when(courtLocationService)
            .getCourtLocation(TestConstants.VALID_COURT_LOCATION);

        MojException.NotFound exception = assertThrows(MojException.NotFound.class,
            () -> courtLocationService.getCourtRates(TestConstants.VALID_COURT_LOCATION),
            "Expected getCourtRates to throw MojException.NotFound");
        assertThat(exception.getMessage()).isEqualTo("Court location not found");
        assertThat(exception.getCause()).isNull();
    }
}
