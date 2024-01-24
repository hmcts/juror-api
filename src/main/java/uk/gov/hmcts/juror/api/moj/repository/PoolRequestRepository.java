package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public interface PoolRequestRepository extends IPoolRequestRepository, JpaRepository<PoolRequest, String>,
    QuerydslPredicateExecutor<PoolRequest> {

    Optional<PoolRequest> findByPoolNumber(String poolNumber);

    Optional<PoolRequest> findByOwnerAndPoolNumber(String owner, String poolNumber);

    /** This function is used to get the list of pools at a court location.
     * @param LocCode 3-digit numeric string to uniquely identify a court location
     *
     * @return List of pools at a court location
     * <p>Each list item is the following index:
     * <ul>
     * <li>(0) pool_number character varying
     * <li>(1) total_possible_in_attendance bigint (those with status responded but not on call)
     * <li>(2) in_attendance bigint (those with status responded and checked in at court)
     * <li>(3) on_call bigint (responded jurors with status on call)
     * <li>(4) total_possible_on_trial bigint (those with status of panel or juror)
     * <li>(5) jurors_on_trial bigint (checked in jurors on trial in panel or juror status)
     * <li>(6) pool_type character varying
     * <li>(7) service_start_date date
     * <p>(only counting active jurors)
     */
    @Query(value = "SELECT * from juror_mod.get_active_pools_at_court_location(:LocCode)", nativeQuery = true)
    List<String> findPoolsByCourtLocation(@Param("LocCode") String LocCode) throws SQLException;

}


