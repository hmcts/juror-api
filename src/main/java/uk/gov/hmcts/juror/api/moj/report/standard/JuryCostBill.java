package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardTableData;
import uk.gov.hmcts.juror.api.moj.domain.QReportsJurorPayments;
import uk.gov.hmcts.juror.api.moj.domain.trial.Trial;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReport;
import uk.gov.hmcts.juror.api.moj.report.datatypes.ReportsJurorPaymentsDataTypes;
import uk.gov.hmcts.juror.api.moj.repository.trial.TrialRepository;

import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
public class JuryCostBill extends AbstractStandardReport {
    private final TrialRepository trialRepository;

    @Autowired
    public JuryCostBill(TrialRepository trialRepository) {
        super(QReportsJurorPayments.reportsJurorPayments,
              ReportsJurorPaymentsDataTypes.ATTENDANCE_DATE,
              ReportsJurorPaymentsDataTypes.FINANCIAL_LOSS_DUE_SUM,
              ReportsJurorPaymentsDataTypes.TRAVEL_DUE_SUM,
              ReportsJurorPaymentsDataTypes.SUBSISTENCE_DUE_SUM,
              ReportsJurorPaymentsDataTypes.SMARTCARD_DUE_SUM,
              ReportsJurorPaymentsDataTypes.TOTAL_DUE_SUM,
              ReportsJurorPaymentsDataTypes.TOTAL_PAID_SUM);

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
        addGroupBy(query, ReportsJurorPaymentsDataTypes.ATTENDANCE_DATE);
    }

    @Override
    public Map<String, AbstractReportResponse.DataTypeValue> getHeadings(
        StandardReportRequest request,
        StandardReportResponse.TableData<StandardTableData> tableData) {

        Trial trial = getTrial(request.getTrialNumber(), trialRepository);

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
