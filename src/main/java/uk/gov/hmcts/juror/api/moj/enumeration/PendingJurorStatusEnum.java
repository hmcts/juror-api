package uk.gov.hmcts.juror.api.moj.enumeration;

public enum PendingJurorStatusEnum {
    AUTHORISED('A'),
    QUEUED('Q'),
    REJECTED('R');

    private final char code;

    PendingJurorStatusEnum(char code) {
        this.code = code;
    }

    public char getCode() {
        return code;
    }
}
