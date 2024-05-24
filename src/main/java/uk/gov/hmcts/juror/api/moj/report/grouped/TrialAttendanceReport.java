package uk.gov.hmcts.juror.api.moj.report.grouped;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedTableData;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.QReportsJurorPayments;
import uk.gov.hmcts.juror.api.moj.domain.trial.Trial;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.report.AbstractGroupedReport;
import uk.gov.hmcts.juror.api.moj.report.ReportGroupBy;
import uk.gov.hmcts.juror.api.moj.report.datatypes.ReportsJurorPaymentsDataTypes;
import uk.gov.hmcts.juror.api.moj.repository.trial.TrialRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

@Component
public class TrialAttendanceReport extends AbstractGroupedReport {

    private final TrialRepository trialRepository;

    @Autowired
    public TrialAttendanceReport(TrialRepository trialRepository) {
        super(QReportsJurorPayments.reportsJurorPayments,
            ReportGroupBy.builder()
                  .dataType(ReportsJurorPaymentsDataTypes.ATTENDANCE_DATE)
                  .removeGroupByFromResponse(true)
                  .build(),
            ReportsJurorPaymentsDataTypes.JUROR_NUMBER,
            ReportsJurorPaymentsDataTypes.FIRST_NAME,
            ReportsJurorPaymentsDataTypes.LAST_NAME,
            ReportsJurorPaymentsDataTypes.CHECKED_IN,
            ReportsJurorPaymentsDataTypes.CHECKED_OUT,
            ReportsJurorPaymentsDataTypes.HOURS_ATTENDED,
            ReportsJurorPaymentsDataTypes.ATTENDANCE_AUDIT,
            ReportsJurorPaymentsDataTypes.PAYMENT_AUDIT,
            ReportsJurorPaymentsDataTypes.TOTAL_DUE,
            ReportsJurorPaymentsDataTypes.TOTAL_PAID);

        this.trialRepository = trialRepository;

        isCourtUserOnly();
    }

    @Override
    public Class<? extends Validators.AbstractRequestValidator> getRequestValidatorClass() {
        return RequestValidator.class;
    }

    @Override
    protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
        query.where(QReportsJurorPayments.reportsJurorPayments.trialNumber.eq(request.getTrialNumber()));
        query.where(QReportsJurorPayments.reportsJurorPayments.locCode.eq(SecurityUtil.getLocCode()));
        query.orderBy(QReportsJurorPayments.reportsJurorPayments.jurorNumber.asc());
    }

    @Override
    public Map<String, GroupedReportResponse.DataTypeValue> getHeadings(
        StandardReportRequest request,
        StandardReportResponse.TableData<GroupedTableData> tableData) {

        Optional<Trial> optTrial = trialRepository.findByTrialNumberAndCourtLocationLocCode(request.getTrialNumber(),
                                                                                         SecurityUtil.getLocCode());

        Trial trial = optTrial.orElseThrow(() -> new MojException.NotFound("", null));

        Map<String, GroupedReportResponse.DataTypeValue> map = loadStandardTrailHeaders(request, trialRepository, true);
        map.put("trial_type", GroupedReportResponse.DataTypeValue.builder()
            .displayName("Trial type")
            .dataType("String")
            .value(trial.getTrialType().getDescription())
            .build());
        map.put("trial_start_date", GroupedReportResponse.DataTypeValue.builder()
            .displayName("Trial start date")
            .dataType("LocalDate")
            .value(DateTimeFormatter.ISO_DATE.format(trial.getTrialStartDate()))
            .build());

        return map;
    }

    public interface RequestValidator extends
        Validators.AbstractRequestValidator,
        Validators.RequireTrialNumber {
    }
}
