package uk.gov.hmcts.juror.api.moj.domain;

import lombok.Getter;
import uk.gov.hmcts.juror.api.moj.service.PrintDataService;

import java.util.function.BiConsumer;

@Getter
public enum FormCode {

    ENG_EXCUSAL("5225", PrintDataService::printExcusalLetter, IJurorStatus.EXCUSED),
    BI_EXCUSAL("5225C", PrintDataService::printExcusalLetter, IJurorStatus.EXCUSED),
    ENG_EXCUSALDENIED("5226", PrintDataService::printExcusalDeniedLetter, IJurorStatus.RESPONDED),
    BI_EXCUSALDENIED("5226C", PrintDataService::printExcusalDeniedLetter, IJurorStatus.RESPONDED),
    ENG_SUMMONS("5221", PrintDataService::printSummonsLetter, IJurorStatus.SUMMONED),
    BI_SUMMONS("5221C", PrintDataService::printSummonsLetter, IJurorStatus.SUMMONED),
    ENG_SUMMONS_REMINDER("5228", PrintDataService::printSummonsReminderLetter, IJurorStatus.SUMMONED),
    BI_SUMMONS_REMINDER("5228C", PrintDataService::printSummonsReminderLetter, IJurorStatus.SUMMONED),
    ENG_CONFIRMATION("5224A", PrintDataService::printConfirmationLetter, IJurorStatus.RESPONDED),
    BI_CONFIRMATION("5224AC", PrintDataService::printConfirmationLetter, IJurorStatus.RESPONDED),
    ENG_DEFERRAL("5229A", PrintDataService::printDeferralLetter, IJurorStatus.DEFERRED),
    BI_DEFERRAL("5229AC", PrintDataService::printDeferralLetter, IJurorStatus.DEFERRED),
    ENG_DEFERRALDENIED("5226A", PrintDataService::printDeferralDeniedLetter, IJurorStatus.RESPONDED),
    BI_DEFERRALDENIED("5226AC", PrintDataService::printDeferralDeniedLetter, IJurorStatus.RESPONDED),
    ENG_POSTPONE("5229", PrintDataService::printPostponeLetter, IJurorStatus.DEFERRED),
    BI_POSTPONE("5229C", PrintDataService::printPostponeLetter, IJurorStatus.DEFERRED),

    // We currently are not anticipating a resend function for request info letters.
    // If we do need one, we will need to write a print function in PrintDataService that only takes a jurorNumber
    // and looks up the previous letter in the bulk_print_data table to retrieve the requested information.
    ENG_REQUESTINFO("5227", null, IJurorStatus.ADDITIONAL_INFO),
    BI_REQUESTINFO("5227C", null, IJurorStatus.ADDITIONAL_INFO),
    ENG_WITHDRAWAL("5224", PrintDataService::printWithdrawalLetter, IJurorStatus.DISQUALIFIED),
    BI_WITHDRAWAL("5224C", PrintDataService::printWithdrawalLetter, IJurorStatus.DISQUALIFIED);
    private final String code;

    private int jurorStatus;  // Todo: this may need to be a list of statuses

    private final BiConsumer<PrintDataService, JurorPool> letterPrinter;

    FormCode(String code, BiConsumer<PrintDataService, JurorPool> letterPrinter, int jurorStatus) {
        this.code = code;
        this.letterPrinter = letterPrinter;
        this.jurorStatus = jurorStatus;
    }

    public static FormCode getFormCode(String code) {
        for (FormCode formCode : FormCode.values()) {
            if (formCode.getCode().equals(code)) {
                return formCode;
            }
        }
        return null;
    }

}

