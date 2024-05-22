package uk.gov.hmcts.juror.api.moj.repository.jurorresponse;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.domain.QUser;
import uk.gov.hmcts.juror.api.moj.domain.UserType;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.QDigitalResponse;

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
                    .when(digitalResponse.urgent.isFalse().and(digitalResponse.superUrgent.isFalse()))
                    .then(1L).otherwise(0L).sum().as("nonUrgent"),
                new CaseBuilder()
                    .when(digitalResponse.urgent.isTrue().or(digitalResponse.superUrgent.isTrue()))
                    .then(1L).otherwise(0L).sum().as("urgent"),
                digitalResponse.count().as("allReplies"))
            .from(digitalResponse)
            .where(digitalResponse.processingStatus.eq(ProcessingStatus.TODO))
            .where(digitalResponse.staff.isNull());
        return query.fetchOne();
    }

    @Override
    public List<Tuple> getAssignRepliesStatisticForUsers() {
        JPAQuery<Tuple> query = getJpaQueryFactory().select(
                user.username.as("login"),
                user.name.as("name"),
                new CaseBuilder()
                    .when(digitalResponse.urgent.isFalse().and(digitalResponse.superUrgent.isFalse()))
                    .then(1L).otherwise(0L).sum().as("nonUrgent"),
                new CaseBuilder()
                    .when(digitalResponse.urgent.isTrue().or(digitalResponse.superUrgent.isTrue()))
                    .then(1L).otherwise(0L).sum().as("urgent"),
                digitalResponse.count().as("allReplies")
            ).from(user)
            .where(user.userType.eq(UserType.BUREAU))
            .leftJoin(digitalResponse)
            .on(user.eq(digitalResponse.staff).and(digitalResponse.processingStatus.eq(ProcessingStatus.TODO)))
            .groupBy(user.username, user.name);
        return query.fetch();
    }

    JPAQueryFactory getJpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }
}
