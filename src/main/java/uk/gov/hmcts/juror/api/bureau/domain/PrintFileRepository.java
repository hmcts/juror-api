package uk.gov.hmcts.juror.api.bureau.domain;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Repository for {@link PrintFile}.
 */
@Repository
public interface PrintFileRepository extends CrudRepository<PrintFile, PrintFileKey>,
    QuerydslPredicateExecutor<PrintFile> {

    /**
     * Finds individual Print File record based on the composite primary key (PrintFileKey).
     *
     * @param partNo        - juror number
     * @param printFileName - file name this record belongs to.
     * @param creationDate  - entry date into juror.print_files table.
     * @return List.  Should always be 1 record.
     */
    List<PrintFile> findByPartNoAndPrintFileNameAndCreationDate(
        String partNo, String printFileName, Date creationDate);

    @Query(nativeQuery = true, value = "SELECT nextval('JUROR.DATA_FILE_NO')")
    int getNextSequenceNo() throws SQLException;

}
