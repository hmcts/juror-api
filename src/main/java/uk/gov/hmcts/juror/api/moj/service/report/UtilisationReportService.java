package uk.gov.hmcts.juror.api.moj.service.report;

import uk.gov.hmcts.juror.api.moj.controller.reports.response.DailyUtilisationReportJurorsResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.DailyUtilisationReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.MonthlyUtilisationReportResponse;

import java.time.LocalDate;

public interface UtilisationReportService {
    DailyUtilisationReportResponse viewDailyUtilisationReport(String locCode, LocalDate reportFromDate,
                                                              LocalDate reportToDate);

    DailyUtilisationReportJurorsResponse viewDailyUtilisationJurors(String locCode, LocalDate reportDate);

    MonthlyUtilisationReportResponse generateMonthlyUtilisationReport(String locCode, LocalDate reportDate);

    MonthlyUtilisationReportResponse viewMonthlyUtilisationReport(String locCode, LocalDate reportDate,
                                                                 boolean previousMonths);

    String getMonthlyUtilisationReports(String locCode);


    enum TableHeading {
        DATE("Date", LocalDate.class.getSimpleName()),
        MONTH("Month", String.class.getSimpleName()),
        JUROR("Juror", String.class.getSimpleName()),
        JUROR_WORKING_DAYS("Juror working days", Integer.class.getSimpleName()),
        SITTING_DAYS("Sitting days", Integer.class.getSimpleName()),
        ATTENDANCE_DAYS("Attendance days", Integer.class.getSimpleName()),
        NON_ATTENDANCE_DAYS("Non-attendance days",  Integer.class.getSimpleName()),
        UTILISATION("Utilisation", Double.class.getSimpleName());

        private String displayName;
        private String dataType;

        TableHeading(String displayName, String dataType) {
            this.displayName = displayName;
            this.dataType = dataType;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDataType() {
            return dataType;
        }
    }
}
