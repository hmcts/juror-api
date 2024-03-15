package uk.gov.hmcts.juror.api.bureau.service;

import jakarta.persistence.EntityManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.juror.api.bureau.domain.PartAmendmentRepository;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorReasonableAdjustmentRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorResponseAuditRepositoryMod;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Tests for third-party rules in {@link ResponseStatusUpdateServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ResponseStatusUpdateThirdPartyRulesTest {

    private static final String AUDITOR_USERNAME = "unitTestUser";
    private static final String JUROR_EMAIL_ADDRESS = "juror@dummy-address.cgi.com";
    private static final String JUROR_PRIMARY_PHONE = "01000000000";
    private static final String JUROR_MOBILE_PHONE = "07000000000";
    private static final String JUROR_NUMBER = "34567";
    private static final String THIRD_PARTY_EMAIL_ADDRESS = "thirdparty@dummy-address.cgi.com";
    private static final String THIRD_PARTY_PRIMARY_PHONE = "01000000003";
    private static final String THIRD_PARTY_MOBILE_PHONE = "07000000003";
    private static final String THIRD_PARTY_REASON = "Needed for test";

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

    // Needs to be a mock so we can verify the setters weren't called on the instance saved to the DB
    private DigitalResponse jurorResponse;

    private JurorPool poolDetails;
    private Juror juror;
    private PoolRequest poolRequest;

    @Before
    public void setUp() {
        jurorResponse = new DigitalResponse();
        jurorResponse.setJurorNumber(JUROR_NUMBER);
        jurorResponse.setProcessingComplete(false);
        jurorResponse.setThirdPartyReason(THIRD_PARTY_REASON);

        // Juror contact details
        jurorResponse.setEmail(JUROR_EMAIL_ADDRESS);
        jurorResponse.setPhoneNumber(JUROR_PRIMARY_PHONE);
        jurorResponse.setAltPhoneNumber(JUROR_MOBILE_PHONE);

        // Third party contact details
        jurorResponse.setEmailAddress(THIRD_PARTY_EMAIL_ADDRESS);
        jurorResponse.setMainPhone(THIRD_PARTY_PRIMARY_PHONE);
        jurorResponse.setOtherPhone(THIRD_PARTY_MOBILE_PHONE);

        poolDetails = new JurorPool();
        juror = new Juror();
        poolDetails.setJuror(juror);
        poolRequest = new PoolRequest();
        poolDetails.setPool(poolRequest);
        poolDetails.getJuror().setJurorNumber(JUROR_NUMBER);
        doReturn(poolDetails).when(poolDetailsRepository).findByJurorJurorNumber(JUROR_NUMBER);
    }

    @Test
    public void jurorEmailDetailsFalse_thirdPartyEmailAddressNotSavedToPool() {
        jurorResponse.setJurorEmailDetails(false);

        statusUpdateService.mergeResponse(jurorResponse, AUDITOR_USERNAME);

        verify(poolDetailsRepository, times(2)).save(poolDetails);
        verify(jurorResponseRepository, times(1)).save(jurorResponse);

        assertThat(poolDetails.getJuror().getEmail()).isNotEqualTo(THIRD_PARTY_EMAIL_ADDRESS);

        assertThat(jurorResponse.getProcessingComplete()).isTrue();
    }

    @Test
    public void jurorEmailDetailsTrue_jurorEmailAddressSavedToPool() {
        jurorResponse.setJurorEmailDetails(true);
        jurorResponse.setThirdPartyReason(null);

        statusUpdateService.mergeResponse(jurorResponse, AUDITOR_USERNAME);

        verify(poolDetailsRepository, times(2)).save(poolDetails);
        verify(jurorResponseRepository, times(1)).save(jurorResponse);

        assertThat(poolDetails.getJuror().getEmail()).isEqualTo(JUROR_EMAIL_ADDRESS);

        assertThat(jurorResponse.getProcessingComplete()).isTrue();
    }

    @Test
    public void jurorPhoneDetailsFalse_thirdPartyPhoneNumbersNotSavedToPool() {
        jurorResponse.setJurorPhoneDetails(false);

        statusUpdateService.mergeResponse(jurorResponse, AUDITOR_USERNAME);

        verify(poolDetailsRepository, times(2)).save(poolDetails);
        verify(jurorResponseRepository, times(1)).save(jurorResponse);

        assertThat(poolDetails.getJuror().getPhoneNumber()).isNotEqualTo(THIRD_PARTY_PRIMARY_PHONE);
        assertThat(poolDetails.getJuror().getAltPhoneNumber()).isNotEqualTo(THIRD_PARTY_MOBILE_PHONE);

        // The response saved to the DB shouldn't be changed, only the pool details should
        assertThat(jurorResponse.getPhoneNumber()).isEqualTo(JUROR_PRIMARY_PHONE);
        assertThat(jurorResponse.getAltPhoneNumber()).isEqualTo(JUROR_MOBILE_PHONE);

        assertThat(jurorResponse.getProcessingComplete()).isTrue();
    }

    @Test
    public void jurorPhoneDetailsTrue_jurorPhoneNumbersSavedToPool() {
        jurorResponse.setJurorPhoneDetails(true);
        jurorResponse.setThirdPartyReason(null);

        statusUpdateService.mergeResponse(jurorResponse, AUDITOR_USERNAME);

        verify(poolDetailsRepository, times(2)).save(poolDetails);
        verify(jurorResponseRepository, times(1)).save(jurorResponse);

        assertThat(poolDetails.getJuror().getPhoneNumber()).isEqualTo(JUROR_PRIMARY_PHONE);
        assertThat(poolDetails.getJuror().getAltPhoneNumber()).isEqualTo(JUROR_MOBILE_PHONE);

        // This is the ONLY field that should change on the juror response object
        assertThat(jurorResponse.getProcessingComplete()).isTrue();
    }
}
