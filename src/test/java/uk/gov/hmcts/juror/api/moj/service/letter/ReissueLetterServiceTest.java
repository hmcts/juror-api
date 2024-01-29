package uk.gov.hmcts.juror.api.moj.service.letter;

import com.querydsl.core.Tuple;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.moj.controller.request.ReissueLetterListRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.ReissueLetterRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.ReissueLetterListResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.BulkPrintData;
import uk.gov.hmcts.juror.api.moj.domain.FormAttribute;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.enumeration.letter.LetterType;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.BulkPrintDataRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorStatusRepository;
import uk.gov.hmcts.juror.api.moj.service.PrintDataService;
import uk.gov.hmcts.juror.api.moj.service.ReissueLetterService;
import uk.gov.hmcts.juror.api.moj.service.ReissueLetterServiceImpl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SuppressWarnings("PMD.ExcessiveImports")
public class ReissueLetterServiceTest {

    @Mock
    private BulkPrintDataRepository bulkPrintDataRepository;

    @Mock
    private JurorPoolRepository jurorPoolRepository;

    @Mock
    private JurorStatusRepository jurorStatusRepository;

    @Mock
    private PrintDataService printDataService;

    @InjectMocks
    ReissueLetterServiceImpl reissueLetterService;

    @Nested
    @DisplayName("Reissue Letter List Tests")
    class ReissueLetterListTests {

        @Test
        @SuppressWarnings("PMD.LawOfDemeter")
        void reissueDeferralLetterListHappyPath() {
            String owner = "400";
            String jurorNumber = "123456789";

            TestUtils.setupAuthentication(owner, "Bureau", "1");

            final ReissueLetterListRequestDto reissueLetterListRequestDto = ReissueLetterListRequestDto.builder()
                .jurorNumber(jurorNumber)
                .letterType(LetterType.DEFERRAL)
                .build();

            final List<Tuple> deferralGrantedLetters = new ArrayList<>();
            Tuple tuple = mock(Tuple.class);
            doReturn(jurorNumber).when(tuple).get(ReissueLetterService.DataType.JUROR_NUMBER.getExpression());
            doReturn("FIRSTNAME").when(tuple).get(ReissueLetterService.DataType.JUROR_FIRST_NAME.getExpression());
            doReturn("LASTNAME").when(tuple).get(ReissueLetterService.DataType.JUROR_LAST_NAME.getExpression());
            doReturn("ABC 2DE").when(tuple).get(ReissueLetterService.DataType.JUROR_POSTCODE.getExpression());
            doReturn("Deferred").when(tuple).get(ReissueLetterService.DataType.JUROR_STATUS.getExpression());
            doReturn(LocalDate.now().plusDays(10)).when(tuple)
                .get(ReissueLetterService.DataType.JUROR_DEFERRED_TO.getExpression());
            doReturn("A").when(tuple).get(ReissueLetterService.DataType.JUROR_DEFERRED_TO_REASON.getExpression());
            doReturn(LocalDate.now().minusDays(2)).when(tuple)
                .get(ReissueLetterService.DataType.DATE_PRINTED.getExpression());
            doReturn("5229A").when(tuple).get(ReissueLetterService.DataType.FORM_CODE.getExpression());
            deferralGrantedLetters.add(tuple);

            doReturn(deferralGrantedLetters).when(bulkPrintDataRepository)
                .findLetters(reissueLetterListRequestDto, LetterType.DEFERRAL.getLetterQueryConsumer());

            final ReissueLetterListResponseDto responseDto =
                reissueLetterService.reissueLetterList(reissueLetterListRequestDto);

            List<List<Object>> data = responseDto.getData();
            Assertions.assertThat(data).isNotNull();
            Assertions.assertThat(data.size()).isEqualTo(1);
            Assertions.assertThat(data.get(0).size()).isEqualTo(9);

            verify(bulkPrintDataRepository, times(1))
                .findLetters(reissueLetterListRequestDto, LetterType.DEFERRAL.getLetterQueryConsumer());
        }

