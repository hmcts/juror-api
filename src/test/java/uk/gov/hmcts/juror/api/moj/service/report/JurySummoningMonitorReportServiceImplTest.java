package uk.gov.hmcts.juror.api.moj.service.report;

import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.JurySummoningMonitorReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.JurySummoningMonitorReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.PoolType;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.BDDAssertions.within;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@SuppressWarnings({
    "PMD.AssertionsShouldIncludeMessage",
    "PMD.UnnecessaryFullyQualifiedName"
})
class JurySummoningMonitorReportServiceImplTest {

    private final static String RESULT_DEFAULTS = "0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,"
        + "0,0,0,0,0,0,0,0,0,0,0";
    private final CourtLocationRepository courtLocationRepository;
    private final PoolRequestRepository poolRequestRepository;
    private final JurorPoolRepository jurorPoolRepository;
    private final JurySummoningMonitorReportService jurySummoningMonitorReportService;

    public JurySummoningMonitorReportServiceImplTest() {
        this.courtLocationRepository = mock(CourtLocationRepository.class);
        this.poolRequestRepository = mock(PoolRequestRepository.class);
        this.jurorPoolRepository = mock(JurorPoolRepository.class);
        this.jurySummoningMonitorReportService = new JurySummoningMonitorReportServiceImpl(jurorPoolRepository,
            courtLocationRepository, poolRequestRepository);

    }

    @BeforeEach
    void beforeEach() {
        TestUtils.setUpMockAuthentication("415", "TEST_USER", "1", List.of("415"));
    }


    @Nested
    @DisplayName("Jury summoning monitor report by Pool tests")
    @SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage") //false positive
    class JurySummoningMonitorByPoolTests {

