package uk.gov.hmcts.juror.api.moj.xerox;

import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocation;
import uk.gov.hmcts.juror.api.moj.domain.FormCode;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.xerox.letters.DbdResponseLetter;
import uk.gov.hmcts.juror.api.moj.xerox.letters.DbdSummonsLetter;
import uk.gov.hmcts.juror.api.moj.xerox.letters.DeferralDeniedLetter;
import uk.gov.hmcts.juror.api.moj.xerox.letters.ExcusalDeniedLetter;
import uk.gov.hmcts.juror.api.moj.xerox.letters.ExcusalLetter;
import uk.gov.hmcts.juror.api.moj.xerox.letters.WithdrawalLetter;

import java.util.Locale;

/** Independent fixed-width expectations shared by unit and database regression tests. */
public final class LetterLayoutTestUtils {
    private LetterLayoutTestUtils() {
    }

    public static LetterBase createLetter(FormCode code, JurorPool pool, CourtLocation court,
                                          CourtLocation bureau, WelshCourtLocation welshCourt) {
        return switch (code) {
            case ENG_DBD_SUMMONS -> new DbdSummonsLetter(pool, court, bureau, welshCourt);
            case ENG_DBD_RESPONSE -> new DbdResponseLetter(pool, court, bureau, welshCourt);
            case ENG_DEFERRALDENIED -> new DeferralDeniedLetter(pool, court, bureau, welshCourt);
            case ENG_EXCUSALDENIED -> new ExcusalDeniedLetter(pool, court, bureau, welshCourt);
            case ENG_EXCUSAL -> new ExcusalLetter(pool, court, bureau, welshCourt);
            case ENG_WITHDRAWAL -> new WithdrawalLetter(pool, court, bureau, welshCourt);
            default -> throw new IllegalArgumentException("Unsupported test form code: " + code);
        };
    }

    public static String expectedRecord(FormCode code, boolean sparse) {
        boolean welsh = code.name().startsWith("BI_");
        boolean dbd = code.name().contains("DBD");
        String jurorAddress = field("831 STREET NAME", 35)
            + field("ANYTOWN", 35)
            + (sparse ? "" : field("JUROR_ADDRESS_3", 35))
            + field("JUROR_ADDRESS_4", 35)
            + (sparse ? "" : field("JUROR_ADDRESS_5", 35))
            + field("SY2 6LU", 10)
            + " ".repeat((dbd ? 0 : 35) + (sparse ? 70 : 0));
        return dbd ? dbdRecord(welsh, sparse, jurorAddress) : decisionRecord(code, welsh, sparse, jurorAddress);
    }

    private static String dbdRecord(boolean welsh, boolean sparse, String jurorAddress) {
        return field("415221201", 9) + field("MR", 10) + field("FNAMEEIGHTTHREEONE", 20)
            + field("LNAMEEIGHTTHREEONEFIVE", 25) + jurorAddress + "641500541"
            + field(LetterBase.getDateOfLetter(), 18)
            + field("MONDAY 6 FEBRUARY, 2017", 32)
            + (welsh ? field("DYDD LLUN 6 CHWEFROR, 2017", 32) : "")
            + field("10:00AM", 8) + "457"
            + field("THE CROWN COURT AT SWANSEA", welsh ? 40 : 59)
            + courtAddress(false, sparse) + field("01792637000", 12)
            + (welsh ? field("ABERTAWE", 40) + dbdWelshCourtAddress(sparse) : "");
    }

    private static String decisionRecord(FormCode code, boolean welsh, boolean sparse, String jurorAddress) {
        String bureauAddress = field("THE COURT SERVICE", 35)
            + (sparse ? "" : field("FREEPOST LON 19669", 35))
            + field("POCOCK STREET", 35) + field("LONDON", 35)
            + field("BUREAU_ADDRESS_5", 35) + field("BUREAU_ADDRESS_6", 35)
            + field("SE1 0YG", 10) + (sparse ? " ".repeat(35) : "");
        String letterDate = welsh ? LetterBase.getWelshDateOfLetter().toUpperCase(Locale.ROOT)
            : LetterBase.getDateOfLetter();
        return field(letterDate, 18)
            + field(welsh ? "YN ABERTAWE" : "THE CROWN COURT AT SWANSEA", welsh ? 40 : 59)
            + field("JURY CENTRAL SUMMONING BUREAU", 40) + bureauAddress
            + field("0845 3555567", 12) + " ".repeat(12)
            + (code.name().contains("DENIED") ? courtAddress(welsh, sparse) : "")
            + field("MR", 10) + field("FNAMEEIGHTTHREEONE", 20) + field("LNAMEEIGHTTHREEONEFI", 20)
            + jurorAddress + "641500541"
            + field(code.name().contains("EXCUSALDENIED") ? "COURT SIGNATORY" : "BUREAU SIGNATORY", 30)
            + field("LNAMEEIGHTTHREEONEFIVE", 25);
    }

    private static String courtAddress(boolean welsh, boolean sparse) {
        return field(welsh ? "Y LLYSOEDD BARN" : "THE LAW COURTS", 35)
            + (sparse ? "" : field(welsh ? "LON SAN HELEN" : "ST HELENS ROAD", 35))
            + field(welsh ? "ABERTAWE" : "SWANSEA", 35)
            + field(welsh ? "WELSH_COURT_ADDRESS_4" : "SHREWSBURY", 35)
            + field(welsh ? "WELSH_COURT_ADDRESS_5" : "COURT_ADDRESS_5", 35)
            + field(welsh ? "WELSH_COURT_ADDRESS_6" : "COURT_ADDRESS_6", 35)
            + field("SY2 6LU", 10) + (sparse ? " ".repeat(35) : "");
    }

    private static String dbdWelshCourtAddress(boolean sparse) {
        // DBD uses five Welsh address lines and a 35-character postcode slot.
        return field("Y LLYSOEDD BARN", 35) + (sparse ? "" : field("LON SAN HELEN", 35))
            + field("ABERTAWE", 35) + field("WELSH_COURT_ADDRESS_4", 35)
            + field("WELSH_COURT_ADDRESS_5", 35) + field("SY2 6LU", 35)
            + (sparse ? " ".repeat(35) : "");
    }

    private static String field(String value, int width) {
        return String.format("%-" + width + "s", value);
    }
}
