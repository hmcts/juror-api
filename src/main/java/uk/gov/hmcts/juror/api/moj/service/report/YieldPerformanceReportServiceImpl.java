package uk.gov.hmcts.juror.api.moj.service.report;

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
import uk.gov.hmcts.juror.api.moj.repository.IPoolCommentRepositoryImpl;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepositoryImpl;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepositoryImpl.getYieldPerformanceData;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class YieldPerformanceReportServiceImpl implements YieldPerformanceReportService {
    private final JurorPoolRepository jurorPoolRepository;
    private final IPoolCommentRepository poolCommentRepository;
    private final CourtQueriesRepository courtQueriesRepository;


    @Override
    public YieldPerformanceReportResponse viewYieldPerformanceReport(
        YieldPerformanceReportRequest yieldPerformanceReportRequest) {

        YieldPerformanceReportResponse response = setupResponseHeaders(yieldPerformanceReportRequest);

        List<String> courtLocCodesList = yieldPerformanceReportRequest.isAllCourts()
            ? courtQueriesRepository.getAllCourtLocCodes(false)
            : yieldPerformanceReportRequest.getCourtLocCodes();

        String courtLocCodes = String.join(",", courtLocCodesList);

        List<JurorPoolRepositoryImpl.YieldPerformanceData>
            yieldPerformanceReportStats = getYieldPerformanceData(jurorPoolRepository,courtLocCodes,
            yieldPerformanceReportRequest.getFromDate(),
            yieldPerformanceReportRequest.getToDate());

        List<IPoolCommentRepositoryImpl.PoolComment> poolComments =
            poolCommentRepository.findPoolCommentsForLocationsAndDates(
            courtLocCodesList,
            yieldPerformanceReportRequest.getFromDate(),
            yieldPerformanceReportRequest.getToDate());

        Map<String, StringBuilder> poolCommentsMap = new HashMap<>();
        if (poolComments != null) {
            poolCommentsMap = getPoolsCommentsMap(poolComments);
        }
        setupResponseDto(response, yieldPerformanceReportStats, poolCommentsMap);

        return response;
    }

    private static Map<String, StringBuilder> getPoolsCommentsMap(
        List<IPoolCommentRepositoryImpl.PoolComment> poolComments) {
        Map<String, StringBuilder> poolCommentsMap = new HashMap<>();

        for (IPoolCommentRepositoryImpl.PoolComment poolComment : poolComments) {
            poolCommentsMap.computeIfPresent(poolComment.getLocCode(),
                (key, val) -> val.append(System.lineSeparator()).append(poolComment.getPoolNo())
                    .append(" - ").append(poolComment.getComment()));
            poolCommentsMap.putIfAbsent(poolComment.getLocCode(), new StringBuilder(poolComment.getPoolNo())
                .append(" - ").append(poolComment.getComment()));
        }
        return poolCommentsMap;
    }

    private void setupResponseDto(YieldPerformanceReportResponse response,
                                  List<JurorPoolRepositoryImpl.YieldPerformanceData> yieldPerformanceReportStats,
                                  Map<String, StringBuilder> poolCommentsMap) {

        for (JurorPoolRepositoryImpl.YieldPerformanceData data : yieldPerformanceReportStats) {

            response.getTableData().getData().add(
                YieldPerformanceReportResponse.TableData.YieldData.builder()
                    .court(data.getCourt() + " (" + data.getLocCode() + ")")
                    .requested(data.getRequested())
                    .confirmed(data.getConfirmed())
                    .comments(poolCommentsMap.get(data.getLocCode()) != null
                        ? poolCommentsMap.get(data.getLocCode()).toString()
                        : "")
                    .balance(data.getBalance())
                    .difference(data.getDifference())
                    .build());
        }

    }

    private YieldPerformanceReportResponse setupResponseHeaders(
        YieldPerformanceReportRequest yieldPerformanceReportRequest) {

        log.info("User {} viewing yield performance report",
            SecurityUtil.getActiveLogin());

        if (!yieldPerformanceReportRequest.isAllCourts()
            && (yieldPerformanceReportRequest.getCourtLocCodes() == null
            || yieldPerformanceReportRequest.getCourtLocCodes().isEmpty())) {
            throw new MojException.BadRequest("No court locations provided", null);
        }

        YieldPerformanceReportResponse response = new YieldPerformanceReportResponse();

        response.setHeadings(getSearchByCourts(
            yieldPerformanceReportRequest.getFromDate(),
            yieldPerformanceReportRequest.getToDate()));

        return response;
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
