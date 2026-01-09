package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    /**
     * Get court location codes that have been updated in the last 12 months
     * (for full report - not limited to 10)
     */
    @Query(value = "SELECT DISTINCT cla.loc_code "
        + "FROM juror_mod.court_location_audit cla "
        + "JOIN juror_mod.rev_info ri ON cla.revision = ri.revision_number "
        + "WHERE cla.rev_type = 1 "  // 1 = MOD (modification)
        + "AND ri.revision_timestamp >= EXTRACT(EPOCH FROM (CURRENT_DATE - INTERVAL '12 months')) * 1000 "
        + "ORDER BY cla.loc_code",
        nativeQuery = true)
    List<String> getRecentlyUpdatedRecordsLastYear();

    /**
     * Get court audit revisions for specific courts in the last 12 months
     * Returns all revisions (not limited to 10 per court)
     */
    @Query(value = "SELECT cla.loc_code, "
        + "cla.public_transport_soft_limit, "
        + "cla.taxi_soft_limit, "
        + "ri.changed_by, "
        + "ri.revision_number, "
        + "ri.revision_timestamp, "  // Added to get actual change date
        + "cl.loc_court_name "
        + "FROM juror_mod.court_location_audit cla "
        + "JOIN juror_mod.rev_info ri ON cla.revision = ri.revision_number "
        + "JOIN juror_mod.court_location cl ON cla.loc_code = cl.loc_code "
        + "WHERE cla.loc_code IN (:codes) "
        + "AND ri.revision_timestamp >= EXTRACT(EPOCH FROM (CURRENT_DATE - INTERVAL '12 months')) * 1000 "
        + "ORDER BY cla.loc_code, ri.revision_number DESC",
        nativeQuery = true)
    List<String> getCourtRevisionsByLocCodesLastYear(List<String> codes);

    /**
     * Get old and new limits for a specific revision.
     * Uses LAG window function to get previous values for comparison.
     *
     * NOTE: The :: (double colon) for PostgreSQL type casting must be escaped
     * in the Java string or Hibernate will interpret it as a named parameter.
     *
     * @param locCode The court location code (e.g., "415")
     * @param revisionNumber The revision number from rev_info table
     * @return CSV string: loc_code,public_transport_soft_limit,taxi_soft_limit,
     *                     previous_public_transport,previous_taxi
     */
    @Query(value = "WITH revision_data AS ( "
        + "    SELECT "
        + "        cla.loc_code, "
        + "        cla.public_transport_soft_limit, "
        + "        cla.taxi_soft_limit, "
        + "        cla.revision, "
        + "        LAG(cla.public_transport_soft_limit) OVER ( "
        + "            PARTITION BY cla.loc_code ORDER BY cla.revision "
        + "        ) AS previous_public_transport, "
        + "        LAG(cla.taxi_soft_limit) OVER ( "
        + "            PARTITION BY cla.loc_code ORDER BY cla.revision "
        + "        ) AS previous_taxi "
        + "    FROM juror_mod.court_location_audit cla "
        + "    WHERE cla.loc_code = :locCode "
        + ") "
        + "SELECT "
        + "    CONCAT(loc_code, ',', "  // Use CONCAT instead of ||
        + "        COALESCE(CAST(public_transport_soft_limit AS VARCHAR), ''), ',', "
        + "        COALESCE(CAST(taxi_soft_limit AS VARCHAR), ''), ',', "
        + "        COALESCE(CAST(previous_public_transport AS VARCHAR), ''), ',', "
        + "        COALESCE(CAST(previous_taxi AS VARCHAR), '')) "
        + "FROM revision_data "
        + "WHERE revision = :revisionNumber",
        nativeQuery = true)
    List<String> getRevisionLimits(
        @Param("locCode") String locCode,
        @Param("revisionNumber") Long revisionNumber);
}
