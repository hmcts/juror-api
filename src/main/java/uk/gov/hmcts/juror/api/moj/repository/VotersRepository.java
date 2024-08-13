package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.Voters;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface VotersRepository extends JpaRepository<Voters, Voters.VotersId>,
    QuerydslPredicateExecutor<Voters> {

}
