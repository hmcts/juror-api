package uk.gov.hmcts.juror.api.moj.report.grouped;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedTableData;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.enumeration.DisqualifyCodeEnum;
import uk.gov.hmcts.juror.api.moj.enumeration.ExcusalCodeEnum;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.report.AbstractGroupedReport;
import uk.gov.hmcts.juror.api.moj.report.AbstractReport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.report.ReportGroupBy;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.util.Map;
import java.util.Objects;

@Component
public class ExcusedAndDisqualifiedListReport extends AbstractGroupedReport {

    @Autowired
    public ExcusedAndDisqualifiedListReport(PoolRequestRepository poolRequestRepository) {
        super(poolRequestRepository,
            QJurorPool.jurorPool,
            ReportGroupBy.builder()
                .dataType(DataType.EXCUSAL_DISQUAL_TYPE)
                .removeGroupByFromResponse(true)
                .build(),
            DataType.JUROR_NUMBER,
            DataType.FIRST_NAME,
            DataType.LAST_NAME,
            DataType.EXCUSAL_DISQUAL_CODE,
            DataType.EXCUSAL_DISQUAL_DECISION_DATE
        );

    }


    @Override
    protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
        query.where(QJurorPool.jurorPool.pool.poolNumber.eq(request.getPoolNumber()));
        query.where(QJurorPool.jurorPool.pool.owner.eq(SecurityUtil.getActiveOwner()));
        query.where(QJurorPool.jurorPool.juror.disqualifyCode.isNotNull()
            .or(QJurorPool.jurorPool.juror.excusalCode.isNotNull()));
    }

    @Override
    protected void postProcessTableData(StandardReportRequest request,
                                        AbstractReportResponse.TableData<GroupedTableData> tableData) {
        tableData.getData().getAllDataItems()
            .forEach(groupedTableData -> {
                Object type = groupedTableData.get(DataType.EXCUSAL_DISQUAL_TYPE.getId());
                Object code = groupedTableData.get(DataType.EXCUSAL_DISQUAL_CODE.getId());
                String description;
                if ("Disqualified".equals(type)) {
                    description = Objects.requireNonNull(DisqualifyCodeEnum.fromCode(code.toString())).getDescription();
                } else if ("Excused".equals(type)) {
                    description = ExcusalCodeEnum.fromCode(code.toString()).getDescription();
                } else {
                    throw new MojException.InternalServerError("Unexpected type", null);
                }
                groupedTableData.put(DataType.EXCUSAL_DISQUAL_CODE.getId(), code + " - " + description);
            });
    }

    @Override
    public Map<String, AbstractReportResponse.DataTypeValue> getHeadings(
        StandardReportRequest request,
        AbstractReportResponse.TableData<GroupedTableData> tableData) {

        Map<String, AbstractReportResponse.DataTypeValue> headers = loadStandardPoolHeaders(request, true, true);
        headers.put("total_excused_and_disqualified",
            AbstractReportResponse.DataTypeValue.builder()
                .displayName("Total excused and disqualified")
                .dataType(Long.class.getSimpleName())
                .value(tableData.getData().getSize())
                .build()
        );
        return headers;
    }

    @Override
    public Class<RequestValidator> getRequestValidatorClass() {
        return RequestValidator.class;
    }

    public interface RequestValidator extends
        AbstractReport.Validators.AbstractRequestValidator,
        AbstractReport.Validators.RequirePoolNumber {

    }
}
