package uk.gov.hmcts.juror.api.juror.domain;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HolidaysRepository extends CrudRepository<Holidays, LocalDate>, QuerydslPredicateExecutor<Holidays> {
    Holidays findByHoliday(LocalDate holiday);


    List<Holidays> findAllByPublicHolidayAndHolidayIsGreaterThanEqual(boolean publicHoliday, LocalDate isAfter);
}
