package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.trial.QPanel;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.repository.trial.TrialRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Component
@SuppressWarnings("PMD.LawOfDemeter")
public class JuryListReport extends AbstractStandardReport {
    private final TrialRepository trialRepository;

    @Autowired
    public JuryListReport(TrialRepository trialRepository) {
        super(
            QPanel.panel,
            DataType.JUROR_NUMBER,
            DataType.FIRST_NAME,
            DataType.LAST_NAME,
            DataType.JUROR_POSTCODE);

        this.trialRepository = trialRepository;
        isCourtUserOnly();
    }


    @Override
    protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
        query.where(QPanel.panel.trial.trialNumber.eq(request.getTrialNumber()));
        query.where(QPanel.panel.trial.courtLocation.owner.eq(SecurityUtil.getActiveOwner()));
        query.orderBy(QJuror.juror.jurorNumber.asc());
    }

    @Override
    public Map<String, StandardReportResponse.DataTypeValue> getHeadings(
        StandardReportRequest request,
        StandardReportResponse.TableData<List<LinkedHashMap<String, Object>>> tableData) {

        return loadStandardTrailHeaders(request, trialRepository, true);
    }

    @Override
    public Class<? extends Validators.AbstractRequestValidator> getRequestValidatorClass() {
        return JuryListReport.RequestValidator.class;
    }

    public interface RequestValidator extends
        Validators.AbstractRequestValidator,
        Validators.RequireTrialNumber {

    }
}
