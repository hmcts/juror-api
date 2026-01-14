package uk.gov.hmcts.juror.api.moj.service.report;

import com.querydsl.core.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.DigitalSummonsRepliesReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.ResponsesCompletedReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.CsvBuilder;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryModImpl;
import uk.gov.hmcts.juror.api.moj.service.summonsmanagement.JurorResponseService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SummonsRepliesReportServiceImpl implements SummonsRepliesReportService {
    private final JurorDigitalResponseRepositoryModImpl jurorDigitalResponseRepositoryMod;
    private final JurorResponseService jurorResponseService;


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

    @Override
    public ResponsesCompletedReportResponse getResponsesCompletedReport(LocalDate monthStartDate) {
        // log the method entry
        log.info("Generating Responses Completed Report for month starting: {}", monthStartDate);

        //check user is a bureau manager
        if (!SecurityUtil.isBureauManager()) {
            throw new MojException.Forbidden("User is not a bureau manager", null);
        }

        ResponsesCompletedReportResponse.TableData.Heading staffNameHeading =
            new ResponsesCompletedReportResponse.TableData.Heading();
        staffNameHeading.setId(0);
        staffNameHeading.setName("Staff Name");
        staffNameHeading.setDataType(String.class.getSimpleName());

        List<ResponsesCompletedReportResponse.TableData.Heading> tableHeadings = new ArrayList<>();
        tableHeadings.add(staffNameHeading);
        // need to add a heading for each day in the month
        LocalDate currentDate = monthStartDate;
        int dayCounter = 1;
        while (currentDate.getMonth() == monthStartDate.getMonth()) {
            ResponsesCompletedReportResponse.TableData.Heading dayHeading =
                new ResponsesCompletedReportResponse.TableData.Heading();
            dayHeading.setId(dayCounter);
            dayHeading.setName(currentDate.toString());
            dayHeading.setDataType(Integer.class.getSimpleName());
            tableHeadings.add(dayHeading);
            currentDate = currentDate.plusDays(1);
            dayCounter++;
        }
        // finally add a total heading
        ResponsesCompletedReportResponse.TableData.Heading totalHeading =
            new ResponsesCompletedReportResponse.TableData.Heading();
        totalHeading.setId(dayCounter+1);
        totalHeading.setName("Total");
        totalHeading.setDataType(Integer.class.getSimpleName());
        tableHeadings.add(totalHeading);

        // Todo: update totalProcessed count
        ResponsesCompletedReportResponse reportResponse = new ResponsesCompletedReportResponse(
            getResponsesCompletedReportHeaders(0));

        reportResponse.getTableData().setHeadings(tableHeadings);

        // need a count of the total responses completed on each day
        Map<LocalDate, Integer> totalResponsesByDate = new ConcurrentHashMap<>();

        List<SummonsRepliesReportService.CompletedResponseRecord> completedResponses =
            jurorResponseService.getResponsesCompletedReport(monthStartDate);

        // for each staff member, build a data row
        List<ResponsesCompletedReportResponse.TableData.DataRow> dataRows = new ArrayList<>();
        for (SummonsRepliesReportService.CompletedResponseRecord completedResponseRecord : completedResponses) {

            // the same staff member may have multiple records for different days, so check if we already have a row
            ResponsesCompletedReportResponse.TableData.DataRow dataRow = dataRows.stream()
                .filter(row -> row.getStaffName().equals(completedResponseRecord.getStaffName()))
                .findFirst()
                .orElse(null);
            if (dataRow == null) {
                dataRow = new ResponsesCompletedReportResponse.TableData.DataRow();
                dataRow.setStaffName(completedResponseRecord.getStaffName());
                // initialize the daily totals list with zeros for each day in the month
                List<Integer> dailyTotals = new ArrayList<>();
                LocalDate tempDate = monthStartDate;
                while (tempDate.getMonth() == monthStartDate.getMonth()) {
                    dailyTotals.add(0);
                    tempDate = tempDate.plusDays(1);

                }
                dataRow.setDailyTotals(dailyTotals);
                dataRow.setStaffTotal(0);
                dataRows.add(dataRow);
            }
            // set the completed responses for the appropriate day
            int dayOfMonth = completedResponseRecord.getDate().getDayOfMonth();
            dataRow.getDailyTotals().set(dayOfMonth - 1, completedResponseRecord.getCompletedResponses());
            // update the staff total
            dataRow.setStaffTotal(dataRow.getStaffTotal() + completedResponseRecord.getCompletedResponses());
            // update the total responses by date
            totalResponsesByDate.merge(completedResponseRecord.getDate(),
                                       completedResponseRecord.getCompletedResponses(),
                                       Integer::sum);
        }

        // add a final row for total responses per day
        ResponsesCompletedReportResponse.TableData.DataRow totalRow =
            new ResponsesCompletedReportResponse.TableData.DataRow();
        totalRow.setStaffName("Total Responses");
        List<Integer> totalDailyTotals = new ArrayList<>();
        int grandTotal = 0;
        LocalDate tempDate = monthStartDate;
        while (tempDate.getMonth() == monthStartDate.getMonth()) {
            Integer dailyTotal = totalResponsesByDate.getOrDefault(tempDate, 0);
            totalDailyTotals.add(dailyTotal);
            grandTotal += dailyTotal;
            tempDate = tempDate.plusDays(1);
        }
        totalRow.setDailyTotals(totalDailyTotals);
        totalRow.setStaffTotal(grandTotal);
        dataRows.add(totalRow);

        reportResponse.getTableData().setData(dataRows);
        log.info("Completed Responses Completed Report generation for month starting: {}", monthStartDate);
        return reportResponse;

    }

    @Override
    public String getResponsesCompletedReportCsv(LocalDate monthStartDate) {

        // run the report to get the data
        ResponsesCompletedReportResponse reportResponse = getResponsesCompletedReport(monthStartDate);

        CsvBuilder csvBuilder =
            new CsvBuilder(
                reportResponse.getTableData().getHeadings().stream()
                    .map(ResponsesCompletedReportResponse.TableData.Heading::getName)
                    .toList());
        // add each data row to the CSV
        for (ResponsesCompletedReportResponse.TableData.DataRow dataRow :
            reportResponse.getTableData().getData()) {
            List<String> rowValues = new ArrayList<>();
            rowValues.add(dataRow.getStaffName());
            for (Integer dailyTotal : dataRow.getDailyTotals()) {
                rowValues.add(dailyTotal.toString());
            }
            rowValues.add(dataRow.getStaffTotal().toString());
            csvBuilder.addRow(rowValues);
        }

        return csvBuilder.build();
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


    private Map<String, AbstractReportResponse.DataTypeValue>
        getResponsesCompletedReportHeaders(int totalProcessed) {
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
            "responses_processed", AbstractReportResponse.DataTypeValue.builder()
                .displayName(ReportHeading.RESPONSES_PROCESSED.getDisplayName())
                .dataType(ReportHeading.RESPONSES_PROCESSED.getDataType())
                .value(totalProcessed)
                .build()
        );
    }


    public enum ReportHeading {
        REPORT_CREATED("Report created", LocalDate.class.getSimpleName()),
        TIME_CREATED("Time created", LocalDateTime.class.getSimpleName()),
        REPLY_COUNT("Total number of replies received", Integer.class.getSimpleName()),
        RESPONSES_PROCESSED("Number of responses processed", Integer.class.getSimpleName());

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
