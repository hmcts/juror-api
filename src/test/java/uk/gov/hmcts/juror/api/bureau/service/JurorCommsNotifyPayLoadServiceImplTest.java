package uk.gov.hmcts.juror.api.bureau.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.juror.api.config.WelshDayMonthTranslationConfig;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.NotifyTemplateFieldMod;
import uk.gov.hmcts.juror.api.moj.domain.NotifyTemplateMappingMod;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.NotifyTemplateFieldRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.service.PoolRequestService;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@SuppressWarnings("Duplicates")
@RunWith(MockitoJUnitRunner.class)
public class JurorCommsNotifyPayLoadServiceImplTest {
    private static final String TIME = "09:15";
    private static final String FIRST_NAME = "Farah";
    private static final String LAST_NAME_1 = "Lee";
    private static final String LAST_NAME = "LASTNAME";
    private static final String EMAIL = "email";
    private static final String JUROR = "juror";
    private static final String EMAIL_1 = "a@aaa.com";
    private static final String EMAIL_2 = "b@b.com";
    private static final String COURT_NAME_1 = "COURTNAME";
    private static final String COURT_NAME = "Awesome Court";
    private static final String JUROR_NUM = "JUROR NUMBER";
    private static final String JUROR_NUMBER = "111222333";
    private static final String TEMPLATE_ID = "123456789";
    private static final String DATE = "Monday 08 July, 2019";
    private static final String EMAIL_ADDRESS = "email address";
    private static final String SERVICE_START_TIME = "SERVICESTARTTIME";
    private static final String SERVICE_START_DATE = "SERVICESTARTDATE";
    private static final String BULK_PRINT_DATA_DETAIL_REC = "bulk_print_data.detail_rec";
    private static final String Description_1 = "ServiceStartDate is present in the payLoad Map";
    private static final String Description_2 = "ServiceStartTime is present in the payLoad Map";
    private static final String Description_3 = "Email address is present in the payLoad Map";


    private NotifyTemplateFieldMod templateField1;
    private NotifyTemplateFieldMod templateField2;
    private NotifyTemplateFieldMod templateField3;
    private NotifyTemplateFieldMod templateField4;

    private NotifyTemplateFieldMod templateField5;
    private NotifyTemplateFieldMod templateField6;
    private NotifyTemplateMappingMod notifyCommsTemplateMapping;
    private Map<String, String> payLoad;
    private List<NotifyTemplateFieldMod> templateFields;
    private JurorPool pool;
    private DigitalResponse jurorResponse;
    private LocalDate hearingDate;
    private CourtLocation court;

    private Juror juror;

    @Mock
    private NotifyTemplateFieldRepositoryMod notifyTemplateFieldRepository;

    @Mock
    private JurorDigitalResponseRepositoryMod jurorResponseRepository;

    @Mock
    private JurorPoolRepository poolRepository;

    @Mock
    private WelshDayMonthTranslationConfig welshDayMonthTranslationConfig;

    @Mock
    private PoolRequestService uniquePoolService;

    @Mock
    private WelshCourtLocationRepository welshCourtLocationRepository;

    @InjectMocks
    private JurorCommsNotifyPayLoadServiceImpl service;

    @Before
    public void setUp() throws Exception {

        templateFields = new LinkedList<>();

        getNotifyCommsTemplateMapping();

        jurorResponse = new DigitalResponse();
        juror = new Juror();

        court = new CourtLocation();

        hearingDate = LocalDate.of(2019, 7, 8);

        juror = new Juror();
        pool = new JurorPool();
        pool.setJuror(juror);
        juror = pool.getJuror();
        juror.setJurorNumber(JUROR_NUMBER);
        juror.setFirstName(FIRST_NAME);
        juror.setLastName(LAST_NAME_1);
        pool.setNextDate(hearingDate);
        juror.setEmail(EMAIL_1);

    }

