package uk.gov.hmcts.juror.api.juror.service;

import com.google.common.collect.Lists;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import uk.gov.hmcts.juror.api.juror.domain.Holidays;
import uk.gov.hmcts.juror.api.juror.domain.HolidaysQueries;
import uk.gov.hmcts.juror.api.juror.domain.HolidaysRepository;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class HolidaysDateServiceImpl implements HolidaysDateService {

    private final HolidaysRepository holidaysRepository;

    @Autowired
    public HolidaysDateServiceImpl(
        final HolidaysRepository holidaysRepository) {

        Assert.notNull(holidaysRepository, "HolidaysRepository cannot be null");
        this.holidaysRepository = holidaysRepository;
    }

    /**
     * Match the dates from juror with Public Holiday dates in Holidays Table.
     *
     * @return holidayDatesList
     */

    @Override
    @Transactional
    public List<Holidays> getHolidayDates(Date holidaysDate) {
        log.info("Called Service : HolidaysServiceImpl.getHolidayDates()..... ");
        log.info("Juror Selected dates: {}", holidaysDate);

        BooleanExpression matchHolidayDates = HolidaysQueries.holidayDatesMatched(holidaysDate);

        List<Holidays> holidayDatesList = Lists.newLinkedList(holidaysRepository.findAll(matchHolidayDates));
        log.info("Count of Holiday Dates: {}", holidayDatesList.size());
        log.debug("Contents of holidayDatesList:{}", holidayDatesList);

        return holidayDatesList;
    }
}
