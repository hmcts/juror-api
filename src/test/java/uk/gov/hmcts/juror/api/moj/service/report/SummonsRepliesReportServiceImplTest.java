package uk.gov.hmcts.juror.api.moj.service.report;

import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.DigitalSummonsRepliesReportResponse;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryModImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.BDDAssertions.within;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.juror.api.moj.service.report.SummonsRepliesReportService.TableHeading.DATE;
import static uk.gov.hmcts.juror.api.moj.service.report.SummonsRepliesReportService.TableHeading.NO_OF_REPLIES;

class SummonsRepliesReportServiceImplTest {

    private final JurorDigitalResponseRepositoryModImpl jurorDigitalResponseRepositoryMod;
    private final SummonsRepliesReportService summonsRepliesReportService;

    public SummonsRepliesReportServiceImplTest() {
        this.jurorDigitalResponseRepositoryMod = mock(JurorDigitalResponseRepositoryModImpl.class);
        this.summonsRepliesReportService = new SummonsRepliesReportServiceImpl(jurorDigitalResponseRepositoryMod);
    }

    @BeforeEach
    void beforeEach() {
        TestUtils.setUpMockAuthentication("400", "TEST_USER", "1", List.of("400"));
    }


    @Nested
    @DisplayName("Digital Summons Replies report tests")
    @SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage") //false positive
    class DigitalSummonsRepliesReportTests {

        @Test
        @SneakyThrows
        void viewDigitalSummonsRepliesNoData() {

            final LocalDate month = LocalDate.parse("2025-06-01");

            when(jurorDigitalResponseRepositoryMod.getDigitalSummonsRepliesForMonth(month))
                .thenReturn(List.of());

            DigitalSummonsRepliesReportResponse response =
                summonsRepliesReportService.getDigitalSummonsRepliesReport(month);

            assertThat(response.getHeadings()).isNotNull();
            Map<String, AbstractReportResponse.DataTypeValue> headings = response.getHeadings();

            validateReportHeadings(headings);

            assertThat(response.getTableData()).isNotNull();
            DigitalSummonsRepliesReportResponse.TableData tableData = response.getTableData();
            assertThat(tableData.getHeadings()).isNotNull();
            Assertions.assertThat(tableData.getHeadings()).hasSize(2);

            validateTableHeadings(tableData);

            Assertions.assertThat(tableData.getData()).isEmpty();
            verify(jurorDigitalResponseRepositoryMod, times(1)).getDigitalSummonsRepliesForMonth(month);

        }

        private void validateTableHeadings(DigitalSummonsRepliesReportResponse.TableData tableData) {
            DigitalSummonsRepliesReportResponse.TableData.Heading tablHeading = tableData.getHeadings().get(0);
            assertThat(tablHeading.getId()).isEqualTo(DATE);
            assertThat(tablHeading.getName()).isEqualTo("Date");
            assertThat(tablHeading.getDataType()).isEqualTo("LocalDate");

            tablHeading = tableData.getHeadings().get(1);
            assertThat(tablHeading.getId()).isEqualTo(NO_OF_REPLIES);
            assertThat(tablHeading.getName()).isEqualTo("No of replies received");
            assertThat(tablHeading.getDataType()).isEqualTo("Integer");

        }

        private void validateReportHeadings(Map<String, AbstractReportResponse.DataTypeValue> headings) {
            assertThat(headings.get("report_created")).isEqualTo(AbstractReportResponse.DataTypeValue.builder()
                .displayName("Report created")
                .dataType("LocalDate")
                .value(LocalDate.now().toString())
                .build());
            assertThat(headings.get("reply_count")).isEqualTo(AbstractReportResponse.DataTypeValue.builder()
                .displayName("Total number of replies received")
                .dataType("Integer")
                .value(Integer.valueOf(0))
                .build());

            AbstractReportResponse.DataTypeValue timeCreated = headings.get("time_created");
            assertThat(timeCreated.getDisplayName()).isEqualTo("Time created");
            assertThat(timeCreated.getDataType()).isEqualTo("LocalDateTime");
            LocalDateTime createdTime = LocalDateTime.parse((String) timeCreated.getValue(),
                                                            DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            assertThat(createdTime).isCloseTo(LocalDateTime.now(), within(10, ChronoUnit.SECONDS))
                .as("Creation time should be correct");
        }
    }

}
