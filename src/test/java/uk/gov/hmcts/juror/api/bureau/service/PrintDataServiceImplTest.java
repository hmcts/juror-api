package uk.gov.hmcts.juror.api.bureau.service;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.domain.BulkPrintData;
import uk.gov.hmcts.juror.api.moj.domain.FormAttribute;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.BulkPrintDataRepository;
import uk.gov.hmcts.juror.api.moj.repository.FormAttributeRepository;
import uk.gov.hmcts.juror.api.moj.service.CourtLocationService;
import uk.gov.hmcts.juror.api.moj.service.PrintDataServiceImpl;
import uk.gov.hmcts.juror.api.moj.utils.RepositoryUtils;
import uk.gov.hmcts.juror.api.moj.xerox.LetterBase;
import uk.gov.hmcts.juror.api.moj.xerox.LetterTestUtils;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(SpringExtension.class)
class PrintDataServiceImplTest {
    private MockedStatic<RepositoryUtils> mockRepositoryUtils;
    @Mock
    private BulkPrintDataRepository bulkPrintDataRepository;
    @Mock
    private CourtLocationService courtLocationService;
    @Mock
    private WelshCourtLocationRepository welshCourtLocationRepository;
    @Mock
    private FormAttribute formAttribute;
    @Mock
    private LetterBase letterBase;
    @Mock
    private JurorStatus jurorStatus;

    @InjectMocks
    private PrintDataServiceImpl printDataService;

    @BeforeEach
    void setStaticMocks() {
        mockRepositoryUtils = mockStatic(RepositoryUtils.class);
        mockRepositoryUtils.when(() -> RepositoryUtils.retrieveFromDatabase(
                eq("FORM_CODE_ENUM"),
                (FormAttributeRepository) any()))
            .thenReturn(formAttribute);
        doReturn(LetterTestUtils.testBureauLocation()).when(courtLocationService)
            .getCourtLocation(PrintDataServiceImpl.BUREAU_LOC_CODE);
        doReturn(LetterTestUtils.testWelshCourtLocation()).when(welshCourtLocationRepository)
            .findByLocCode(any());
    }

    @AfterEach
    void clearStaticMocks() {
        if (mockRepositoryUtils != null) {
            mockRepositoryUtils.close();
        }
    }

    @Test
    void bulkPrintSummonsLetterThrowsWithNullList() {
        assertThatExceptionOfType(MojException.InternalServerError.class).isThrownBy(() ->
            printDataService.bulkPrintSummonsLetter(null));
    }

    @Test
    void bulkPrintSummonsLetterThrowsWithEmptyList() {
        assertThatExceptionOfType(MojException.InternalServerError.class).isThrownBy(() ->
            printDataService.bulkPrintSummonsLetter(new ArrayList<JurorPool>()));
    }

    @Test
    void bulkPrintSummonsLetterCallsForEachListMember() {
        final LocalDate date = LocalDate.of(2017, Month.FEBRUARY, 6);

        doReturn(IJurorStatus.SUMMONED).when(jurorStatus).getStatus();
        ArrayList<JurorPool> dummyList = new ArrayList<>();
        dummyList.add(LetterTestUtils.testJurorPool(date));
        dummyList.add(LetterTestUtils.testJurorPool(date));
        dummyList.add(LetterTestUtils.testJurorPool(date));
        printDataService.bulkPrintSummonsLetter(dummyList);
        verify(bulkPrintDataRepository, times(3)).save(any());
    }

    @Test
    void bulkPrintSummonsLetterSkipsDisqualifiedListMembers() {
        final LocalDate date = LocalDate.of(2017, Month.FEBRUARY, 6);

        doReturn(IJurorStatus.SUMMONED).when(jurorStatus).getStatus();
        ArrayList<JurorPool> dummyList = new ArrayList<>();
        dummyList.add(LetterTestUtils.testJurorPool(date));
        dummyList.add(LetterTestUtils.testJurorPool(date));
        dummyList.add(LetterTestUtils.testDisqualifiedJurorPool(date));
        printDataService.bulkPrintSummonsLetter(dummyList);
        verify(bulkPrintDataRepository, times(2)).save(any());
    }

    @Test
    void printDeferralLetterThrowsWithNullList() {
        assertThatExceptionOfType(MojException.InternalServerError.class)
            .isThrownBy(() -> printDataService.printDeferralLetter(null));
    }

