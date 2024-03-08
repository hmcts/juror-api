package uk.gov.hmcts.juror.api.moj.service.letter.court;

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
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.FailedToAttendLetterData;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.LetterListResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.LetterResponseData;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.FailedToAttendLetterList;
import uk.gov.hmcts.juror.api.moj.enumeration.letter.CourtLetterType;
import uk.gov.hmcts.juror.api.moj.repository.letter.court.FailedToAttendLetterListRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CourtFailedToAttendLetterServiceImplTest {
    private FailedToAttendLetterListRepository failedToAttendLetterListRepository;
    private CourtFailedToAttendLetterServiceImpl courtFailedToAttendLetterService;

    private MockedStatic<SecurityUtil> securityUtilMockedStatic;

    @BeforeEach
    void beforeEach() {
        this.failedToAttendLetterListRepository = mock(FailedToAttendLetterListRepository.class);
        this.courtFailedToAttendLetterService =
            new CourtFailedToAttendLetterServiceImpl(failedToAttendLetterListRepository);

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
    @DisplayName("getEligibleList - Failed To Attend")
    class FailedToAttend {

        static final String JUROR_5555555 = "5555555";
        static final String POOL_NUMBER_6666666 = "6666666";
        static final String OWNER = "415";

        @Test
        @DisplayName("getEligibleList - exclude printed")
        @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
        void eligibleListExcludePrinted() {
            mockCurrentUser(OWNER);

            List<FailedToAttendLetterList> results = List.of(createFailedToAttendLetterList("61",
                LocalDateTime.now().minusDays(8), LocalDate.now().minusDays(10)));

            when(failedToAttendLetterListRepository.findJurorsEligibleForFailedToAttendLetter(any(), any()))
                .thenReturn(results);

            CourtLetterListRequestDto requestDto = CourtLetterListRequestDto.builder()
                .letterType(CourtLetterType.FAILED_TO_ATTEND)
                .jurorNumber(JUROR_5555555 + "61")
                .includePrinted(Boolean.FALSE)
                .build();

            LetterListResponseDto response = courtFailedToAttendLetterService.getEligibleList(requestDto);

            List<String> headings = response.getHeadings();
            validateHeadings(headings);

            List<String> dataTypes = response.getDataTypes();
            validateDataTypes(dataTypes);

            List<? extends LetterResponseData> data = response.getData();
            validateData((FailedToAttendLetterData) data.get(0), "61", null, LocalDate.now().minusDays(10));
        }

        @Test
        @DisplayName("getEligibleList - include printed")
        @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
        void eligibleListIncludePrinted() {
            mockCurrentUser(OWNER);

            List<FailedToAttendLetterList> results = List.of(createFailedToAttendLetterList("61",
                LocalDateTime.now().minusDays(8), LocalDate.now().minusDays(10)));

            when(failedToAttendLetterListRepository.findJurorsEligibleForFailedToAttendLetter(any(), any()))
                .thenReturn(results);

            CourtLetterListRequestDto requestDto = CourtLetterListRequestDto.builder()
                .letterType(CourtLetterType.FAILED_TO_ATTEND)
                .jurorNumber(JUROR_5555555 + "61")
                .includePrinted(Boolean.TRUE)
                .build();

            LetterListResponseDto response = courtFailedToAttendLetterService.getEligibleList(requestDto);

            List<String> headings = response.getHeadings();
            validateHeadings(headings);

            List<String> dataTypes = response.getDataTypes();
            validateDataTypes(dataTypes);

            List<? extends LetterResponseData> data = response.getData();
            validateData((FailedToAttendLetterData) data.get(0), "61",
                LocalDate.now().minusDays(8), LocalDate.now().minusDays(10));
        }

        @Test
        @DisplayName("getEligibleList - no data")
        @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
        void eligibleListNoData() {
            mockCurrentUser(OWNER);

            List<FailedToAttendLetterList> results = new ArrayList<>();

            when(failedToAttendLetterListRepository.findJurorsEligibleForFailedToAttendLetter(any(), any()))
                .thenReturn(results);

            CourtLetterListRequestDto requestDto = CourtLetterListRequestDto.builder()
                .letterType(CourtLetterType.FAILED_TO_ATTEND)
                .jurorNumber(JUROR_5555555 + "63")
                .includePrinted(Boolean.FALSE)
                .build();

            LetterListResponseDto response = courtFailedToAttendLetterService.getEligibleList(requestDto);

            List<String> headings = response.getHeadings();
            validateHeadings(headings);

            List<String> dataTypes = response.getDataTypes();
            validateDataTypes(dataTypes);

            assertThat(response.getData().isEmpty());
        }

        private FailedToAttendLetterList createFailedToAttendLetterList(String jurorNumberPostfix,
                                                                        LocalDateTime dateLetterPrinted,
                                                                        LocalDate absentDate) {
            return FailedToAttendLetterList.builder()
                .jurorNumber(JUROR_5555555 + jurorNumberPostfix)
                .firstName("FirstName")
                .lastName("LastName")
                .postcode("CH1 2AN")
                .status("responded")
                .poolNumber(POOL_NUMBER_6666666 + jurorNumberPostfix)
                .datePrinted(dateLetterPrinted)
                .absentDate(absentDate)
                .build();
        }

        private void validateHeadings(List<String> headings) {
            assertThat(headings.size()).isEqualTo(5);
            assertThat(headings.get(0)).isEqualToIgnoringCase("Juror number");
            assertThat(headings.get(1)).isEqualToIgnoringCase("First name");
            assertThat(headings.get(2)).isEqualToIgnoringCase("Last name");
            assertThat(headings.get(3)).isEqualToIgnoringCase("Absent date");
            assertThat(headings.get(4)).isEqualToIgnoringCase("Date printed");
        }

        private void validateDataTypes(List<String> dataTypes) {
            assertThat(dataTypes.size()).isEqualTo(5);
            assertThat(dataTypes.get(0)).isEqualToIgnoringCase("string");
            assertThat(dataTypes.get(1)).isEqualToIgnoringCase("string");
            assertThat(dataTypes.get(2)).isEqualToIgnoringCase("string");
            assertThat(dataTypes.get(3)).isEqualToIgnoringCase("date");
            assertThat(dataTypes.get(4)).isEqualToIgnoringCase("date");
        }

        private void validateData(FailedToAttendLetterData data, String jurorNumberPostfix, LocalDate datePrinted,
                                  LocalDate absentDate) {
            assertThat(data.getJurorNumber()).isEqualToIgnoringCase(JUROR_5555555 + jurorNumberPostfix);
            assertThat(data.getPoolNumber()).isEqualToIgnoringCase(POOL_NUMBER_6666666 + jurorNumberPostfix);
            assertThat(data.getFirstName()).isEqualToIgnoringCase("FirstName");
            assertThat(data.getLastName()).isEqualToIgnoringCase("LastName");
            assertThat(data.getDatePrinted()).isEqualTo(datePrinted);
            assertThat(data.getAbsentDate()).isEqualTo(absentDate);
        }
    }
}