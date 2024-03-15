package uk.gov.hmcts.juror.api.bureau.service;

import jakarta.persistence.EntityManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.bureau.domain.PartAmendmentRepository;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.JurorReasonableAdjustment;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.ReasonableAdjustments;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorReasonableAdjustmentRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorResponseAuditRepositoryMod;

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
    private JurorDigitalResponseRepositoryMod jurorResponseRepository;
    @Mock
    private JurorResponseAuditRepositoryMod auditRepository;
    @Mock
    private JurorPoolRepository poolDetailsRepository;
    @Mock
    private PartAmendmentRepository amendmentRepository;
    @Mock
    private JurorHistoryRepository historyRepository;
    @Mock
    private UrgencyService urgencyService;
    @Mock
    private EntityManager entityManager;
    @Mock
    private AssignOnUpdateService assignOnUpdateService;

    @Mock
    private JurorReasonableAdjustmentRepository specialNeedsRepository;

    @Mock
    private WelshCourtLocationRepository welshCourtLocationRepository;

    @InjectMocks
    private ResponseStatusUpdateServiceImpl statusUpdateService;

    private DigitalResponse jurorResponse;
    private JurorPool poolDetails;

    @Before
    public void setup() {
        jurorResponse = new DigitalResponse();
        jurorResponse.setJurorNumber(JUROR_NUMBER);
        jurorResponse.setProcessingComplete(false);

        Juror juror = new Juror();
        poolDetails = new JurorPool();
        poolDetails.setJuror(juror);

        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber(TestConstants.VALID_POOL_NUMBER);
        poolDetails.setPool(poolRequest);
        doReturn(poolDetails).when(poolDetailsRepository).findByJurorJurorNumber(JUROR_NUMBER);

    }

    @Test
    public void mergeResponse_mainPhoneNumberStartsWith07() {
        jurorResponse.setPhoneNumber(MOBILE_NUMBER);
        jurorResponse.setAltPhoneNumber(WORK_NUMBER);


        statusUpdateService.mergeResponse(jurorResponse, AUDITOR_USERNAME);
        assertThat(poolDetails.getJuror().getPhoneNumber()).isNull();

        /*
        if the Main phone number starts with an 07 then it should be allocated to the mobile phone number
         */
        assertThat(poolDetails.getJuror().getAltPhoneNumber()).isEqualTo(MOBILE_NUMBER);

        /*
        if the Another phone has not been allocated to the mobile number it should be allocated to the Work number
         */
        assertThat(poolDetails.getJuror().getWorkPhone()).isEqualTo(WORK_NUMBER);
    }

    @Test
    public void mergeResponse_mainPhoneNumberDoesNotStartWith07_altPhoneNumberStartsWith07() {
        jurorResponse.setPhoneNumber(HOME_NUMBER);
        jurorResponse.setAltPhoneNumber(MOBILE_NUMBER);

        statusUpdateService.mergeResponse(jurorResponse, AUDITOR_USERNAME);

        /*
        if the Main phone number has not been allocated to the mobile number it should be allocated to the Home number
         */
        assertThat(poolDetails.getJuror().getPhoneNumber()).isEqualTo(HOME_NUMBER);

        /*
        if the Main phone number does not start with an 07 but the Another one does then the Another phone will be
        allocated to the mobile phone number
         */
        assertThat(poolDetails.getJuror().getAltPhoneNumber()).isEqualTo(MOBILE_NUMBER);

        assertThat(poolDetails.getJuror().getWorkPhone()).isNull();
    }

    @Test
    public void mergeResponse_neitherPhoneNumberStartsWith07() {
        jurorResponse.setPhoneNumber(HOME_NUMBER);
        jurorResponse.setAltPhoneNumber(WORK_NUMBER);

        statusUpdateService.mergeResponse(jurorResponse, AUDITOR_USERNAME);

        /*
        if the Main phone number has not been allocated to the mobile number it should be allocated to the Home number
         */
        assertThat(poolDetails.getJuror().getPhoneNumber()).isEqualTo(HOME_NUMBER);

        /*
        if the Another phone has not been allocated to the mobile number it should be allocated to the Work number
         */
        assertThat(poolDetails.getJuror().getWorkPhone()).isEqualTo(WORK_NUMBER);

        assertThat(poolDetails.getJuror().getAltPhoneNumber()).isNull();
    }

    @Test
    public void mergeResponse_SingleSpecialNeeds() {

        ReasonableAdjustments special = new ReasonableAdjustments("D", "DIET");

        //set single special need
        JurorReasonableAdjustment specialNeed = new JurorReasonableAdjustment();
        specialNeed.setJurorNumber("209092530");
        specialNeed.setReasonableAdjustment(special);
        specialNeed.setReasonableAdjustmentDetail("Some Details");

        List<JurorReasonableAdjustment> jurorNeeds = new ArrayList<>();

        jurorNeeds.add(specialNeed);

        jurorResponse.setReasonableAdjustments(jurorNeeds);

        doReturn(jurorNeeds).when(specialNeedsRepository).findByJurorNumber(JUROR_NUMBER);

        statusUpdateService.mergeResponse(jurorResponse, AUDITOR_USERNAME);

        //verify the details have been saved to pool
        assertThat(poolDetails.getJuror().getReasonableAdjustmentCode()).isEqualTo("D");


    }

    @Test
    public void mergeResponse_MultipleSpecialNeeds() {

        ReasonableAdjustments specialOne = new ReasonableAdjustments("D", "DIET");

        JurorReasonableAdjustment specialNeedOne = new JurorReasonableAdjustment();
        specialNeedOne.setJurorNumber("209092530");
        specialNeedOne.setReasonableAdjustment(specialOne);
        specialNeedOne.setReasonableAdjustmentDetail("Some Details");

        ReasonableAdjustments specialTwo = new ReasonableAdjustments("L", "LIMITED MOBILITY");
        JurorReasonableAdjustment specialNeedTwo = new JurorReasonableAdjustment();
        specialNeedTwo.setJurorNumber("209092530");
        specialNeedTwo.setReasonableAdjustment(specialTwo);
        specialNeedTwo.setReasonableAdjustmentDetail("Some Details");


        List<JurorReasonableAdjustment> jurorNeeds = new ArrayList<>();

        jurorNeeds.add(specialNeedOne);
        jurorNeeds.add(specialNeedTwo);

        jurorResponse.setReasonableAdjustments(jurorNeeds);

        doReturn(jurorNeeds).when(specialNeedsRepository).findByJurorNumber(JUROR_NUMBER);

        statusUpdateService.mergeResponse(jurorResponse, AUDITOR_USERNAME);

        //verify the details have been saved to pool
        assertThat(poolDetails.getJuror().getReasonableAdjustmentCode()).isEqualTo("M");


    }


}
