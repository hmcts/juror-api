package uk.gov.hmcts.juror.api.moj.service.report;

import com.querydsl.core.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.YieldPerformanceReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.YieldPerformanceReportResponse;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.CourtQueriesRepository;
import uk.gov.hmcts.juror.api.moj.repository.IPoolCommentRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class YieldPerformanceReportServiceImpl implements YieldPerformanceReportService {
    private final JurorPoolRepository jurorPoolRepository;
    private final IPoolCommentRepository poolCommentRepository;
    private final CourtQueriesRepository courtQueriesRepository;


    @Override
    public YieldPerformanceReportResponse viewYieldPerformanceReport(
        YieldPerformanceReportRequest yieldPerformanceReportRequest){

        YieldPerformanceReportResponse response = new YieldPerformanceReportResponse();

        setupResponseHeaders(yieldPerformanceReportRequest, response);

        String courtLocCodes = yieldPerformanceReportRequest.isAllCourts()
            ? String.join(",", courtQueriesRepository.getAllCourtLocCodes(false))
            : String.join(",", yieldPerformanceReportRequest.getCourtLocCodes());
        try {
            List<String> yieldPerformanceReportStats =
                jurorPoolRepository.getYieldPerformanceReportStats(courtLocCodes,
                    yieldPerformanceReportRequest.getFromDate(),
                    yieldPerformanceReportRequest.getToDate());

            List<Tuple> poolComments = poolCommentRepository.findPoolCommentsForLocationsAndDates(
                yieldPerformanceReportRequest.getCourtLocCodes(),
                yieldPerformanceReportRequest.getFromDate(),
                yieldPerformanceReportRequest.getToDate());

            Map<String, StringBuilder> poolCommentsMap = getPoolsCommentsMap(poolComments);

            setupResponseDto(response, yieldPerformanceReportStats, poolCommentsMap);

        } catch (Exception e) {
            log.error("Error getting yield performance report by court", e);
            throw new MojException.InternalServerError("Error getting yield performance report by court", e);
        }

        return response;
    }

    private static Map<String, StringBuilder> getPoolsCommentsMap(List<Tuple> poolComments) {
        Map<String, StringBuilder> poolCommentsMap = new HashMap<>();

        for (Tuple poolComment : poolComments) {
            poolCommentsMap.computeIfPresent(poolComment.get(0, String.class),
                (key, val) -> val.append("\r\n").append(poolComment.get(1, String.class))
                    .append(" - ").append(poolComment.get(2, String.class)));
            poolCommentsMap.putIfAbsent(poolComment.get(0, String.class), new StringBuilder(poolComment.get(1,
                String.class)).append(" - ").append(poolComment.get(2, String.class)));
        }
        return poolCommentsMap;
    }

    private void setupResponseDto(YieldPerformanceReportResponse response, List<String> yieldPerformanceReportStats,
                                  Map<String, StringBuilder> poolCommentsMap) {
        if (yieldPerformanceReportStats != null && !yieldPerformanceReportStats.isEmpty()) {
            for (String result : yieldPerformanceReportStats) {
                List<String> values = List.of(result.split(","));
                setupResponse(response, values, poolCommentsMap);
            }
        }
    }

    private void setupResponse(YieldPerformanceReportResponse response, List<String> result,
                               Map<String, StringBuilder> poolCommentsMap) {

        response.getYieldPerformanceData().add(YieldPerformanceReportResponse.YieldPerformanceData.builder()
            .courtLocation(result.get(0))
            .requested(Integer.parseInt(result.get(1)))
            .confirmed(Integer.parseInt(result.get(2)))
            .comments(poolCommentsMap.get(result.get(0)) != null
                ? poolCommentsMap.get(result.get(0)).toString()
                : "")
            .balance(0) // TODO: calculate balance
            .difference(0) // TODO: calculate difference
            .build());

    }

    private void setupResponseHeaders(YieldPerformanceReportRequest yieldPerformanceReportRequest,
                                      YieldPerformanceReportResponse response) {

        log.info("User {} viewing yield performance report",
            SecurityUtil.getActiveLogin());

        if (!yieldPerformanceReportRequest.isAllCourts()
            && (yieldPerformanceReportRequest.getCourtLocCodes() == null
            || yieldPerformanceReportRequest.getCourtLocCodes().isEmpty())) {
            throw new MojException.BadRequest("No court locations provided", null);
        }

        response.setHeadings(getSearchByCourts(
            yieldPerformanceReportRequest.getFromDate(),
            yieldPerformanceReportRequest.getToDate()));

    }


    private Map<String, AbstractReportResponse.DataTypeValue>
        getSearchByCourts(LocalDate reportFrom, LocalDate reportTo) {

        return Map.of(
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
        DATE_FROM("Date from", LocalDate.class.getSimpleName()),
        DATE_TO("Date to", LocalDate.class.getSimpleName()),
        REPORT_CREATED("Report created", LocalDate.class.getSimpleName()),
        TIME_CREATED("Time created", LocalDateTime.class.getSimpleName());

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
