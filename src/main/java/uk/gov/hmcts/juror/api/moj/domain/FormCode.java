package uk.gov.hmcts.juror.api.moj.domain;

import lombok.Getter;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.service.PrintDataService;

import java.util.function.BiConsumer;

@Getter
public enum FormCode {

    ENG_EXCUSAL("5225", PrintDataService::printExcusalLetter, IJurorStatus.EXCUSED),
    BI_EXCUSAL("5225C", PrintDataService::printExcusalLetter, IJurorStatus.EXCUSED),
    ENG_EXCUSALDENIED("5226", PrintDataService::printExcusalDeniedLetter, IJurorStatus.SUMMONED),
    BI_EXCUSALDENIED("5226C", PrintDataService::printExcusalDeniedLetter, IJurorStatus.SUMMONED),
    ENG_SUMMONS("5221", PrintDataService::reprintSummonsLetter, IJurorStatus.SUMMONED),
    BI_SUMMONS("5221C", PrintDataService::reprintSummonsLetter, IJurorStatus.SUMMONED),
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
    // The additional information status on juror is not used in modernisation code
    ENG_REQUESTINFO("5227", PrintDataService::reprintRequestInfoLetter, IJurorStatus.SUMMONED),
    BI_REQUESTINFO("5227C", PrintDataService::reprintRequestInfoLetter, IJurorStatus.SUMMONED),
    ENG_WITHDRAWAL("5224", PrintDataService::printWithdrawalLetter, IJurorStatus.DISQUALIFIED),
    BI_WITHDRAWAL("5224C", PrintDataService::printWithdrawalLetter, IJurorStatus.DISQUALIFIED);
    private final String code;

    private final int jurorStatus;

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
        throw new MojException.InternalServerError("Unknown form code '" + code + "'", null);
    }

}

