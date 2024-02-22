package uk.gov.hmcts.juror.api.moj.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.QCourtLocation;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("PMD.LawOfDemeter")
@Repository
public interface CourtLocationRepository extends CrudRepository<CourtLocation, String>,
    QuerydslPredicateExecutor<CourtLocation>,
    RevisionRepository<CourtLocation, String, Long> {


    Optional<CourtLocation> findByLocCode(String locCode);

    List<CourtLocation> findByLocCodeIn(List<String> locCode);

    Optional<CourtLocation> findByName(String locName);

    List<CourtLocation> findByOwner(String owner);

    default List<String> findLocCodeByOwner(EntityManager entityManager, String owner) {
        QCourtLocation courtLocation = QCourtLocation.courtLocation;
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        return queryFactory
            .select(courtLocation.locCode)
            .from(courtLocation)
            .where(courtLocation.owner.eq(owner))
            .orderBy(courtLocation.locCode.asc())
            .fetch();
    }
}
