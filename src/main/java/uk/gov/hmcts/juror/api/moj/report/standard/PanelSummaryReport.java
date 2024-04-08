package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.QJurorTrial;
import uk.gov.hmcts.juror.api.moj.domain.trial.QTrial;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.repository.trial.TrialRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Component
@SuppressWarnings("PMD.LawOfDemeter")
public class PanelSummaryReport extends AbstractStandardReport {
    private final TrialRepository trialRepository;

    @Autowired
    public PanelSummaryReport(PoolRequestRepository poolRequestRepository, TrialRepository trialRepository) {
        super(
            poolRequestRepository,
            QJurorTrial.jurorTrial,
            DataType.JUROR_NUMBER,
            DataType.FIRST_NAME,
            DataType.LAST_NAME);

        this.trialRepository = trialRepository;
        isCourtUserOnly();
    }


    @Override
    protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
        query.where(QJurorTrial.jurorTrial.trialNumber.eq(request.getTrialNumber()));
        query.where(QJurorTrial.jurorTrial.locCode.eq(SecurityUtil.getActiveOwner()));
        query.orderBy(QJurorPool.jurorPool.juror.jurorNumber.asc());
    }

    @Override
    public Map<String, StandardReportResponse.DataTypeValue> getHeadings(
        StandardReportRequest request,
        StandardReportResponse.TableData<List<LinkedHashMap<String, Object>>> tableData) {

        Map<String, StandardReportResponse.DataTypeValue> map = loadTrialHeaders(request, trialRepository);
        map.put("panel_summary", StandardReportResponse.DataTypeValue.builder()
            .displayName("Panel Summary")
            .dataType(Long.class.getSimpleName())
            .value(tableData.getData().size())
            .build());
        return map;
    }

    @Override
    public Class<?> getRequestValidatorClass() {
        return PanelSummaryReport.RequestValidator.class;
    }

    public interface RequestValidator extends
        Validators.AbstractRequestValidator,
        Validators.RequireTrialNumber {

    }
}
