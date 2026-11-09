package uk.gov.hmcts.juror.api.moj.domain;


import lombok.Getter;

@Getter
public enum TemporaryCourtAddress {
    TAUNTON("""
                Taunton Crown Court Sitting at Worle,
                North Somerset Court House,
                The Hedges,
                Weston Super Mare,
                BS22 7BB.""");

    private final String temporaryCourtAddress;

    TemporaryCourtAddress(String temporaryCourtAddress) {
        this.temporaryCourtAddress = temporaryCourtAddress;
    }

    public String getTemporaryCourtAddress() {
        return temporaryCourtAddress;
    }
}
