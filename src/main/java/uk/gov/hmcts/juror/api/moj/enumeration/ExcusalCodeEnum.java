package uk.gov.hmcts.juror.api.moj.enumeration;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Excusal codes (reasons), mapped to the database table t_exc_code.
 * (removed deceased as that is a separate flow)
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum ExcusalCodeEnum {
    A("A", "Moved from area"),
    B("B", "Student"),
    C("C", "Childcare"),
    E("E", "Parliament/European assembly"),
    F("F", "The Forces"),
    G("G", "Financial hardship"),
    H("H", "Medical profession"),
    I("I", "Ill"),
    J("J", "Excuse by Bureau, too many jurors"),
    K("K", "Criminal Record"),
    L("L", "Language difficulties"),
    M("M", "Medical"),
    N("N", "Mental Health"),
    O("O", "Other"),
    P("P", "Postponement of service"),
    R("R", "Religious reasons"),
    S("S", "Recently served"),
    T("T", "Travelling difficulties"),
    U("U", "Works in administration of justice"),
    V("V", "Over 69"),
    W("W", "Work related"),
    X("X", "Carer"),
    Y("Y", "Holiday"),
    Z("D", "Bereavement"),
    CE("CE", "CJS employee (unable to transfer"),
    DC("DC", "Deferred by court - too many jurors"),
    PE("PE", "Personal engagement");

    private final String code;
    private final String description;

}
