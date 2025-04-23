package uk.gov.hmcts.juror.api.moj.domain;


import lombok.Getter;

@Getter
public enum TemporaryCourtAddress {
    TAUNTON("Taunton Crown Court Sitting at Worle," +
            "North Somerset Court House, " +
            "The Hedges Weston Super Mare, " +
            "BS22 7BB."),
    HARROW("Hendon Magistrates Court," +
           "The Court House," +
           "The Hyde," +
           "Hendon," +
           "NW9 7BY." +
           "or" +
           "Willesden Magistrates Court," +
           "448 High Road," +
           "London," +
           "NW10 2DZ.");
    private final String temporaryCourtAddress;

    TemporaryCourtAddress(String temporaryCourtAddress) {
        this.temporaryCourtAddress = temporaryCourtAddress;
    }

    public String getTemporaryCourtAddress() {
        return temporaryCourtAddress;
    }
}
