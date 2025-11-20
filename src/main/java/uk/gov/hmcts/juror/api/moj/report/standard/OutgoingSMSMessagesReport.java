package uk.gov.hmcts.juror.api.moj.report.standard;

import uk.gov.hmcts.juror.api.moj.domain.QAppearance;
import uk.gov.hmcts.juror.api.moj.domain.messages.QMessage;
import uk.gov.hmcts.juror.api.moj.report.AbstractReport;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReport;
import uk.gov.hmcts.juror.api.moj.report.DataType;

public class OutgoingSMSMessagesReport extends AbstractStandardReport {
    public OutgoingSMSMessagesReport(){
        super(QMessage.message,
              DataType.COURT_LOCATION_NAME_AND_CODE_MP,
              DataType.REMINDER,
              DataType.FAILED_TO_ATTEND,
              DataType.DATE_AND_TIME_CHANGED,
              DataType.TIME_CHANGED,
              DataType.COMPLETED_ATTENDED,
              DataType.COMPLETE_NOT_NEEDED,
              DataType.NEXT_DATE,
              DataType.ON_CALL,
              DataType.PLEASE_CONTACT,
              DataType.DELAYED_START,
              DataType.SELECTION,
              DataType.BAD_WEATHER,
              DataType.BRING_LUNCH,
              DataType.CHECK_JUNK_EMAIL,
              DataType.EXCUSED,
              DataType.SENTENCING);
    }
    @Override
    public Class<PoolRatioReport.RequestValidator> getRequestValidatorClass() {
        return PoolRatioReport.RequestValidator.class;
    }

    public interface RequestValidator extends
        AbstractReport.Validators.AbstractRequestValidator,
        AbstractReport.Validators.RequireFromDate,
        AbstractReport.Validators.RequireToDate,
        AbstractReport.Validators.RequireCourts {

    }
}
