package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardTableData;
import uk.gov.hmcts.juror.api.moj.domain.trial.QPanel;
import uk.gov.hmcts.juror.api.moj.domain.trial.Trial;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.repository.trial.TrialRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.util.HashMap;
import java.util.Map;


@Component
public class BallotPanelTrialReport extends AbstractStandardReport {
    private final TrialRepository trialRepository;

    @Autowired
    public BallotPanelTrialReport(TrialRepository trialRepository) {
        super(
            QPanel.panel,
            DataType.JUROR_NUMBER,
            DataType.FIRST_NAME,
            DataType.LAST_NAME,
            DataType.JUROR_POSTCODE
        );
        this.trialRepository = trialRepository;
        isCourtUserOnly();
    }

    @Override
    protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
        query.where(QPanel.panel.trial.trialNumber.eq(request.getTrialNumber()));
        query.where(QPanel.panel.trial.courtLocation.locCode.in(SecurityUtil.getCourts()));
        query.orderBy(QPanel.panel.juror.lastName.asc(), QPanel.panel.juror.jurorNumber.asc());
    }

    @Override
    public Map<String, AbstractReportResponse.DataTypeValue> getHeadings(
        StandardReportRequest request,
        AbstractReportResponse.TableData<StandardTableData> tableData) {
        return new HashMap<>();
    }

    @Override
    protected void postProcessTableData(
        StandardReportRequest request,
        AbstractReportResponse.TableData<StandardTableData> tableData) {
        Trial trial = getTrial(request.getTrialNumber(), this.trialRepository);

        if (trial.getAnonymous() != null && trial.getAnonymous()) {
            tableData.removeData(DataType.FIRST_NAME, DataType.LAST_NAME, DataType.JUROR_POSTCODE);
        }
    }

    @Override
    public Class<BallotPanelTrialReport.RequestValidator> getRequestValidatorClass() {
        return BallotPanelTrialReport.RequestValidator.class;
    }

    public interface RequestValidator extends
        Validators.AbstractRequestValidator,
        Validators.RequireTrialNumber {
    }
}
