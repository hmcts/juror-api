package uk.gov.hmcts.juror.api.bureau.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.juror.api.bureau.domain.JurorCommsPrintFiles;
import uk.gov.hmcts.juror.api.bureau.domain.JurorCommsPrintFilesRepository;
import uk.gov.hmcts.juror.api.bureau.domain.NotifyTemplateField;
import uk.gov.hmcts.juror.api.bureau.domain.PrintFile;
import uk.gov.hmcts.juror.api.bureau.domain.PrintFileRepository;
import uk.gov.hmcts.juror.api.bureau.notify.JurorCommsNotifyTemplateType;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponse;
import uk.gov.hmcts.juror.api.juror.domain.Pool;
import uk.gov.hmcts.juror.api.juror.domain.PoolRepository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
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

    private Pool pool;
    List<NotifyTemplateField> templateFields = new LinkedList<>();
    List<JurorCommsPrintFiles> jurorCommsPrintFilesList = new LinkedList<>();
    List<PrintFile> printFileList = new LinkedList<>();

    Date currentDate = Date.from(LocalDateTime.now().toInstant(ZoneOffset.UTC));

    @Mock
    private JurorCommsPrintFilesRepository jurorCommsPrintFilesRepository;

    @Mock
    private PoolRepository poolRepository;

    @Mock
    private JurorCommsNotificationService jurorCommsNotificationService;

    @Mock
    private PrintFileRepository printFileRepository;

    @InjectMocks
    private JurorCommsLetterServiceImpl service;

    @Before
    public void setUp() throws Exception {
        JurorResponse juror = JurorResponse.builder()
            .jurorNumber(JUROR_NUMBER)
            .firstName("Farah")
            .lastName("Lee")
            .email("a@b.com")
            .build();

        pool = Pool.builder()
            .jurorNumber(JUROR_NUMBER)
            .firstName("Farah")
            .lastName("Lee")
            .email("a@b.com")
            .welsh(false)
            .build();

        JurorCommsPrintFiles jurorCommsPrintFiles1 = new JurorCommsPrintFiles();
        jurorCommsPrintFiles1.setPrintFileName("File001");
        jurorCommsPrintFiles1.setCreationDate(currentDate);
        jurorCommsPrintFiles1.setFormType("5224A");
        jurorCommsPrintFiles1.setJurorNumber("123456789");
        jurorCommsPrintFiles1.setDigitalComms("N");
        jurorCommsPrintFiles1.setTemplateId("abcd1234");
        jurorCommsPrintFiles1.setDetailRec("    Farah     Lee       YYY   " + JUROR_NUMBER + "XX     ");

        JurorCommsPrintFiles jurorCommsPrintFiles2 = new JurorCommsPrintFiles();
        jurorCommsPrintFiles2.setPrintFileName("File001");
        jurorCommsPrintFiles2.setCreationDate(currentDate);
        jurorCommsPrintFiles2.setFormType("5229A");
        jurorCommsPrintFiles2.setJurorNumber("987654321");
        jurorCommsPrintFiles2.setDigitalComms("N");
        jurorCommsPrintFiles2.setTemplateId("dcba4321");
        jurorCommsPrintFiles2.setDetailRec("    Farah     Lee       YYY   \"+JUROR_NUMBER+\"XX     ");

        jurorCommsPrintFilesList.add(jurorCommsPrintFiles1);
        jurorCommsPrintFilesList.add(jurorCommsPrintFiles2);

        NotifyTemplateField templateField1 = NotifyTemplateField.builder()
            .id(1L)
            .templateId(TEMPLATE_ID)
            .templateField("FIRSTNAME")
            .databaseField("PRINT_FILES.DETAIL_REC")
            .positionFrom(5)
            .positionTo(14)
            .build();

        NotifyTemplateField templateField2 = NotifyTemplateField.builder()
            .id(2L)
            .templateId(TEMPLATE_ID)
            .templateField("LASTNAME")
            .databaseField("PRINT_FILES.DETAIL_REC")
            .positionFrom(15)
            .positionTo(24)
            .build();
        NotifyTemplateField templateField3 = NotifyTemplateField.builder()
            .id(3L)
            .templateId(TEMPLATE_ID)
            .templateField("JUROR NUMBER")
            .databaseField("PRINT_FILES.DETAIL_REC")
            .positionFrom(31)
            .positionTo(39)
            .build();

        templateFields.add(templateField1);
        templateFields.add(templateField2);
        templateFields.add(templateField3);

        PrintFile printFile = new PrintFile();
        printFile.setPrintFileName("File001");
        printFile.setCreationDate(currentDate);
        printFile.setPartNo("123456789");
        printFile.setDetailRec("    Farah     Lee       YYY   " + JUROR_NUMBER + "XX     ");
        printFile.setFormType("5224A");
        printFile.setDigitalComms(false);

        printFileList.add(printFile);

    }

    @Test
    public void process_HappyPath() {
        given(jurorCommsPrintFilesRepository.findAll()).willReturn(jurorCommsPrintFilesList);
        given(poolRepository.findByJurorNumber(anyString())).willReturn(pool);
        given(printFileRepository.findByPartNoAndPrintFileNameAndCreationDate(anyString(), anyString(),
            any(Date.class))).willReturn(printFileList);

        service.process();
        verify(jurorCommsNotificationService, times(2)).sendJurorComms(any(Pool.class),
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