    @Test
    public void generatePayLoadData_printFiles_HappyPath() throws ParseException {
        final String detailRec = "    Farah     Lee       YYY   " + JUROR_NUMBER + "XX     ";

        getCourt(false);
        PoolRequest poolRequest = new PoolRequest();
        pool.setPool(poolRequest);
        poolRequest.setCourtLocation(court);
        given(notifyTemplateFieldRepository.findByTemplateId(TEMPLATE_ID)).willReturn(templateFields);
        payLoad = service.generatePayLoadData(TEMPLATE_ID, detailRec, pool);

        assertThat(payLoad)
            .as("Juror number is present in the payLoad Map")
            .isNotEmpty()
            .containsKey(JUROR_NUM)
            .containsValue(JUROR_NUMBER);
        assertThat(payLoad)
            .as("Lastname is present in the payLoad Map")
            .isNotEmpty()
            .containsKey(LAST_NAME)
            .containsValue(LAST_NAME_1);
        assertThat(payLoad)
            .as(Description_1)
            .isNotEmpty()
            .containsKey(SERVICE_START_DATE)
            .containsValue(DATE);
        assertThat(payLoad)
            .as(Description_2)
            .isNotEmpty()
            .containsKey(SERVICE_START_TIME)
            .containsValue(TIME);
        assertThat(payLoad)
            .as(Description_3)
            .isNotEmpty()
            .containsKey(EMAIL_ADDRESS)
            .containsValue(EMAIL_1);

        verify(notifyTemplateFieldRepository).findByTemplateId(TEMPLATE_ID);
    }


    @Test
    public void generatePayLoadData_sentToCourt_HappyPath() throws ParseException {
        //print_files.detail_rec not present for sent to court.
        templateFields.remove(templateFields.remove(templateField1));
        templateFields.remove(templateFields.remove(templateField2));
        templateFields.remove(templateFields.remove(templateField3));

        NotifyTemplateFieldMod templateField = NotifyTemplateFieldMod.builder()
            .id(7L)
            .templateId(TEMPLATE_ID)
            .templateField(COURT_NAME_1)
            .databaseField("COURT_LOCATION.COURT_LOC_NAME")
            .jdClassName("court")
            .jdClassProperty("locCourtName")
            .build();

        templateFields.add(templateField);

        getCourt(true);
        PoolRequest poolRequest = new PoolRequest();
        pool.setPool(poolRequest);
        poolRequest.setCourtLocation(court);

        given(notifyTemplateFieldRepository.findByTemplateId(TEMPLATE_ID)).willReturn(templateFields);
        payLoad = service.generatePayLoadData(TEMPLATE_ID, pool);

        assertThat(payLoad)
            .as(Description_1)
            .isNotEmpty()
            .containsKey(SERVICE_START_DATE)
            .containsValue(DATE);
        assertThat(payLoad)
            .as(Description_2)
            .isNotEmpty()
            .containsKey(SERVICE_START_TIME)
            .containsValue(TIME);
        assertThat(payLoad)
            .as(Description_3)
            .isNotEmpty()
            .containsKey(EMAIL_ADDRESS)
            .containsValue(EMAIL_1);
        assertThat(payLoad)
            .as("Court Name is present in the payLoad Map")
            .isNotEmpty()
            .containsKey(COURT_NAME_1)
            .containsValue(COURT_NAME);

        verify(notifyTemplateFieldRepository).findByTemplateId(TEMPLATE_ID);
    }

