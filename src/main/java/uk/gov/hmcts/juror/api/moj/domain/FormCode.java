package uk.gov.hmcts.juror.api.moj.domain;

import lombok.Getter;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.service.PrintDataService;

import java.util.List;
import java.util.function.BiConsumer;

@Getter
public enum FormCode {

    ENG_EXCUSAL("5225", PrintDataService::printExcusalLetter, List.of(IJurorStatus.EXCUSED)),
    BI_EXCUSAL("5225C", PrintDataService::printExcusalLetter, List.of(IJurorStatus.EXCUSED)),
    ENG_EXCUSALDENIED("5226", PrintDataService::printExcusalDeniedLetter, List.of(IJurorStatus.SUMMONED,
                                                                                  IJurorStatus.RESPONDED)),
    BI_EXCUSALDENIED("5226C", PrintDataService::printExcusalDeniedLetter, List.of(IJurorStatus.SUMMONED,
                                                                                  IJurorStatus.RESPONDED)),
    ENG_SUMMONS("5221", PrintDataService::reprintSummonsLetter, List.of(IJurorStatus.SUMMONED)),
    BI_SUMMONS("5221C", PrintDataService::reprintSummonsLetter, List.of(IJurorStatus.SUMMONED)),
    ENG_SUMMONS_REMINDER("5228", PrintDataService::printSummonsReminderLetter, List.of(IJurorStatus.SUMMONED)),
    BI_SUMMONS_REMINDER("5228C", PrintDataService::printSummonsReminderLetter, List.of(IJurorStatus.SUMMONED)),
    ENG_CONFIRMATION("5224A", PrintDataService::printConfirmationLetter, List.of(IJurorStatus.RESPONDED)),
    BI_CONFIRMATION("5224AC", PrintDataService::printConfirmationLetter, List.of(IJurorStatus.RESPONDED)),
    ENG_DEFERRAL("5229A", PrintDataService::printDeferralLetter, List.of(IJurorStatus.DEFERRED)),
    BI_DEFERRAL("5229AC", PrintDataService::printDeferralLetter, List.of(IJurorStatus.DEFERRED)),
    ENG_DEFERRALDENIED("5226A", PrintDataService::printDeferralDeniedLetter, List.of(IJurorStatus.RESPONDED)),
    BI_DEFERRALDENIED("5226AC", PrintDataService::printDeferralDeniedLetter, List.of(IJurorStatus.RESPONDED)),
    ENG_POSTPONE("5229", PrintDataService::printPostponeLetter, List.of(IJurorStatus.DEFERRED)),
    BI_POSTPONE("5229C", PrintDataService::printPostponeLetter, List.of(IJurorStatus.DEFERRED)),
    // The additional information status on juror is not used in modernisation code
    ENG_REQUESTINFO("5227", PrintDataService::reprintRequestInfoLetter, List.of(IJurorStatus.SUMMONED)),
    BI_REQUESTINFO("5227C", PrintDataService::reprintRequestInfoLetter, List.of(IJurorStatus.SUMMONED)),
    ENG_WITHDRAWAL("5224", PrintDataService::printWithdrawalLetter, List.of(IJurorStatus.DISQUALIFIED)),
    BI_WITHDRAWAL("5224C", PrintDataService::printWithdrawalLetter, List.of(IJurorStatus.DISQUALIFIED));
    private final String code;

    private final List<Integer> jurorStatus;

    private final BiConsumer<PrintDataService, JurorPool> letterPrinter;

    FormCode(String code, BiConsumer<PrintDataService, JurorPool> letterPrinter, List<Integer> jurorStatus) {
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

