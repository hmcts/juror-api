package uk.gov.hmcts.juror.api.moj.enumeration;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class IdCheckCodeTest {

    @Test
    void confirmIdCheckCodeEnumValues() {
        assertEquals(IdCheckCodeEnum.A, 'A', "Bank Statement");
        assertEquals(IdCheckCodeEnum.B, 'B', "Birth Certificate");
        assertEquals(IdCheckCodeEnum.C, 'C', "Credit Card");
        assertEquals(IdCheckCodeEnum.D, 'D', "Drivers Licence");
        assertEquals(IdCheckCodeEnum.E, 'E', "EU Nat ID Card");
        assertEquals(IdCheckCodeEnum.F, 'F', "Bus Pass");
        assertEquals(IdCheckCodeEnum.H, 'H', "Home Office Doc");
        assertEquals(IdCheckCodeEnum.I, 'I', "Company ID");
        assertEquals(IdCheckCodeEnum.L, 'L', "Cheque Bk, Crd 3Stts");
        assertEquals(IdCheckCodeEnum.M, 'M', "Medical Card");
        assertEquals(IdCheckCodeEnum.N, 'N', "None");
        assertEquals(IdCheckCodeEnum.O, 'O', "Other");
        assertEquals(IdCheckCodeEnum.P, 'P', "Passport");
        assertEquals(IdCheckCodeEnum.S, 'S', "Nat Insurance Card");
        assertEquals(IdCheckCodeEnum.T, 'T', "Travel Card");
        assertEquals(IdCheckCodeEnum.U, 'U', "Utility Bill");
        assertEquals(IdCheckCodeEnum.V, 'V', "Bank or Visa card");
        assertEquals(IdCheckCodeEnum.W, 'W', "Work Permit");
        assertEquals(IdCheckCodeEnum.X, 'X', "DSS ID");

    }

    @Test
    void confirmGetCode() {
        assertThat(IdCheckCodeEnum.getCode(IdCheckCodeEnum.A)).isEqualTo('A');
        assertThat(IdCheckCodeEnum.getCode(IdCheckCodeEnum.B)).isEqualTo('B');
        assertThat(IdCheckCodeEnum.getCode(IdCheckCodeEnum.C)).isEqualTo('C');
        assertThat(IdCheckCodeEnum.getCode(IdCheckCodeEnum.D)).isEqualTo('D');
        assertThat(IdCheckCodeEnum.getCode(IdCheckCodeEnum.E)).isEqualTo('E');
        assertThat(IdCheckCodeEnum.getCode(IdCheckCodeEnum.F)).isEqualTo('F');
        assertThat(IdCheckCodeEnum.getCode(IdCheckCodeEnum.H)).isEqualTo('H');
        assertThat(IdCheckCodeEnum.getCode(IdCheckCodeEnum.I)).isEqualTo('I');
        assertThat(IdCheckCodeEnum.getCode(IdCheckCodeEnum.L)).isEqualTo('L');
        assertThat(IdCheckCodeEnum.getCode(IdCheckCodeEnum.M)).isEqualTo('M');
        assertThat(IdCheckCodeEnum.getCode(IdCheckCodeEnum.N)).isEqualTo('N');
        assertThat(IdCheckCodeEnum.getCode(IdCheckCodeEnum.O)).isEqualTo('O');
        assertThat(IdCheckCodeEnum.getCode(IdCheckCodeEnum.P)).isEqualTo('P');
        assertThat(IdCheckCodeEnum.getCode(IdCheckCodeEnum.S)).isEqualTo('S');
        assertThat(IdCheckCodeEnum.getCode(IdCheckCodeEnum.T)).isEqualTo('T');
        assertThat(IdCheckCodeEnum.getCode(IdCheckCodeEnum.U)).isEqualTo('U');
        assertThat(IdCheckCodeEnum.getCode(IdCheckCodeEnum.V)).isEqualTo('V');
        assertThat(IdCheckCodeEnum.getCode(IdCheckCodeEnum.W)).isEqualTo('W');
        assertThat(IdCheckCodeEnum.getCode(IdCheckCodeEnum.X)).isEqualTo('X');
    }

    @Test
    void confirmGetDescription() {
        assertThat(IdCheckCodeEnum.getDescription(IdCheckCodeEnum.A)).isEqualTo("Bank Statement");
        assertThat(IdCheckCodeEnum.getDescription(IdCheckCodeEnum.B)).isEqualTo("Birth Certificate");
        assertThat(IdCheckCodeEnum.getDescription(IdCheckCodeEnum.C)).isEqualTo("Credit Card");
        assertThat(IdCheckCodeEnum.getDescription(IdCheckCodeEnum.D)).isEqualTo("Drivers Licence");
        assertThat(IdCheckCodeEnum.getDescription(IdCheckCodeEnum.E)).isEqualTo("EU Nat ID Card");
        assertThat(IdCheckCodeEnum.getDescription(IdCheckCodeEnum.F)).isEqualTo("Bus Pass");
        assertThat(IdCheckCodeEnum.getDescription(IdCheckCodeEnum.H)).isEqualTo("Home Office Doc");
        assertThat(IdCheckCodeEnum.getDescription(IdCheckCodeEnum.I)).isEqualTo("Company ID");
        assertThat(IdCheckCodeEnum.getDescription(IdCheckCodeEnum.L)).isEqualTo("Cheque Bk, Crd 3Stts");
        assertThat(IdCheckCodeEnum.getDescription(IdCheckCodeEnum.M)).isEqualTo("Medical Card");
        assertThat(IdCheckCodeEnum.getDescription(IdCheckCodeEnum.N)).isEqualTo("None");
        assertThat(IdCheckCodeEnum.getDescription(IdCheckCodeEnum.O)).isEqualTo("Other");
        assertThat(IdCheckCodeEnum.getDescription(IdCheckCodeEnum.P)).isEqualTo("Passport");
        assertThat(IdCheckCodeEnum.getDescription(IdCheckCodeEnum.S)).isEqualTo("Nat Insurance Card");
        assertThat(IdCheckCodeEnum.getDescription(IdCheckCodeEnum.T)).isEqualTo("Travel Card");
        assertThat(IdCheckCodeEnum.getDescription(IdCheckCodeEnum.U)).isEqualTo("Utility Bill");
        assertThat(IdCheckCodeEnum.getDescription(IdCheckCodeEnum.V)).isEqualTo("Bank or Visa card");
        assertThat(IdCheckCodeEnum.getDescription(IdCheckCodeEnum.W)).isEqualTo("Work Permit");
        assertThat(IdCheckCodeEnum.getDescription(IdCheckCodeEnum.X)).isEqualTo("DSS ID");
    }

    @Test
    void confirmGetIdCheckCodeEnum() {
        assertThat(IdCheckCodeEnum.getIdCheckCodeEnum('A')).isEqualTo(IdCheckCodeEnum.A);
        assertThat(IdCheckCodeEnum.getIdCheckCodeEnum('B')).isEqualTo(IdCheckCodeEnum.B);
        assertThat(IdCheckCodeEnum.getIdCheckCodeEnum('C')).isEqualTo(IdCheckCodeEnum.C);
        assertThat(IdCheckCodeEnum.getIdCheckCodeEnum('D')).isEqualTo(IdCheckCodeEnum.D);
        assertThat(IdCheckCodeEnum.getIdCheckCodeEnum('E')).isEqualTo(IdCheckCodeEnum.E);
        assertThat(IdCheckCodeEnum.getIdCheckCodeEnum('F')).isEqualTo(IdCheckCodeEnum.F);
        assertThat(IdCheckCodeEnum.getIdCheckCodeEnum('H')).isEqualTo(IdCheckCodeEnum.H);
        assertThat(IdCheckCodeEnum.getIdCheckCodeEnum('I')).isEqualTo(IdCheckCodeEnum.I);
        assertThat(IdCheckCodeEnum.getIdCheckCodeEnum('L')).isEqualTo(IdCheckCodeEnum.L);
        assertThat(IdCheckCodeEnum.getIdCheckCodeEnum('M')).isEqualTo(IdCheckCodeEnum.M);
        assertThat(IdCheckCodeEnum.getIdCheckCodeEnum('N')).isEqualTo(IdCheckCodeEnum.N);
        assertThat(IdCheckCodeEnum.getIdCheckCodeEnum('O')).isEqualTo(IdCheckCodeEnum.O);
        assertThat(IdCheckCodeEnum.getIdCheckCodeEnum('P')).isEqualTo(IdCheckCodeEnum.P);
        assertThat(IdCheckCodeEnum.getIdCheckCodeEnum('S')).isEqualTo(IdCheckCodeEnum.S);
        assertThat(IdCheckCodeEnum.getIdCheckCodeEnum('T')).isEqualTo(IdCheckCodeEnum.T);
        assertThat(IdCheckCodeEnum.getIdCheckCodeEnum('U')).isEqualTo(IdCheckCodeEnum.U);
        assertThat(IdCheckCodeEnum.getIdCheckCodeEnum('V')).isEqualTo(IdCheckCodeEnum.V);
        assertThat(IdCheckCodeEnum.getIdCheckCodeEnum('W')).isEqualTo(IdCheckCodeEnum.W);
        assertThat(IdCheckCodeEnum.getIdCheckCodeEnum('X')).isEqualTo(IdCheckCodeEnum.X);
    }

    private void assertEquals(IdCheckCodeEnum type, Character code, String description) {
        assertThat(type.getCode())
            .isEqualTo(code);
        assertThat(type.getDescription()).isEqualTo(description);
    }
}
