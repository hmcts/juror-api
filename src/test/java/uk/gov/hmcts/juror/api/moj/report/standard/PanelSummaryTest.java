package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.QJurorTrial;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReportTestSupport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.repository.trial.TrialRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("PMD.LawOfDemeter")
class PanelSummaryTest extends AbstractStandardReportTestSupport<PanelSummaryReport> {

    @Mock
    private TrialRepository trialRepository;

    public PanelSummaryTest() {
        super(QJurorTrial.jurorTrial,
            PanelSummaryReport.RequestValidator.class,
            DataType.JUROR_NUMBER,
            DataType.FIRST_NAME,
            DataType.LAST_NAME);
    }


    @Override
    public PanelSummaryReport createReport(PoolRequestRepository poolRequestRepository) {
        return new PanelSummaryReport(poolRequestRepository, trialRepository);
    }

    @Override
    public void positivePreProcessQueryTypical(JPAQuery<Tuple> query, StandardReportRequest request) {
        request.setTrialNumber(TestConstants.VALID_TRIAL_NUMBER);
        report.preProcessQuery(query, request);
        verify(query, times(1))
            .where(QJurorTrial.jurorTrial.trialNumber.eq(TestConstants.VALID_TRIAL_NUMBER));
        verify(query, times(1));
        verify(query, times(1))
            .where(QJurorTrial.jurorTrial.locCode.eq(TestConstants.VALID_COURT_LOCATION));
        verify(query, times(1));
    }

    @Override
    public Map<String, StandardReportResponse.DataTypeValue> positiveGetHeadingsTypical(
        StandardReportRequest request,
        AbstractReportResponse.TableData<List<LinkedHashMap<String, Object>>> tableData,
        List<LinkedHashMap<String, Object>> data) {
        
        when(data.size()).thenReturn(2);
        Map<String, StandardReportResponse.DataTypeValue> map = report.getHeadings(request, tableData);
        assertHeadingContains(map,
            request,
            true,
            Map.of(
                "panel_summary",
                StandardReportResponse.DataTypeValue.builder()
                    .displayName("Panel Summary")
                    .dataType(Long.class.getSimpleName())
                    .value(2)
                    .build()
            ));
        verify(tableData, times(1)).getData();
        verify(data, times(1)).size();
        return map;
    }

    @Override
    protected StandardReportRequest getValidRequest() {
        return StandardReportRequest.builder()
            .reportType(report.getName())
            .trialNumber(TestConstants.VALID_TRIAL_NUMBER)
            .build();
    }

    @Test
    void negativeMissingTrialNumber() {
        StandardReportRequest request = getValidRequest();
        request.setTrialNumber(null);
        assertValidationFails(request, new ValidationFailure("trialNumber", "must not be blank"));
    }

}
