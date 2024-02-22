package uk.gov.hmcts.juror.api.moj.service.letter;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import uk.gov.hmcts.juror.api.moj.controller.request.letter.court.CourtLetterListRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.LetterListResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.LetterResponseData;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.PostponeLetterData;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.PostponedLetterList;
import uk.gov.hmcts.juror.api.moj.enumeration.letter.CourtLetterType;
import uk.gov.hmcts.juror.api.moj.repository.letter.court.PostponementLetterListRepository;
import uk.gov.hmcts.juror.api.moj.service.letter.court.CourtPostponementLetterServiceImpl;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("PMD.LawOfDemeter")
class CourtPostponementLetterServiceImplTest {

    private PostponementLetterListRepository postponementLetterListRepository;
    private CourtPostponementLetterServiceImpl courtPostponementLetterService;

    private MockedStatic<SecurityUtil> securityUtilMockedStatic;

    @BeforeEach
    void beforeEach() {
        this.postponementLetterListRepository = mock(PostponementLetterListRepository.class);
        this.courtPostponementLetterService = new CourtPostponementLetterServiceImpl(postponementLetterListRepository);

        Authentication auth = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void afterEach() {
        if (securityUtilMockedStatic != null) {
            securityUtilMockedStatic.close();
        }
    }

    private void mockCurrentUser(String owner) {
        securityUtilMockedStatic = Mockito.mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getActiveOwner)
            .thenReturn(owner);
    }

    @Nested
    @DisplayName("getEligibleList - Postponed")
    class Postponed {

        @Test
        @DisplayName("getEligibleList - exclude printed")
        @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
        void eligibleListExcludePrinted() {
            String owner = "415";
            mockCurrentUser(owner);

            String jurorNumber = "641500001";
            String poolNumber = "415240101";
            String reason = "Postpone";
            LocalDate postponedTo = LocalDate.of(2024, 1, 29);
            boolean includePrinted = false;

            List<PostponedLetterList> results =
                List.of(createPostponedLetterList(jurorNumber, postponedTo, reason, poolNumber));

            when(postponementLetterListRepository.findJurorsEligibleForPostponementLetter(any(),
                any())).thenReturn(results);

            CourtLetterListRequestDto requestDto = CourtLetterListRequestDto.builder()
                .letterType(CourtLetterType.POSTPONED)
                .jurorNumber(jurorNumber)
                .includePrinted(includePrinted)
                .build();

            LetterListResponseDto response = courtPostponementLetterService.getEligibleList(requestDto);

            List<String> headings = response.getHeadings();
            validateHeadings(headings);

            List<String> dataTypes = response.getDataTypes();
            validateDataTypes(dataTypes);

            List<? extends LetterResponseData> data = response.getData();
            validateData((PostponeLetterData) data.get(0), jurorNumber, postponedTo, reason, poolNumber, null);
        }

        @Test
        @DisplayName("getEligibleList - include printed")
        @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
        void eligibleListIncludePrinted() {
            String owner = "415";
            mockCurrentUser(owner);

            String jurorNumber = "641500001";
            String poolNumber = "415240101";
            String reason = "Moved from area";
            LocalDate postponedTo = LocalDate.of(2024, 1, 29);
            LocalDateTime datePrinted = LocalDateTime.of(2024, 1, 22, 12, 35);
            boolean includePrinted = true;

            List<PostponedLetterList> results =
                List.of(createPostponedLetterList(jurorNumber, postponedTo, reason, poolNumber,
                    datePrinted));

            when(postponementLetterListRepository.findJurorsEligibleForPostponementLetter(any(),
                any())).thenReturn(results);

            CourtLetterListRequestDto requestDto = CourtLetterListRequestDto.builder()
                .letterType(CourtLetterType.POSTPONED)
                .jurorNumber(jurorNumber)
                .includePrinted(includePrinted)
                .build();

            LetterListResponseDto response = courtPostponementLetterService.getEligibleList(requestDto);

            List<String> headings = response.getHeadings();
            validateHeadings(headings);

            List<String> dataTypes = response.getDataTypes();
            validateDataTypes(dataTypes);

            List<? extends LetterResponseData> data = response.getData();
            validateData((PostponeLetterData) data.get(0), jurorNumber, postponedTo, reason, poolNumber,
                datePrinted.toLocalDate());
        }

