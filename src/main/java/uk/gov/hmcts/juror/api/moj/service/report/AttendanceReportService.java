package uk.gov.hmcts.juror.api.moj.service.report;

import uk.gov.hmcts.juror.api.moj.controller.reports.response.WeekendAttendanceReportResponse;

public interface AttendanceReportService {

    WeekendAttendanceReportResponse getWeekendAttendanceReport();

    enum TableHeading {
        COURT_LOCATION_NAME_AND_CODE("Court Location Name And Code", String.class.getSimpleName()),
        SATURDAY_TOTAL("Saturday",  Integer.class.getSimpleName()),
        SUNDAY_TOTAL("Sunday",  Integer.class.getSimpleName()),
        HOLIDAY_TOTAL("Bank holiday",  Integer.class.getSimpleName()),
        TOTAL_PAID("Total paid",  Double.class.getSimpleName());

        private final String displayName;
        private final String dataType;

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
