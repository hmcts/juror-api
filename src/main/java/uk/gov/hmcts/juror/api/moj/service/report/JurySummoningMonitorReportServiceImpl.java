package uk.gov.hmcts.juror.api.moj.service.report;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.JurySummoningMonitorReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.JurySummoningMonitorReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.PoolType;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class JurySummoningMonitorReportServiceImpl implements JurySummoningMonitorReportService {
    private final CourtLocationRepository courtLocationRepository;
    private final JurorRepository jurorRepository;

    @Override
    public JurySummoningMonitorReportResponse viewJurySummoningMonitorReport(
        JurySummoningMonitorReportRequest jurySummoningMonitorReportRequest) {

        JurySummoningMonitorReportResponse response = new JurySummoningMonitorReportResponse();

        if ("POOL".equals(jurySummoningMonitorReportRequest.getSearchBy())) {
            log.info("User {} viewing jury summoning monitor report by pool for pool number: {}",
                SecurityUtil.getActiveLogin(),
                jurySummoningMonitorReportRequest.getPoolNumber());

        } else {
            log.info("User {} viewing jury summoning monitor report by court",
                SecurityUtil.getActiveLogin());
        }

        return new JurySummoningMonitorReportResponse();
    }

    private Map<String, AbstractReportResponse.DataTypeValue>
        getSearchByPoolHeaders(String court, String poolNumber, PoolType poolType,
                           LocalDate serviceStartDate) {
        return Map.of(
            "date", AbstractReportResponse.DataTypeValue.builder()
                .displayName(ReportHeading.COURT.getDisplayName())
                .dataType(ReportHeading.COURT.getDataType())
                .value(court)
                .build(),
            "pool_number", AbstractReportResponse.DataTypeValue.builder()
                .displayName(ReportHeading.POOL_NUMBER.getDisplayName())
                .dataType(ReportHeading.POOL_NUMBER.getDataType())
                .value(poolNumber)
                .build(),
            "pool_type", AbstractReportResponse.DataTypeValue.builder()
                .displayName(ReportHeading.POOL_TYPE.getDisplayName())
                .dataType(ReportHeading.POOL_TYPE.getDataType())
                .value(poolType.getDescription())
                .build(),
            "service_start_date", AbstractReportResponse.DataTypeValue.builder()
                .displayName(ReportHeading.SERVICE_START_DATE.getDisplayName())
                .dataType(ReportHeading.SERVICE_START_DATE.getDataType())
                .value(serviceStartDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .build(),
            "report_created", AbstractReportResponse.DataTypeValue.builder()
                .displayName(ReportHeading.REPORT_CREATED.getDisplayName())
                .dataType(ReportHeading.REPORT_CREATED.getDataType())
                .value(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                .build(),
            "time_created", AbstractReportResponse.DataTypeValue.builder()
                .displayName(ReportHeading.TIME_CREATED.getDisplayName())
                .dataType(ReportHeading.TIME_CREATED.getDataType())
                .value(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build()
        );
    }

    private Map<String, AbstractReportResponse.DataTypeValue>
        getSearchByCourts(List<String> courtsList, LocalDate reportFrom, LocalDate reportTo) {

        StringBuilder courts = new StringBuilder();
        for (String court : courtsList) {
            courts.append(court).append(", ");
        }
        // remove the trailing comma
        courts.deleteCharAt(courts.length() - 2);
        String courtsString = courts.toString().trim();

        return Map.of(
                "courts", AbstractReportResponse.DataTypeValue.builder()
                .displayName(ReportHeading.COURTS.getDisplayName())
                .dataType(ReportHeading.COURTS.getDataType())
                .value(courtsString)
                .build(),
                "dateFrom", AbstractReportResponse.DataTypeValue.builder()
                .displayName(ReportHeading.DATE_FROM.getDisplayName())
                .dataType(ReportHeading.DATE_FROM.getDataType())
                .value(reportFrom.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .build(),
                "dateTo", AbstractReportResponse.DataTypeValue.builder()
                .displayName(ReportHeading.DATE_TO.getDisplayName())
                .dataType(ReportHeading.DATE_TO.getDataType())
                .value(reportTo.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .build(),
                "report_created", AbstractReportResponse.DataTypeValue.builder()
                .displayName(ReportHeading.REPORT_CREATED.getDisplayName())
                .dataType(ReportHeading.REPORT_CREATED.getDataType())
                .value(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                .build(),
                "time_created", AbstractReportResponse.DataTypeValue.builder()
                .displayName(ReportHeading.TIME_CREATED.getDisplayName())
                .dataType(ReportHeading.TIME_CREATED.getDataType())
                .value(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build()
        );
    }



    public enum ReportHeading {
        POOL_NUMBER("Pool number", LocalDate.class.getSimpleName()),
        DATE_FROM("Date from", LocalDate.class.getSimpleName()),
        DATE_TO("Date to", LocalDate.class.getSimpleName()),
        SERVICE_START_DATE("Service start date", LocalDate.class.getSimpleName()),
        REPORT_CREATED("Report created", LocalDate.class.getSimpleName()),
        TIME_CREATED("Time created", LocalDateTime.class.getSimpleName()),
        POOL_TYPE("Pool type", String.class.getSimpleName()),
        COURT("Court", String.class.getSimpleName()),
        COURTS("Courts", String.class.getSimpleName());

        private String displayName;

        private String dataType;

        ReportHeading(String displayName, String dataType) {
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
