package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.BulkPrintData;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for {@link BulkPrintData}.
 */
@Repository
public interface BulkPrintDataRepository extends IReissueLetterRepository, JpaRepository<BulkPrintData, Long>,
    QuerydslPredicateExecutor<BulkPrintData> {
    long countByJurorNo(String jurorNo);

    /**
     * Finds individual bulk print data record based on the composite primary key (BulkPrintDataKey).
     *
     * @param jurorNo       - juror number
     * @param id - record id
     * @param creationDate  - entry date into mod.bulk_print_data table.
     * @return List.  Should always be 1 record.
     */
    List<BulkPrintData> findByJurorNoAndIdAndCreationDate(
        String jurorNo, Long id, LocalDate creationDate);

}
