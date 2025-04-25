package uk.gov.hmcts.juror.api.moj.domain;

public enum TemporaryCourtPhone {
    TAUNTON("01823 334 100"),
    HARROW("Hendon 020 8358 1000 or Willesden 020 8358 1000");

    private final String temporaryCourtPhone;

    TemporaryCourtPhone(String temporaryCourtPhone) {
        this.temporaryCourtPhone = temporaryCourtPhone;
    }

    public String getTemporaryCourtPhone() {
        return temporaryCourtPhone;
    }
}
