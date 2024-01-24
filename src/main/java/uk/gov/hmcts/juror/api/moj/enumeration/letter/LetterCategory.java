package uk.gov.hmcts.juror.api.moj.enumeration.letter;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum LetterCategory {
    ELIGIBILITY("Eligibility"),
    JUROR_DETAILS("Juror details"),
    REPLY_TYPE("Reply type"),
    CJS_EMPLOYMENT("CJS Employment"),
    REASONABLE_ADJUSTMENTS("Reasonable adjustments");

    private final String categoryName;
}
