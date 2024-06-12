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
import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, String>, QuerydslPredicateExecutor<User>, IUserRepository {

    List<User> findAllByUsernameIn(List<String> username);

    User findByUsername(String username);

    Optional<User> findByEmailIgnoreCase(String email);

    default Iterable<User> findUsersByCourt(EntityManager entityManager, String court) {
        QCourtLocation courtLocation = QCourtLocation.courtLocation;
        QUser user = QUser.user;

        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        return queryFactory
            .select(user)
            .from(user)
            .join(user.courts, courtLocation)
            .on(courtLocation.locCode.eq(court))
            .where(user.active.eq(true))
            .fetch();
    }

    boolean existsByEmail(String email);
}
