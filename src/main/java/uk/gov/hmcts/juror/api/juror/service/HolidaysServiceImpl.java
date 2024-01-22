package uk.gov.hmcts.juror.api.juror.service;

import io.jsonwebtoken.lang.Assert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.juror.controller.request.JurorHolidaysRequestDto;
import uk.gov.hmcts.juror.api.juror.controller.response.JurorHolidaysResponseDto;
import uk.gov.hmcts.juror.api.juror.domain.Holidays;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class HolidaysServiceImpl implements HolidaysService {


    private final HolidaysDateService holidaysDateService;

    @Autowired
    public HolidaysServiceImpl(
        final HolidaysDateService holidaysDateService) {
        Assert.notNull(holidaysDateService, "HolidaysDateService cannot be null ");
        this.holidaysDateService = holidaysDateService;
    }

    @Override
    public JurorHolidaysResponseDto.MatchingHolidayDates getMatchingHolidayDates(JurorHolidaysRequestDto requestDto) {
        log.info("Called Service : HolidaysServiceImpl.getMatchingHolidayDates() ");
        List<Date> holidaysDate = requestDto.getHolidaysDate();

        Date firstCheckDate = holidaysDate.get(0);
        Date secondCheckDate = holidaysDate.get(1);
        Date thirdCheckDate = holidaysDate.get(2);


        log.info("Selected Juror Dates {}:", holidaysDate);
        log.info("First Selected Juror Date {}:", firstCheckDate);
        log.info("Second Selected Juror Date {}:", secondCheckDate);
        log.info("Third Selected Juror Date {}:", thirdCheckDate);


        JurorHolidaysResponseDto.MatchingHolidayDates matchingHolidayDates =
            new JurorHolidaysResponseDto.MatchingHolidayDates();
        final List<Holidays> publicHolidaysDates = new ArrayList<>();
        final Date[] dates = new Date[]{firstCheckDate, secondCheckDate, thirdCheckDate};
        for (int i = 0;
             i < dates.length;
             i++) {
            for (Holidays holiday : holidaysDateService.getHolidayDates(dates[i])) {
                publicHolidaysDates.add(holiday);
            }
        }
        matchingHolidayDates.setPublicHolidayDates(publicHolidaysDates);

        log.info("value of publicHolidaysDates {}:", publicHolidaysDates);

        log.info("count of publicHolidaysDates {}:", publicHolidaysDates.size());

        log.info("value of matchingHolidayDates {}:", matchingHolidayDates);

        return matchingHolidayDates;


    }

}
