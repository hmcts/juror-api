package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourtLocationRepository extends CrudRepository<CourtLocation, String>,
    QuerydslPredicateExecutor<CourtLocation>,
    RevisionRepository<CourtLocation, String, Long> {


    Optional<CourtLocation> findByLocCode(String locCode);

    List<CourtLocation> findByLocCodeIn(List<String> locCode);

    Optional<CourtLocation> findByName(String locName);

    List<CourtLocation> findByOwner(String owner);

    @Override
    List<CourtLocation> findAll();
}
