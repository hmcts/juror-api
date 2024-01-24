package uk.gov.hmcts.juror.api.juror.domain;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface HolidaysRepository extends CrudRepository<Holidays, Date>, QuerydslPredicateExecutor<Holidays> {
    Holidays findByHoliday(Date holiday);
}
