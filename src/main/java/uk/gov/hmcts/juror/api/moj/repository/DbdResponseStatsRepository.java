package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.hmcts.juror.api.moj.domain.DbdResponseStats;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface DbdResponseStatsRepository extends JpaRepository<DbdResponseStats, Long> {

    List<DbdResponseStats> findByLocCodeInAndSummonsDateBetween(
        Collection<String> locCodes, LocalDateTime start, LocalDateTime end);
}
