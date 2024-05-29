package uk.gov.hmcts.juror.api.bureau.scheduler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.juror.api.bureau.service.UrgencyService;
import uk.gov.hmcts.juror.api.bureau.service.UserService;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponseQueries;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.ModJurorDetail;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorPaperResponseRepositoryMod;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class UrgentSuperUrgentStatusSchedulerTest {

    private static final String NON_CLOSED_STATUS = ProcessingStatus.TODO.name();

    private JurorPool poolDetails;

    private Juror juror;


    @Mock
    private JurorDigitalResponseRepositoryMod jurorResponseRepo;
    @Mock
    private JurorPaperResponseRepositoryMod paperJurorResponseRepo;

    @Mock
    private UserService userService;

    @Mock
    private JurorPoolRepository poolrepo;

    @Mock
    private UrgencyService urgencyService;

    @InjectMocks
    UrgentSuperUrgentStatusScheduler urgentSuperUrgentStatusScheduler;

    private List<DigitalResponse> responseBacklog;


    @Before
    public void setUp() {
        LocalDateTime responseReceived = LocalDateTime.now();

        //set up some known static dates relative to a start point
        ModJurorDetail jurorBureauDetail = new ModJurorDetail();
        jurorBureauDetail.setProcessingStatus(NON_CLOSED_STATUS);
        jurorBureauDetail.setDateReceived(responseReceived.toLocalDate());

        jurorBureauDetail.setHearingDate(responseReceived.toLocalDate());

        DigitalResponse jurorResponse = new DigitalResponse();
        jurorResponse.setProcessingStatus(uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus.TODO);
        jurorResponse.setDateReceived(responseReceived);

        poolDetails = new JurorPool();
        juror = new Juror();
        poolDetails.setNextDate(responseReceived.toLocalDate());
        responseBacklog = new LinkedList<>();

        final LocalDateTime now = LocalDateTime.now();
        DigitalResponse response = new DigitalResponse();
        response.setJurorNumber("12345678");
        response.setDateReceived(LocalDateTime.from(now.minusHours(1)));
        response.setProcessingStatus(ProcessingStatus.TODO);
        response.setUrgent(false);
        response.setSuperUrgent(false);
        responseBacklog.add(response);

    }

    @Test
    public void nonUrgentResponseTurnsSuperUrgent() throws Exception {

        //poolDetails.setReadOnly(Boolean.TRUE);

        poolDetails.setJuror(juror);
        juror = poolDetails.getJuror();
        juror.setJurorNumber("12345678");

        final List<ProcessingStatus> pendingStatuses = List.of(ProcessingStatus.CLOSED);

        given(jurorResponseRepo.findAll(JurorResponseQueries.byStatusNotClosed(pendingStatuses)
            .and(JurorResponseQueries.jurorIsNotTransferred())
            .and(JurorResponseQueries.isDigital()))).willReturn(
            responseBacklog);

        DigitalResponse jurorResponse = responseBacklog.get(0);
        given(poolrepo.findByJurorJurorNumberAndIsActiveAndOwner(
            jurorResponse.getJurorNumber(),
            true,
            "400"
        )).willReturn(poolDetails);
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
