package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardTableData;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.domain.QAppearance;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.service.jurormanagement.JurorAppearanceService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class JuryAttendanceAuditReport extends AbstractStandardReport {


    private final JurorAppearanceService appearanceService;

    public JuryAttendanceAuditReport(JurorAppearanceService appearanceService) {
        super(QAppearance.appearance,
            DataType.JUROR_NUMBER,
            DataType.FIRST_NAME,
            DataType.LAST_NAME,
            DataType.APPEARANCE_CHECKED_IN,
            DataType.APPEARANCE_CHECKED_OUT
        );
        isCourtUserOnly();
        this.appearanceService = appearanceService;
    }


    @Override
    protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
        query.where(QAppearance.appearance.attendanceAuditNumber.eq(request.getJuryAuditNumber()));
        query.where(QAppearance.appearance.locCode.in(SecurityUtil.getCourts()));
    }

    @Override
    public Map<String, AbstractReportResponse.DataTypeValue> getHeadings(
        StandardReportRequest request,
        AbstractReportResponse.TableData<StandardTableData> tableData) {

        Optional<Appearance> appearanceOpt =
            appearanceService.getFirstAppearanceWithAuditNumber(request.getJuryAuditNumber(),
                SecurityUtil.getCourts());

        if (appearanceOpt.isEmpty() || tableData.getData().isEmpty()) {
            throw new MojException.NotFound("Audit Number not found", null);
        }

        Map<String, AbstractReportResponse.DataTypeValue> headings = new HashMap<>(Map.of(
            "attendance_date", AbstractReportResponse.DataTypeValue.builder()
                .displayName("Attendance date")
                .dataType(LocalDate.class.getSimpleName())
                .value(DateTimeFormatter.ISO_DATE.format(appearanceOpt.get().getAttendanceDate()))
                .build(),
            "audit_number", AbstractReportResponse.DataTypeValue.builder()
                .displayName("Audit number")
                .dataType(String.class.getSimpleName())
                .value(request.getJuryAuditNumber())
                .build(),
            "trial_number", AbstractReportResponse.DataTypeValue.builder()
                .displayName("Trial number")
                .dataType(String.class.getSimpleName())
                .value(appearanceOpt.get().getTrialNumber())
                .build(),
            "total", AbstractReportResponse.DataTypeValue.builder()
                .displayName("Total")
                .dataType(Long.class.getSimpleName())
                .value(tableData.getData().size())
                .build()
        ));

        addCourtNameHeader(headings, appearanceOpt.get().getCourtLocation());
        return headings;
    }

    @Override
    public Class<RequestValidator> getRequestValidatorClass() {
        return RequestValidator.class;
    }


    public interface RequestValidator extends
        Validators.AbstractRequestValidator,
        Validators.RequireJuryAuditNumber {

    }
}
