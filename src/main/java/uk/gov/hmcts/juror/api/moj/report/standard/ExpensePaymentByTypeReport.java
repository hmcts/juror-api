package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardTableData;
import uk.gov.hmcts.juror.api.moj.domain.QAppearance;
import uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage;
import uk.gov.hmcts.juror.api.moj.report.AbstractReport;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReport;
import uk.gov.hmcts.juror.api.moj.report.DataType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ExpensePaymentByTypeReport extends AbstractStandardReport {
    public ExpensePaymentByTypeReport(){
        super(QAppearance.appearance,
                DataType.COURT_LOCATION_NAME_AND_CODE_EP,
                DataType.LOSS_OF_EARNINGS_TOTAL,
                DataType.CHILDCARE_TOTAL,
                DataType.MISCELLANEOUS_TOTAL,
                DataType.PUBLIC_TRANSPORT_TOTAL,
                DataType.HIRED_VEHICLE_TOTAL,
                DataType.MOTORCYCLE_TOTAL,
                DataType.CAR_TOTAL,
                DataType.PARKING_TOTAL,
                DataType.SUBSISTENCE_TOTAL
        );
        isCourtUserOnly();
    }

  @Override
  protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
    query.where(
        QAppearance.appearance.attendanceDate.between(request.getFromDate(), request.getToDate()));
    query.where(QAppearance.appearance.locCode.in(request.getCourts()));
    query.where(QAppearance.appearance.isDraftExpense.isFalse());
    query.where(QAppearance.appearance.appearanceStage.in(AppearanceStage.EXPENSE_ENTERED,AppearanceStage.EXPENSE_EDITED));
    query.where(QAppearance.appearance.hideOnUnpaidExpenseAndReports.isFalse());
    query.orderBy(
        QAppearance.appearance.courtLocation.name.asc(),
        QAppearance.appearance.courtLocation.locCode.asc());
    query.groupBy(
        QAppearance.appearance.courtLocation.locCode, QAppearance.appearance.courtLocation.name);

}
        @Override
        public Map<String, StandardReportResponse.DataTypeValue> getHeadings(
                StandardReportRequest request,
                AbstractReportResponse.TableData<StandardTableData> tableData) {

            LocalDateTime now = LocalDateTime.now();

            Map<String, StandardReportResponse.DataTypeValue> map = new ConcurrentHashMap<>();

            map.put("expense_payment_title", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Expense Payment")
                    .dataType(String.class.getSimpleName())
                    .value("Expense Payment By Type")
                    .build());

            map.put("date_from", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Date from")
                    .dataType(LocalDate.class.getSimpleName())
                    .value(DateTimeFormatter.ISO_DATE.format(request.getFromDate()))
                    .build());

            map.put("date_to", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Date to")
                    .dataType(LocalDate.class.getSimpleName())
                    .value(DateTimeFormatter.ISO_DATE.format(request.getToDate()))
                    .build());

            return map;
        }


        @Override
    public Class<ExpensePaymentByTypeReport.RequestValidator> getRequestValidatorClass() {
        return ExpensePaymentByTypeReport.RequestValidator.class;
    }

    public interface RequestValidator extends
            AbstractReport.Validators.AbstractRequestValidator,
            AbstractReport.Validators.RequireFromDate,
            AbstractReport.Validators.RequireToDate,
            AbstractReport.Validators.RequireCourts {

    }

}
