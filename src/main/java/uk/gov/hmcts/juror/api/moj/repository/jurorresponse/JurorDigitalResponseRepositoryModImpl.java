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
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.util.List;

public class JurorDigitalResponseRepositoryModImpl implements IJurorDigitalResponseRepositoryMod {
    @PersistenceContext
    EntityManager entityManager;
    private final QDigitalResponse digitalResponse = QDigitalResponse.digitalResponse;
    private final QUser user = QUser.user;

    @Override
    public Tuple getAssignRepliesStatistics() {
        JPAQuery<Tuple> query = getJpaQueryFactory().select(
                new CaseBuilder().when(digitalResponse.staff.isNull()
                        .and(digitalResponse.urgent.isFalse().and(digitalResponse.superUrgent.isFalse())))
                    .then(1).otherwise(0).sum().as("nonUrgent"),
                new CaseBuilder().when(digitalResponse.staff.isNull()
                    .and(digitalResponse.urgent.isTrue().or(digitalResponse.superUrgent.isTrue()))
                ).then(1).otherwise(0).sum().as("urgent"),
                new CaseBuilder().when(digitalResponse.staff.isNull()).then(1).otherwise(0).sum().as("allReplies"))
            .from(digitalResponse)
            .where(digitalResponse.processingStatus.eq(ProcessingStatus.TODO));
        return query.fetchOne();
    }

    @Override
    public List<Tuple> getAssignRepliesStatisticForUsers() {
        JPAQuery<Tuple> query = getJpaQueryFactory().select(
                user.username.as("login"),
                user.name.as("name"),
                new CaseBuilder().when(
                    digitalResponse.processingStatus.eq(ProcessingStatus.TODO)
                        .and(digitalResponse.urgent.isFalse().and(digitalResponse.superUrgent.isFalse()))
                ).then(1).otherwise(0).sum().as("nonUrgent"),
                new CaseBuilder().when(
                    digitalResponse.processingStatus.eq(ProcessingStatus.TODO)
                        .and(digitalResponse.urgent.isTrue().or(digitalResponse.superUrgent.isTrue()))
                ).then(1).otherwise(0).sum().as("urgent"),
                new CaseBuilder().when(
                    digitalResponse.processingStatus.eq(ProcessingStatus.TODO)
                ).then(1).otherwise(0).sum().as("allReplies")
            ).from(digitalResponse)
            .join(user)
            .on(user.eq(digitalResponse.staff).and(user.userType.eq(UserType.BUREAU)))
            .groupBy(user.username, user.name);
        return query.fetch();
    }

    JPAQueryFactory getJpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }
}
