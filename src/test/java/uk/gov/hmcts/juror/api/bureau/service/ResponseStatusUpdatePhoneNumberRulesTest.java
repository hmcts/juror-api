package uk.gov.hmcts.juror.api.bureau.service;

import jakarta.persistence.EntityManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.juror.api.bureau.domain.BureauJurorSpecialNeed;
import uk.gov.hmcts.juror.api.bureau.domain.BureauJurorSpecialNeedsRepository;
import uk.gov.hmcts.juror.api.bureau.domain.JurorResponseAuditRepository;
import uk.gov.hmcts.juror.api.bureau.domain.PartAmendmentRepository;
import uk.gov.hmcts.juror.api.bureau.domain.PartHistRepository;
import uk.gov.hmcts.juror.api.bureau.domain.TSpecial;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponse;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponseRepository;
import uk.gov.hmcts.juror.api.juror.domain.Pool;
import uk.gov.hmcts.juror.api.juror.domain.PoolRepository;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocationRepository;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;


@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unused")
public class ResponseStatusUpdatePhoneNumberRulesTest {

    private static final String AUDITOR_USERNAME = "unitTestUser";
    private static final String HOME_NUMBER = "01000000000";
    private static final String JUROR_NUMBER = "34567";
    private static final String MOBILE_NUMBER = "07000000000";
    private static final String WORK_NUMBER = "01000000123";

    @Mock
    private JurorResponseRepository jurorResponseRepository;
    @Mock
    private JurorResponseAuditRepository auditRepository;
    @Mock
    private PoolRepository poolDetailsRepository;
    @Mock
    private PartAmendmentRepository amendmentRepository;
    @Mock
    private PartHistRepository historyRepository;
    @Mock
    private UrgencyService urgencyService;
    @Mock
    private EntityManager entityManager;
    @Mock
    private AssignOnUpdateService assignOnUpdateService;

    @Mock
    private BureauJurorSpecialNeedsRepository specialNeedsRepository;

    @Mock
    private WelshCourtLocationRepository welshCourtLocationRepository;

    @InjectMocks
    private ResponseStatusUpdateServiceImpl statusUpdateService;

    private JurorResponse jurorResponse;
    private Pool poolDetails;

    @Before
    public void setup() {
        jurorResponse = new JurorResponse();
        jurorResponse.setJurorNumber(JUROR_NUMBER);
        jurorResponse.setProcessingComplete(false);

        poolDetails = new Pool();
        poolDetails.setJurorNumber(JUROR_NUMBER);

        doReturn(poolDetails).when(poolDetailsRepository).findByJurorNumber(JUROR_NUMBER);
    }

    @Test
    public void mergeResponse_mainPhoneNumberStartsWith07() {
        jurorResponse.setPhoneNumber(MOBILE_NUMBER);
        jurorResponse.setAltPhoneNumber(WORK_NUMBER);


        statusUpdateService.mergeResponse(jurorResponse, AUDITOR_USERNAME);

        assertThat(poolDetails.getPhoneNumber()).isNull();

        /*
        if the Main phone number starts with an 07 then it should be allocated to the mobile phone number
         */
        assertThat(poolDetails.getAltPhoneNumber()).isEqualTo(MOBILE_NUMBER);

        /*
        if the Another phone has not been allocated to the mobile number it should be allocated to the Work number
         */
        assertThat(poolDetails.getWorkPhone()).isEqualTo(WORK_NUMBER);
    }

    @Test
    public void mergeResponse_mainPhoneNumberDoesNotStartWith07_altPhoneNumberStartsWith07() {
        jurorResponse.setPhoneNumber(HOME_NUMBER);
        jurorResponse.setAltPhoneNumber(MOBILE_NUMBER);

        statusUpdateService.mergeResponse(jurorResponse, AUDITOR_USERNAME);

        /*
        if the Main phone number has not been allocated to the mobile number it should be allocated to the Home number
         */
        assertThat(poolDetails.getPhoneNumber()).isEqualTo(HOME_NUMBER);

        /*
        if the Main phone number does not start with an 07 but the Another one does then the Another phone will be
        allocated to the mobile phone number
         */
        assertThat(poolDetails.getAltPhoneNumber()).isEqualTo(MOBILE_NUMBER);

        assertThat(poolDetails.getWorkPhone()).isNull();
    }

    @Test
    public void mergeResponse_neitherPhoneNumberStartsWith07() {
        jurorResponse.setPhoneNumber(HOME_NUMBER);
        jurorResponse.setAltPhoneNumber(WORK_NUMBER);

        statusUpdateService.mergeResponse(jurorResponse, AUDITOR_USERNAME);

        /*
        if the Main phone number has not been allocated to the mobile number it should be allocated to the Home number
         */
        assertThat(poolDetails.getPhoneNumber()).isEqualTo(HOME_NUMBER);

        /*
        if the Another phone has not been allocated to the mobile number it should be allocated to the Work number
         */
        assertThat(poolDetails.getWorkPhone()).isEqualTo(WORK_NUMBER);

        assertThat(poolDetails.getAltPhoneNumber()).isNull();
    }

    @Test
    public void mergeResponse_SingleSpecialNeeds() {

        TSpecial tSpecial = new TSpecial("D", "DIET");

        //set single special need
        BureauJurorSpecialNeed specialNeed = new BureauJurorSpecialNeed();
        specialNeed.setJurorNumber("209092530");
        specialNeed.setSpecialNeed(tSpecial);
        specialNeed.setDetail("Some Details");

        List<BureauJurorSpecialNeed> jurorNeeds = new ArrayList<>();

        jurorNeeds.add(specialNeed);

        jurorResponse.setSpecialNeeds(jurorNeeds);

        doReturn(jurorNeeds).when(specialNeedsRepository).findByJurorNumber(JUROR_NUMBER);

        statusUpdateService.mergeResponse(jurorResponse, AUDITOR_USERNAME);

        //verify the details have been saved to pool
        assertThat(poolDetails.getSpecialNeed()).isEqualTo("D");


    }

    @Test
    public void mergeResponse_MultipleSpecialNeeds() {

        TSpecial tSpecialOne = new TSpecial("D", "DIET");
        TSpecial tSpecialTwo = new TSpecial("L", "LIMITED MOBILITY");


        BureauJurorSpecialNeed specialNeedOne = new BureauJurorSpecialNeed();
        specialNeedOne.setJurorNumber("209092530");
        specialNeedOne.setSpecialNeed(tSpecialOne);
        specialNeedOne.setDetail("Some Details");

        BureauJurorSpecialNeed specialNeedTwo = new BureauJurorSpecialNeed();
        specialNeedTwo.setJurorNumber("209092530");
        specialNeedTwo.setSpecialNeed(tSpecialTwo);
        specialNeedTwo.setDetail("Some Details");


        List<BureauJurorSpecialNeed> jurorNeeds = new ArrayList<>();

        jurorNeeds.add(specialNeedOne);
        jurorNeeds.add(specialNeedTwo);

        jurorResponse.setSpecialNeeds(jurorNeeds);

        doReturn(jurorNeeds).when(specialNeedsRepository).findByJurorNumber(JUROR_NUMBER);

        statusUpdateService.mergeResponse(jurorResponse, AUDITOR_USERNAME);

        //verify the details have been saved to pool
        assertThat(poolDetails.getSpecialNeed()).isEqualTo("M");


    }


}
