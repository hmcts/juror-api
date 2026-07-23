package uk.gov.hmcts.juror.api.moj.repository.jurorresponse;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.QUser;
import uk.gov.hmcts.juror.api.moj.domain.UserType;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.QDigitalResponse;
import uk.gov.hmcts.juror.api.moj.enumeration.ReplyMethod;

import java.time.LocalDate;
import java.util.List;

public class JurorDigitalResponseRepositoryModImpl implements IJurorDigitalResponseRepositoryMod {
    @PersistenceContext
    EntityManager entityManager;
    private static final QDigitalResponse DIGITAL_RESPONSE = QDigitalResponse.digitalResponse;
    private static final QUser USER = QUser.user;

    @Override
    public Tuple getAssignRepliesStatistics() {
        JPAQuery<Tuple> query = getJpaQueryFactory().select(
                new CaseBuilder()
                    .when(DIGITAL_RESPONSE.urgent.isFalse()
                                .and(DIGITAL_RESPONSE.processingStatus.ne(ProcessingStatus.AWAITING_CONTACT)
                    .or(DIGITAL_RESPONSE.processingStatus.ne(ProcessingStatus.AWAITING_COURT_REPLY))
                                .or(DIGITAL_RESPONSE.processingStatus.ne(ProcessingStatus.AWAITING_TRANSLATION))))
                    .then(1L).otherwise(0L).sum().as("nonUrgent"),
                new CaseBuilder()
                    .when(DIGITAL_RESPONSE.urgent.isTrue())
                    .then(1L).otherwise(0L).sum().as("urgent"),
                new CaseBuilder()
                    .when(DIGITAL_RESPONSE.processingStatus.eq(ProcessingStatus.AWAITING_CONTACT)
                              .or(DIGITAL_RESPONSE.processingStatus.eq(ProcessingStatus.AWAITING_COURT_REPLY))
                              .or(DIGITAL_RESPONSE.processingStatus.eq(ProcessingStatus.AWAITING_TRANSLATION)))
                    .then(1L).otherwise(0L).sum().as("awaitingInfo"),
                DIGITAL_RESPONSE.count().as("allReplies"))
            .from(DIGITAL_RESPONSE)
            .where(DIGITAL_RESPONSE.processingStatus.ne(ProcessingStatus.CLOSED))
            .where(DIGITAL_RESPONSE.juror.bureauTransferDate.isNull())
            .where(DIGITAL_RESPONSE.staff.isNull())
            .where(DIGITAL_RESPONSE.replyType.type.eq(ReplyMethod.DIGITAL.getDescription()));
        return query.fetchOne();
    }

    @Override
    @Transactional
    public List<Tuple> getAssignRepliesStatisticForUsers() {
        JPAQuery<Tuple> query = getJpaQueryFactory().select(
                USER.username.as("login"),
                USER.name.as("name"),
                new CaseBuilder()
                    .when(DIGITAL_RESPONSE.urgent.isFalse()
                              .and(QJuror.juror.isNotNull())
                              .and(DIGITAL_RESPONSE.processingStatus.eq(ProcessingStatus.TODO))
                    )
                    .then(1L).otherwise(0L).sum().as("nonUrgent"),
                new CaseBuilder()
                    .when(
                        DIGITAL_RESPONSE.urgent.isTrue()
                            .and(QJuror.juror.isNotNull())
                            .and(DIGITAL_RESPONSE.processingStatus.eq(ProcessingStatus.TODO)
                            ))
                    .then(1L).otherwise(0L).sum().as("urgent"),
                new CaseBuilder()
                    .when(QJuror.juror.isNotNull()
                              .and(QJurorPool.jurorPool.isNotNull())
                              .and(DIGITAL_RESPONSE.processingStatus.eq(ProcessingStatus.AWAITING_CONTACT)
                                       .or(DIGITAL_RESPONSE.processingStatus.eq(ProcessingStatus.AWAITING_COURT_REPLY))
                                       .or(DIGITAL_RESPONSE.processingStatus.eq(ProcessingStatus.AWAITING_TRANSLATION))
                              )
                    )
                    .then(1L).otherwise(0L).sum().as("awaitingInfo"),
                QJuror.juror.count().as("allReplies")
            ).from(USER)
            .where(USER.userType.eq(UserType.BUREAU))
            .where(USER.active.isTrue())
            .leftJoin(DIGITAL_RESPONSE)
            .on(USER.eq(DIGITAL_RESPONSE.staff)
                    .and(DIGITAL_RESPONSE.processingStatus.ne(ProcessingStatus.CLOSED)))
            .leftJoin(QJuror.juror).on(QJuror.juror.jurorNumber.eq(DIGITAL_RESPONSE.jurorNumber)
                                           .and(QJuror.juror.bureauTransferDate.isNull()))
            .leftJoin(QJurorPool.jurorPool).on(QJurorPool.jurorPool.juror.jurorNumber.eq(DIGITAL_RESPONSE.jurorNumber)
                                                   .and(QJurorPool.jurorPool.owner.eq("400"))
                                                   .and(QJurorPool.jurorPool.isActive.isTrue()))
            .groupBy(USER.username, USER.name);
        return query.fetch();
    }


    @Override
    @Transactional
    public List<Tuple> getDigitalSummonsRepliesForMonth(LocalDate startMonth) {
        JPAQueryFactory queryFactory = getJpaQueryFactory();

        // Extract day of month from dateReceived
        DateExpression<LocalDate> dayOfMonth =
            Expressions.dateTemplate(LocalDate.class, "cast({0} as date)", DIGITAL_RESPONSE.dateReceived);

        LocalDate nextMonth = startMonth.plusMonths(1);

        return queryFactory
            .select(dayOfMonth.as("day_of_month"), DIGITAL_RESPONSE.count().as("responses"))
            .from(DIGITAL_RESPONSE)
            .where(
                DIGITAL_RESPONSE.replyType.type.eq("Digital"),
                DIGITAL_RESPONSE.dateReceived.goe(startMonth.atStartOfDay()),
                DIGITAL_RESPONSE.dateReceived.lt(nextMonth.atStartOfDay())
            )
            .groupBy(dayOfMonth)
            .orderBy(dayOfMonth.asc())
            .fetch();

    }

    JPAQueryFactory getJpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }
}
