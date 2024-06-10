package uk.gov.hmcts.juror.api.moj.service.report;

import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.YieldPerformanceReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.YieldPerformanceReportResponse;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.CourtQueriesRepository;
import uk.gov.hmcts.juror.api.moj.repository.IPoolCommentRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.BDDAssertions.within;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.juror.api.moj.controller.reports.response.YieldPerformanceReportResponse.TableHeading.BALANCE;
import static uk.gov.hmcts.juror.api.moj.controller.reports.response.YieldPerformanceReportResponse.TableHeading.COMMENTS;
import static uk.gov.hmcts.juror.api.moj.controller.reports.response.YieldPerformanceReportResponse.TableHeading.CONFIRMED;
import static uk.gov.hmcts.juror.api.moj.controller.reports.response.YieldPerformanceReportResponse.TableHeading.COURT;
import static uk.gov.hmcts.juror.api.moj.controller.reports.response.YieldPerformanceReportResponse.TableHeading.DIFFERENCE;
import static uk.gov.hmcts.juror.api.moj.controller.reports.response.YieldPerformanceReportResponse.TableHeading.REQUESTED;

@SuppressWarnings({
    "PMD.AssertionsShouldIncludeMessage",
    "PMD.UnnecessaryFullyQualifiedName"
})
class YieldPerformanceReportServiceImplTest {

    private final JurorPoolRepository jurorPoolRepository;
    private final IPoolCommentRepository poolCommentRepository;
    private final CourtQueriesRepository courtQueriesRepository;
    private final YieldPerformanceReportService yieldPerformanceReportService;

    public YieldPerformanceReportServiceImplTest() {
        this.jurorPoolRepository = mock(JurorPoolRepository.class);
        this.poolCommentRepository = mock(IPoolCommentRepository.class);
        this.courtQueriesRepository = mock(CourtQueriesRepository.class);
        this.yieldPerformanceReportService = new YieldPerformanceReportServiceImpl(jurorPoolRepository,
            poolCommentRepository, courtQueriesRepository);
    }

    @BeforeEach
    void beforeEach() {
        TestUtils.setUpMockAuthentication("400", "BUREAU_USER", "1", List.of("400"));
    }

    @Nested
    @DisplayName("Yield Performance report tests")
    @SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage") //false positive
    class YieldPerformanceReportTests {

        @Test
        @SneakyThrows
        void viewYieldPerformanceReportAllCourtsNoResultsAndValidHeadings() {

            final LocalDate reportFromDate = LocalDate.of(2024, 4, 20);
            final LocalDate reportToDate = LocalDate.of(2024, 5, 13);

            YieldPerformanceReportRequest yieldPerformanceReportRequest = YieldPerformanceReportRequest.builder()
                .fromDate(reportFromDate)
                .toDate(reportToDate)
                .allCourts(true)
                .build();

            when(courtQueriesRepository.getAllCourtLocCodes(false))
                .thenReturn(List.of("415", "416"));

            when(poolCommentRepository.findPoolCommentsForLocationsAndDates(List.of("415", "416"), reportFromDate,
                reportToDate))
                .thenReturn(List.of());

            when(jurorPoolRepository.getYieldPerformanceReportStats("415,416", reportFromDate, reportToDate))
                .thenReturn(List.of());

            YieldPerformanceReportResponse response = yieldPerformanceReportService.viewYieldPerformanceReport(
                yieldPerformanceReportRequest
            );

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

            assertThat(response.getTableData()).isNotNull();
            YieldPerformanceReportResponse.TableData tableData = response.getTableData();
            assertThat(tableData.getHeadings()).isNotNull();
            Assertions.assertThat(tableData.getHeadings()).hasSize(6);

            validateTableHeadings(tableData);

            assertThat(tableData.getData()).isNotNull();
            assertThat(tableData.getData().size()).isZero();

            verify(courtQueriesRepository, times(1)).getAllCourtLocCodes(false);
            verify(poolCommentRepository, times(1)).findPoolCommentsForLocationsAndDates(List.of("415", "416"),
                reportFromDate, reportToDate);
            verify(jurorPoolRepository, times(1)).getYieldPerformanceReportStats("415,416",
                reportFromDate, reportToDate);

        }