        @Test
        @SneakyThrows
        void viewJurySummoningMonitorByPoolReportNoResultsAndValidHeadings() {

            final String poolNumber = "415230701";
            CourtLocation court = setupCourt("415", "415");

            JurySummoningMonitorReportRequest jurySummoningMonitorReportRequest =
                JurySummoningMonitorReportRequest.builder()
                    .searchBy("POOL")
                    .poolNumber("415230701")
                    .build();

            PoolRequest poolRequest = PoolRequest.builder()
                .courtLocation(court)
                .poolNumber(poolNumber)
                .poolType(new PoolType("CRO", "Crown Court"))
                .returnDate(LocalDate.of(2024, 5, 13))
                .build();

            when(poolRequestRepository.findByPoolNumber(poolNumber)).thenReturn(Optional.of(poolRequest));
            when(jurorPoolRepository.getJsmReportByPool(poolNumber)).thenReturn(RESULT_DEFAULTS);

            JurySummoningMonitorReportResponse response =
                jurySummoningMonitorReportService.viewJurySummoningMonitorReport(
                    jurySummoningMonitorReportRequest);

            assertThat(response.getHeadings()).isNotNull();
            Map<String, AbstractReportResponse.DataTypeValue> headings = response.getHeadings();

            validateReportHeadings(headings);

            AbstractReportResponse.DataTypeValue timeCreated = headings.get("time_created");
            assertThat(timeCreated.getDisplayName()).isEqualTo("Time created");
            assertThat(timeCreated.getDataType()).isEqualTo("LocalDateTime");
            LocalDateTime createdTime = LocalDateTime.parse((String) timeCreated.getValue(),
                DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            assertThat(createdTime).isCloseTo(LocalDateTime.now(), within(10, ChronoUnit.SECONDS))
                .as("Creation time should be correct");

            verify(poolRequestRepository, times(1)).findByPoolNumber(poolNumber);
            verify(jurorPoolRepository, times(1)).getJsmReportByPool(poolNumber);
        }


        private void validateReportHeadings(Map<String, AbstractReportResponse.DataTypeValue> headings) {

            Assertions.assertThat(headings.get("court")).isEqualTo(AbstractReportResponse.DataTypeValue.builder()
                .displayName("Court")
                .dataType("String")
                .value("Test Court (415)")
                .build());
            assertThat(headings.get("pool_number")).isEqualTo(AbstractReportResponse.DataTypeValue.builder()
                .displayName("Pool number")
                .dataType("String")
                .value("415230701")
                .build());
            assertThat(headings.get("pool_type")).isEqualTo(AbstractReportResponse.DataTypeValue.builder()
                .displayName("Pool type")
                .dataType("String")
                .value("Crown Court")
                .build());
            assertThat(headings.get("service_start_date")).isEqualTo(AbstractReportResponse.DataTypeValue.builder()
                .displayName("Service start date")
                .dataType("LocalDate")
                .value(LocalDate.of(2024, 5, 13).format(DateTimeFormatter.ISO_LOCAL_DATE))
                .build());
            assertThat(headings.get("report_created")).isEqualTo(AbstractReportResponse.DataTypeValue.builder()
                .displayName("Report created")
                .dataType("LocalDate")
                .value(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                .build());

        }

        @Test
        void viewJurySummoningMonitorReportInvalidPoolNumber() {

            final String poolNumber = "987654321";

            JurySummoningMonitorReportRequest jurySummoningMonitorReportRequest =
                JurySummoningMonitorReportRequest.builder()
                    .searchBy("POOL")
                    .poolNumber(poolNumber)
                    .build();

            when(poolRequestRepository.findByPoolNumber(poolNumber)).thenReturn(Optional.empty());

            assertThatExceptionOfType(MojException.NotFound.class)
                .isThrownBy(() -> jurySummoningMonitorReportService.viewJurySummoningMonitorReport(
                    jurySummoningMonitorReportRequest));

            verify(poolRequestRepository, times(1)).findByPoolNumber(poolNumber);

        }
    }


    @Nested
    @DisplayName("Jury summoning monitor report by Court tests")
    @SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage") //false positive
    class JurySummoningMonitorByCourtTests {

        @Test
        @SneakyThrows
        void viewJurySummoningMonitorByCourtReportNoResultsAndValidHeadings() {

            final String locCode = "415";

            CourtLocation court = setupCourt(locCode, "415");

            JurySummoningMonitorReportRequest jurySummoningMonitorReportRequest =
                JurySummoningMonitorReportRequest.builder()
                    .searchBy("COURT")
                    .courtLocCodes(List.of(locCode))
                    .fromDate(LocalDate.of(2024, 4, 19))
                    .toDate(LocalDate.of(2024, 5, 20))
                    .build();

            when(courtLocationRepository.findByLocCodeIn(List.of(locCode)))
                .thenReturn(List.of(court));

            JurySummoningMonitorReportResponse response =
                jurySummoningMonitorReportService.viewJurySummoningMonitorReport(
                    jurySummoningMonitorReportRequest);

            assertThat(response.getHeadings()).isNotNull();
            Map<String, AbstractReportResponse.DataTypeValue> headings = response.getHeadings();

            validateReportHeadings(headings);

            AbstractReportResponse.DataTypeValue timeCreated = headings.get("time_created");
            assertThat(timeCreated.getDisplayName()).isEqualTo("Time created");
            assertThat(timeCreated.getDataType()).isEqualTo("LocalDateTime");
            LocalDateTime createdTime = LocalDateTime.parse((String) timeCreated.getValue(),
                DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            assertThat(createdTime).as("Creation time should be correct")
                .isCloseTo(LocalDateTime.now(), within(10, ChronoUnit.SECONDS));

            verify(courtLocationRepository, times(1)).findByLocCodeIn(List.of(locCode));

        }

        private void validateReportHeadings(Map<String, AbstractReportResponse.DataTypeValue> headings) {

            Assertions.assertThat(headings.get("courts")).isEqualTo(AbstractReportResponse.DataTypeValue.builder()
                .displayName("Courts")
                .dataType("String")
                .value("Test Court (415)")
                .build());
            assertThat(headings.get("date_from")).isEqualTo(AbstractReportResponse.DataTypeValue.builder()
                .displayName("Date from")
                .dataType("LocalDate")
                .value("2024-04-19")
                .build());
            assertThat(headings.get("date_to")).isEqualTo(AbstractReportResponse.DataTypeValue.builder()
                .displayName("Date to")
                .dataType("LocalDate")
                .value("2024-05-20")
                .build());
            assertThat(headings.get("report_created")).isEqualTo(AbstractReportResponse.DataTypeValue.builder()
                .displayName("Report created")
                .dataType("LocalDate")
                .value(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                .build());
        }

        @Test
        void viewJurySummoningMonitorReportEmptyCourtsList() {

            JurySummoningMonitorReportRequest jurySummoningMonitorReportRequest =
                JurySummoningMonitorReportRequest.builder()
                    .searchBy("COURT")
                    .courtLocCodes(List.of())
                    .fromDate(LocalDate.of(2024, 4, 19))
                    .toDate(LocalDate.of(2024, 5, 20))
                    .build();

            assertThatExceptionOfType(MojException.BadRequest.class)
                .isThrownBy(() -> jurySummoningMonitorReportService
                    .viewJurySummoningMonitorReport(jurySummoningMonitorReportRequest));

            verifyNoInteractions(courtLocationRepository);
        }
    }


    private CourtLocation setupCourt(String locCode, String owner) {
        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setName("Test Court");
        courtLocation.setLocCode(locCode);
        courtLocation.setOwner(owner);
        when(courtLocationRepository.findById(locCode))
            .thenReturn(Optional.of(courtLocation));

        return courtLocation;
    }

}
