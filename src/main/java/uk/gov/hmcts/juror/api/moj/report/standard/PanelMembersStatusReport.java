package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardTableData;
import uk.gov.hmcts.juror.api.moj.domain.trial.QPanel;
import uk.gov.hmcts.juror.api.moj.report.AbstractReport;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.repository.trial.TrialRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.util.Map;

@Component
public class PanelMembersStatusReport extends AbstractStandardReport {

    private final TrialRepository trialRepository;

    @Autowired
    public PanelMembersStatusReport(TrialRepository trialRepository) {
        super(
            QPanel.panel,
            DataType.JUROR_NUMBER_FROM_TRIAL,
            DataType.PANEL_STATUS
        );

        isCourtUserOnly();
        this.trialRepository = trialRepository;
    }

    @Override
    protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
        query.where(QPanel.panel.trial.trialNumber.eq(request.getTrialNumber()));
        query.where(QPanel.panel.trial.courtLocation.locCode.eq(SecurityUtil.getLocCode()));
        query.orderBy(QPanel.panel.juror.jurorNumber.asc());
    }

    @Override
    public Map<String, AbstractReportResponse.DataTypeValue> getHeadings(
        StandardReportRequest request,
        AbstractReportResponse.TableData<StandardTableData> tableData) {

        return loadStandardTrialHeaders(request, this.trialRepository);
    }

    @Override
    public Class<? extends Validators.AbstractRequestValidator> getRequestValidatorClass() {
        return RequestValidator.class;
    }

    public interface RequestValidator extends
        AbstractReport.Validators.AbstractRequestValidator,
        AbstractReport.Validators.RequireTrialNumber {
    }
}
