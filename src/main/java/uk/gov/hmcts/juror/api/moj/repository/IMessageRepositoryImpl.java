package uk.gov.hmcts.juror.api.moj.repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.juror.domain.QCourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.managementdashboard.SmsMessagesReportResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.messages.QMessage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class IMessageRepositoryImpl implements IMessageRepository {

    @PersistenceContext
    EntityManager entityManager;

    private static final QMessage MESSAGE = QMessage.message;
    private static final QCourtLocation COURT_LOCATION = QCourtLocation.courtLocation;

    @Override
    public List<SmsMessagesReportResponseDto.SmsMessagesRecord> getSmsMessageCounts() {

        // get the start date of the current month
        // TODO: this needs to be confirmed to be the start date required
        LocalDate startOfCurrentMonth = LocalDate.now().withDayOfMonth(1);
        // get the localDateTime for the start of the current month at midnight
        LocalDateTime startOfCurrentMonthAtMidnight  = startOfCurrentMonth.atStartOfDay();

        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);


        JPAQuery<Tuple> query = queryFactory.select(
                MESSAGE.locationCode.locCode,
                MESSAGE.locationCode.name,
                MESSAGE.count().as("smsCount")
            )
            .from(MESSAGE)
            .join(MESSAGE.locationCode, COURT_LOCATION)
            .where(
                MESSAGE.phone.isNotNull()
                    .and(MESSAGE.email.isNull())
                    .and(MESSAGE.fileDatetime.between(
                        startOfCurrentMonthAtMidnight,
                        LocalDateTime.now()
                    ))
            )
            .groupBy(MESSAGE.locationCode.locCode, MESSAGE.locationCode.name)
            .orderBy(MESSAGE.count().desc());

        List<Tuple> results = query.fetch();
        log.info("Fetched {} SMS message count records", results.size());
        return results.stream()
            .map(tuple -> new SmsMessagesReportResponseDto.SmsMessagesRecord(
                tuple.get(MESSAGE.locationCode.locCode) + " (" + tuple.get(MESSAGE.locationCode.name) + ")",
                tuple.get(2, Long.class)
            ))
            .toList();

    }
}
