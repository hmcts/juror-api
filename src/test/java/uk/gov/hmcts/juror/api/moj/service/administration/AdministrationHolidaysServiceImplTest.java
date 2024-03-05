package uk.gov.hmcts.juror.api.moj.service.administration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.Holidays;
import uk.gov.hmcts.juror.api.juror.domain.HolidaysRepository;
import uk.gov.hmcts.juror.api.moj.controller.response.administration.HolidayDate;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@DisplayName("AdministrationServiceImpl")
class AdministrationHolidaysServiceImplTest {

    private AdministrationHolidaysServiceImpl administrationHolidaysService;

    private HolidaysRepository holidaysRepository;

    private CourtLocationRepository courtLocationRepository;

    @BeforeEach
    void beforeEach() {
        holidaysRepository = mock(HolidaysRepository.class);
        courtLocationRepository = mock(CourtLocationRepository.class);
        this.administrationHolidaysService =
            spy(new AdministrationHolidaysServiceImpl(holidaysRepository, courtLocationRepository));
    }

    @Nested
    @DisplayName("List<Holidays> findAllPublicHolidays()")
    class FindAllPublicHolidays {

        @Test
        void positiveTypical() {
            List<Holidays> holidays = List.of(
                mock(Holidays.class),
                mock(Holidays.class),
                mock(Holidays.class)
            );
            when(holidaysRepository.findAllByPublicHolidayAndHolidayIsGreaterThanEqual(anyBoolean(), any()))
                .thenReturn(holidays);

            assertThat(administrationHolidaysService.findAllPublicHolidays()).isEqualTo(holidays);
            LocalDate startOfYear = LocalDate.now().with(TemporalAdjusters.firstDayOfYear());
            verify(holidaysRepository, times(1))
                .findAllByPublicHolidayAndHolidayIsGreaterThanEqual(true, startOfYear);
            verifyNoMoreInteractions(holidaysRepository);
        }
    }

    @Nested
    @DisplayName("List<Holidays> findAllCourtHolidays(String locCode)")
    class FindAllCourtHolidays {

        @Test
        void positiveTypical() {
            List<Holidays> holidays = List.of(
                mock(Holidays.class),
                mock(Holidays.class),
                mock(Holidays.class)
            );
            when(holidaysRepository.findAllByPublicHolidayAndHolidayIsGreaterThanEqualAndCourtLocationLocCode(
                anyBoolean(), any(), anyString()))
                .thenReturn(holidays);

            assertThat(
                administrationHolidaysService.findAllCourtHolidays(TestConstants.VALID_COURT_LOCATION)).isEqualTo(
                holidays);
            LocalDate startOfYear = LocalDate.now().with(TemporalAdjusters.firstDayOfYear());
            verify(holidaysRepository, times(1))
                .findAllByPublicHolidayAndHolidayIsGreaterThanEqualAndCourtLocationLocCode(false, startOfYear,
                    TestConstants.VALID_COURT_LOCATION);
            verifyNoMoreInteractions(holidaysRepository);
        }
    }

    @Nested
    @DisplayName("public List<HolidayDate> viewNonSittingDays(String locCode)")
    class ViewNonSittingDays {

        private Holidays mockHoliday(LocalDate date, String description) {
            return Holidays.builder()
                .holiday(date)
                .description(description)
                .build();
        }

        @Test
        void positiveTypical() {
            doReturn(List.of(
                mockHoliday(LocalDate.of(2023, 1, 1), "desc1"),
                mockHoliday(LocalDate.of(2023, 2, 1), "desc2"),
                mockHoliday(LocalDate.of(2023, 1, 2), "desc3"),
                mockHoliday(LocalDate.of(2023, 2, 2), "desc4")
            )).when(administrationHolidaysService).findAllCourtHolidays(TestConstants.VALID_COURT_LOCATION);

            assertThat(administrationHolidaysService.viewNonSittingDays(TestConstants.VALID_COURT_LOCATION))
                .isEqualTo(List.of(
                        new HolidayDate(LocalDate.of(2023, 1, 1), "desc1"),
                        new HolidayDate(LocalDate.of(2023, 2, 1), "desc2"),
                        new HolidayDate(LocalDate.of(2023, 1, 2), "desc3"),
                        new HolidayDate(LocalDate.of(2023, 2, 2), "desc4")
                    )
                );

            verify(administrationHolidaysService, times(1))
                .findAllCourtHolidays(TestConstants.VALID_COURT_LOCATION);
        }
    }


    @Nested
    @DisplayName("public Map<Integer, List<BankHolidayDate>> viewBankHolidays()")
    class ViewBankHolidays {

        private Holidays mockHoliday(LocalDate date, String description) {
            return Holidays.builder()
                .holiday(date)
                .description(description)
                .build();
        }

        @Test
        void positiveTypical() {
            List<Holidays> holidays = List.of(
                mockHoliday(LocalDate.of(2021, 1, 1), "desc1"),
                mockHoliday(LocalDate.of(2021, 2, 1), "desc2"),
                mockHoliday(LocalDate.of(2022, 1, 1), "desc3"),
                mockHoliday(LocalDate.of(2021, 3, 1), "desc4"),
                mockHoliday(LocalDate.of(2023, 1, 4), "desc5")
            );
            doReturn(holidays).when(administrationHolidaysService).findAllPublicHolidays();

            assertThat(administrationHolidaysService.viewBankHolidays()).isEqualTo(
                Map.of(
                    2021, List.of(
                        new HolidayDate(LocalDate.of(2021, 1, 1), "desc1"),
                        new HolidayDate(LocalDate.of(2021, 2, 1), "desc2"),
                        new HolidayDate(LocalDate.of(2021, 3, 1), "desc4")
                    ),
                    2022, List.of(
                        new HolidayDate(LocalDate.of(2022, 1, 1), "desc3")
                    ),
                    2023, List.of(
                        new HolidayDate(LocalDate.of(2023, 1, 4), "desc5")
                    )
                )
            );
            verify(administrationHolidaysService, times(1))
                .findAllPublicHolidays();
        }

