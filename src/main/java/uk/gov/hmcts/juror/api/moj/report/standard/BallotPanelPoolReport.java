package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import javax.xml.crypto.Data;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class BallotPanelPoolReport extends AbstractStandardReport {

    @Autowired
    public BallotPanelPoolReport(PoolRequestRepository poolRequestRepository) {
        super(poolRequestRepository,
              QJurorPool.jurorPool,
              DataType.JUROR_NUMBER,
              DataType.FIRST_NAME,
              DataType.LAST_NAME,
              DataType.JUROR_POSTCODE
        );
        isCourtUserOnly();
    }

    @Override
    protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
        query.where(QJurorPool.jurorPool.pool.poolNumber.eq(request.getPoolNumber()));
        query.where(QJurorPool.jurorPool.pool.courtLocation.locCode.eq(request.getLocCode()));
        query.orderBy(QJurorPool.jurorPool.juror.jurorNumber.asc());
    }

    @Override
    public Map<String, StandardReportResponse.DataTypeValue> getHeadings(StandardReportRequest request,
                                                                         StandardReportResponse.TableData<
                                                                             List<LinkedHashMap<String, Object>>>
                                                                             tableData) {

        return new HashMap<>();
    }

    @Override
    public Class<PersonAttendingSummaryReport.RequestValidator> getRequestValidatorClass() {
        return PersonAttendingSummaryReport.RequestValidator.class;
    }

    public interface RequestValidator extends
        Validators.AbstractRequestValidator,
        Validators.RequireDate,
        Validators.RequireIncludeSummoned {

    }
}
