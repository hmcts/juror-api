package uk.gov.hmcts.juror.api.moj.repository.jurorresponse;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.AbstractJurorResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Repository
@Transactional(readOnly = true)
public interface JurorCommonResponseRepositoryMod
    extends JurorResponseRepositoryMod<JurorCommonResponseRepositoryMod.AbstractResponse>,
    IJurorCommonResponseRepositoryMod {

    Set<ProcessingStatus> PENDING_STATUS = Set.of(
        ProcessingStatus.AWAITING_CONTACT,
        ProcessingStatus.AWAITING_TRANSLATION,
        ProcessingStatus.AWAITING_COURT_REPLY
    );
    Set<ProcessingStatus> TODO_STATUS = Set.of(
        ProcessingStatus.TODO
    );

    Set<ProcessingStatus> COMPLETE_STATUS = Set.of(
        ProcessingStatus.CLOSED
    );

    @Entity
    @AllArgsConstructor
    @SuperBuilder
    @Getter
    @Setter
    @Table(name = "juror_response", schema = "juror_mod")
    @EqualsAndHashCode(callSuper = true)
    class AbstractResponse extends AbstractJurorResponse {

    }

    long countByProcessingStatusIn(Set<ProcessingStatus> status);

    AbstractResponse findByJurorNumber(String jurorNumber);

    @Query(nativeQuery = true, value = "SELECT u.name AS staff, DATE(jr.completed_at) AS day, COUNT(*) AS work_count "
        + "FROM juror_mod.juror_response jr "
        + "JOIN juror_mod.users u ON jr.staff_login = u.username "
        + "WHERE jr.processing_status = 'CLOSED' "
        + "  AND jr.reply_type = 'Digital' "
        + "  AND jr.completed_at >= :start "
        + "  AND jr.completed_at <  :end "
        + "GROUP BY u.name, DATE(jr.completed_at) "
        + "ORDER BY u.name, DATE(jr.completed_at)")
    List<String> getResponsesCompletedReportData(@Param("start") LocalDate startDate,
                                                    @Param("end") LocalDate endDate);

}
