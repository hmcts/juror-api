package uk.gov.hmcts.juror.api.moj.domain;

import lombok.Getter;

@Getter
public enum TemporaryCourtPhone {
    TAUNTON("01823 281 100"),
    HARROW("02085111421");

    private final String temporaryCourtPhone;

    TemporaryCourtPhone(String temporaryCourtPhone) {
        this.temporaryCourtPhone = temporaryCourtPhone;
    }

    public String getTemporaryCourtPhone() {
        return temporaryCourtPhone;
    }
}
