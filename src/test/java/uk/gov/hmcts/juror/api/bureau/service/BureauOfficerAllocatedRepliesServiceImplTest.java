package uk.gov.hmcts.juror.api.bureau.service;

import com.querydsl.core.Tuple;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.juror.api.bureau.domain.UserQueries;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.repository.UserRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;

import java.util.Arrays;
import java.util.List;

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

    @Mock
    Tuple backlogStats = Mockito.mock(Tuple.class);

    @InjectMocks
    private BureauOfficerAllocatedRepliesServiceImpl allocatedRepliesService;

    @Before
    public void setUp() {
        User user1 = User.builder().username("staff1").name("Staff 1").active(true).build();
        User user2 = User.builder().username("staff2").name("Staff 2").active(true).build();

        when(responseRepo.getAssignRepliesStatistics()).thenReturn(backlogStats);
        when(backlogStats.get(0, Integer.class)).thenReturn(7);
        when(backlogStats.get(1, Integer.class)).thenReturn(8);
        when(backlogStats.get(2, Integer.class)).thenReturn(20);

        doReturn(Arrays.asList(user1, user2)).when(userRepo).findAll(UserQueries.activeBureauOfficers());
        Tuple user1Data = setupUserData(user1, 10, 15, 25);
        Tuple user2Data = setupUserData(user2, 12, 0, 20);

        doReturn(List.of(user1Data, user2Data)).when(responseRepo).getAssignRepliesStatisticForUsers();
    }

    @Test
    public void backLogCount_no_staff_status_toDo() {
        assertThat(allocatedRepliesService.getBackLogData().getBureauBacklogCount().getNonUrgent()).isEqualTo(7);
        assertThat(allocatedRepliesService.getBackLogData().getBureauBacklogCount().getUrgent()).isEqualTo(8);
        assertThat(allocatedRepliesService.getBackLogData().getBureauBacklogCount().getAllReplies()).isEqualTo(20);
    }

    @Test
    public void assigned_assigned_staff_status_todo() {
        assertThat(allocatedRepliesService.getBackLogData().getData().get(0).getName()).isEqualTo("Staff 1");
        assertThat(allocatedRepliesService.getBackLogData().getData().get(0).getNonUrgent()).isEqualTo(10);
        assertThat(allocatedRepliesService.getBackLogData().getData().get(0).getUrgent()).isEqualTo(15);
        assertThat(allocatedRepliesService.getBackLogData().getData().get(0).getAllReplies()).isEqualTo(25);


        assertThat(allocatedRepliesService.getBackLogData().getData().get(1).getName()).isEqualTo("Staff 2");
        assertThat(allocatedRepliesService.getBackLogData().getData().get(1).getNonUrgent()).isEqualTo(12);
        assertThat(allocatedRepliesService.getBackLogData().getData().get(1).getAllReplies()).isEqualTo(20);
    }

    private Tuple setupUserData(User user, int nonUrgent, int urgent, int allReplies) {
        Tuple data = Mockito.mock(Tuple.class);
        when(data.get(0, String.class)).thenReturn(user.getUsername());
        when(data.get(1, String.class)).thenReturn(user.getName());
        when(data.get(2, Integer.class)).thenReturn(nonUrgent);
        when(data.get(3, Integer.class)).thenReturn(urgent);
        when(data.get(4, Integer.class)).thenReturn(allReplies);
        return data;
    }
}
