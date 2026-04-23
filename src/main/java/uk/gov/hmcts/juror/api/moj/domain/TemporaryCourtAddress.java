package uk.gov.hmcts.juror.api.moj.domain;


import lombok.Getter;

@Getter
public enum TemporaryCourtAddress {
    TAUNTON("Taunton Crown Court Sitting at Worle,\n"
                +
                "North Somerset Court House,\n"
                +
                "The Hedges,\n"
                +
                "Weston Super Mare,\n"
                +
                "BS22 7BB.");

    private final String temporaryCourtAddress;

    TemporaryCourtAddress(String temporaryCourtAddress) {
        this.temporaryCourtAddress = temporaryCourtAddress;
    }

    public String getTemporaryCourtAddress() {
        return temporaryCourtAddress;
    }
}
