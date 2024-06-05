package uk.gov.hmcts.juror.api.moj.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DisqualifyCode {

    A("A", "Age"),
    B("B", "Bail"),
    C("C", "Conviction"),
    N("N", "Mental Capacity Act"),
    O("O", "Mental Health Act"),
    R("R", "Residency"),
    M("M", "Suffering From a Mental Disorder"),
    J("J", "Involved in Justice Administration or a Member of the Clergy"),
    E("E", "Electronic Police Check Failure"),
    D("D", "JUDICIAL DISQUALIFICATION");

    private final String code;
    private final String description;

    public static String getCode(DisqualifyCode code) {
        return code.code;
    }

    public static String getDescription(DisqualifyCode code) {
        return code.description;
    }

    public static DisqualifyCode getDisqualifyCode(String code) {
        for (DisqualifyCode disqualifyCode : DisqualifyCode.values()) {
            if (disqualifyCode.getCode().equals(code)) {
                return disqualifyCode;
            }
        }
        return null;
    }
}
