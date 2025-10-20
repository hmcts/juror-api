package uk.gov.hmcts.juror.api.moj.report.grouped;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedTableData;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.report.AbstractGroupedReport;
import uk.gov.hmcts.juror.api.moj.report.AbstractReport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.report.ReportGroupBy;
import uk.gov.hmcts.juror.api.moj.service.CourtLocationService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ReasonableAdjustmentAndCjeReport extends AbstractGroupedReport {

    private final CourtLocationService courtLocationService;

    @Autowired
    public ReasonableAdjustmentAndCjeReport(CourtLocationService courtLocationService) {
        super(QJurorPool.jurorPool,
              ReportGroupBy.builder()
                  .dataType(DataType.COURT_LOCATION_NAME_AND_CODE)
                  .removeGroupByFromResponse(true)
                  .build(),
              DataType.JUROR_NUMBER,
              DataType.FIRST_NAME,
              DataType.LAST_NAME,
              DataType.POOL_NUMBER_BY_JP,
              DataType.CONTACT_DETAILS,
              DataType.NEXT_ATTENDANCE_DATE,
              DataType.OPTIC_REFERENCE,
              DataType.JUROR_REASONABLE_ADJUSTMENT_WITH_MESSAGE);

        this.courtLocationService = courtLocationService;
    }

    @Override
    public Class<? extends Validators.AbstractRequestValidator> getRequestValidatorClass() {
        return RequestValidator.class;
    }

    @Override
    protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
        query.where(QJuror.juror.reasonableAdjustmentCode.isNotNull()
            .and(QJurorPool.jurorPool.nextDate.between(request.getFromDate(), request.getToDate())));

        if (SecurityUtil.isCourt()) {
            query.where(QJurorPool.jurorPool.owner.eq(SecurityUtil.getActiveOwner()));
        }

        query.orderBy(QJuror.juror.jurorNumber.asc());
    }

    @Override
    public Map<String, GroupedReportResponse.DataTypeValue> getHeadings(
        StandardReportRequest request,
        StandardReportResponse.TableData<GroupedTableData> tableData) {

        Map<String, GroupedReportResponse.DataTypeValue> map = new ConcurrentHashMap<>();
        map.put("total_reasonable_adjustments", GroupedReportResponse.DataTypeValue.builder()
            .displayName("Total jurors with reasonable adjustments")
            .dataType(Long.class.getSimpleName())
            .value(tableData.getData().getSize())
            .build());

        if (SecurityUtil.isCourt()) {
            Map.Entry<String, GroupedReportResponse.DataTypeValue> entry =
                getCourtNameHeader(courtLocationService.getCourtLocation(SecurityUtil.getActiveOwner()));
            map.put(entry.getKey(), entry.getValue());
        }

        return map;
    }

    public interface RequestValidator extends
        AbstractReport.Validators.AbstractRequestValidator,
        AbstractReport.Validators.RequireFromDate,
        AbstractReport.Validators.RequireToDate {
    }
}
