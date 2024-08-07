package uk.gov.hmcts.juror.api.bureau.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponseQueries;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorResponseAuditRepositoryMod;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@RunWith(MockitoJUnitRunner.class)
public class BureauBacklogCountServiceImplTest {


    @Mock
    private JurorDigitalResponseRepositoryMod responseRepo;

    @Mock
    private JurorResponseAuditRepositoryMod jurorResponseAuditRepository;

    @InjectMocks
    private BureauBacklogCountServiceImpl backlogCountService;


    private List<DigitalResponse> backlog;



    @Before
    public void setUp() {

        final LocalDateTime now = LocalDateTime.now();
        backlog = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            DigitalResponse response = new DigitalResponse();
            response.setJurorNumber(String.valueOf(i));
            response.setDateReceived(now.minusHours(i));
            response.setStaff(null);
            response.setUrgent(false);
            backlog.add(response);
        }
        DigitalResponse  response1 = new DigitalResponse();
        response1.setUrgent(true);
        response1.setProcessingStatus(jurorResponseAuditRepository, ProcessingStatus.TODO);
        response1.setStaff(null);
        backlog.add(response1);

        DigitalResponse  response2 = new DigitalResponse();
        response2.setUrgent(true);
        response2.setProcessingStatus(jurorResponseAuditRepository, ProcessingStatus.TODO);
        response2.setStaff(null);
        backlog.add(response2);

        DigitalResponse  response3 = new DigitalResponse();
        response3.setUrgent(false);
        response3.setProcessingStatus(jurorResponseAuditRepository, ProcessingStatus.TODO);
        response3.setStaff(null);

        backlog.add(response3);

        DigitalResponse  response4 = new DigitalResponse();
        response4.setUrgent(false);
        response4.setProcessingStatus(jurorResponseAuditRepository, ProcessingStatus.TODO);
        response4.setStaff(null);
        backlog.add(response4);

        DigitalResponse  response5 = new DigitalResponse();
        response5.setUrgent(false);
        response5.setProcessingStatus(jurorResponseAuditRepository, ProcessingStatus.TODO);
        response5.setStaff(null);
        backlog.add(response5);

    }

    @Test
    public void backLogCount_unassignedStaff_Status_ToDo() {

        doReturn((long) backlog.size()).when(responseRepo).count(JurorResponseQueries.byUnassignedTodoNonUrgent());
        assertThat(backlogCountService.getBacklogNonUrgentCount()).isEqualTo(15);

        assertThat(backlog.parallelStream().filter(r -> !r.isUrgent()).count()).isEqualTo(13);

        assertThat(backlog.parallelStream().filter(DigitalResponse::isUrgent).count()).isEqualTo(2);


    }


}
