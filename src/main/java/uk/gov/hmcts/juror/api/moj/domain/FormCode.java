package uk.gov.hmcts.juror.api.moj.domain;

import lombok.Getter;

@Getter
public enum FormCode {


    ENG_SUMMONS("5221"),
    BI_SUMMONS("5221C"),
    ENG_CONFIRMATION("5224A"),
    BI_CONFIRMATION("5224AC"),
    ENG_DEFERRAL("5229A"),
    BI_DEFERRAL("5229AC"),
    ENG_DEFERRALDENIED("5226A"),
    BI_DEFERRALDENIED("5226AC");

    private final String code;

    FormCode(String code) {
        this.code = code;
    }

}
