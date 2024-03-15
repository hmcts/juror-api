package uk.gov.hmcts.juror.api.bureau.service;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.juror.api.bureau.controller.ResponseUpdateController;
import uk.gov.hmcts.juror.api.bureau.domain.ChangeLog;
import uk.gov.hmcts.juror.api.bureau.domain.ChangeLogRepository;
import uk.gov.hmcts.juror.api.bureau.domain.PhoneLogRepository;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.JurorResponseCjsEmployment;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.UserRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorReasonableAdjustmentRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorResponseCjsEmploymentRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.ReasonableAdjustmentsRepository;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class ResponseUpdateServiceImplTest {

    @Mock
    private JurorPoolRepository poolRepository;

    @Mock
    private JurorHistoryRepository partHistRepository;

    @Mock
    private PhoneLogRepository phoneLogRepository;

    @Mock
    private JurorDigitalResponseRepositoryMod responseRepository;

    @Mock
    private ChangeLogRepository changeLogRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private JurorReasonableAdjustmentRepository bureauJurorSpecialNeedsRepository;

    @Mock
    private ReasonableAdjustmentsRepository reasonableAdjustmentsRepository;

    @Mock
    private JurorResponseCjsEmploymentRepositoryMod cjsRepository;

    @Mock
    private AssignOnUpdateService assignOnUpdateService;

    @InjectMocks
    private ResponseUpdateServiceImpl responseUpdateService;

    @Test
    public void updateJurorEligibility_happyPath() {

        String jurorId = "0123456789";
        String login = "testlogin";
        Integer version = 0;

        // configure mocks
        DigitalResponse domain = mock(DigitalResponse.class);
        given(domain.getProcessingComplete()).willReturn(false);

        User staff = mock(User.class);
        given(staff.getUsername()).willReturn(login);
        ResponseUpdateController.JurorEligibilityDto dto = mock(ResponseUpdateController.JurorEligibilityDto.class);
        given(dto.getVersion()).willReturn(version);

        given(responseRepository.findByJurorNumber(jurorId)).willReturn(domain);
        given(userRepository.findByUsername(login)).willReturn(staff);

        // execute method
        responseUpdateService.updateJurorEligibility(dto, jurorId, login);

        // make assertions/verify execution
        verify(responseRepository).findByJurorNumber(jurorId);
        verify(userRepository).findByUsername(login);
        verify(domain, times(2)).getProcessingComplete();
        // capture the changeLog delivered to the changeLogRepository
        final ArgumentCaptor<ChangeLog> changeLogCaptor = ArgumentCaptor.forClass(ChangeLog.class);
        verify(changeLogRepository).save(changeLogCaptor.capture());
        final ChangeLog capturedChangeLog = changeLogCaptor.getValue();
        assertThat(capturedChangeLog.getJurorNumber()).isEqualToIgnoringCase(jurorId);
        assertThat(capturedChangeLog.getStaff().getUsername()).isEqualToIgnoringCase(login);
        verify(responseRepository).save(domain);
    }

    @Test
    public void updateJurorCjs_happyPath() {

        String jurorId = "0123456789";
        String login = "testlogin";
        Integer version = 0;

        // configure mocks
        DigitalResponse domain = mock(DigitalResponse.class);
        given(domain.getProcessingComplete()).willReturn(false);

        JurorResponseCjsEmployment policeEntry = mock(JurorResponseCjsEmployment.class);
        given(policeEntry.getCjsEmployerDetails()).willReturn("Original police details");

        given(cjsRepository.findByJurorNumberAndCjsEmployer(jurorId, "Police Force")).willReturn(policeEntry);

        // return our BureauJurorCJS on save just to avoid NullPointerExceptions
        given(cjsRepository.save(any(JurorResponseCjsEmployment.class))).willReturn(policeEntry);

        User staff = mock(User.class);

        ResponseUpdateController.CJSEmploymentDetailsDto dto =
            mock(ResponseUpdateController.CJSEmploymentDetailsDto.class);
        given(dto.getVersion()).willReturn(version);
        given(dto.getPoliceForceDetails()).willReturn(null); // we want the entry to be deleted
        given(dto.getPrisonServiceDetails()).willReturn("Prison details");
        given(dto.getJudiciaryEmployment()).willReturn(true);
        given(dto.getHmctsEmployment()).willReturn(true);
        given(dto.getNcaEmployment()).willReturn(true);
        given(dto.getOtherDetails()).willReturn("Other details");

        given(responseRepository.findByJurorNumber(jurorId)).willReturn(domain);
        given(userRepository.findByUsername(login)).willReturn(staff);

        // execute method
        responseUpdateService.updateCjs(dto, jurorId, login);

        // make assertions/verify execution
        verify(responseRepository).findByJurorNumber(jurorId);
        verify(userRepository).findByUsername(login);

        // verify optimistic locking logic
        verify(entityManager, times(1)).lock(domain, LockModeType.OPTIMISTIC_FORCE_INCREMENT);

        // verify updateAndLogCjs logic
        verify(cjsRepository, times(6)).findByJurorNumberAndCjsEmployer(anyString(), anyString());

        verify(cjsRepository, times(5)).save(any(JurorResponseCjsEmployment.class));
        verify(cjsRepository, times(1)).delete(any(JurorResponseCjsEmployment.class));

        // verify save logic
        verify(changeLogRepository, times(1)).save(any(ChangeLog.class));
        verify(responseRepository).save(domain);
    }

    @Test
    public void comparisonHash() {
        final String testNote = "I am a test note.";
        final String comparisonHash = ResponseUpdateServiceImpl.comparisonHash(testNote);

        final HashCode manualHash = Hashing.md5().hashString(ResponseUpdateServiceImpl.HASH_SALT.concat(testNote),
            StandardCharsets.UTF_8);

        assertThat(comparisonHash)
            .hasSize(32)
            .isEqualTo(manualHash.toString());
    }

    @Test
    public void comparisonHash_nullNotesString() {
        final String testNote = null;
        final String comparisonHash = ResponseUpdateServiceImpl.comparisonHash(testNote);

        final HashCode manualHash = Hashing.md5().hashString(ResponseUpdateServiceImpl.HASH_SALT + testNote,
            StandardCharsets.UTF_8);

        assertThat(comparisonHash)
            .hasSize(32)
            .isEqualTo(manualHash.toString());
    }
}
