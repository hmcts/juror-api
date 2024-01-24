package uk.gov.hmcts.juror.api.moj.service.deferralmaintenance;

import com.querydsl.core.Tuple;
import org.mockito.Mockito;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class ManageDeferralsServiceTestData {
    private ManageDeferralsServiceTestData() {
    }

    public static JurorPool createJurorPoolForDeferrals(String owner) {
        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCode("415");
        courtLocation.setName("Chester");

        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber("415230101");
        poolRequest.setCourtLocation(courtLocation);

        JurorStatus jurorStatus = new JurorStatus();
        jurorStatus.setStatus(IJurorStatus.RESPONDED);
        jurorStatus.setStatusDesc("Responded");

        Juror juror = new Juror();
        juror.setTitle("Dr");
        juror.setFirstName("Test");
        juror.setLastName("Person");
        juror.setAddressLine1("Address Line One");
        juror.setAddressLine2("Address Line Two");
        juror.setAddressLine3("Address Line Three");
        juror.setAddressLine4("Town");
        juror.setAddressLine5("County");
        juror.setPostcode("PO19 1SX");

        JurorPool jurorPool = new JurorPool();
        jurorPool.setOwner(owner);
        jurorPool.setStatus(jurorStatus);
        jurorPool.setPool(poolRequest);

        juror.setAssociatedPools(Set.of(jurorPool));
        jurorPool.setJuror(juror);

        return jurorPool;
    }

    public static DigitalResponse createJurorResponseForDeferrals(String jurorNumber) {
        DigitalResponse digitalResponse = new DigitalResponse();
        digitalResponse.setJurorNumber(jurorNumber);
        digitalResponse.setDeferralReason("C");
        digitalResponse.setDeferralDate("29/05/2023, 12/6/2023, 3/7/2023");

        return digitalResponse;
    }

    public static DigitalResponse createJurorResponseWithoutDeferrals(String jurorNumber) {
        DigitalResponse digitalResponse = createJurorResponseForDeferrals(jurorNumber);
        digitalResponse.setDeferralDate(null);

        return digitalResponse;
    }

    public static List<Tuple> createActivePoolsForDeferralsFirstDate() {
        List<Tuple> firstDateQueryResult = new ArrayList<>();
        Tuple deferralOption1ForFirstDate = Mockito.mock(Tuple.class);
        setUpMockQueryResult(deferralOption1ForFirstDate, "415220502",
            LocalDate.of(2023, 6, 1), 4, 2);

        Tuple deferralOption2ForFirstDate = Mockito.mock(Tuple.class);
        setUpMockQueryResult(deferralOption2ForFirstDate, "415220401",
            LocalDate.of(2023, 5, 30), 2, 4);

        firstDateQueryResult.add(deferralOption1ForFirstDate);
        firstDateQueryResult.add(deferralOption2ForFirstDate);

        return firstDateQueryResult;
    }

    public static List<Tuple> createActivePoolsForDeferralsSecondDate() {
        List<Tuple> secondDateQueryResult = new ArrayList<>();
        Tuple deferralOption1ForSecondDate = Mockito.mock(Tuple.class);
        setUpMockQueryResult(deferralOption1ForSecondDate, "415220503",
            LocalDate.of(2023, 6, 12), 4, 0);

        secondDateQueryResult.add(deferralOption1ForSecondDate);

        return secondDateQueryResult;
    }

    public static List<Tuple> createActivePoolsForDeferralsThirdDate() {
        return new ArrayList<>();
    }

    private static void setUpMockQueryResult(Tuple deferralOption,
                                             String poolNumber,
                                             LocalDate serviceStartDate,
                                             int poolMembersRequested,
                                             int activePoolMemberCount) {
        Mockito.doReturn(poolNumber).when(deferralOption).get(0, String.class);
        Mockito.doReturn(serviceStartDate).when(deferralOption).get(1, LocalDate.class);
        Mockito.doReturn(poolMembersRequested).when(deferralOption).get(2, Integer.class);
        Mockito.doReturn(activePoolMemberCount).when(deferralOption).get(3, Integer.class);
    }
}