        @Test
        @DisplayName("getEligibleList - no data")
        @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
        void eligibleListNoData() {
            String owner = "415";
            mockCurrentUser(owner);

            String jurorNumber = "641500001";
            boolean includePrinted = false;

            List<PostponedLetterList> results = new ArrayList<>();

            when(postponementLetterListRepository.findJurorsEligibleForPostponementLetter(any(),
                any())).thenReturn(results);

            CourtLetterListRequestDto requestDto = CourtLetterListRequestDto.builder()
                .letterType(CourtLetterType.POSTPONED)
                .jurorNumber(jurorNumber)
                .includePrinted(includePrinted)
                .build();

            LetterListResponseDto response = courtPostponementLetterService.getEligibleList(requestDto);

            List<String> headings = response.getHeadings();
            validateHeadings(headings);

            List<String> dataTypes = response.getDataTypes();
            validateDataTypes(dataTypes);

            Assertions.assertThat(response.getData()).isEmpty();
        }

        private PostponedLetterList createPostponedLetterList(String jurorNumber, LocalDate postponedTo, String reason,
                                                              String poolNumber) {
            return createPostponedLetterList(jurorNumber, postponedTo, reason, poolNumber, null);
        }

        private PostponedLetterList createPostponedLetterList(String jurorNumber, LocalDate postponedTo, String reason,
                                                              String poolNumber, LocalDateTime datePrinted) {
            return PostponedLetterList.builder()
                .jurorNumber(jurorNumber)
                .firstName("Test")
                .lastName("Person")
                .postcode("CH1 2AN")
                .status("postponed")
                .deferralDate(postponedTo)
                .deferralReason(reason)
                .poolNumber(poolNumber)
                .datePrinted(datePrinted)
                .build();
        }

        private void validateHeadings(List<String> headings) {
            Assertions.assertThat(headings.size()).isEqualTo(9);
            Assertions.assertThat(headings.get(0)).isEqualToIgnoringCase("Juror number");
            Assertions.assertThat(headings.get(1)).isEqualToIgnoringCase("First name");
            Assertions.assertThat(headings.get(2)).isEqualToIgnoringCase("Last name");
            Assertions.assertThat(headings.get(3)).isEqualToIgnoringCase("Postcode");
            Assertions.assertThat(headings.get(4)).isEqualToIgnoringCase("Status");
            Assertions.assertThat(headings.get(5)).isEqualToIgnoringCase("Postponed to");
            Assertions.assertThat(headings.get(6)).isEqualToIgnoringCase("Reason");
            Assertions.assertThat(headings.get(7)).isEqualToIgnoringCase("Date printed");
            Assertions.assertThat(headings.get(8)).isEqualToIgnoringCase("Pool Number");
        }

        private void validateDataTypes(List<String> dataTypes) {
            Assertions.assertThat(dataTypes.size()).isEqualTo(9);
            Assertions.assertThat(dataTypes.get(0)).isEqualToIgnoringCase("string");
            Assertions.assertThat(dataTypes.get(1)).isEqualToIgnoringCase("string");
            Assertions.assertThat(dataTypes.get(2)).isEqualToIgnoringCase("string");
            Assertions.assertThat(dataTypes.get(3)).isEqualToIgnoringCase("string");
            Assertions.assertThat(dataTypes.get(4)).isEqualToIgnoringCase("string");
            Assertions.assertThat(dataTypes.get(5)).isEqualToIgnoringCase("date");
            Assertions.assertThat(dataTypes.get(6)).isEqualToIgnoringCase("string");
            Assertions.assertThat(dataTypes.get(7)).isEqualToIgnoringCase("date");
            Assertions.assertThat(dataTypes.get(8)).isEqualToIgnoringCase("hidden");
        }

        private void validateData(PostponeLetterData data, String jurorNumber, LocalDate postponedTo, String reason,
                                  String poolNumber, LocalDate datePrinted) {
            Assertions.assertThat(data.getJurorNumber()).isEqualToIgnoringCase(jurorNumber);
            Assertions.assertThat(data.getFirstName()).isEqualToIgnoringCase("Test");
            Assertions.assertThat(data.getLastName()).isEqualToIgnoringCase("Person");
            Assertions.assertThat(data.getPostcode()).isEqualToIgnoringCase("CH1 2AN");
            Assertions.assertThat(data.getStatus()).isEqualToIgnoringCase("Postponed");
            Assertions.assertThat(data.getPostponedTo()).isEqualTo(postponedTo);
            Assertions.assertThat(data.getReason()).isEqualToIgnoringCase(reason);
            Assertions.assertThat(data.getDatePrinted()).isEqualTo(datePrinted);
            Assertions.assertThat(data.getPoolNumber()).isEqualToIgnoringCase(poolNumber);
        }
    }
}