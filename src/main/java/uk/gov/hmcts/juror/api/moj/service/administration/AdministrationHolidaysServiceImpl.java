package uk.gov.hmcts.juror.api.moj.service.administration;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

import static java.util.stream.Collectors.groupingBy;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AdministrationHolidaysServiceImpl implements AdministrationHolidaysService {

    private final HolidaysRepository holidaysRepository;
    private final CourtLocationRepository courtLocationRepository;


    @Override
    @Transactional(readOnly = true)
    public Map<Integer, List<HolidayDate>> viewBankHolidays() {
        return findAllPublicHolidays().stream().map(HolidayDate::new)
            .collect(groupingBy(bankHolidayDate -> bankHolidayDate.getDate().getYear()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<HolidayDate> viewNonSittingDays(String locCode) {
        return findAllCourtHolidays(locCode).stream().map(HolidayDate::new).toList();
    }

    @Override
    @Transactional
    public void deleteNonSittingDays(String locCode, LocalDate date) {
        Holidays holidays =
            holidaysRepository.findByCourtLocationLocCodeAndHolidayAndPublicHolidayIsFalse(locCode, date)
                .orElseThrow(
                    () -> new MojException.NotFound(
                        "No non-sitting day found for " + locCode + " on " + date.toString(),
                        null));
        holidaysRepository.delete(holidays);
    }

    @Override
    public void addNonSittingDays(String locCode, HolidayDate holidayDate) {
        if (holidaysRepository.findByCourtLocationLocCodeAndHolidayAndPublicHolidayIsFalse(locCode,
            holidayDate.getDate()).isPresent()) {
            throw new MojException.BusinessRuleViolation(
                "Non-sitting day already exists for " + locCode + " on " + holidayDate.getDate().toString(),
                MojException.BusinessRuleViolation.ErrorCode.DAY_ALREADY_EXISTS);
        }
        CourtLocation courtLocation = courtLocationRepository.findByLocCode(locCode)
            .orElseThrow(() -> new MojException.NotFound("Court location not found", null));
        holidaysRepository.save(Holidays.builder()
            .holiday(holidayDate.getDate())
            .description(holidayDate.getDescription())
            .courtLocation(courtLocation)
            .publicHoliday(false)
            .build());
    }

    @Override
    public List<LocalDate> getSundayDates(LocalDate fromDate, LocalDate toDate) {
        return fromDate.datesUntil(toDate.plusDays(1))
            .filter(date -> date.getDayOfWeek().getValue() == 7)
            .toList();
    }

    @Override
    public List<LocalDate> getSaturdayDates(LocalDate fromDate, LocalDate toDate) {
        return fromDate.datesUntil(toDate.plusDays(1))
            .filter(date -> date.getDayOfWeek().getValue() == 6)
            .toList();
    }

    List<Holidays> findAllPublicHolidays() {
        return holidaysRepository.findAllByPublicHolidayAndHolidayIsGreaterThanEqual(
            true, LocalDate.now().with(TemporalAdjusters.firstDayOfYear()));
    }

    List<Holidays> findAllCourtHolidays(String locCode) {
        return holidaysRepository.findAllByPublicHolidayAndHolidayIsGreaterThanEqualAndCourtLocationLocCode(
            false, LocalDate.now().with(TemporalAdjusters.firstDayOfYear()), locCode);
    }
}
