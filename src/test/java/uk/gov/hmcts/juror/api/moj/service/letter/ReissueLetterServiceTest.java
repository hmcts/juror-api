package uk.gov.hmcts.juror.api.moj.service.letter;

import com.querydsl.core.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
import uk.gov.hmcts.juror.api.moj.domain.FormCode;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.enumeration.letter.LetterType;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.BulkPrintDataRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorStatusRepository;
import uk.gov.hmcts.juror.api.moj.service.JurorHistoryService;
import uk.gov.hmcts.juror.api.moj.service.PrintDataService;
import uk.gov.hmcts.juror.api.moj.service.ReissueLetterService;
import uk.gov.hmcts.juror.api.moj.service.ReissueLetterServiceImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.LawOfDemeter"})
public class ReissueLetterServiceTest {

    @Mock
    private BulkPrintDataRepository bulkPrintDataRepository;

    @Mock
    private JurorPoolRepository jurorPoolRepository;

    @Mock
    private JurorStatusRepository jurorStatusRepository;

    @Mock
    private PrintDataService printDataService;

    @Mock
    private JurorHistoryService jurorHistoryService;

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

            TestUtils.setUpMockAuthentication(owner, "Bureau", "1", List.of("400"));

            final ReissueLetterListRequestDto reissueLetterListRequestDto = ReissueLetterListRequestDto.builder()
                .jurorNumber(jurorNumber)
                .letterType(LetterType.DEFERRAL_GRANTED)
                .build();

            final List<Tuple> deferralGrantedLetters = getDeferralGrantedLetters(jurorNumber);

            doReturn(deferralGrantedLetters).when(bulkPrintDataRepository)
                .findLetters(reissueLetterListRequestDto, LetterType.DEFERRAL_GRANTED.getLetterQueryConsumer());

            final ReissueLetterListResponseDto responseDto =
                reissueLetterService.reissueLetterList(reissueLetterListRequestDto);

            List<List<Object>> data = responseDto.getData();
            assertThat(data).isNotNull().hasSize(1);
            assertThat(data.get(0)).hasSize(10);

