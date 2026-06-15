package uk.gov.hmcts.juror.api.moj.service.report;

import uk.gov.hmcts.juror.api.moj.controller.reports.request.CourtsAndDatesReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.SittingDaysStatsReportResponse;

public interface SittingDaysReportService {

    SittingDaysStatsReportResponse getSittingDaysStats(CourtsAndDatesReportRequest courtsAndDatesReportRequest);

    enum TableHeading {
        COURT_LOCATION_NAME_AND_CODE("Court", String.class.getSimpleName()),
        ZERO_SITTING_DAYS("0 days", Integer.class.getSimpleName()),
        ONE_SITTING_DAY("1 day", Integer.class.getSimpleName()),
        TWO_SITTING_DAYS("2 days", Integer.class.getSimpleName()),
        THREE_SITTING_DAYS("3 days", Integer.class.getSimpleName()),
        FOUR_SITTING_DAYS("4 days", Integer.class.getSimpleName()),
        FIVE_SITTING_DAYS("5 days", Integer.class.getSimpleName()),
        SIX_SITTING_DAYS("6 days", Integer.class.getSimpleName()),
        SEVEN_SITTING_DAYS("7 days", Integer.class.getSimpleName()),
        EIGHT_SITTING_DAYS("8 days", Integer.class.getSimpleName()),
        NINE_SITTING_DAYS("9 days", Integer.class.getSimpleName()),
        TEN_SITTING_DAYS("10 days", Integer.class.getSimpleName()),
        ELEVEN_OR_MORE_SITTING_DAYS("11 or more days", Integer.class.getSimpleName()),
        TOTAL_JURORS("Total jurors", Integer.class.getSimpleName()),
        TOTAL_SITTING_DAYS("Total sitting days", Integer.class.getSimpleName());

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
