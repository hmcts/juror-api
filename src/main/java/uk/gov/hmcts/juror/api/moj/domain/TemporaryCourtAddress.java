package uk.gov.hmcts.juror.api.moj.domain;


import lombok.Getter;

@Getter
public enum TemporaryCourtAddress {
    TAUNTON("Taunton Crown Court Sitting at Worle,\n" +
            "North Somerset Court House,\n" +
            "The Hedges Weston Super Mare,\n" +
            "BS22 7BB."),
    HARROW("Hendon Magistrates Court,\n" +
           "The Court House,\n" +
           "The Hyde,\n" +
           "Hendon,\n" +
           "NW9 7BY.\n" +
           "or\n" +
           "Willesden Magistrates Court,\n" +
           "448 High Road,\n" +
           "London,\n" +
           "NW10 2DZ.");
    private final String temporaryCourtAddress;

    TemporaryCourtAddress(String temporaryCourtAddress) {
        this.temporaryCourtAddress = temporaryCourtAddress;
    }

    public String getTemporaryCourtAddress() {
        return temporaryCourtAddress;
    }
}
