package uk.gov.hmcts.juror.api.moj.service.report;

import com.querydsl.core.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.DigitalSummonsRepliesReportResponse;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryModImpl;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SummonsRepliesReportServiceImpl implements SummonsRepliesReportService {
    private final JurorDigitalResponseRepositoryModImpl jurorDigitalResponseRepositoryMod;


    @Override
    @Transactional(readOnly = true)
    public DigitalSummonsRepliesReportResponse getDigitalSummonsRepliesReport(LocalDate month) {

        List<Tuple> results = jurorDigitalResponseRepositoryMod.getDigitalSummonsRepliesForMonth(month);
        List<DigitalSummonsRepliesReportResponse.TableData.DataRow> dataRows = new ArrayList<>();

        int totalReplies = 0;
        if (!results.isEmpty()) {
            for (Tuple result : results) {
                DigitalSummonsRepliesReportResponse.TableData.DataRow dataRow =
                    new DigitalSummonsRepliesReportResponse.TableData.DataRow();
                dataRow.setDate(result.get(0, Date.class).toLocalDate());
                final int total = result.get(1, Long.class).intValue();
                dataRow.setNoOfReplies(total);
                totalReplies += total;
                dataRows.add(dataRow);
            }
        }

        DigitalSummonsRepliesReportResponse reportResponse =
                new DigitalSummonsRepliesReportResponse(getDigitalSummonsRepliesReportHeaders(totalReplies));

        reportResponse.getTableData().setData(dataRows);

        return reportResponse;
    }


    private Map<String, AbstractReportResponse.DataTypeValue>
        getDigitalSummonsRepliesReportHeaders(int replyCount) {
        return Map.of(
            "report_created", AbstractReportResponse.DataTypeValue.builder()
                .displayName(ReportHeading.REPORT_CREATED.getDisplayName())
                .dataType(ReportHeading.REPORT_CREATED.getDataType())
                .value(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                .build(),
            "time_created", AbstractReportResponse.DataTypeValue.builder()
                .displayName(ReportHeading.TIME_CREATED.getDisplayName())
                .dataType(ReportHeading.TIME_CREATED.getDataType())
                .value(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build(),
            "reply_count", AbstractReportResponse.DataTypeValue.builder()
                .displayName(ReportHeading.REPLY_COUNT.getDisplayName())
                .dataType(ReportHeading.REPLY_COUNT.getDataType())
                .value(replyCount)
                .build()
        );
    }


    public enum ReportHeading {
        REPORT_CREATED("Report created", LocalDate.class.getSimpleName()),
        TIME_CREATED("Time created", LocalDateTime.class.getSimpleName()),
        REPLY_COUNT("Total number of replies received", Integer.class.getSimpleName());

        private final String displayName;

        private final String dataType;

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