    @Test
    void printDeferralLetterCallsCommit() {
        final LocalDate date = LocalDate.of(2017, Month.FEBRUARY, 6);

        doReturn(IJurorStatus.SUMMONED).when(jurorStatus).getStatus();
        printDataService.printDeferralLetter(LetterTestUtils.testJurorPool(date));
        verify(bulkPrintDataRepository, times(1)).save(any());
    }

    @Test
    void printDeferralDeniedLetterThrowsWithNullList() {
        assertThatExceptionOfType(MojException.InternalServerError.class)
            .isThrownBy(() -> printDataService.printDeferralDeniedLetter(null));
    }

    @Test
    void printDeferralDeniedLetterCallsCommit() {
        final LocalDate date = LocalDate.of(2017, Month.FEBRUARY, 6);

        doReturn(IJurorStatus.SUMMONED).when(jurorStatus).getStatus();
        printDataService.printDeferralDeniedLetter(LetterTestUtils.testJurorPool(date));
        verify(bulkPrintDataRepository, times(1)).save(any());
    }

    @Test
    void printExcusalDeniedLetterThrowsWithNullList() {
        assertThatExceptionOfType(MojException.InternalServerError.class)
            .isThrownBy(() -> printDataService.printExcusalDeniedLetter(null));
    }

    @Test
    void printExcusalDeniedLetterCallsCommit() {
        final LocalDate date = LocalDate.of(2017, Month.FEBRUARY, 6);

        doReturn(IJurorStatus.SUMMONED).when(jurorStatus).getStatus();
        printDataService.printExcusalDeniedLetter(LetterTestUtils.testJurorPool(date));
        verify(bulkPrintDataRepository, times(1)).save(any());
    }

    @Test
    void printConfrimationLetterThrowsWithNullList() {
        assertThatExceptionOfType(MojException.InternalServerError.class)
            .isThrownBy(() -> printDataService.printConfirmationLetter(null));
    }

    @Test
    void printConfirmationLetterCallsCommit() {
        final LocalDate date = LocalDate.of(2017, Month.FEBRUARY, 6);

        doReturn(IJurorStatus.SUMMONED).when(jurorStatus).getStatus();
        printDataService.printConfirmationLetter(LetterTestUtils.testJurorPool(date));
        verify(bulkPrintDataRepository, times(1)).save(any());
    }

    @Test
    void printPostponeLetterThrowsWithNullList() {
        assertThatExceptionOfType(MojException.InternalServerError.class)
            .isThrownBy(() -> printDataService.printPostponeLetter(null));
    }

    @Test
    void printPostponeLetterCallsCommit() {
        final LocalDate date = LocalDate.of(2017, Month.FEBRUARY, 6);

        doReturn(IJurorStatus.SUMMONED).when(jurorStatus).getStatus();
        printDataService.printPostponeLetter(LetterTestUtils.testJurorPool(date));
    }

    @Test
    void printExcusalLetterThrowsWithNullList() {
        assertThatExceptionOfType(MojException.InternalServerError.class)
            .isThrownBy(() -> printDataService.printExcusalLetter(null));
    }

    @Test
    void printExcusalLetterCallsCommit() {
        final LocalDate date = LocalDate.of(2017, Month.FEBRUARY, 6);
        doReturn(IJurorStatus.SUMMONED).when(jurorStatus).getStatus();
        printDataService.printExcusalLetter(LetterTestUtils.testJurorPool(date));
        verify(bulkPrintDataRepository, times(1)).save(any());
    }

    @Test
    void commitDataWritesLetterData() {
        doReturn("test-letter-string").when(letterBase).getLetterString();
        doReturn("123456789").when(letterBase).getJurorNumber();
        doReturn("FORM_CODE_ENUM").when(letterBase).getFormCode();

        printDataService.commitData(letterBase);

        ArgumentCaptor<BulkPrintData> committedRecord = ArgumentCaptor.forClass(BulkPrintData.class);
        verify(bulkPrintDataRepository).save(committedRecord.capture());

        assertThat(committedRecord.getValue().getJurorNo()).isEqualTo("123456789");
        assertThat(committedRecord.getValue().getFormAttribute()).isEqualTo(formAttribute);
        assertThat(committedRecord.getValue().getDetailRec()).isEqualTo("test-letter-string");
        assertThat(committedRecord.getValue().getCreationDate()).isEqualTo(LocalDate.now());

    }
}
