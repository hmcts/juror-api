package uk.gov.hmcts.juror.api.bureau.scheduler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.juror.api.bureau.domain.BureauJurorDetail;
import uk.gov.hmcts.juror.api.bureau.service.UrgencyService;
import uk.gov.hmcts.juror.api.bureau.service.UserService;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponse;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponseQueries;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponseRepository;
import uk.gov.hmcts.juror.api.juror.domain.Pool;
import uk.gov.hmcts.juror.api.juror.domain.PoolRepository;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class UrgentSuperUrgentStatusSchedulerTest {

    private static final String NON_CLOSED_STATUS = ProcessingStatus.TODO.name();
    private BureauJurorDetail jurorBureauDetail;
    private JurorResponse jurorResponse;
    private Pool poolDetails;

    private LocalDateTime RESPONSE_RECEIVED;

    @Mock
    private JurorResponseRepository jurorResponseRepo;

    @Mock
    private UserService userService;

    @Mock
    private PoolRepository poolrepo;

    @Mock
    private UrgencyService urgencyService;

    @InjectMocks
    UrgentSuperUrgentStatusScheduler urgentSuperUrgentStatusScheduler;

    private List<JurorResponse> responseBacklog;


    @Before
    public void setUp() {

        RESPONSE_RECEIVED = LocalDateTime.now();

        //set up some known static dates relative to a start point
        LocalDateTime HEARING_DATE_VALID = LocalDateTime.now().plus(35, ChronoUnit.DAYS);

        jurorBureauDetail = new BureauJurorDetail();
        jurorBureauDetail.setProcessingStatus(NON_CLOSED_STATUS);
        jurorBureauDetail.setDateReceived(Date.from(RESPONSE_RECEIVED.toInstant(ZoneOffset.UTC)));
        jurorBureauDetail.setHearingDate(Date.from(HEARING_DATE_VALID.toInstant(ZoneOffset.UTC)));

        jurorResponse = new JurorResponse();
        jurorResponse.setProcessingStatus(uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus.TODO);
        jurorResponse.setDateReceived(Date.from(RESPONSE_RECEIVED.toInstant(ZoneOffset.UTC)));

        poolDetails = new Pool();
        poolDetails.setHearingDate(Date.from(HEARING_DATE_VALID.toInstant(ZoneOffset.UTC)));

        responseBacklog = new LinkedList<>();

        final LocalDateTime now = LocalDateTime.now();
        JurorResponse response = new JurorResponse();
        response.setJurorNumber("12345678");
        response.setDateReceived(Date.from(now.minusHours(1).atZone(ZoneId.systemDefault()).toInstant()));
        response.setProcessingStatus(ProcessingStatus.TODO);
        responseBacklog.add(response);

    }

    @Test
    public void nonUrgentResponseTurnsSuperUrgent() throws Exception {

        poolDetails.setReadOnly(Boolean.TRUE);
        poolDetails.setJurorNumber("12345678");

        final List<ProcessingStatus> pendingStatuses = List.of(ProcessingStatus.CLOSED);

        given(jurorResponseRepo.findAll(JurorResponseQueries.byStatusNotClosed(pendingStatuses))).willReturn(
            responseBacklog);

        JurorResponse jurorResponse = responseBacklog.get(0);
        given(poolrepo.findByJurorNumber(jurorResponse.getJurorNumber())).willReturn(poolDetails);
        //given(poolrepo.findOne(jurorResponse.getJurorNumber())).willReturn(poolDetails);

        given(urgencyService.isSuperUrgent(jurorResponse, poolDetails)).willReturn(Boolean.TRUE);
        given(urgencyService.isUrgent(jurorResponse, poolDetails)).willReturn(Boolean.FALSE);

        urgentSuperUrgentStatusScheduler.process();

        verify(jurorResponseRepo, times(1)).save(jurorResponse);
        verify(urgencyService, times(1)).setUrgencyFlags(responseBacklog.get(0), poolDetails);

        userService.assignUrgentResponse(jurorResponse);

        verify(jurorResponseRepo, times(1)).save(jurorResponse);


    }


}
