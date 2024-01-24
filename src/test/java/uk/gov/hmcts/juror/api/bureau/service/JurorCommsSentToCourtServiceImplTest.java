package uk.gov.hmcts.juror.api.bureau.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.juror.api.bureau.domain.NotifyTemplateField;
import uk.gov.hmcts.juror.api.bureau.notify.JurorCommsNotifyTemplateType;
import uk.gov.hmcts.juror.api.juror.domain.Pool;
import uk.gov.hmcts.juror.api.juror.domain.PoolRepository;

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
public class JurorCommsSentToCourtServiceImplTest {
    private static final String TEMPLATE_ID = "123456789";

    private static final String JUROR_TITLE = "JT";

    private NotifyTemplateField templateField1, templateField2, templateField3, templateField4, templateField5;
    private Pool pool1, pool2;
    List<NotifyTemplateField> templateFields = new LinkedList<>();
    List<Pool> poolList = new LinkedList<>();

    @Mock
    private PoolRepository poolRepository;

    @Mock
    private AppSettingService appSetting;

    @Mock
    private JurorCommsNotificationService jurorCommsNotificationService;

    @InjectMocks
    private JurorCommsSentToCourtServiceImpl service;

    @Before
    public void setUp() throws Exception {

        pool1 = Pool.builder()
            .jurorNumber("987654321")
            .firstName("Farah")
            .lastName("Lee")
            .email("a@b.com")
            .welsh(false)
            .notifications(0)
            .build();

        pool2 = Pool.builder()
            .jurorNumber("123456789")
            .title(JUROR_TITLE)
            .firstName("Simon")
            .lastName("Jones")
            .email("c@d.com")
            .welsh(false)
            .notifications(0)
            .build();

        poolList.add(pool1);
        poolList.add(pool2);


        templateField1 = NotifyTemplateField.builder()
            .id(1L)
            .templateId(TEMPLATE_ID)
            .templateField("FIRSTNAME")
            .databaseField("POOL.FNAME")
            .jdClassName("pool")
            .jdClassProperty("firstName")
            .build();

        templateField2 = NotifyTemplateField.builder()
            .id(2L)
            .templateId(TEMPLATE_ID)
            .templateField("LASTNAME")
            .databaseField("POOL.LNAME")
            .jdClassName("pool")
            .jdClassProperty("lastName")
            .build();
        templateField3 = NotifyTemplateField.builder()
            .id(3L)
            .templateId(TEMPLATE_ID)
            .templateField("SERVICESTARTDATE")
            .databaseField("POOL.NEXT_DATE")
            .jdClassName("pool")
            .jdClassProperty("hearingDate")
            .build();
        templateField4 = NotifyTemplateField.builder()
            .id(4L)
            .templateId(TEMPLATE_ID)
            .templateField("SERVICESTARTTIME")
            .databaseField("UNIQUE_POOL.ATTEND_TIME")
            .jdClassName("uniquePool")
            .jdClassProperty("attendTime")
            .build();
        templateField5 = NotifyTemplateField.builder()
            .id(5L)
            .templateId(TEMPLATE_ID)
            .templateField("email address")
            .databaseField("POOL.H_EMAIL")
            .jdClassName("pool")
            .jdClassProperty("email")
            .build();


        templateFields.add(templateField1);
        templateFields.add(templateField2);
        templateFields.add(templateField3);
        templateFields.add(templateField4);
        templateFields.add(templateField5);

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
    public void process_noPending_sentToCourt_pool() {
        given(poolRepository.findAll(any(BooleanExpression.class))).willReturn(new LinkedList<Pool>());

        service.process();
        verifyNoInteractions(jurorCommsNotificationService);
    }

}