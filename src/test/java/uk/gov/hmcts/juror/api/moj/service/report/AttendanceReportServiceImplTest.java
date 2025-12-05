package uk.gov.hmcts.juror.api.moj.service.report;

import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.WeekendAttendanceReportResponse;
import uk.gov.hmcts.juror.api.moj.repository.AppearanceRepository;
import uk.gov.hmcts.juror.api.moj.service.administration.AdministrationHolidaysService;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.juror.api.moj.service.report.AttendanceReportService.TableHeading.COURT_LOCATION_NAME_AND_CODE;
import static uk.gov.hmcts.juror.api.moj.service.report.AttendanceReportService.TableHeading.HOLIDAY_TOTAL;
import static uk.gov.hmcts.juror.api.moj.service.report.AttendanceReportService.TableHeading.SATURDAY_TOTAL;
import static uk.gov.hmcts.juror.api.moj.service.report.AttendanceReportService.TableHeading.SUNDAY_TOTAL;
import static uk.gov.hmcts.juror.api.moj.service.report.AttendanceReportService.TableHeading.TOTAL_PAID;

class AttendanceReportServiceImplTest {

    private final AttendanceReportServiceImpl attendanceReportService;

    private final AppearanceRepository appearanceRepository;
    private final AdministrationHolidaysService holidaysService;


    public AttendanceReportServiceImplTest() {
        this.appearanceRepository = mock(AppearanceRepository.class);
        this.holidaysService = mock(AdministrationHolidaysService.class);
        this.attendanceReportService = new AttendanceReportServiceImpl(appearanceRepository,
                                                                   holidaysService);
    }

    @BeforeEach
    void beforeEach() {
        TestUtils.setUpMockAuthentication("400", "TEST_USER", "1", List.of("400"));
    }


    @Nested
    @DisplayName("Weekend attendance report tests")
    @SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage") //false positive
    class WeekendAttendanceReportTests {

        @Test
        @SneakyThrows
        void weekendAttendanceReportNoData() {

            when(holidaysService.viewBankHolidays())
                .thenReturn(Map.of());
            when(appearanceRepository.getAllWeekendAttendances(anyList(), anyList(),
                                                                anyList(), anyList()))
                .thenReturn(List.of());

            WeekendAttendanceReportResponse response =
                attendanceReportService.getWeekendAttendanceReport();

            assertThat(response.getHeadings()).isNotNull();
            Map<String, AbstractReportResponse.DataTypeValue> headings = response.getHeadings();

            validateReportHeadings(headings);

            assertThat(response.getTableData()).isNotNull();
            WeekendAttendanceReportResponse.TableData tableData = response.getTableData();
            assertThat(tableData.getHeadings()).isNotNull();
            Assertions.assertThat(tableData.getHeadings()).hasSize(5);

            validateTableHeadings(tableData);

            Assertions.assertThat(tableData.getData()).isEmpty();
            verify(holidaysService, times(1)).viewBankHolidays();
            verify(appearanceRepository, times(1)).getAllWeekendAttendances(anyList(), anyList(),
                                                                            anyList(), anyList());

        }

        private void validateTableHeadings(WeekendAttendanceReportResponse.TableData tableData) {
            WeekendAttendanceReportResponse.TableData.Heading tablHeading = tableData.getHeadings().get(0);
            assertThat(tablHeading.getId()).isEqualTo(COURT_LOCATION_NAME_AND_CODE);
            assertThat(tablHeading.getName()).isEqualTo("Court Location Name And Code");
            assertThat(tablHeading.getDataType()).isEqualTo("String");

            tablHeading = tableData.getHeadings().get(1);
            assertThat(tablHeading.getId()).isEqualTo(SATURDAY_TOTAL);
            assertThat(tablHeading.getName()).isEqualTo("Saturday");
            assertThat(tablHeading.getDataType()).isEqualTo("Integer");

            tablHeading = tableData.getHeadings().get(2);
            assertThat(tablHeading.getId()).isEqualTo(SUNDAY_TOTAL);
            assertThat(tablHeading.getName()).isEqualTo("Sunday");
            assertThat(tablHeading.getDataType()).isEqualTo("Integer");

            tablHeading = tableData.getHeadings().get(3);
            assertThat(tablHeading.getId()).isEqualTo(HOLIDAY_TOTAL);
            assertThat(tablHeading.getName()).isEqualTo("Bank holiday");
            assertThat(tablHeading.getDataType()).isEqualTo("Integer");

            tablHeading = tableData.getHeadings().get(4);
            assertThat(tablHeading.getId()).isEqualTo(TOTAL_PAID);
            assertThat(tablHeading.getName()).isEqualTo("Total paid");
            assertThat(tablHeading.getDataType()).isEqualTo("Double");
        }

        private void validateReportHeadings(Map<String, AbstractReportResponse.DataTypeValue> headings) {

            //validate the report heading map is empty
            assertThat(headings)
                .isNotNull()
                .isEmpty();
        }
    }

}