        @Test
        void positiveNoneFound() {
            doReturn(List.of()).when(administrationHolidaysService).findAllPublicHolidays();
            assertThat(administrationHolidaysService.viewBankHolidays()).isEmpty();
            verify(administrationHolidaysService, times(1))
                .findAllPublicHolidays();
        }
    }

    @Nested
    @DisplayName("public void deleteNonSittingDays(String locCode, LocalDate date)")
    class DeleteNonSittingDays {

        @Test
        void positiveDayFound() {
            Holidays holidays = mock(Holidays.class);
            when(holidaysRepository.findByCourtLocationLocCodeAndHolidayAndPublicHolidayIsFalse(anyString(), any()))
                .thenReturn(Optional.of(holidays));

            LocalDate date = LocalDate.now();
            administrationHolidaysService.deleteNonSittingDays(TestConstants.VALID_COURT_LOCATION, date);
            verify(holidaysRepository, times(1))
                .findByCourtLocationLocCodeAndHolidayAndPublicHolidayIsFalse(TestConstants.VALID_COURT_LOCATION, date);
            verify(holidaysRepository, times(1))
                .delete(holidays);
        }

        @Test
        void negativeNotFound() {
            when(holidaysRepository.findByCourtLocationLocCodeAndHolidayAndPublicHolidayIsFalse(anyString(), any()))
                .thenReturn(Optional.empty());

            MojException.NotFound exception = assertThrows(MojException.NotFound.class, () ->
                    administrationHolidaysService.deleteNonSittingDays(TestConstants.VALID_COURT_LOCATION,
                        LocalDate.of(2024, 2, 29)),
                "Expected exception not thrown when no holiday found");
            assertThat(exception.getMessage()).isEqualTo("No non-sitting day found for 415 on 2024-02-29");
            assertThat(exception.getCause()).isNull();
        }
    }

    @Nested
    @DisplayName("public void addNonSittingDays(String locCode, HolidayDate holidayDate)")
    class AddNonSittingDays {
        @Test
        void positiveTypical() {
            when(holidaysRepository.findByCourtLocationLocCodeAndHolidayAndPublicHolidayIsFalse(anyString(), any()))
                .thenReturn(Optional.empty());
            CourtLocation courtLocation = mock(CourtLocation.class);
            when(courtLocationRepository.findByLocCode(anyString()))
                .thenReturn(Optional.of(courtLocation));

            administrationHolidaysService.addNonSittingDays(TestConstants.VALID_COURT_LOCATION,
                new HolidayDate(LocalDate.of(2024, 2, 29), "desc"));

            verify(holidaysRepository, times(1))
                .findByCourtLocationLocCodeAndHolidayAndPublicHolidayIsFalse(TestConstants.VALID_COURT_LOCATION,
                    LocalDate.of(2024, 2, 29));
            verify(courtLocationRepository, times(1))
                .findByLocCode(TestConstants.VALID_COURT_LOCATION);
            verify(holidaysRepository, times(1))
                .save(Holidays.builder()
                    .holiday(LocalDate.of(2024, 2, 29))
                    .description("desc")
                    .courtLocation(courtLocation)
                    .publicHoliday(false)
                    .build());
        }

        @Test
        void negativeAlreadyExists() {
            when(holidaysRepository.findByCourtLocationLocCodeAndHolidayAndPublicHolidayIsFalse(anyString(), any()))
                .thenReturn(Optional.of(mock(Holidays.class)));

            MojException.BusinessRuleViolation exception = assertThrows(MojException.BusinessRuleViolation.class, () ->
                    administrationHolidaysService.addNonSittingDays(TestConstants.VALID_COURT_LOCATION,
                        new HolidayDate(LocalDate.of(2024, 2, 29), "desc")),
                "Expected exception not thrown when holiday already exists");
            assertThat(exception.getMessage()).isEqualTo("Non-sitting day already exists for 415 on 2024-02-29");
            assertThat(exception.getErrorCode()).isEqualTo(
                MojException.BusinessRuleViolation.ErrorCode.DAY_ALREADY_EXISTS);
        }

        @Test
        void negativeCourtLocationNotFound() {
            when(holidaysRepository.findByCourtLocationLocCodeAndHolidayAndPublicHolidayIsFalse(anyString(), any()))
                .thenReturn(Optional.empty());
            when(courtLocationRepository.findByLocCode(anyString()))
                .thenReturn(Optional.empty());

            MojException.NotFound exception = assertThrows(MojException.NotFound.class, () ->
                    administrationHolidaysService.addNonSittingDays(TestConstants.VALID_COURT_LOCATION,
                        new HolidayDate(LocalDate.of(2024, 2, 29), "desc")),
                "Expected exception not thrown when court location not found");
            assertThat(exception.getMessage()).isEqualTo("Court location not found");
            assertThat(exception.getCause()).isNull();
        }
    }
}
