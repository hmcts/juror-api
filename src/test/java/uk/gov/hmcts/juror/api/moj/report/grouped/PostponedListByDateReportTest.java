package uk.gov.hmcts.juror.api.moj.report.grouped;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.report.AbstractGroupedReportTestSupport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.fail;

public class PostponedListByDateReportTest extends AbstractGroupedReportTestSupport<PostponedListByDateReport> {
    public PostponedListByDateReportTest() {
        super(QJurorPool.jurorPool,
            PostponedListByDateReport.RequestValidator.class,
            DataType.POOL_NUMBER,
            true,
            DataType.JUROR_NUMBER,
            DataType.FIRST_NAME,
            DataType.LAST_NAME,
            DataType.POSTCODE,
            DataType.POSTPONED_TO);
    }

    @Override
    public PostponedListByDateReport createReport(PoolRequestRepository poolRequestRepository) {
        return new PostponedListByDateReport(poolRequestRepository);
    }

    @Override
    protected StandardReportRequest getValidRequest() {
        return StandardReportRequest.builder()
            .reportType(report.getName())
            .fromDate(LocalDate.of(2020, 1, 1))
            .toDate(LocalDate.of(2020, 1, 2))
            .build();
    }

    @Override
    public void positivePreProcessQueryTypical(JPAQuery<Tuple> query, StandardReportRequest request) {
        fail("Not implemented");
    }

    @Override
    public Map<String, AbstractReportResponse.DataTypeValue> positiveGetHeadingsTypical(StandardReportRequest request,
                                                                                        AbstractReportResponse.TableData<Map<String, List<LinkedHashMap<String, Object>>>> tableData,
                                                                                        Map<String,
                                                                                            List<LinkedHashMap<String
                                                                                                , Object>>> data) {
        fail("Not implemented");
        return null;
    }



    @Test
    void negativeMissingFromDate() {
        StandardReportRequest request = getValidRequest();
        request.setFromDate(null);
        assertValidationFails(request, new ValidationFailure("fromDate", "must not be null"));
    }

    @Test
    void negativeMissingToDate() {
        StandardReportRequest request = getValidRequest();
        request.setToDate(null);
        assertValidationFails(request, new ValidationFailure("toDate", "must not be null"));
    }
}
