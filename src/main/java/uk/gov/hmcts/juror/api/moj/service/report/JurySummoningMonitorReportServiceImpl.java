package uk.gov.hmcts.juror.api.moj.service.report;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.JurySummoningMonitorReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.JurySummoningMonitorReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.PoolType;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.utils.PoolRequestUtils;
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
    private final PoolRequestRepository poolRequestRepository;

    @Override
    public JurySummoningMonitorReportResponse viewJurySummoningMonitorReport(
        JurySummoningMonitorReportRequest jurySummoningMonitorReportRequest) {

        final boolean isSearchByPool = "POOL".equals(jurySummoningMonitorReportRequest.getSearchBy());

        JurySummoningMonitorReportResponse response = new JurySummoningMonitorReportResponse();

        setupResponseHeaders(isSearchByPool, jurySummoningMonitorReportRequest, response);

        // TODO implement the logic to get the report data

        return response;
    }

    private void setupResponseHeaders(boolean isSearchByPool,
                                      JurySummoningMonitorReportRequest jurySummoningMonitorReportRequest,
                           JurySummoningMonitorReportResponse response) {
        if (isSearchByPool) {

            String poolNumber = jurySummoningMonitorReportRequest.getPoolNumber();

            log.info("User {} viewing jury summoning monitor report by pool for pool number: {}",
                SecurityUtil.getActiveLogin(),
                poolNumber);

            PoolRequest poolRequest = PoolRequestUtils.getActivePoolRecord(poolRequestRepository, poolNumber);

            response.setHeadings(
                getSearchByPoolHeaders(poolRequest.getCourtLocation(), poolNumber, poolRequest.getPoolType(),
                    poolRequest.getReturnDate()));

        } else {
            log.info("User {} viewing jury summoning monitor report by court",
                SecurityUtil.getActiveLogin());

            if (!jurySummoningMonitorReportRequest.isAllCourts()
                && (jurySummoningMonitorReportRequest.getCourtLocCodes() == null
                || jurySummoningMonitorReportRequest.getCourtLocCodes().isEmpty())) {
                throw new MojException.BadRequest("No court locations provided", null);
            }

            response.setHeadings(getSearchByCourts(jurySummoningMonitorReportRequest.getCourtLocCodes(),
                jurySummoningMonitorReportRequest.isAllCourts(),
                jurySummoningMonitorReportRequest.getFromDate(),
                jurySummoningMonitorReportRequest.getToDate()));
        }
    }

    private Map<String, AbstractReportResponse.DataTypeValue>
        getSearchByPoolHeaders(CourtLocation court, String poolNumber, PoolType poolType,
                           LocalDate serviceStartDate) {

        String courtName = court.getName() + " (" + court.getLocCode() + ")";
        return Map.of(
            "court", AbstractReportResponse.DataTypeValue.builder()
                .displayName(ReportHeading.COURT.getDisplayName())
                .dataType(ReportHeading.COURT.getDataType())
                .value(courtName)
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
        getSearchByCourts(List<String> courtsList, boolean allCourts, LocalDate reportFrom, LocalDate reportTo) {

        String courts = "All courts";
        if (!allCourts) {

            StringBuilder courtsBuilder = new StringBuilder();

            courtLocationRepository.findByLocCodeIn(courtsList).stream().forEach(c -> {
                courtsBuilder.append(c.getName()).append(" (").append(c.getLocCode()).append(')').append(", ");
            });

            // remove the trailing comma if we have any courts
            if (courtsBuilder.length() > 2) {
                courtsBuilder.deleteCharAt(courtsBuilder.length() - 2);
            }
            courts = courtsBuilder.toString().trim();
        }

        return Map.of(
            "courts", AbstractReportResponse.DataTypeValue.builder()
                .displayName(ReportHeading.COURTS.getDisplayName())
                .dataType(ReportHeading.COURTS.getDataType())
                .value(courts)
                .build(),
            "date_from", AbstractReportResponse.DataTypeValue.builder()
                .displayName(ReportHeading.DATE_FROM.getDisplayName())
                .dataType(ReportHeading.DATE_FROM.getDataType())
                .value(reportFrom.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .build(),
            "date_to", AbstractReportResponse.DataTypeValue.builder()
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
        POOL_NUMBER("Pool number", String.class.getSimpleName()),
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
