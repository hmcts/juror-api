package uk.gov.hmcts.juror.api.bureau.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.juror.api.bureau.notify.JurorCommsNotifyTemplateType;
import uk.gov.hmcts.juror.api.juror.domain.Pool;
import uk.gov.hmcts.juror.api.juror.domain.PoolRepository;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
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

    private Pool pool1;
    private Pool pool2;
    List<Pool> poolList = new LinkedList<>();

    @Mock
    private PoolRepository poolRepository;

    @Mock
    private JurorCommsNotificationService jurorCommsNotificationService;

    @InjectMocks
    private JurorCommsWeeklyInfoServiceImpl service;

    @Before
    public void setUp() throws Exception {

        pool1 = Pool.builder()
            .jurorNumber("987654321")
            .firstName("Farah")
            .lastName("Lee")
            .email("a@b.com")
            .welsh(false)
            .notifications(0)
            .readOnly(false)
            .policeCheck("P")
            .hearingDate(Date.from(Instant.now().plus(20, ChronoUnit.DAYS).atZone(ZoneId.systemDefault()).toInstant()))
            .build();

        pool2 = Pool.builder()
            .jurorNumber("123456789")
            .title(JUROR_TITLE)
            .firstName("Simon")
            .lastName("Jones")
            .email("c@d.com")
            .welsh(false)
            .readOnly(false)
            .policeCheck("P")
            .hearingDate(Date.from(Instant.now().plus(15, ChronoUnit.DAYS).atZone(ZoneId.systemDefault()).toInstant()))
            .notifications(0)
            .build();

        poolList.add(pool1);
        poolList.add(pool2);

    }

    @Test
    public void process_HappyPath() {
        given(poolRepository.findAll(any(BooleanExpression.class))).willReturn(poolList);
        service.process();
        verify(jurorCommsNotificationService, times(2)).sendJurorComms(any(Pool.class),
            any(JurorCommsNotifyTemplateType.class),
            eq(null), eq(null), anyBoolean());
        verify(poolRepository, times(2)).save(any(Pool.class));
    }

    @Test
    public void process_noPending_inforComms_pool() {
        given(poolRepository.findAll(any(BooleanExpression.class))).willReturn(new LinkedList<Pool>());

        service.process();
        verifyNoInteractions(jurorCommsNotificationService);
    }

}