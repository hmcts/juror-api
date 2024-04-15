package uk.gov.hmcts.juror.api.moj.repository.jurorresponse;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.AbstractJurorResponse;

import java.time.LocalDateTime;
import java.util.Set;

@Repository
@Transactional(readOnly = true)
public interface JurorCommonResponseRepositoryMod extends JurorResponseRepositoryMod<JurorCommonResponseRepositoryMod.AbstractResponse> {
    @Entity
    @AllArgsConstructor
    @SuperBuilder
    @Getter
    @Setter
    @Table(name = "juror_response", schema = "juror_mod")
    @EqualsAndHashCode(callSuper = true)
    class AbstractResponse extends AbstractJurorResponse {

    }

    long countByStaffUsernameEqualsAndProcessingStatusIn(String username, Set<ProcessingStatus> status);

    long countByStaffUsernameEqualsAndProcessingStatusInAndCompletedAtIsBetween(String username,
                                                                                Set<ProcessingStatus> status,
                                                                                LocalDateTime fromDate,
                                                                                LocalDateTime toDate
    );


    default long countTodo(String username) {
        return countByStaffUsernameEqualsAndProcessingStatusIn(username, Set.of(ProcessingStatus.TODO));
    }


    default long countPending(String username) {
        return countByStaffUsernameEqualsAndProcessingStatusIn(username, Set.of(
            ProcessingStatus.AWAITING_CONTACT,
            ProcessingStatus.AWAITING_TRANSLATION,
            ProcessingStatus.AWAITING_COURT_REPLY
        ));
    }

    default long countComplete(String username) {
        return countByStaffUsernameEqualsAndProcessingStatusIn(username, Set.of(ProcessingStatus.CLOSED));
    }

    default long countComplete(String username, LocalDateTime from, LocalDateTime to) {
        return countByStaffUsernameEqualsAndProcessingStatusInAndCompletedAtIsBetween(
            username,
            Set.of(ProcessingStatus.CLOSED),
            from,
            to);
    }
}
