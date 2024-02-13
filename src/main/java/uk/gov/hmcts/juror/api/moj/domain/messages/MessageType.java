package uk.gov.hmcts.juror.api.moj.domain.messages;

import lombok.Getter;

@Getter
public enum MessageType {
    REMIND_TO_ATTEND(1, 18, SendType.EMAIL_AND_SMS),
    FAILED_TO_ATTEND_COURT(2, 19, SendType.EMAIL_AND_SMS),
    ATTENDANCE_DATE_AND_TIME_CHANGED_COURT(3, 20, SendType.EMAIL_AND_SMS),
    ATTENDANCE_TIME_CHANGED_COURT(4, 21, SendType.EMAIL_AND_SMS),
    COMPLETE_ATTENDED_COURT(5, 22, SendType.EMAIL_AND_SMS),
    COMPLETE_NOT_NEEDED_COURT(6, 23, SendType.EMAIL_AND_SMS),
    NEXT_ATTENDANCE_DATE_COURT(7, 24, SendType.EMAIL_AND_SMS),
    ON_CALL_COURT(8, 25, SendType.EMAIL_AND_SMS),
    PLEASE_CONTACT_COURT(9, 26, SendType.EMAIL_AND_SMS),
    DELAYED_START_COURT(10, 27, SendType.EMAIL_AND_SMS),
    SELECTION_COURT(11, 28, SendType.EMAIL_AND_SMS),
    BAD_WEATHER_COURT(12, 29, SendType.EMAIL_AND_SMS),
    CHECK_INBOX_COURT(13, 30, SendType.SMS),
    BRING_LUNCH_COURT(14, 31, SendType.EMAIL_AND_SMS),
    EXCUSED_COURT(15, 32, SendType.EMAIL_AND_SMS),
    SENTENCING_INVITE_COURT(16, 33, SendType.EMAIL),
    SENTENCING_DATE_COURT(17, 34, SendType.EMAIL);

    private final int englishMessageId;
    private final int welshMessageId;
    private final SendType sendType;

    MessageType(int englishMessageId, int welshMessageId, SendType sendType) {
        this.englishMessageId = englishMessageId;
        this.welshMessageId = welshMessageId;
        this.sendType = sendType;
    }

    public enum SendType {
        EMAIL, SMS, EMAIL_AND_SMS;

        public boolean supports(SendType sendType) {
            return EMAIL_AND_SMS.equals(this) || EMAIL_AND_SMS.equals(sendType) || this.equals(sendType);
        }
    }
}
