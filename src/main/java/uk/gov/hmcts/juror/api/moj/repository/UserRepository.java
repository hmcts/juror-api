package uk.gov.hmcts.juror.api.moj.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.juror.domain.QCourtLocation;
import uk.gov.hmcts.juror.api.moj.domain.QUser;
import uk.gov.hmcts.juror.api.moj.domain.User;

import java.util.List;

@Repository
public interface UserRepository extends CrudRepository<User, String>, QuerydslPredicateExecutor<User> {

    List<User> findAllByUsernameIn(List<String> username);

    User findByUsername(String username);

    default Iterable<User> findUsersByCourt(EntityManager entityManager, String court) {
        QCourtLocation courtLocation = QCourtLocation.courtLocation;
        QUser user = QUser.user;

        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        return queryFactory
            .select(user)
            .from(user)
            .join(courtLocation)
            .on(user.owner.eq(courtLocation.owner))
            .where(user.active.eq(true))
            .where(courtLocation.locCode.eq(court))
            .fetch();
    }
}
