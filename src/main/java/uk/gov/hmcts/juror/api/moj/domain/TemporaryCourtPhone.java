package uk.gov.hmcts.juror.api.moj.domain;

import lombok.Getter;

@Getter
public enum TemporaryCourtPhone {
    TAUNTON("01823 334 100"),
    HARROW("Hendon 02085111421 or Willesden 02085111421");

    private final String temporaryCourtPhone;

    TemporaryCourtPhone(String temporaryCourtPhone) {
        this.temporaryCourtPhone = temporaryCourtPhone;
    }

    public String getTemporaryCourtPhone() {
        return temporaryCourtPhone;
    }
}
