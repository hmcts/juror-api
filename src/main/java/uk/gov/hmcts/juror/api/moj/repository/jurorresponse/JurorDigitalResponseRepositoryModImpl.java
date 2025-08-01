package uk.gov.hmcts.juror.api.moj.repository.jurorresponse;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QUser;
import uk.gov.hmcts.juror.api.moj.domain.UserType;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.QDigitalResponse;
import uk.gov.hmcts.juror.api.moj.enumeration.ReplyMethod;

import java.util.List;

public class JurorDigitalResponseRepositoryModImpl implements IJurorDigitalResponseRepositoryMod {
    @PersistenceContext
    EntityManager entityManager;
    private final QDigitalResponse digitalResponse = QDigitalResponse.digitalResponse;
    private final QUser user = QUser.user;

    @Override
    public Tuple getAssignRepliesStatistics() {
        JPAQuery<Tuple> query = getJpaQueryFactory().select(
                new CaseBuilder()
                    .when(digitalResponse.urgent.isFalse().
                              and(digitalResponse.processingStatus.ne(ProcessingStatus.AWAITING_CONTACT)
                    .or(digitalResponse.processingStatus.ne(ProcessingStatus.AWAITING_COURT_REPLY))
                              .or(digitalResponse.processingStatus.ne(ProcessingStatus.AWAITING_TRANSLATION))))
                    .then(1L).otherwise(0L).sum().as("nonUrgent"),
                new CaseBuilder()
                    .when(digitalResponse.urgent.isTrue())
                    .then(1L).otherwise(0L).sum().as("urgent"),
                new CaseBuilder()
                    .when(digitalResponse.processingStatus.eq(ProcessingStatus.AWAITING_CONTACT)
                              .or(digitalResponse.processingStatus.eq(ProcessingStatus.AWAITING_COURT_REPLY))
                              .or(digitalResponse.processingStatus.eq(ProcessingStatus.AWAITING_TRANSLATION)))
                    .then(1L).otherwise(0L).sum().as("awaitingInfo"),
                digitalResponse.count().as("allReplies"))
            .from(digitalResponse)
            .where(digitalResponse.processingStatus.ne(ProcessingStatus.CLOSED))
            .where(digitalResponse.juror.bureauTransferDate.isNull())
            .where(digitalResponse.staff.isNull())
            .where(digitalResponse.replyType.type.eq(ReplyMethod.DIGITAL.getDescription()));
        return query.fetchOne();
    }

    @Override
    @Transactional
    public List<Tuple> getAssignRepliesStatisticForUsers() {
        JPAQuery<Tuple> query = getJpaQueryFactory().select(
                user.username.as("login"),
                user.name.as("name"),
                new CaseBuilder()
                    .when(digitalResponse.urgent.isFalse()
                            .and(QJuror.juror.isNotNull())
                           .and(digitalResponse.processingStatus.eq(ProcessingStatus.TODO)))
                    .then(1L).otherwise(0L).sum().as("nonUrgent"),
                new CaseBuilder()
                    .when(digitalResponse.urgent.isTrue().and(QJuror.juror.isNotNull())
                    .and(digitalResponse.processingStatus.eq(ProcessingStatus.TODO)))
                    .then(1L).otherwise(0L).sum().as("urgent"),
                new CaseBuilder()
                    .when(digitalResponse.processingStatus.eq(ProcessingStatus.AWAITING_CONTACT)
                              .or(digitalResponse.processingStatus.eq(ProcessingStatus.AWAITING_COURT_REPLY))
                              .or(digitalResponse.processingStatus.eq(ProcessingStatus.AWAITING_TRANSLATION)))
                    .then(1L).otherwise(0L).sum().as("awaitingInfo"),
                QJuror.juror.count().as("allReplies")
            ).from(user)
            .where(user.userType.eq(UserType.BUREAU))
            .where(user.active.isTrue())
            .leftJoin(digitalResponse)
            .on(user.eq(digitalResponse.staff)
                .and(digitalResponse.processingStatus.ne(ProcessingStatus.CLOSED)))
            .leftJoin(QJuror.juror).on(QJuror.juror.jurorNumber.eq(digitalResponse.jurorNumber)
                .and(QJuror.juror.bureauTransferDate.isNull()))
            .groupBy(user.username, user.name);
        return query.fetch();
    }

    JPAQueryFactory getJpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }
}
