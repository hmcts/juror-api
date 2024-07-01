package uk.gov.hmcts.juror.api.bureau.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.juror.api.bureau.notify.JurorCommsNotifyTemplateType;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.PoliceCheck;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@SuppressWarnings("Duplicates")
@RunWith(MockitoJUnitRunner.class)
public class JurorCommsWeeklyInfoServiceImplTest {

    private static final String JUROR_TITLE = "JT";

    List<JurorPool> poolList = new LinkedList<>();

    @Mock
    private JurorPoolRepository poolRepository;

    @Mock
    private JurorCommsNotificationService jurorCommsNotificationService;

    @InjectMocks
    private JurorCommsWeeklyInfoServiceImpl service;

    @Before
    public void setUp() throws Exception {

        JurorPool pool1 = new JurorPool();
        Juror juror1 = new Juror();
        pool1.setJuror(juror1);
        juror1.setJurorNumber("987654321");
        juror1.setFirstName("Farah");
        juror1.setLastName("Lee");
        juror1.setEmail("a@b.com");
        juror1.setWelsh(false);
        juror1.setNotifications(0);
        juror1.setPoliceCheck(PoliceCheck.ELIGIBLE);
        pool1.setNextDate(LocalDateTime.now().plusDays(20L).toLocalDate());

        JurorPool pool2 = new JurorPool();
        Juror juror2 = new Juror();
        pool2.setJuror(juror2);
        juror2.setTitle(JUROR_TITLE);
        juror2.setJurorNumber("123456789");
        juror2.setFirstName("Simon");
        juror2.setLastName("Jones");
        juror2.setEmail("c@d.com");
        juror2.setWelsh(false);
        juror2.setNotifications(0);
        juror2.setPoliceCheck(PoliceCheck.ELIGIBLE);
        pool2.setNextDate(LocalDateTime.now().plusDays(15L).toLocalDate());

        poolList.add(pool1);
        poolList.add(pool2);

    }

    @Test
    public void process_HappyPath() {
        given(poolRepository.findAll(any(BooleanExpression.class))).willReturn(poolList);
        service.process();
        verify(jurorCommsNotificationService, times(2)).sendJurorComms(any(JurorPool.class),
            any(JurorCommsNotifyTemplateType.class),
            eq(null), eq(null), anyBoolean());
        verify(poolRepository, times(2)).save(any(JurorPool.class));
    }

    @Test
    public void process_noPending_inforComms_pool() {
        given(poolRepository.findAll(any(BooleanExpression.class))).willReturn(new LinkedList<>());

        service.process();
        verifyNoInteractions(jurorCommsNotificationService);
    }

}
