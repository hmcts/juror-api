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
import uk.gov.hmcts.juror.api.moj.domain.NotifyTemplateFieldMod;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.service.AppSettingService;

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

    private NotifyTemplateFieldMod templateField1;
    private NotifyTemplateFieldMod templateField2;
    private NotifyTemplateFieldMod templateField3;
    private NotifyTemplateFieldMod templateField4;
    private NotifyTemplateFieldMod templateField5;
    private JurorPool pool1;
    private JurorPool pool2;
    List<NotifyTemplateFieldMod> templateFields = new LinkedList<>();
    List<JurorPool> poolList = new LinkedList<>();

    private Juror juror1;
    private Juror juror2;

    @Mock
    private JurorPoolRepository poolRepository;

    @Mock
    private AppSettingService appSetting;

    @Mock
    private JurorCommsNotificationService jurorCommsNotificationService;

    @InjectMocks
    private JurorCommsSentToCourtServiceImpl service;

    @Before
    public void setUp() throws Exception {

        juror1 = new Juror();
        pool1 = new JurorPool();
        pool1.setJuror(juror1);
        juror1.setJurorNumber("987654321");
        juror1.setFirstName("Farah");
        juror1.setLastName("Lee");
        juror1.setEmail("a@b.com");
        juror1.setWelsh(false);
        juror1.setNotifications(0);


        juror2 = new Juror();
        pool2 = new JurorPool();
        pool2.setJuror(juror2);
        juror2.setJurorNumber("987654321");
        juror2.setFirstName("Farah");
        juror2.setLastName("Lee");
        juror2.setEmail("a@b.com");
        juror2.setWelsh(false);
        juror2.setNotifications(0);

        poolList.add(pool1);
        poolList.add(pool2);


        templateField1 = NotifyTemplateFieldMod.builder()
            .id(1L)
            .templateId(TEMPLATE_ID)
            .templateField("FIRSTNAME")
            .databaseField("POOL.FNAME")
            .jdClassName("pool")
            .jdClassProperty("firstName")
            .build();

        templateField2 = NotifyTemplateFieldMod.builder()
            .id(2L)
            .templateId(TEMPLATE_ID)
            .templateField("LASTNAME")
            .databaseField("POOL.LNAME")
            .jdClassName("pool")
            .jdClassProperty("lastName")
            .build();
        templateField3 = NotifyTemplateFieldMod.builder()
            .id(3L)
            .templateId(TEMPLATE_ID)
            .templateField("SERVICESTARTDATE")
            .databaseField("POOL.NEXT_DATE")
            .jdClassName("pool")
            .jdClassProperty("hearingDate")
            .build();
        templateField4 = NotifyTemplateFieldMod.builder()
            .id(4L)
            .templateId(TEMPLATE_ID)
            .templateField("SERVICESTARTTIME")
            .databaseField("UNIQUE_POOL.ATTEND_TIME")
            .jdClassName("uniquePool")
            .jdClassProperty("attendTime")
            .build();
        templateField5 = NotifyTemplateFieldMod.builder()
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
        verify(jurorCommsNotificationService, times(2)).sendJurorComms(any(JurorPool.class),
            any(JurorCommsNotifyTemplateType.class),
            eq(null), eq(null), anyBoolean());
        verify(poolRepository, times(2)).save(any(JurorPool.class));
    }

    @Test
    public void process_noPending_sentToCourt_pool() {
        given(poolRepository.findAll(any(BooleanExpression.class))).willReturn(new LinkedList<JurorPool>());

        service.process();
        verifyNoInteractions(jurorCommsNotificationService);
    }

}