    @Test
    public void generatePayLoadData_superUrgentSentToCourt_HappyPath() throws ParseException {

        NotifyTemplateFieldMod templateField = NotifyTemplateFieldMod.builder()
            .id(8L)
            .templateId(TEMPLATE_ID)
            .templateField(EMAIL_ADDRESS)
            .databaseField("JURUR_RESPONSE.EMAIL")
            .jdClassName("jurorResponse")
            .jdClassProperty(EMAIL)
            .build();

        templateFields.add(templateField);

        getJurorResponse();


        getCourt(true);
        PoolRequest poolRequest = new PoolRequest();
        pool.setPool(poolRequest);
        poolRequest.setCourtLocation(court);

        given(jurorResponseRepository.findByJurorNumber(JUROR_NUMBER)).willReturn(jurorResponse);
        given(notifyTemplateFieldRepository.findByTemplateId(TEMPLATE_ID)).willReturn(templateFields);

        payLoad = service.generatePayLoadData(TEMPLATE_ID, pool);

        assertThat(payLoad)
            .as(Description_1)
            .isNotEmpty()
            .containsKey(SERVICE_START_DATE)
            .containsValue(DATE);
        assertThat(payLoad)
            .as(Description_2)
            .isNotEmpty()
            .containsKey(SERVICE_START_TIME)
            .containsValue(TIME);
        assertThat(payLoad)
            .as(Description_3)
            .isNotEmpty()
            .containsKey(EMAIL_ADDRESS)
            .containsValue(EMAIL_2);

        verify(notifyTemplateFieldRepository).findByTemplateId(TEMPLATE_ID);
        verify(jurorResponseRepository).findByJurorNumber(JUROR_NUMBER);
    }


    private CourtLocation getCourt(final boolean sentToCourt) {
        if (sentToCourt == true) {
            court.setLocCourtName(COURT_NAME);
        }
        court.setCourtAttendTime(LocalTime.parse(TIME));
        return court;
    }


    private DigitalResponse getJurorResponse() {
        jurorResponse.setEmail(EMAIL_2);
        jurorResponse.setPhoneNumber("07112293123");
        return jurorResponse;
    }


    private void getNotifyCommsTemplateMapping() {
        templateField1 = NotifyTemplateFieldMod.builder()
            .id(1L)
            .templateId(TEMPLATE_ID)
            .templateField("FIRSTNAME")
            .databaseField(BULK_PRINT_DATA_DETAIL_REC)
            .positionFrom(5)
            .positionTo(14)
            .build();

        templateField2 = NotifyTemplateFieldMod.builder()
            .id(2L)
            .templateId(TEMPLATE_ID)
            .templateField(LAST_NAME)
            .databaseField(BULK_PRINT_DATA_DETAIL_REC)
            .positionFrom(15)
            .positionTo(24)
            .build();
        templateField3 = NotifyTemplateFieldMod.builder()
            .id(3L)
            .templateId(TEMPLATE_ID)
            .templateField(JUROR_NUM)
            .databaseField(BULK_PRINT_DATA_DETAIL_REC)
            .positionFrom(31)
            .positionTo(39)
            .build();

        templateField4 = NotifyTemplateFieldMod.builder()
            .id(4L)
            .templateId(TEMPLATE_ID)
            .templateField(SERVICE_START_DATE)
            .databaseField("jurorPool.next_date")
            .jdClassName(JUROR)
            .jdClassProperty("nextDate")
            .build();
        templateField5 = NotifyTemplateFieldMod.builder()
            .id(5L)
            .templateId(TEMPLATE_ID)
            .templateField(SERVICE_START_TIME)
            .databaseField("pool.attend_time")
            .jdClassName("Pool")
            .jdClassProperty("attendTime")
            .build();
        templateField6 = NotifyTemplateFieldMod.builder()
            .id(6L)
            .templateId(TEMPLATE_ID)
            .templateField(EMAIL_ADDRESS)
            .databaseField("juror.h_email")
            .jdClassName(JUROR)
            .jdClassProperty(EMAIL)
            .build();


        templateFields.add(templateField1);
        templateFields.add(templateField2);
        templateFields.add(templateField3);
        templateFields.add(templateField4);
        templateFields.add(templateField5);
        templateFields.add(templateField6);
        notifyCommsTemplateMapping = NotifyTemplateMappingMod.builder().templateId(TEMPLATE_ID.toString()).build();
    }
}
