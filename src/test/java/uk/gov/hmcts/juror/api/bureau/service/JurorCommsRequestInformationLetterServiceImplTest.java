package uk.gov.hmcts.juror.api.bureau.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.juror.api.bureau.notify.JurorCommsNotifyTemplateType;
import uk.gov.hmcts.juror.api.moj.domain.BulkPrintData;
import uk.gov.hmcts.juror.api.moj.domain.BulkPrintDataNotifyComms;
import uk.gov.hmcts.juror.api.moj.domain.BulkPrintDataNotifyCommsRepository;
import uk.gov.hmcts.juror.api.moj.domain.FormAttribute;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.NotifyTemplateFieldMod;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.repository.BulkPrintDataRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@SuppressWarnings("Duplicates")
@RunWith(MockitoJUnitRunner.class)
public class JurorCommsRequestInformationLetterServiceImplTest {
    private static final String JUROR_NUMBER = "111222333";
    private static final String TEMPLATE_ID = "123456789";

    private JurorPool pool;

    List<NotifyTemplateFieldMod> templateFields = new LinkedList<>();
    List<BulkPrintDataNotifyComms> jurorCommsPrintFilesList = new LinkedList<>();
    List<BulkPrintData> printFileList = new LinkedList<>();

    LocalDate currentDate = LocalDate.now();

    @Mock
    private BulkPrintDataNotifyCommsRepository jurorCommsPrintFilesRepository;

    @Mock
    private JurorPoolRepository poolRepository;

    @Mock
    private JurorCommsNotificationService jurorCommsNotificationService;

    @Mock
    private BulkPrintDataRepository printFileRepository;

    @InjectMocks
    private JurorCommsLetterServiceImpl service;

    @Before
    public void setUp() throws Exception {
        DigitalResponse juror = new DigitalResponse();
        juror.setJurorNumber(JUROR_NUMBER);
        juror.setFirstName("Farah");
        juror.setLastName("Lee");
        juror.setEmail("a@b.com");


        Juror jurorSet = new Juror();
        pool = new JurorPool();
        pool.setJuror(jurorSet);
        pool = new JurorPool();
        jurorSet.setJurorNumber(JUROR_NUMBER);
        jurorSet.setFirstName("Farah");
        jurorSet.setLastName("Lee");
        jurorSet.setEmail("a@b.com");
        jurorSet.setWelsh(false);


        BulkPrintDataNotifyComms jurorCommsPrintFiles1 = new BulkPrintDataNotifyComms();
        jurorCommsPrintFiles1.setId(1L);
        FormAttribute formAttribute = new FormAttribute();
        jurorCommsPrintFiles1.setFormAttribute(formAttribute);
        jurorCommsPrintFiles1.setCreationDate(currentDate);
        formAttribute.setFormType("5224A");
        jurorCommsPrintFiles1.setJurorNo("123456789");
        jurorCommsPrintFiles1.setDigitalComms(false);
        jurorCommsPrintFiles1.setTemplateId("abcd1234");
        jurorCommsPrintFiles1.setDetailRec("    Farah     Lee       YYY   " + JUROR_NUMBER + "XX     ");

        BulkPrintDataNotifyComms jurorCommsPrintFiles2 = new BulkPrintDataNotifyComms();
        jurorCommsPrintFiles2.setId(1L);
        FormAttribute formAttribute2 = new FormAttribute();
        jurorCommsPrintFiles2.setFormAttribute(formAttribute2);
        jurorCommsPrintFiles2.setCreationDate(currentDate);
        formAttribute2.setFormType("5229A");
        jurorCommsPrintFiles2.setJurorNo("987654321");
        jurorCommsPrintFiles2.setDigitalComms(false);
        jurorCommsPrintFiles2.setTemplateId("dcba4321");
        jurorCommsPrintFiles2.setDetailRec("    Farah     Lee       YYY   \"+JUROR_NUMBER+\"XX     ");

        jurorCommsPrintFilesList.add(jurorCommsPrintFiles1);
        jurorCommsPrintFilesList.add(jurorCommsPrintFiles2);

        NotifyTemplateFieldMod templateField1 = NotifyTemplateFieldMod.builder()
            .id(1L)
            .templateId(TEMPLATE_ID)
            .templateField("FIRSTNAME")
            .positionFrom(5)
            .positionTo(14)
            .build();

        NotifyTemplateFieldMod templateField2 = NotifyTemplateFieldMod.builder()
            .id(2L)
            .templateId(TEMPLATE_ID)
            .templateField("LASTNAME")
            .positionFrom(15)
            .positionTo(24)
            .build();
        NotifyTemplateFieldMod templateField3 = NotifyTemplateFieldMod.builder()
            .id(3L)
            .templateId(TEMPLATE_ID)
            .templateField("JUROR NUMBER")
            .positionFrom(31)
            .positionTo(39)
            .build();

        templateFields.add(templateField1);
        templateFields.add(templateField2);
        templateFields.add(templateField3);

        BulkPrintData printFile = new BulkPrintData();
        FormAttribute formAttributep = new FormAttribute();
        printFile.setFormAttribute(formAttributep);
        printFile.setId(1L);
        printFile.setCreationDate(currentDate);
        printFile.setJurorNo("123456789");
        printFile.setDetailRec("    Farah     Lee       YYY   " + JUROR_NUMBER + "XX     ");
        formAttributep.setFormType("5224A");
        printFile.setDigitalComms(false);

        printFileList.add(printFile);

    }

    @Test
    public void process_HappyPath() {
        given(jurorCommsPrintFilesRepository.findAll()).willReturn(jurorCommsPrintFilesList);
        given(poolRepository.findByJurorJurorNumberAndIsActiveAndOwner(anyString(),anyBoolean(),anyString()))
            .willReturn(pool);
        given(printFileRepository.findByJurorNoAndIdAndCreationDate(anyString(), anyLong(),
                                                                    any(LocalDate.class))).willReturn(printFileList);

        service.process();
        verify(jurorCommsNotificationService, times(2)).sendJurorComms(any(JurorPool.class),
            any(JurorCommsNotifyTemplateType.class),
            anyString(), anyString(), anyBoolean());
        verify(printFileRepository, times(2)).saveAll(printFileList);
    }

    @Test
    public void process_noPending_PrintFiles() {
        given(jurorCommsPrintFilesRepository.findAll()).willReturn(new LinkedList<>());

        service.process();
        verifyNoInteractions(jurorCommsNotificationService);
        verifyNoInteractions(printFileRepository);
    }
}
