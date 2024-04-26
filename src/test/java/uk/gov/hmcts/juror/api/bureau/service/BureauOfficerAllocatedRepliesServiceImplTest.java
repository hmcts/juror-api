package uk.gov.hmcts.juror.api.bureau.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.juror.api.bureau.domain.UserQueries;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponseQueries;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.repository.UserRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BureauOfficerAllocatedRepliesServiceImplTest {

    @Mock
    private JurorDigitalResponseRepositoryMod responseRepo;

    @Mock
    private UserRepository userRepo;

    @Mock
    private BureauBacklogCountService bureauBacklogCountService;

    @InjectMocks
    private BureauOfficerAllocatedRepliesServiceImpl allocatedRepliesService;

    private User user1;
    private User user2;

    @Before
    public void setUp() {
        user1 = User.builder().username("staff1").name("Staff 1").active(true).build();
        user2 = User.builder().username("staff2").name("Staff 2").active(true).build();
    }

    @Test
    public void backLogCount_no_staff_status_toDo() {

        when(bureauBacklogCountService.getBacklogNonUrgentCount()).thenReturn(7L);
        when(bureauBacklogCountService.getBacklogUrgentCount()).thenReturn(8L);
        when(bureauBacklogCountService.getBacklogAllRepliesCount()).thenReturn(20L);

        assertThat(allocatedRepliesService.getBackLogData().getBureauBacklogCount().getNonUrgent()).isEqualTo(7);
        assertThat(allocatedRepliesService.getBackLogData().getBureauBacklogCount().getUrgent()).isEqualTo(8);
        assertThat(allocatedRepliesService.getBackLogData().getBureauBacklogCount().getAllReplies()).isEqualTo(20);
    }

    @Test
    public void assigned_assigned_staff_status_todo() {

        doReturn(Arrays.asList(user1, user2)).when(userRepo).findAll(UserQueries.activeBureauOfficers());

        doReturn(10L).when(responseRepo).count(JurorResponseQueries.byAssignedNonUrgent(user1));
        doReturn(15L).when(responseRepo).count(JurorResponseQueries.byAssignedUrgent(user1));
        doReturn(25L).when(responseRepo).count(JurorResponseQueries.byAssignedAll(user1));

        doReturn(12L).when(responseRepo).count(JurorResponseQueries.byAssignedNonUrgent(user2));
        doReturn(20L).when(responseRepo).count(JurorResponseQueries.byAssignedAll(user2));


        assertThat(allocatedRepliesService.getBackLogData().getData().get(0).getName()).isEqualTo("Staff 1");
        assertThat(allocatedRepliesService.getBackLogData().getData().get(0).getNonUrgent()).isEqualTo(10);
        assertThat(allocatedRepliesService.getBackLogData().getData().get(0).getUrgent()).isEqualTo(15);
        assertThat(allocatedRepliesService.getBackLogData().getData().get(0).getAllReplies()).isEqualTo(25);


        assertThat(allocatedRepliesService.getBackLogData().getData().get(1).getName()).isEqualTo("Staff 2");
        assertThat(allocatedRepliesService.getBackLogData().getData().get(1).getNonUrgent()).isEqualTo(12);
        assertThat(allocatedRepliesService.getBackLogData().getData().get(1).getAllReplies()).isEqualTo(20);
    }


}
