package uk.gov.hmcts.juror.api.moj.report.grouped;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedTableData;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.QReportsJurorPayments;
import uk.gov.hmcts.juror.api.moj.domain.trial.Trial;
import uk.gov.hmcts.juror.api.moj.report.AbstractGroupedReport;
import uk.gov.hmcts.juror.api.moj.report.ReportGroupBy;
import uk.gov.hmcts.juror.api.moj.report.datatypes.ReportsJurorPaymentsDataTypes;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.trial.TrialRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@SuppressWarnings("PMD.LawOfDemeter")
public class TrialAttendanceReport extends AbstractGroupedReport {

    private final CourtLocationRepository courtLocationRepository;
    private final TrialRepository trialRepository;

    @Autowired
    public TrialAttendanceReport(CourtLocationRepository courtLocationRepository,
                                 TrialRepository trialRepository) {
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

        this.courtLocationRepository = courtLocationRepository;
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
        query.orderBy(QReportsJurorPayments.reportsJurorPayments.jurorNumber.asc());
    }

    @Override
    public Map<String, GroupedReportResponse.DataTypeValue> getHeadings(
        StandardReportRequest request,
        StandardReportResponse.TableData<GroupedTableData> tableData) {

        Optional<Trial> optTrial = trialRepository.findByTrialNumberAndCourtLocationLocCode(request.getTrialNumber(),
                                                                                         SecurityUtil.getLocCode());

        Trial trial = optTrial.orElse(null);
        assert trial != null;

        Map<String, GroupedReportResponse.DataTypeValue> map = new ConcurrentHashMap<>();
        map.put("trial_number", GroupedReportResponse.DataTypeValue.builder()
            .displayName("Trial number")
            .dataType("String")
            .value(request.getTrialNumber())
            .build());
        map.put("trial_names", GroupedReportResponse.DataTypeValue.builder()
            .displayName("Names")
            .dataType("String")
            .value(trial.getDescription())
            .build());
        map.put("trial_type", GroupedReportResponse.DataTypeValue.builder()
            .displayName("Trial type")
            .dataType("String")
            .value(trial.getTrialType().getDescription())
            .build());
        map.put("trial_start_date", GroupedReportResponse.DataTypeValue.builder()
            .displayName("Trial start date")
            .dataType("LocalDate")
            .value(trial.getTrialStartDate())
            .build());
        map.put("trial_courtroom", GroupedReportResponse.DataTypeValue.builder()
            .displayName("Courtroom")
            .dataType("String")
            .value(trial.getCourtroom().getDescription())
            .build());
        map.put("trial_judge", GroupedReportResponse.DataTypeValue.builder()
            .displayName("Judge")
            .dataType("String")
            .value(trial.getJudge().getName())
            .build());

        map.put("court_name", AbstractReportResponse.DataTypeValue.builder().displayName("Court Name")
            .dataType(String.class.getSimpleName())
            .value(getCourtNameString(courtLocationRepository, SecurityUtil.getLocCode()))
            .build());

        return map;
    }

    public interface RequestValidator extends
        Validators.AbstractRequestValidator,
        Validators.RequireTrialNumber {
    }
}
