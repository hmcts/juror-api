package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorPoolId;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface JurorPoolRepository extends IJurorPoolRepository, JpaRepository<JurorPool, JurorPoolId>,
    QuerydslPredicateExecutor<JurorPool> {

    Optional<JurorPool> findByJurorJurorNumberAndOwnerAndDeferralDate(String jurorNumber, String owner,
                                                                      LocalDate deferralDate);

    List<JurorPool> findByPoolPoolNumberAndIsActive(String poolNumber, boolean isActive);

    Optional<JurorPool> findByJurorJurorNumberAndPoolPoolNumberAndIsActive(String jurorNumber, String poolNumber,
                                                                           boolean isActive);

    List<JurorPool> findByJurorJurorNumberAndIsActive(String jurorNumber, boolean isActive);

    List<JurorPool> findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(String jurorNumber, boolean isActive);

    Optional<JurorPool> findByOwnerAndJurorJurorNumberAndPoolPoolNumber(String owner, String jurorNumber,
                                                                        String poolNumber);

    List<JurorPool> findByPoolPoolNumberAndOwnerAndIsActive(String poolNumber, String owner, boolean isActive);

    List<JurorPool> findByPoolPoolNumberAndWasDeferredAndIsActive(String poolNumber, boolean wasDeferred,
                                                                  boolean isActive);

    JurorPool findByJurorJurorNumberAndPoolPoolNumber(String jurorNumber, String poolNumber);

    JurorPool findByPoolCourtLocationLocCodeAndJurorJurorNumberAndIsActiveTrue(String locCode, String jurorNumber);

    JurorPool findByJurorJurorNumberAndStatusAndIsActive(String jurorNumber, JurorStatus status, boolean isActive);

    JurorPool findByJurorJurorNumberAndPoolPoolNumberAndStatus(String jurorNumber, String poolNumber,
                                                               JurorStatus status);

    List<JurorPool> findByJurorJurorNumberOrderByDateCreatedDesc(String jurorNumber);

    JurorPool findByOwnerAndJurorJurorNumberAndPoolPoolNumberAndIsActive(String owner, String jurorNumber,
                                                                         String poolNumber, boolean isActive);

    JurorPool findByJurorJurorNumber(String jurorNumber);

    JurorPool findByJurorJurorNumberAndIsActiveAndOwner(String jurorNumber, boolean isActive, String owner);
}
