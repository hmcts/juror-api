package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardTableData;
import uk.gov.hmcts.juror.api.moj.domain.QAppearance;
import uk.gov.hmcts.juror.api.moj.enumeration.AttendanceType;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.util.Map;

@Component
public class AttendanceGraphReport extends AbstractStandardReport {

    public AttendanceGraphReport() {
        super(QAppearance.appearance,
            DataType.ATTENDANCE_DATE,
            DataType.ATTENDANCE_COUNT);
        isCourtUserOnly();
    }


    @Override
    protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
        query.where(QAppearance.appearance.attendanceDate.between(request.getFromDate(), request.getToDate()));
        query.where(QAppearance.appearance.locCode.eq(SecurityUtil.getLocCode()));
        query.where(QAppearance.appearance.attendanceType.notIn(AttendanceType.ABSENT, AttendanceType.NON_ATTENDANCE,
            AttendanceType.NON_ATTENDANCE_LONG_TRIAL));
        addGroupBy(query, DataType.ATTENDANCE_DATE);
    }

    @Override
    public Map<String, AbstractReportResponse.DataTypeValue> getHeadings(StandardReportRequest request,
                                                                         AbstractReportResponse.TableData<StandardTableData> tableData) {
        return Map.of();
    }


    @Override
    public Class<? extends Validators.AbstractRequestValidator> getRequestValidatorClass() {
        return RequestValidator.class;
    }

    public interface RequestValidator extends
        Validators.AbstractRequestValidator,
        Validators.RequireFromDate,
        Validators.RequireToDate {
    }
}
