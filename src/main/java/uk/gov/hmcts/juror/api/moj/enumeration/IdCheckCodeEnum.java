package uk.gov.hmcts.juror.api.moj.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum IdCheckCodeEnum {

    A('A', "Bank Statement"),
    B('B', "Birth Certificate"),
    C('C', "Credit Card"),
    D('D', "Drivers Licence"),
    E('E', "EU Nat ID Card"),
    F('F', "Bus Pass"),
    H('H', "Home Office Doc"),
    I('I', "Company ID"),
    L('L', "Cheque Bk, Crd 3Stts"),
    M('M', "Medical Card"),
    N('N', "None"),
    O('O', "Other"),
    P('P', "Passport"),
    S('S', "Nat Insurance Card"),
    T('T', "Travel Card"),
    U('U', "Utility Bill"),
    V('V', "Bank or Visa card"),
    W('W', "Work Permit"),
    X('X', "DSS ID");
    
    private final Character code;
    private final String description;

    public static Character getCode(IdCheckCodeEnum code) {
        return code.code;
    }

    public static String getDescription(IdCheckCodeEnum code) {
        return code.description;
    }

    public static IdCheckCodeEnum getIdCheckCodeEnum(String code) {
        for (IdCheckCodeEnum idCheckCode : IdCheckCodeEnum.values()) {
            if (idCheckCode.getCode().equals(code)) {
                return idCheckCode;
            }
        }
        return null;
    }
}
