package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.data.jpa.repository.Query;
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

    List<CourtLocation> findByLocCodeInOrderByName(List<String> locCode);

    Optional<CourtLocation> findByName(String locName);

    List<CourtLocation> findByOwner(String owner);

    @Override
    List<CourtLocation> findAll();

    @Query(value = "SELECT MAX(clu.revision) FROM juror_mod.court_location_audit clu WHERE clu.loc_code = ?1",
        nativeQuery = true)
    Long getLatestRevision(String locCode);

    @Query(value = "select * from ( "
        + "  select cla.loc_code, cla.public_transport_soft_limit, cla.taxi_soft_limit,"
        + "  ri.changed_by, ri.revision_number, cl.loc_name, "
        + "  row_number() over (partition by cla.loc_code order by ri.revision_number desc) as rn "
        + "  from juror_mod.court_location_audit cla "
        + "  join juror_mod.rev_info ri on cla.revision = ri.revision_number "
        + "  join juror_mod.court_location cl on cla.loc_code = cl.loc_code "
        + "  where cla.loc_code in (:codes) "
        + ") t where rn <= 10 order by loc_code, revision_number desc",  nativeQuery = true)
    List<String> getCourtRevisionsByLocCodes(List<String> codes);

    @Query(value = "select loc_code from juror_mod.court_location_audit cla "
        + "where rev_type = 1 "
        + "order by revision desc "
        + "limit 10", nativeQuery = true)
    List<String> getRecentlyUpdatedRecords();

}
