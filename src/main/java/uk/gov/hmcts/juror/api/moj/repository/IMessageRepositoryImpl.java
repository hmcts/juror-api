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

        final LocalDate currentDate = LocalDate.now();

        // determine the year of the start of the financial year
        final int financialYearStartYear = currentDate.getMonthValue() >= 4
                                            ? currentDate.getYear() : currentDate.getYear() - 1;

        // create the LocalDate for 1st April of that year
        final LocalDate financialYearStartDate = LocalDate.of(financialYearStartYear, 4, 1);
        // create the LocalDate for 31st March of the next year
        final LocalDate financialYearEndDate = financialYearStartDate.plusYears(1).minusDays(1);

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
                        financialYearStartDate.atStartOfDay(),
                        financialYearEndDate.atStartOfDay()
                    ))
            )
            .groupBy(MESSAGE.locationCode.locCode, MESSAGE.locationCode.name)
            .orderBy(MESSAGE.count().desc());

        List<Tuple> results = query.fetch();
        log.info("Fetched {} SMS message count records", results.size());
        return results.stream()
            .map(tuple -> new SmsMessagesReportResponseDto.SmsMessagesRecord(
                tuple.get(MESSAGE.locationCode.name) + " (" + tuple.get(MESSAGE.locationCode.locCode) + ")",
                tuple.get(2, Long.class).intValue() // should be a value if tuple exists
            ))
            .toList();

    }
}