        private void validateTableHeadings(YieldPerformanceReportResponse.TableData tableData) {
            YieldPerformanceReportResponse.TableData.Heading tablHeading = tableData.getHeadings().get(0);
            assertThat(tablHeading.getId()).isEqualTo(COURT);
            assertThat(tablHeading.getName()).isEqualTo("Court");
            assertThat(tablHeading.getDataType()).isEqualTo("String");

            YieldPerformanceReportResponse.TableData.Heading tablHeading1 = tableData.getHeadings().get(1);
            assertThat(tablHeading1.getId()).isEqualTo(REQUESTED);
            assertThat(tablHeading1.getName()).isEqualTo("Requested");
            assertThat(tablHeading1.getDataType()).isEqualTo("Integer");

            YieldPerformanceReportResponse.TableData.Heading tablHeading2 = tableData.getHeadings().get(2);
            assertThat(tablHeading2.getId()).isEqualTo(CONFIRMED);
            assertThat(tablHeading2.getName()).isEqualTo("Confirmed");
            assertThat(tablHeading2.getDataType()).isEqualTo("Integer");

            YieldPerformanceReportResponse.TableData.Heading tablHeading3 = tableData.getHeadings().get(3);
            assertThat(tablHeading3.getId()).isEqualTo(BALANCE);
            assertThat(tablHeading3.getName()).isEqualTo("Balance");
            assertThat(tablHeading3.getDataType()).isEqualTo("Integer");

            YieldPerformanceReportResponse.TableData.Heading tablHeading4 = tableData.getHeadings().get(4);
            assertThat(tablHeading4.getId()).isEqualTo(DIFFERENCE);
            assertThat(tablHeading4.getName()).isEqualTo("Difference");
            assertThat(tablHeading4.getDataType()).isEqualTo("Double");

            YieldPerformanceReportResponse.TableData.Heading tablHeading5 = tableData.getHeadings().get(5);
            assertThat(tablHeading5.getId()).isEqualTo(COMMENTS);
            assertThat(tablHeading5.getName()).isEqualTo("Comments");
            assertThat(tablHeading5.getDataType()).isEqualTo("String");
        }

        private void validateReportHeadings(Map<String, AbstractReportResponse.DataTypeValue> headings) {
            Assertions.assertThat(headings.get("date_from")).isEqualTo(AbstractReportResponse.DataTypeValue.builder()
                .displayName("Date from")
                .dataType("LocalDate")
                .value("2024-04-20")
                .build());
            assertThat(headings.get("date_to")).isEqualTo(AbstractReportResponse.DataTypeValue.builder()
                .displayName("Date to")
                .dataType("LocalDate")
                .value("2024-05-13")
                .build());
            assertThat(headings.get("report_created")).isEqualTo(AbstractReportResponse.DataTypeValue.builder()
                .displayName("Report created")
                .dataType("LocalDate")
                .value(LocalDate.now().toString())
                .build());
        }

        @Test
        void viewYieldPerformanceReportNoCourtLocation() {

            final LocalDate reportFromDate = LocalDate.of(2024, 4, 20);
            final LocalDate reportToDate = LocalDate.of(2024, 5, 13);

            YieldPerformanceReportRequest yieldPerformanceReportRequest = YieldPerformanceReportRequest.builder()
                .fromDate(reportFromDate)
                .toDate(reportToDate)
                .allCourts(false)
                .courtLocCodes(List.of())
                .build();

            assertThatExceptionOfType(MojException.BadRequest.class)
                .isThrownBy(() -> yieldPerformanceReportService.viewYieldPerformanceReport(
                    yieldPerformanceReportRequest));

        }
    }

}