            verify(bulkPrintDataRepository, times(1))
                .findLetters(reissueLetterListRequestDto, LetterType.DEFERRAL_GRANTED.getLetterQueryConsumer());
        }

        private List<Tuple> getDeferralGrantedLetters(String jurorNumber) {
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
            return deferralGrantedLetters;
        }

        @Test
        void reissueDeferralLetterListUnhappyNoResults() {
            String owner = "400";
            String jurorNumber = "123456789";

            TestUtils.setUpMockAuthentication(owner, "Bureau", "1", List.of("400"));

            final ReissueLetterListRequestDto reissueLetterListRequestDto = ReissueLetterListRequestDto.builder()
                .jurorNumber(jurorNumber)
                .letterType(LetterType.DEFERRAL_GRANTED)
                .build();

            final List<Tuple> deferralGrantedLetters = new ArrayList<>();
            doReturn(deferralGrantedLetters).when(bulkPrintDataRepository)
                .findLetters(reissueLetterListRequestDto, LetterType.DEFERRAL_GRANTED.getLetterQueryConsumer());

            assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(() ->
                reissueLetterService.reissueLetterList(reissueLetterListRequestDto));

            verify(bulkPrintDataRepository, times(1))
                .findLetters(reissueLetterListRequestDto, LetterType.DEFERRAL_GRANTED.getLetterQueryConsumer());
        }

        @Test
        @SuppressWarnings("PMD.LawOfDemeter")
        void reissueConfirmationLetterListHappyPath() {
            String owner = "400";
            String jurorNumber = "123456789";

            TestUtils.setUpMockAuthentication(owner, "Bureau", "1", List.of("400"));

            final ReissueLetterListRequestDto reissueLetterListRequestDto = ReissueLetterListRequestDto.builder()
                .jurorNumber(jurorNumber)
                .letterType(LetterType.CONFIRMATION)
                .build();

            final List<Tuple> confirmationLetters = getConfirmationGrantedLetters(jurorNumber);

            doReturn(confirmationLetters).when(bulkPrintDataRepository)
                .findLetters(reissueLetterListRequestDto, LetterType.CONFIRMATION.getLetterQueryConsumer());

            final ReissueLetterListResponseDto responseDto =
                reissueLetterService.reissueLetterList(reissueLetterListRequestDto);

            List<List<Object>> data = responseDto.getData();
            assertThat(data).isNotNull().hasSize(1);
            assertThat(data.get(0)).hasSize(7);

            verify(bulkPrintDataRepository, times(1))
                .findLetters(reissueLetterListRequestDto, LetterType.CONFIRMATION.getLetterQueryConsumer());
        }

        private List<Tuple> getConfirmationGrantedLetters(String jurorNumber) {
            final List<Tuple> confirmationLetters = new ArrayList<>();
            Tuple tuple = mock(Tuple.class);
            doReturn(jurorNumber).when(tuple).get(ReissueLetterService.DataType.JUROR_NUMBER.getExpression());
            doReturn("FIRSTNAME").when(tuple).get(ReissueLetterService.DataType.JUROR_FIRST_NAME.getExpression());
            doReturn("LASTNAME").when(tuple).get(ReissueLetterService.DataType.JUROR_LAST_NAME.getExpression());
            doReturn("ABC 2DE").when(tuple).get(ReissueLetterService.DataType.JUROR_POSTCODE.getExpression());
            doReturn(LocalDate.now().minusDays(2)).when(tuple)
                .get(ReissueLetterService.DataType.DATE_PRINTED.getExpression());
            doReturn(FormCode.ENG_CONFIRMATION.getCode()).when(tuple)
                .get(ReissueLetterService.DataType.FORM_CODE.getExpression());
            confirmationLetters.add(tuple);
            return confirmationLetters;
        }

        @Test
        @SuppressWarnings("PMD.LawOfDemeter")
        void reissueDeferralDeniedLetterListHappyPath() {
            String owner = "400";
            String jurorNumber = "123456789";

            TestUtils.setupAuthentication(owner, "Bureau", "1");

            final ReissueLetterListRequestDto reissueLetterListRequestDto = ReissueLetterListRequestDto.builder()
                .jurorNumber(jurorNumber)
                .letterType(LetterType.DEFERRAL_REFUSED)
                .build();

            final List<Tuple> deferralGrantedLetters = getDeferralDeniedLetters(jurorNumber);

            doReturn(deferralGrantedLetters).when(bulkPrintDataRepository)
                .findLetters(reissueLetterListRequestDto, LetterType.DEFERRAL_REFUSED.getLetterQueryConsumer());

            final ReissueLetterListResponseDto responseDto =
                reissueLetterService.reissueLetterList(reissueLetterListRequestDto);

            List<List<Object>> data = responseDto.getData();
            assertThat(data).isNotNull().hasSize(1);
            assertThat(data.get(0)).hasSize(10);

            verify(bulkPrintDataRepository, times(1))
                .findLetters(reissueLetterListRequestDto, LetterType.DEFERRAL_REFUSED.getLetterQueryConsumer());
        }

        @ParameterizedTest
        @ValueSource(strings = {"5229", "5229C"})
        void reissuePostponeLetterList(String formCode) {
            String owner = "400";
            final String jurorNumber = "555555561";

            TestUtils.setUpMockAuthentication(owner, "Bureau", "1", List.of("400"));

            final ReissueLetterListRequestDto reissueLetterListRequestDto = ReissueLetterListRequestDto.builder()
                .jurorNumber(jurorNumber)
                .letterType(LetterType.POSTPONED)
                .build();

            final List<Tuple> postponementLetters = getPostponementLetters(jurorNumber, formCode);

            doReturn(postponementLetters).when(bulkPrintDataRepository)
                .findLetters(reissueLetterListRequestDto, LetterType.POSTPONED.getLetterQueryConsumer());

            final ReissueLetterListResponseDto responseDto =
                reissueLetterService.reissueLetterList(reissueLetterListRequestDto);

            List<List<Object>> data = responseDto.getData();
            assertThat(data).isNotNull().hasSize(1);
            assertThat(data.get(0)).hasSize(10);

            assertThat(data.get(0).get(9))
                .as("Expect form code to be " + formCode)
                .isEqualTo(formCode);
            assertThat(data.get(0).get(4))
                .as("Expect status description to be Postponed")
                .isEqualTo("Postponed");

            verify(bulkPrintDataRepository, times(1))
                .findLetters(reissueLetterListRequestDto, LetterType.POSTPONED.getLetterQueryConsumer());
        }

        @Test
        @SuppressWarnings("PMD.LawOfDemeter")
        void reissueWithdrawalLetterListHappyPath() {
            String owner = "400";
            String jurorNumber = "123456789";

            TestUtils.setupAuthentication(owner, "Bureau", "1");

            final ReissueLetterListRequestDto reissueLetterListRequestDto = ReissueLetterListRequestDto.builder()
                .jurorNumber(jurorNumber)
                .letterType(LetterType.WITHDRAWAL)
                .build();

            final List<Tuple> getWithdrawalLetters = getWithdrawalLetters(jurorNumber);

            doReturn(getWithdrawalLetters).when(bulkPrintDataRepository)
                .findLetters(reissueLetterListRequestDto, LetterType.WITHDRAWAL.getLetterQueryConsumer());

            final ReissueLetterListResponseDto responseDto =
                reissueLetterService.reissueLetterList(reissueLetterListRequestDto);

            List<List<Object>> data = responseDto.getData();
            assertThat(data).isNotNull().hasSize(1);
            assertThat(data.get(0)).hasSize(10);

            verify(bulkPrintDataRepository, times(1))
                .findLetters(reissueLetterListRequestDto, LetterType.WITHDRAWAL.getLetterQueryConsumer());
        }

        private List<Tuple> getDeferralDeniedLetters(String jurorNumber) {
            final List<Tuple> deferralGrantedLetters = new ArrayList<>();
            Tuple tuple = mock(Tuple.class);
            doReturn(jurorNumber).when(tuple).get(ReissueLetterService.DataType.JUROR_NUMBER.getExpression());
            doReturn("FIRSTNAME").when(tuple).get(ReissueLetterService.DataType.JUROR_FIRST_NAME.getExpression());
            doReturn("LASTNAME").when(tuple).get(ReissueLetterService.DataType.JUROR_LAST_NAME.getExpression());
            doReturn("ABC 2DE").when(tuple).get(ReissueLetterService.DataType.JUROR_POSTCODE.getExpression());
            doReturn("Deferred").when(tuple).get(ReissueLetterService.DataType.JUROR_STATUS.getExpression());
            doReturn(LocalDateTime.now().minusDays(10)).when(tuple)
                .get(ReissueLetterService.DataType.JUROR_DEFERRAL_DATE_REFUSED.getExpression());
            doReturn("A").when(tuple).get(ReissueLetterService.DataType.JUROR_DEFERRAL_REJECTED_REASON.getExpression());
            doReturn(LocalDate.now().minusDays(10)).when(tuple)
                .get(ReissueLetterService.DataType.DATE_PRINTED.getExpression());
            doReturn("5226A").when(tuple).get(ReissueLetterService.DataType.FORM_CODE.getExpression());
            deferralGrantedLetters.add(tuple);
            return deferralGrantedLetters;
        }

        private List<Tuple> getPostponementLetters(String jurorNumber, String formCode) {
            final List<Tuple> postponeLetters = new ArrayList<>();
            Tuple tuple = mock(Tuple.class);
            doReturn(jurorNumber).when(tuple).get(ReissueLetterService.DataType.JUROR_NUMBER.getExpression());
            doReturn("FIRSTNAME").when(tuple).get(ReissueLetterService.DataType.JUROR_FIRST_NAME.getExpression());
            doReturn("LASTNAME").when(tuple).get(ReissueLetterService.DataType.JUROR_LAST_NAME.getExpression());
            doReturn("ABC 2DE").when(tuple).get(ReissueLetterService.DataType.JUROR_POSTCODE.getExpression());
            doReturn("Postponed").when(tuple).get(ReissueLetterService.DataType.JUROR_STATUS.getExpression());
            doReturn(LocalDateTime.now().minusDays(10)).when(tuple)
                .get(ReissueLetterService.DataType.JUROR_POSTPONED_TO.getExpression());
            doReturn("P").when(tuple).get(ReissueLetterService.DataType.JUROR_DEFERRED_TO_REASON.getExpression());
            doReturn(LocalDate.now().minusDays(10)).when(tuple)
                .get(ReissueLetterService.DataType.DATE_PRINTED.getExpression());
            doReturn(true).when(tuple).get(ReissueLetterService.DataType.EXTRACTED_FLAG.getExpression());
            doReturn(formCode).when(tuple).get(ReissueLetterService.DataType.FORM_CODE.getExpression());
            postponeLetters.add(tuple);
            return postponeLetters;
        }

        private List<Tuple> getWithdrawalLetters(String jurorNumber) {
            final List<Tuple> withdrawalLetters = new ArrayList<>();
            Tuple tuple = mock(Tuple.class);
            doReturn(jurorNumber).when(tuple).get(ReissueLetterService.DataType.JUROR_NUMBER.getExpression());
            doReturn("FIRSTNAME").when(tuple).get(ReissueLetterService.DataType.JUROR_FIRST_NAME.getExpression());
            doReturn("LASTNAME").when(tuple).get(ReissueLetterService.DataType.JUROR_LAST_NAME.getExpression());
            doReturn("ABC 2DE").when(tuple).get(ReissueLetterService.DataType.JUROR_POSTCODE.getExpression());
            doReturn("Deferred").when(tuple).get(ReissueLetterService.DataType.JUROR_STATUS.getExpression());
            doReturn(LocalDateTime.now().minusDays(10)).when(tuple)
                .get(ReissueLetterService.DataType.JUROR_WITHDRAWAL_DATE.getExpression());
            doReturn("A").when(tuple).get(ReissueLetterService.DataType.JUROR_WITHDRAWAL_REASON.getExpression());
            doReturn(LocalDate.now().minusDays(10)).when(tuple)
                .get(ReissueLetterService.DataType.DATE_PRINTED.getExpression());
            doReturn("5224").when(tuple).get(ReissueLetterService.DataType.FORM_CODE.getExpression());
            withdrawalLetters.add(tuple);
            return withdrawalLetters;
        }

        @Test
        @SuppressWarnings("PMD.LawOfDemeter")
        void reissueInitialSummonsLetterListHappyPath() {
            String owner = "400";
            String jurorNumber = "123456789";

            TestUtils.setUpMockAuthentication(owner, "Bureau", "1", List.of("400"));

            final ReissueLetterListRequestDto reissueLetterListRequestDto = ReissueLetterListRequestDto.builder()
                .jurorNumber(jurorNumber)
                .letterType(LetterType.SUMMONS)
                .build();

            final List<Tuple> initialSummonsLetters = getInitialSummonsLetters(jurorNumber);

            doReturn(initialSummonsLetters).when(bulkPrintDataRepository)
                .findLetters(reissueLetterListRequestDto, LetterType.SUMMONS.getLetterQueryConsumer());

            final ReissueLetterListResponseDto responseDto =
                reissueLetterService.reissueLetterList(reissueLetterListRequestDto);

            List<List<Object>> data = responseDto.getData();
            assertThat(data).isNotNull().hasSize(1);
            assertThat(data.get(0)).hasSize(7);

            verify(bulkPrintDataRepository, times(1))
                .findLetters(reissueLetterListRequestDto, LetterType.SUMMONS.getLetterQueryConsumer());
        }

        private List<Tuple> getInitialSummonsLetters(String jurorNumber) {
            final List<Tuple> initialSummonsLetters = new ArrayList<>();
            Tuple tuple = mock(Tuple.class);
            doReturn(jurorNumber).when(tuple).get(ReissueLetterService.DataType.JUROR_NUMBER.getExpression());
            doReturn("FIRSTNAME").when(tuple).get(ReissueLetterService.DataType.JUROR_FIRST_NAME.getExpression());
            doReturn("LASTNAME").when(tuple).get(ReissueLetterService.DataType.JUROR_LAST_NAME.getExpression());
            doReturn("ABC 2DE").when(tuple).get(ReissueLetterService.DataType.JUROR_POSTCODE.getExpression());
            doReturn(LocalDate.now().minusDays(2)).when(tuple)
                .get(ReissueLetterService.DataType.DATE_PRINTED.getExpression());
            doReturn("5221").when(tuple).get(ReissueLetterService.DataType.FORM_CODE.getExpression());
            initialSummonsLetters.add(tuple);
            return initialSummonsLetters;
        }
    }

    @Nested
    @DisplayName("Reissue Letter Tests")
    class ReissueLetterTests {

        @ParameterizedTest
        @ValueSource(strings = {"5229A", "5229AC"})
        void reissueDeferralLetterHappyPath(String formCode) {
            String owner = "400";

            TestUtils.setUpMockAuthentication(owner, "Bureau", "1", List.of("400"));

            final ReissueLetterRequestDto.ReissueLetterRequestData
                reissueLetterRequestData = getReissueLetterRequestData(formCode);

            final ReissueLetterRequestDto reissueLetterRequestDto =
                getReissueLetterRequestDto(reissueLetterRequestData);

            final BulkPrintData bulkPrintData = getBulkPrintData(reissueLetterRequestData);

            doReturn(Optional.of(bulkPrintData)).when(bulkPrintDataRepository)
                .findByJurorNumberFormCodeDatePrinted(reissueLetterRequestData.getJurorNumber(),
                    reissueLetterRequestData.getFormCode(), reissueLetterRequestData.getDatePrinted());

            doReturn(Optional.empty()).when(bulkPrintDataRepository)
                .findByJurorNumberFormCodeAndPending(reissueLetterRequestData.getJurorNumber(),
                    reissueLetterRequestData.getFormCode());

            JurorStatus deferredStatus = new JurorStatus();
            when(jurorStatusRepository.findById(IJurorStatus.DEFERRED))
                .thenReturn(Optional.ofNullable(deferredStatus));

            List<JurorPool> jurorPools = getJurorPools();
            when(jurorPoolRepository.findByJurorJurorNumberAndStatusOrderByDateCreatedDesc(
                reissueLetterRequestData.getJurorNumber(), deferredStatus)).thenReturn(jurorPools);

            doReturn(jurorPools).when(jurorPoolRepository)
                .findByJurorJurorNumberAndStatusOrderByDateCreatedDesc(reissueLetterRequestData.getJurorNumber(),
                    deferredStatus);

            reissueLetterService.reissueLetter(reissueLetterRequestDto);

            verify(bulkPrintDataRepository, times(1))
                .findByJurorNumberFormCodeDatePrinted(reissueLetterRequestData.getJurorNumber(),
                    reissueLetterRequestData.getFormCode(), reissueLetterRequestData.getDatePrinted());

            verify(bulkPrintDataRepository, times(1))
                .findByJurorNumberFormCodeAndPending(reissueLetterRequestData.getJurorNumber(),
                    reissueLetterRequestData.getFormCode());

            verify(jurorStatusRepository, times(1)).findById(IJurorStatus.DEFERRED);
            verify(jurorPoolRepository, times(1))
                .findByJurorJurorNumberAndStatusOrderByDateCreatedDesc(reissueLetterRequestData.getJurorNumber(),
                    deferredStatus);
        }

        @ParameterizedTest
        @ValueSource(strings = {"5224", "5224C"})
        void reissueWithdrawalLetterHappyPath(String formCode) {
            String owner = "400";

            TestUtils.setUpMockAuthentication(owner, "Bureau", "1", List.of("400"));

            final ReissueLetterRequestDto.ReissueLetterRequestData
                reissueLetterRequestData = getReissueLetterRequestData(formCode);

            final ReissueLetterRequestDto reissueLetterRequestDto =
                getReissueLetterRequestDto(reissueLetterRequestData);

            final BulkPrintData bulkPrintData = getBulkPrintData(reissueLetterRequestData);

            doReturn(Optional.of(bulkPrintData)).when(bulkPrintDataRepository)
                .findByJurorNumberFormCodeDatePrinted(reissueLetterRequestData.getJurorNumber(),
                    reissueLetterRequestData.getFormCode(), reissueLetterRequestData.getDatePrinted());

            doReturn(Optional.empty()).when(bulkPrintDataRepository)
                .findByJurorNumberFormCodeAndPending(reissueLetterRequestData.getJurorNumber(),
                    reissueLetterRequestData.getFormCode());

            JurorStatus withdrawalStatus = new JurorStatus();
            when(jurorStatusRepository.findById(IJurorStatus.DISQUALIFIED))
                .thenReturn(Optional.ofNullable(withdrawalStatus));

            List<JurorPool> jurorPools = getJurorPools();
            when(jurorPoolRepository.findByJurorJurorNumberAndStatusOrderByDateCreatedDesc(
                reissueLetterRequestData.getJurorNumber(), withdrawalStatus)).thenReturn(jurorPools);

            doReturn(jurorPools).when(jurorPoolRepository)
                .findByJurorJurorNumberAndStatusOrderByDateCreatedDesc(reissueLetterRequestData.getJurorNumber(),
                    withdrawalStatus);

            reissueLetterService.reissueLetter(reissueLetterRequestDto);

            verify(bulkPrintDataRepository, times(1))
                .findByJurorNumberFormCodeDatePrinted(reissueLetterRequestData.getJurorNumber(),
                    reissueLetterRequestData.getFormCode(), reissueLetterRequestData.getDatePrinted());

            verify(bulkPrintDataRepository, times(1))
                .findByJurorNumberFormCodeAndPending(reissueLetterRequestData.getJurorNumber(),
                    reissueLetterRequestData.getFormCode());

            verify(jurorStatusRepository, times(1)).findById(IJurorStatus.DISQUALIFIED);
            verify(jurorPoolRepository, times(1))
                .findByJurorJurorNumberAndStatusOrderByDateCreatedDesc(reissueLetterRequestData.getJurorNumber(),
                    withdrawalStatus);
        }

        @ParameterizedTest
        @ValueSource(strings = {"5229A", "5229AC"})
        void reissueLetterUnhappyAlreadyPending(String formCode) {
            String owner = "400";

            TestUtils.setUpMockAuthentication(owner, "Bureau", "1", List.of("400"));

            final ReissueLetterRequestDto.ReissueLetterRequestData
                reissueLetterRequestData = getReissueLetterRequestData(formCode);

            final BulkPrintData bulkPrintData = getBulkPrintData(reissueLetterRequestData);

            ReissueLetterRequestDto reissueLetterRequestDto = ReissueLetterRequestDto.builder()
                .letters(List.of(reissueLetterRequestData))
                .build();

            doReturn(Optional.ofNullable(bulkPrintData)).when(bulkPrintDataRepository)
                .findByJurorNumberFormCodeDatePrinted(reissueLetterRequestData.getJurorNumber(),
                    reissueLetterRequestData.getFormCode(), reissueLetterRequestData.getDatePrinted());

            doReturn(Optional.ofNullable(bulkPrintData)).when(bulkPrintDataRepository)
                .findByJurorNumberFormCodeAndPending(reissueLetterRequestData.getJurorNumber(),
                    reissueLetterRequestData.getFormCode());

            assertThatExceptionOfType(MojException.BadRequest.class).isThrownBy(() ->
                reissueLetterService.reissueLetter(reissueLetterRequestDto));

            verify(bulkPrintDataRepository, times(1))
                .findByJurorNumberFormCodeDatePrinted(reissueLetterRequestData.getJurorNumber(),
                    reissueLetterRequestData.getFormCode(), reissueLetterRequestData.getDatePrinted());
            verify(bulkPrintDataRepository, times(1))
                .findByJurorNumberFormCodeAndPending(reissueLetterRequestData.getJurorNumber(),
                    reissueLetterRequestData.getFormCode());
            verify(jurorStatusRepository, times(0)).findById(Mockito.anyInt());
            verify(jurorPoolRepository, times(0))
                .findByJurorJurorNumberAndStatusOrderByDateCreatedDesc(Mockito.anyString(), Mockito.any());
        }

        @ParameterizedTest
        @ValueSource(strings = {"5229A", "5229AC"})
        void reissueLetterUnhappyNotFound(String formCode) {
            String owner = "400";

            TestUtils.setUpMockAuthentication(owner, "Bureau", "1", List.of("400"));

            final ReissueLetterRequestDto.ReissueLetterRequestData
                reissueLetterRequestData = getReissueLetterRequestData(formCode);

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
            verify(bulkPrintDataRepository, times(0))
                .findByJurorNumberFormCodeAndPending(reissueLetterRequestData.getJurorNumber(),
                    reissueLetterRequestData.getFormCode());
            verify(jurorStatusRepository, times(0)).findById(Mockito.anyInt());
            verify(jurorPoolRepository, times(0))
                .findByJurorJurorNumberAndStatusOrderByDateCreatedDesc(Mockito.anyString(), Mockito.any());
        }

        @ParameterizedTest
        @ValueSource(strings = {"5229", "5229C"})
        void reissuePostponeLetterHappyPath(String formCode) {
            String owner = "400";

            TestUtils.setUpMockAuthentication(owner, "Bureau", "1", List.of("400"));

            final ReissueLetterRequestDto.ReissueLetterRequestData
                reissueLetterRequestData = getReissueLetterRequestData(formCode);

            final ReissueLetterRequestDto reissueLetterRequestDto =
                getReissueLetterRequestDto(reissueLetterRequestData);

            final BulkPrintData bulkPrintData = getBulkPrintData(reissueLetterRequestData);

            doReturn(Optional.of(bulkPrintData)).when(bulkPrintDataRepository)
                .findByJurorNumberFormCodeDatePrinted(reissueLetterRequestData.getJurorNumber(),
                    reissueLetterRequestData.getFormCode(), reissueLetterRequestData.getDatePrinted());

            doReturn(Optional.empty()).when(bulkPrintDataRepository)
                .findByJurorNumberFormCodeAndPending(reissueLetterRequestData.getJurorNumber(),
                    reissueLetterRequestData.getFormCode());

            JurorStatus postponedStatus = new JurorStatus();
            when(jurorStatusRepository.findById(IJurorStatus.DEFERRED))
                .thenReturn(Optional.ofNullable(postponedStatus));

            List<JurorPool> jurorPools = getJurorPools();
            when(jurorPoolRepository.findByJurorJurorNumberAndStatusOrderByDateCreatedDesc(
                reissueLetterRequestData.getJurorNumber(), postponedStatus)).thenReturn(jurorPools);

            doReturn(jurorPools).when(jurorPoolRepository)
                .findByJurorJurorNumberAndStatusOrderByDateCreatedDesc(reissueLetterRequestData.getJurorNumber(),
                    postponedStatus);

            reissueLetterService.reissueLetter(reissueLetterRequestDto);

            verify(bulkPrintDataRepository, times(1))
                .findByJurorNumberFormCodeDatePrinted(reissueLetterRequestData.getJurorNumber(),
                    reissueLetterRequestData.getFormCode(), reissueLetterRequestData.getDatePrinted());

            verify(bulkPrintDataRepository, times(1))
                .findByJurorNumberFormCodeAndPending(reissueLetterRequestData.getJurorNumber(),
                    reissueLetterRequestData.getFormCode());

            verify(jurorStatusRepository, times(1)).findById(IJurorStatus.DEFERRED);
            verify(jurorPoolRepository, times(1))
                .findByJurorJurorNumberAndStatusOrderByDateCreatedDesc(reissueLetterRequestData.getJurorNumber(),
                    postponedStatus);
        }

        @ParameterizedTest
        @ValueSource(strings = {"5221", "5221C"})
        void reissueInitialSummonsLetterHappyPath(String formCode) {
            String owner = "400";

            TestUtils.setUpMockAuthentication(owner, "Bureau", "1", List.of("400"));

            final ReissueLetterRequestDto.ReissueLetterRequestData
                reissueLetterRequestData = getReissueLetterRequestData(formCode);

            final ReissueLetterRequestDto reissueLetterRequestDto =
                getReissueLetterRequestDto(reissueLetterRequestData);

            final BulkPrintData bulkPrintData = getBulkPrintData(reissueLetterRequestData);

            doReturn(Optional.of(bulkPrintData)).when(bulkPrintDataRepository)
                .findByJurorNumberFormCodeDatePrinted(reissueLetterRequestData.getJurorNumber(),
                    reissueLetterRequestData.getFormCode(), reissueLetterRequestData.getDatePrinted());

            doReturn(Optional.empty()).when(bulkPrintDataRepository)
                .findByJurorNumberFormCodeAndPending(reissueLetterRequestData.getJurorNumber(),
                    reissueLetterRequestData.getFormCode());

            JurorStatus summoned = new JurorStatus();
            when(jurorStatusRepository.findById(IJurorStatus.SUMMONED))
                .thenReturn(Optional.ofNullable(summoned));

            List<JurorPool> jurorPools = getJurorPools();
            when(jurorPoolRepository.findByJurorJurorNumberAndStatusOrderByDateCreatedDesc(
                reissueLetterRequestData.getJurorNumber(), summoned)).thenReturn(jurorPools);

            doReturn(jurorPools).when(jurorPoolRepository)
                .findByJurorJurorNumberAndStatusOrderByDateCreatedDesc(reissueLetterRequestData.getJurorNumber(),
                    summoned);

            reissueLetterService.reissueLetter(reissueLetterRequestDto);

            verify(bulkPrintDataRepository, times(1))
                .findByJurorNumberFormCodeDatePrinted(reissueLetterRequestData.getJurorNumber(),
                    reissueLetterRequestData.getFormCode(), reissueLetterRequestData.getDatePrinted());

            verify(bulkPrintDataRepository, times(1))
                .findByJurorNumberFormCodeAndPending(reissueLetterRequestData.getJurorNumber(),
                    reissueLetterRequestData.getFormCode());

            verify(jurorStatusRepository, times(1)).findById(IJurorStatus.SUMMONED);
            verify(jurorPoolRepository, times(1))
                .findByJurorJurorNumberAndStatusOrderByDateCreatedDesc(reissueLetterRequestData.getJurorNumber(),
                    summoned);
        }

        @ParameterizedTest
        @ValueSource(strings = {"5228", "5228C"})
        void reissueInitialSummonsReminderLetterHistory(String formCode) {
            String owner = "400";

            TestUtils.setUpMockAuthentication(owner, "Bureau", "1", List.of("400"));

            final ReissueLetterRequestDto.ReissueLetterRequestData
                reissueLetterRequestData = getReissueLetterRequestData(formCode);

            final ReissueLetterRequestDto reissueLetterRequestDto =
                getReissueLetterRequestDto(reissueLetterRequestData);

            final BulkPrintData bulkPrintData = getBulkPrintData(reissueLetterRequestData);

            doReturn(Optional.of(bulkPrintData)).when(bulkPrintDataRepository)
                .findByJurorNumberFormCodeDatePrinted(reissueLetterRequestData.getJurorNumber(),
                    reissueLetterRequestData.getFormCode(), reissueLetterRequestData.getDatePrinted());

            doReturn(Optional.empty()).when(bulkPrintDataRepository)
                .findByJurorNumberFormCodeAndPending(reissueLetterRequestData.getJurorNumber(),
                    reissueLetterRequestData.getFormCode());

            JurorStatus summoned = new JurorStatus();
            when(jurorStatusRepository.findById(IJurorStatus.SUMMONED))
                .thenReturn(Optional.ofNullable(summoned));

            List<JurorPool> jurorPools = getJurorPools();
            when(jurorPoolRepository.findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc("555555561", true))
                .thenReturn(jurorPools);

            doReturn(jurorPools).when(jurorPoolRepository)
                .findByJurorJurorNumberAndStatusOrderByDateCreatedDesc(reissueLetterRequestData.getJurorNumber(),
                    summoned);

            doNothing().when(jurorHistoryService).createSummonsReminderLetterHistory(jurorPools.get(0));

            reissueLetterService.reissueLetter(reissueLetterRequestDto);

            verify(bulkPrintDataRepository, times(1))
                .findByJurorNumberFormCodeDatePrinted(reissueLetterRequestData.getJurorNumber(),
                    reissueLetterRequestData.getFormCode(), reissueLetterRequestData.getDatePrinted());

            verify(bulkPrintDataRepository, times(1))
                .findByJurorNumberFormCodeAndPending(reissueLetterRequestData.getJurorNumber(),
                    reissueLetterRequestData.getFormCode());

            verify(jurorStatusRepository, times(1)).findById(IJurorStatus.SUMMONED);
            verify(jurorPoolRepository, times(1))
                .findByJurorJurorNumberAndStatusOrderByDateCreatedDesc(reissueLetterRequestData.getJurorNumber(),
                    summoned);
            verify(jurorHistoryService, times(1))
                .createSummonsReminderLetterHistory(jurorPools.get(0));
        }

        private static BulkPrintData getBulkPrintData(
            ReissueLetterRequestDto.ReissueLetterRequestData reissueLetterRequestData) {
            final BulkPrintData bulkPrintData = BulkPrintData.builder()
                .jurorNo(reissueLetterRequestData.getJurorNumber())
                .formAttribute(FormAttribute.builder().formType(reissueLetterRequestData.getFormCode()).build())
                .creationDate(reissueLetterRequestData.getDatePrinted())
                .build();
            return bulkPrintData;
        }

        private static ReissueLetterRequestDto getReissueLetterRequestDto(
            ReissueLetterRequestDto.ReissueLetterRequestData reissueLetterRequestData) {
            final ReissueLetterRequestDto reissueLetterRequestDto = ReissueLetterRequestDto.builder()
                .letters(List.of(reissueLetterRequestData))
                .build();
            return reissueLetterRequestDto;
        }

        @Test
        void reissueInitialSummonsReminderLetterHistoryNotImplementedForFormCode() {
            String owner = "400";

            TestUtils.setUpMockAuthentication(owner, "Bureau", "1", List.of("400"));

            final ReissueLetterRequestDto.ReissueLetterRequestData
                reissueLetterRequestData = getReissueLetterRequestData("5221");

            final ReissueLetterRequestDto reissueLetterRequestDto =
                getReissueLetterRequestDto(reissueLetterRequestData);

            final BulkPrintData bulkPrintData = getBulkPrintData(reissueLetterRequestData);

            doReturn(Optional.of(bulkPrintData)).when(bulkPrintDataRepository)
                .findByJurorNumberFormCodeDatePrinted(reissueLetterRequestData.getJurorNumber(),
                    reissueLetterRequestData.getFormCode(), reissueLetterRequestData.getDatePrinted());

            doReturn(Optional.empty()).when(bulkPrintDataRepository)
                .findByJurorNumberFormCodeAndPending(reissueLetterRequestData.getJurorNumber(),
                    reissueLetterRequestData.getFormCode());

            JurorStatus summoned = new JurorStatus();
            when(jurorStatusRepository.findById(IJurorStatus.SUMMONED))
                .thenReturn(Optional.ofNullable(summoned));

            List<JurorPool> jurorPools = getJurorPools();
            when(jurorPoolRepository.findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc("555555561", true))
                .thenReturn(jurorPools);

            doReturn(jurorPools).when(jurorPoolRepository)
                .findByJurorJurorNumberAndStatusOrderByDateCreatedDesc(reissueLetterRequestData.getJurorNumber(),
                    summoned);

            doNothing().when(jurorHistoryService).createSummonsReminderLetterHistory(jurorPools.get(0));

            reissueLetterService.reissueLetter(reissueLetterRequestDto);

            verify(bulkPrintDataRepository, times(1))
                .findByJurorNumberFormCodeDatePrinted(reissueLetterRequestData.getJurorNumber(),
                    reissueLetterRequestData.getFormCode(), reissueLetterRequestData.getDatePrinted());

            verify(bulkPrintDataRepository, times(1))
                .findByJurorNumberFormCodeAndPending(reissueLetterRequestData.getJurorNumber(),
                    reissueLetterRequestData.getFormCode());

            verify(jurorStatusRepository, times(1)).findById(IJurorStatus.SUMMONED);
            verify(jurorPoolRepository, times(1))
                .findByJurorJurorNumberAndStatusOrderByDateCreatedDesc(reissueLetterRequestData.getJurorNumber(),
                    summoned);
            verifyNoMoreInteractions(jurorHistoryService);
        }
    }

    @Nested
    @DisplayName("Delete Letter Tests")
    class DeleteLetterTests {

        @ParameterizedTest
        @ValueSource(strings = {"5229A", "5229AC"})
        void deleteLetterHappyPath(String formCode) {
            String owner = "400";

            TestUtils.setUpMockAuthentication(owner, "Bureau", "1", List.of("400"));

            final ReissueLetterRequestDto.ReissueLetterRequestData
                reissueLetterRequestData = getReissueLetterRequestData(formCode);

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

        @ParameterizedTest
        @ValueSource(strings = {"5229A", "5229AC"})
        void deleteLetterUnhappyNoData(String formCode) {
            String owner = "400";

            TestUtils.setUpMockAuthentication(owner, "Bureau", "1", List.of("400"));

            final ReissueLetterRequestDto.ReissueLetterRequestData
                reissueLetterRequestData = getReissueLetterRequestData(formCode);

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

    private static List<JurorPool> getJurorPools() {
        JurorPool jurorPool = mock(JurorPool.class);
        List<JurorPool> jurorPools = new ArrayList<>();
        jurorPools.add(jurorPool);
        return jurorPools;
    }

    private static ReissueLetterRequestDto.ReissueLetterRequestData getReissueLetterRequestData(String formCode) {
        final ReissueLetterRequestDto.ReissueLetterRequestData reissueLetterRequestData =
            ReissueLetterRequestDto.ReissueLetterRequestData.builder()
                .jurorNumber("555555561")
                .formCode(formCode)
                .datePrinted(LocalDate.now().minusDays(1))
                .build();
        return reissueLetterRequestData;
    }
}
