package uk.gov.hmcts.juror.api.moj.domain;


import lombok.Getter;
import uk.gov.hmcts.juror.api.moj.exception.MojException;

@Getter
public enum IContactCode {
    SICKNESS("SI", "Sickness"),
    DISCUSS_EXCUSAL("EL", "Discuss Excusal"),
    DISCUSS_DEFERRAL("DE", "Discuss Deferral"),
    TRAVEL_INQUIRY("TR", "Travel inquiry"),
    PARKING_INQUIRY("PA", "Parking inquiry"),
    NO_SHOW("NS", "No show"),
    CHECK_COURT_FACILITIES("FA", "Check what facilities are available at court"),
    VISIT("VI", "Visit"),
    MEDICAL_E("ME", "Medical"),
    MEDICAL_D("MD", "Medical"),

    OUTGOING_PHONE_CALL("OU", "Outgoing Phone Call"),
    CHASING_EXPENSE_PAYMENT("CS", "Chasing Expense payment - SSCL"),
    GENERAL("GE", "General"),
    TRAVEL_TO_COURT("CP", "Car parking/Taxi/Travel to court"),
    REQUEST_EXCUSAL("RE", "Request Excusal"),
    PHONE_INQUIRY("IN", "Phone Inquiry"),
    EXPENSE_ENQUIRY("EX", "Expense Enquiry"),
    REQUEST_DEFERRAL("RD", "Request Deferral"),
    DIFFICULTY_COMPLETING_SUMMONS_REPLY("DS", "Difficulty completing the reply to summons"),
    RELOCATE_TO_ANOTHER_COURT("RC", "Relocate to another court"),
    REASONABLE_ADJUSTMENTS("RA", "Reasonable Adjustments"),
    PRE_COURT_VISIT("PV", "Pre-Court Visit"),
    CHANGE_OF_ADDRESS("CA", "Change of address"),
    CHANGE_OF_NAME("CN", "Change of name"),
    ISSUE_ACCESSING_DIGITAL_SERVICE("IA", "Issues accessing digital service"),
    LATE_REPLY("LR", "Late response to summons"),
    REQUEST_UPDATE("PE", "Requesting an update on a previous enquiry"),
    ELIGIBILITY_QUERY("EQ", "Eligibility query"),
    EARLY_RELEASE("ER", "Discuss the reason for being released early"),
    APPEAL("AP", "Appeal"),
    GOING_TO_BE_LATE("GL", "Going to be late"),
    UNABLE_TO_ATTEND("UA", "Unable to attend"),
    CHECK_HEARING("SD", "Check sentence hearing date/result"),
    LENGTH_OF_SERVICE("LS", "Times and Length of service");

    private final String code;
    private final String description;

    IContactCode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static IContactCode fromCode(String code) {
        for (IContactCode contactCode : IContactCode.values()) {
            if (contactCode.getCode().equals(code)) {
                return contactCode;
            }
        }
        throw new MojException.BadRequest("Contact code '" + code + "'", null);
    }
}
