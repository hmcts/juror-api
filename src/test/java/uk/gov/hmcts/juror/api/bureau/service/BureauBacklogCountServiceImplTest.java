package uk.gov.hmcts.juror.api.bureau.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponse;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponseQueries;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponseRepository;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.domain.User;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@RunWith(MockitoJUnitRunner.class)
public class BureauBacklogCountServiceImplTest {


    @Mock
    private JurorResponseRepository responseRepo;

    @InjectMocks
    private BureauBacklogCountServiceImpl backlogCountService;


    private List<JurorResponse> backlog;


    @Before
    public void setUp() {

        final LocalDateTime now = LocalDateTime.now();
        backlog = new LinkedList<>();
        for (int i = 0;
             i < 10;
             i++) {
            JurorResponse response = new JurorResponse();
            response.setJurorNumber(String.valueOf(i));
            response.setDateReceived(
                Date.from(now.minusHours(i).atZone(ZoneId.systemDefault()).toInstant()));
            response.setStaff(null);
            response.setUrgent(false);
            response.setSuperUrgent(false);
            backlog.add(response);
        }
        JurorResponse response1 = JurorResponse.builder()
            .urgent(true)
            .superUrgent(false)
            .processingStatus(ProcessingStatus.TODO)
            .staff(null)
            .build();
        backlog.add(response1);

        JurorResponse response2 = JurorResponse.builder()
            .urgent(true)
            .superUrgent(false)
            .processingStatus(ProcessingStatus.TODO)
            .staff(null)
            .build();
        backlog.add(response2);

        JurorResponse response3 = JurorResponse.builder()
            .urgent(false)
            .superUrgent(true)
            .processingStatus(ProcessingStatus.TODO)
            .staff(null)
            .build();

        backlog.add(response3);

        JurorResponse response4 = JurorResponse.builder()
            .urgent(false)
            .superUrgent(true)
            .processingStatus(ProcessingStatus.TODO)
            .staff(null)
            .build();
        backlog.add(response4);

        JurorResponse response5 = JurorResponse.builder()
            .urgent(false)
            .superUrgent(true)
            .processingStatus(ProcessingStatus.TODO)
            .staff(new User())
            .build();
        backlog.add(response5);

    }

    @Test
    public void backLogCount_unassignedStaff_Status_ToDo() {

        doReturn((long) backlog.size()).when(responseRepo).count(JurorResponseQueries.backlog());
        assertThat(backlogCountService.getBacklogNonUrgentCount()).isEqualTo(15);

        assertThat(backlog.parallelStream().filter(r -> r.getUrgent().equals(false)).count()).isEqualTo(13);

        assertThat(backlog.parallelStream().filter(JurorResponse::getSuperUrgent).count()).isEqualTo(3);
        assertThat(backlog.parallelStream().filter(JurorResponse::getUrgent).count()).isEqualTo(2);


    }


}
