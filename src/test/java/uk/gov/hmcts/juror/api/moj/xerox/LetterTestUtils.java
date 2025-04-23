package uk.gov.hmcts.juror.api.moj.xerox;

import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocation;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;

//False positive this class is used to set up test data instead of running tests
@SuppressWarnings("PMD.JUnit4TestShouldUseTestAnnotation")
public final class LetterTestUtils {

    private LetterTestUtils() {

    }

    static String pad(String data, Integer width) {
        return StringUtils.rightPad(data, width, " ");
    }

    static String emptyField(Integer width) {
        return StringUtils.rightPad("", width, " ");
    }

    static Juror testJuror() {
        Juror juror = new Juror();
        juror.setTitle("MR");
        juror.setFirstName("FNAMEEIGHTTHREEONE");
        juror.setLastName("LNAMEEIGHTTHREEONEFIVE");
        juror.setAddressLine1("831 STREET NAME");
        juror.setAddressLine2("ANYTOWN");
        juror.setAddressLine3("JUROR_ADDRESS_3");
        juror.setAddressLine4("JUROR_ADDRESS_4");
        juror.setAddressLine5("JUROR_ADDRESS_5");
        juror.setPostcode("SY2 6LU");
        juror.setJurorNumber("641500541");

        return juror;
    }

    static Juror testWelshJuror() {
        Juror juror = new Juror();
        juror.setTitle("MR");
        juror.setFirstName("FNAMEEIGHTTHREEONE");
        juror.setLastName("LNAMEEIGHTTHREEONEFIVE");
        juror.setAddressLine1("831 STREET NAME");
        juror.setAddressLine2("ANYTOWN");
        juror.setAddressLine3("JUROR_ADDRESS_3");
        juror.setAddressLine4("JUROR_ADDRESS_4");
        juror.setAddressLine5("JUROR_ADDRESS_5");
        juror.setPostcode("SY2 6LU");
        juror.setJurorNumber("641500541");
        juror.setWelsh(true);

        return juror;
    }

    static PoolRequest testPoolRequest(LocalDate date) {
        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber("415221201");
        poolRequest.setCourtLocation(testCourtLocation());
        poolRequest.setReturnDate(date);
        poolRequest.setAttendTime(LocalDateTime.of(2017, Month.FEBRUARY, 6, 10, 0));

        return poolRequest;
    }

    public static JurorPool testJurorPool(LocalDate date) {
        JurorPool jurorPool = new JurorPool();
        jurorPool.setOwner("400");
        jurorPool.setNextDate(date);
        jurorPool.setDeferralDate(date);
        jurorPool.setPool(testPoolRequest(date));
        jurorPool.setJuror(testJuror());
        JurorStatus status = new JurorStatus();
        status.setStatus(IJurorStatus.SUMMONED);
        jurorPool.setStatus(status);

        return jurorPool;
    }

    public static JurorPool testDisqualifiedJurorPool(LocalDate date) {
        JurorPool jurorPool = new JurorPool();
        jurorPool.setOwner("400");
        jurorPool.setNextDate(date);
        jurorPool.setDeferralDate(date);
        jurorPool.setPool(testPoolRequest(date));
        jurorPool.setJuror(testJuror());
        JurorStatus status = new JurorStatus();
        status.setStatus(IJurorStatus.DISQUALIFIED);
        jurorPool.setStatus(status);

        return jurorPool;
    }

    static JurorPool testWelshJurorPool(LocalDate date) {
        JurorPool jurorPool = new JurorPool();
        jurorPool.setOwner("400");
        jurorPool.setNextDate(date);
        jurorPool.setDeferralDate(date);
        jurorPool.setPool(testPoolRequest(date));
        jurorPool.setJuror(testWelshJuror());

        return jurorPool;
    }

    public static CourtLocation testBureauLocation() {
        CourtLocation courtLocationBureau = new CourtLocation();
        courtLocationBureau.setName("JURY CENTRAL SUMMONING BUREAU");
        courtLocationBureau.setAddress1("THE COURT SERVICE");
        courtLocationBureau.setAddress2("FREEPOST LON 19669");
        courtLocationBureau.setAddress3("POCOCK STREET");
        courtLocationBureau.setAddress4("LONDON");
        courtLocationBureau.setAddress5("BUREAU_ADDRESS_5");
        courtLocationBureau.setAddress6("BUREAU_ADDRESS_6");
        courtLocationBureau.setPostcode("SE1 0YG");
        courtLocationBureau.setLocPhone("0845 3555567");
        courtLocationBureau.setCourtFaxNo("01274 840275");
        courtLocationBureau.setSignatory("JURY MANAGER");

        return courtLocationBureau;
    }

    static CourtLocation testCourtLocation() {
        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setCourtAttendTime(LocalTime.of(10,0));
        courtLocation.setLocCode("457");
        courtLocation.setInsertIndicators("TWO WEEKS");
        courtLocation.setLocCourtName("THE CROWN COURT AT SWANSEA");
        courtLocation.setName("SWANSEA");
        courtLocation.setAddress1("THE LAW COURTS");
        courtLocation.setAddress2("ST HELENS ROAD");
        courtLocation.setAddress3("SWANSEA");
        courtLocation.setAddress4("SHREWSBURY");
        courtLocation.setAddress5("COURT_ADDRESS_5");
        courtLocation.setAddress6("COURT_ADDRESS_6");
        courtLocation.setPostcode("SY2 6LU");
        courtLocation.setLocPhone("01792637000");
        courtLocation.setCourtFaxNo("01473 228560");
        courtLocation.setSignatory("JURY MANAGER");
        courtLocation.setInsertIndicators("TWO WEEKS");

        return courtLocation;
    }

    public static WelshCourtLocation testWelshCourtLocation() {
        WelshCourtLocation courtLocationWelsh = new WelshCourtLocation();
        courtLocationWelsh.setLocCourtName("ABERTAWE");
        courtLocationWelsh.setCorrespondenceName("YN ABERTAWE");
        courtLocationWelsh.setAddress1("Y LLYSOEDD BARN");
        courtLocationWelsh.setAddress2("LON SAN HELEN");
        courtLocationWelsh.setAddress3("ABERTAWE");
        courtLocationWelsh.setAddress4("WELSH_COURT_ADDRESS_4");
        courtLocationWelsh.setAddress5("WELSH_COURT_ADDRESS_5");
        courtLocationWelsh.setAddress6("WELSH_COURT_ADDRESS_6");

        return courtLocationWelsh;
    }
}