        @Test
        void reissueDeferralLetterListUnhappyNoResults() {
            String owner = "400";
            String jurorNumber = "123456789";

            TestUtils.setupAuthentication(owner, "Bureau", "1");

            final ReissueLetterListRequestDto reissueLetterListRequestDto = ReissueLetterListRequestDto.builder()
                .jurorNumber(jurorNumber)
                .letterType(LetterType.DEFERRAL)
                .build();

            final List<Tuple> deferralGrantedLetters = new ArrayList<>();
            doReturn(deferralGrantedLetters).when(bulkPrintDataRepository)
                .findLetters(reissueLetterListRequestDto, LetterType.DEFERRAL.getLetterQueryConsumer());

            assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(() ->
                reissueLetterService.reissueLetterList(reissueLetterListRequestDto));

            verify(bulkPrintDataRepository, times(1))
                .findLetters(reissueLetterListRequestDto, LetterType.DEFERRAL.getLetterQueryConsumer());
        }

    }

    @Nested
    @DisplayName("Reissue Letter Tests")
    class ReissueLetterTests {

        @Test
        void reissueDeferralLetterHappyPath() {
            String owner = "400";

            TestUtils.setupAuthentication(owner, "Bureau", "1");

            final ReissueLetterRequestDto.ReissueLetterRequestData reissueLetterRequestData =
                ReissueLetterRequestDto.ReissueLetterRequestData.builder()
                    .jurorNumber("555555561")
                    .formCode("5229A")
                    .datePrinted(LocalDate.now().minusDays(1))
                    .build();

            final ReissueLetterRequestDto reissueLetterRequestDto = ReissueLetterRequestDto.builder()
                .letters(List.of(reissueLetterRequestData))
                .build();

            final BulkPrintData bulkPrintData = BulkPrintData.builder()
                .jurorNo(reissueLetterRequestData.getJurorNumber())
                .formAttribute(FormAttribute.builder().formType(reissueLetterRequestData.getFormCode()).build())
                .creationDate(reissueLetterRequestData.getDatePrinted())
                .build();

            doReturn(Optional.of(bulkPrintData)).when(bulkPrintDataRepository)
                .findByJurorNumberFormCodeDatePrinted(reissueLetterRequestData.getJurorNumber(),
                    reissueLetterRequestData.getFormCode(), reissueLetterRequestData.getDatePrinted());

            JurorPool jurorPool = mock(JurorPool.class);
            JurorStatus deferredStatus = new JurorStatus();

            when(jurorStatusRepository.findById(IJurorStatus.DEFERRED))
                .thenReturn(Optional.ofNullable(deferredStatus));

            doReturn(jurorPool).when(jurorPoolRepository).
                findByJurorJurorNumberAndStatus(reissueLetterRequestData.getJurorNumber(),
                    deferredStatus);

            reissueLetterService.reissueLetter(reissueLetterRequestDto);

            verify(bulkPrintDataRepository, times(1))
                .findByJurorNumberFormCodeDatePrinted(reissueLetterRequestData.getJurorNumber(),
                    reissueLetterRequestData.getFormCode(), reissueLetterRequestData.getDatePrinted());
            verify(jurorStatusRepository, times(1)).findById(IJurorStatus.DEFERRED);
            verify(jurorPoolRepository, times(1))
                .findByJurorJurorNumberAndStatus(reissueLetterRequestData.getJurorNumber(), deferredStatus);
        }

