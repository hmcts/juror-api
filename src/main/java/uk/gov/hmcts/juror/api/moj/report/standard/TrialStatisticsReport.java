package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardTableData;
import uk.gov.hmcts.juror.api.moj.domain.trial.QTrial;
import uk.gov.hmcts.juror.api.moj.report.AbstractReport;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.utils.DateUtils;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
public class TrialStatisticsReport extends AbstractStandardReport {
    public TrialStatisticsReport() {
        super(QTrial.trial,
            DataType.TRIAL_JUDGE_NAME,
            DataType.TRIAL_TYPE,
            DataType.TRIAL_NUMBER,
            DataType.TRIAL_PANELLED_COUNT,
            DataType.TRIAL_JURORS_COUNT,
            DataType.TRIAL_NUMBER_START_DATE,
            DataType.TRIAL_NUMBER_END_DATE
        );
        isCourtUserOnly();
    }

    @Override
    protected void postProcessTableData(StandardReportRequest request,
                                        AbstractReportResponse.TableData<StandardTableData> tableData) {
        final String id = "number_of_days";
        tableData.getHeadings()
            .add(AbstractReportResponse.TableData.Heading.builder()
                .id(id)
                .name("Number of days")
                .dataType(Long.class.getSimpleName())
                .build());

        tableData.getData()
            .forEach(stringObjectLinkedHashMap -> {
                Object trialStart = stringObjectLinkedHashMap.get(DataType.TRIAL_NUMBER_START_DATE.getId());
                Object trialEnd = stringObjectLinkedHashMap.get(DataType.TRIAL_NUMBER_END_DATE.getId());
                if (trialStart == null || trialEnd == null) {
                    return;
                }
                LocalDate startDate = LocalDate.from(DateTimeFormatter.ISO_DATE.parse(trialStart.toString()));
                LocalDate endDate = LocalDate.from(DateTimeFormatter.ISO_DATE.parse(trialEnd.toString()));
                stringObjectLinkedHashMap.put(id, DateUtils.getWorkingDaysBetween(startDate, endDate));
            });
        tableData.removeData(DataType.TRIAL_NUMBER_START_DATE, DataType.TRIAL_NUMBER_END_DATE);
    }

    @Override
    protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
        query.where(
            QTrial.trial.trialStartDate.between(request.getFromDate(), request.getToDate())
                .or(QTrial.trial.trialEndDate.between(request.getFromDate(), request.getToDate()))
        );
        query.where(QTrial.trial.courtLocation.locCode.eq(SecurityUtil.getLocCode()));

        addGroupBy(
            query,
            DataType.TRIAL_JUDGE_NAME,
            DataType.TRIAL_TYPE,
            DataType.TRIAL_NUMBER,
            DataType.TRIAL_NUMBER_START_DATE,
            DataType.TRIAL_NUMBER_END_DATE,
            DataType.TRIAL_COURT_LOCATION
        );
    }

    @Override
    public Map<String, AbstractReportResponse.DataTypeValue> getHeadings(
        StandardReportRequest request,
        AbstractReportResponse.TableData<StandardTableData> tableData) {
        return Map.of(
            "date_from", AbstractReportResponse.DataTypeValue.builder()
                .displayName("Date from")
                .dataType(LocalDate.class.getSimpleName())
                .value(request.getFromDate().format(DateTimeFormatter.ISO_LOCAL_DATE))
                .build(),
            "date_to", AbstractReportResponse.DataTypeValue.builder()
                .displayName("Date to")
                .dataType(LocalDate.class.getSimpleName())
                .value(request.getToDate().format(DateTimeFormatter.ISO_LOCAL_DATE))
                .build());
    }

    @Override
    public Class<RequestValidator> getRequestValidatorClass() {
        return RequestValidator.class;
    }

    public interface RequestValidator extends
        AbstractReport.Validators.AbstractRequestValidator,
        AbstractReport.Validators.RequireFromDate,
        AbstractReport.Validators.RequireToDate {
    }
}
