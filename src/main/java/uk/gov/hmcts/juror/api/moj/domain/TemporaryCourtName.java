package uk.gov.hmcts.juror.api.moj.domain;

import lombok.Getter;

@Getter
public enum TemporaryCourtName {
    TAUNTON("Taunton Crown Court Sitting at Worle"),
    HARROW("Hendon Magistrates Court or Willesden Magistrates Court");
 private final String temporaryCourtName;

    TemporaryCourtName(String temporaryCourtName) {
        this.temporaryCourtName = temporaryCourtName;
    }

    public String getTemporaryCourtName() {
        return temporaryCourtName;
    }
}