        @Test
        void reissueDeferralLetterUnhappyNoData() {
            String owner = "400";

            TestUtils.setupAuthentication(owner, "Bureau", "1");

            ReissueLetterRequestDto.ReissueLetterRequestData reissueLetterRequestData =
                ReissueLetterRequestDto.ReissueLetterRequestData.builder()
                    .jurorNumber("555555561")
                    .formCode("5229A")
                    .datePrinted(LocalDate.now().minusDays(1))
                    .build();

            ReissueLetterRequestDto reissueLetterRequestDto = ReissueLetterRequestDto.builder()
                .letters(List.of(reissueLetterRequestData))
                .build();

            doReturn(Optional.empty()).when(bulkPrintDataRepository)
                .findByJurorNumberFormCodeDatePrinted(reissueLetterRequestData.getJurorNumber(),
                    reissueLetterRequestData.getFormCode(), reissueLetterRequestData.getDatePrinted());

            assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(() ->
                reissueLetterService.reissueLetter(reissueLetterRequestDto));

            verify(bulkPrintDataRepository, times(1))
                .findByJurorNumberFormCodeDatePrinted(reissueLetterRequestData.getJurorNumber(),
                    reissueLetterRequestData.getFormCode(), reissueLetterRequestData.getDatePrinted());
            verify(jurorStatusRepository, times(0)).findById(Mockito.anyInt());
            verify(jurorPoolRepository, times(0))
                .findByJurorJurorNumberAndStatus(Mockito.anyString(), Mockito.any());
        }

    }

    @Nested
    @DisplayName("Delete Letter Tests")
    class DeleteLetterTests {

        @Test
        void deleteDeferralLetterHappyPath() {
            String owner = "400";

            TestUtils.setupAuthentication(owner, "Bureau", "1");

            ReissueLetterRequestDto.ReissueLetterRequestData reissueLetterRequestData =
                ReissueLetterRequestDto.ReissueLetterRequestData.builder()
                    .jurorNumber("555555561")
                    .formCode("5229A")
                    .datePrinted(LocalDate.now().minusDays(1))
                    .build();

            ReissueLetterRequestDto reissueLetterRequestDto = ReissueLetterRequestDto.builder()
                .letters(List.of(reissueLetterRequestData))
                .build();

            BulkPrintData bulkPrintData = BulkPrintData.builder()
                .jurorNo(reissueLetterRequestData.getJurorNumber())
                .formAttribute(FormAttribute.builder().formType(reissueLetterRequestData.getFormCode()).build())
                .creationDate(reissueLetterRequestData.getDatePrinted())
                .build();

            doReturn(Optional.of(bulkPrintData)).when(bulkPrintDataRepository)
                .findByJurorNumberFormCodeDatePrinted(reissueLetterRequestData.getJurorNumber(),
                    reissueLetterRequestData.getFormCode(), reissueLetterRequestData.getDatePrinted());

            reissueLetterService.deletePendingLetter(reissueLetterRequestDto);

            verify(bulkPrintDataRepository, times(1))
                .findByJurorNumberFormCodeDatePrinted(reissueLetterRequestData.getJurorNumber(),
                    reissueLetterRequestData.getFormCode(), reissueLetterRequestData.getDatePrinted());
            verify(bulkPrintDataRepository, times(1)).delete(bulkPrintData);

        }

        @Test
        void deleteDeferralLetterUnhappyNoData() {
            String owner = "400";

            TestUtils.setupAuthentication(owner, "Bureau", "1");

            ReissueLetterRequestDto.ReissueLetterRequestData reissueLetterRequestData =
                ReissueLetterRequestDto.ReissueLetterRequestData.builder()
                    .jurorNumber("555555561")
                    .formCode("5229A")
                    .datePrinted(LocalDate.now().minusDays(1))
                    .build();

            ReissueLetterRequestDto reissueLetterRequestDto = ReissueLetterRequestDto.builder()
                .letters(List.of(reissueLetterRequestData))
                .build();

            doReturn(Optional.empty()).when(bulkPrintDataRepository)
                .findByJurorNumberFormCodeDatePrinted(reissueLetterRequestData.getJurorNumber(),
                    reissueLetterRequestData.getFormCode(), reissueLetterRequestData.getDatePrinted());

            assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(() ->
                reissueLetterService.reissueLetter(reissueLetterRequestDto));

            verify(bulkPrintDataRepository, times(1))
                .findByJurorNumberFormCodeDatePrinted(reissueLetterRequestData.getJurorNumber(),
                    reissueLetterRequestData.getFormCode(), reissueLetterRequestData.getDatePrinted());
            verify(bulkPrintDataRepository, times(0)).save(Mockito.any());

        }

    }
}
