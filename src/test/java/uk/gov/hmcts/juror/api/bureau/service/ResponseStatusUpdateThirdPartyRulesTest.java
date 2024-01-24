package uk.gov.hmcts.juror.api.bureau.service;

import jakarta.persistence.EntityManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.juror.api.bureau.domain.BureauJurorSpecialNeedsRepository;
import uk.gov.hmcts.juror.api.bureau.domain.JurorResponseAuditRepository;
import uk.gov.hmcts.juror.api.bureau.domain.PartAmendmentRepository;
import uk.gov.hmcts.juror.api.bureau.domain.PartHistRepository;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponse;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponseRepository;
import uk.gov.hmcts.juror.api.juror.domain.Pool;
import uk.gov.hmcts.juror.api.juror.domain.PoolRepository;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocationRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Tests for third-party rules in {@link ResponseStatusUpdateServiceImpl}
 */
@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings({"unused", "ResultOfMethodCallIgnored"})
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

    // Needs to be a mock so we can verify the setters weren't called on the instance saved to the DB
    private JurorResponse jurorResponse;

    private Pool poolDetails;

    @Before
    public void setUp() {
        jurorResponse = new JurorResponse();
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

        poolDetails = new Pool();
        poolDetails.setJurorNumber(JUROR_NUMBER);
        doReturn(poolDetails).when(poolDetailsRepository).findByJurorNumber(JUROR_NUMBER);
    }

    @Test
    public void jurorEmailDetailsFalse_thirdPartyEmailAddressNotSavedToPool() {
        jurorResponse.setJurorEmailDetails(false);

        statusUpdateService.mergeResponse(jurorResponse, AUDITOR_USERNAME);

        verify(poolDetailsRepository, times(2)).save(poolDetails);
        verify(jurorResponseRepository, times(1)).save(jurorResponse);

        assertThat(poolDetails.getEmail()).isNotEqualTo(THIRD_PARTY_EMAIL_ADDRESS);

        assertThat(jurorResponse.getProcessingComplete()).isTrue();
    }

    @Test
    public void jurorEmailDetailsTrue_jurorEmailAddressSavedToPool() {
        jurorResponse.setJurorEmailDetails(true);
        jurorResponse.setThirdPartyReason(null);

        statusUpdateService.mergeResponse(jurorResponse, AUDITOR_USERNAME);

        verify(poolDetailsRepository, times(2)).save(poolDetails);
        verify(jurorResponseRepository, times(1)).save(jurorResponse);

        assertThat(poolDetails.getEmail()).isEqualTo(JUROR_EMAIL_ADDRESS);

        assertThat(jurorResponse.getProcessingComplete()).isTrue();
    }

    @Test
    public void jurorPhoneDetailsFalse_thirdPartyPhoneNumbersNotSavedToPool() {
        jurorResponse.setJurorPhoneDetails(false);

        statusUpdateService.mergeResponse(jurorResponse, AUDITOR_USERNAME);

        verify(poolDetailsRepository, times(2)).save(poolDetails);
        verify(jurorResponseRepository, times(1)).save(jurorResponse);

        assertThat(poolDetails.getPhoneNumber()).isNotEqualTo(THIRD_PARTY_PRIMARY_PHONE);
        assertThat(poolDetails.getAltPhoneNumber()).isNotEqualTo(THIRD_PARTY_MOBILE_PHONE);

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

        assertThat(poolDetails.getPhoneNumber()).isEqualTo(JUROR_PRIMARY_PHONE);
        assertThat(poolDetails.getAltPhoneNumber()).isEqualTo(JUROR_MOBILE_PHONE);

        // This is the ONLY field that should change on the juror response object
        assertThat(jurorResponse.getProcessingComplete()).isTrue();
    }
}
