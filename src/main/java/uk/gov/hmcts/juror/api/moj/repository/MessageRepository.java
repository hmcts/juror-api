package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.messages.Message;

import java.util.List;

@Repository
public interface MessageRepository extends IMessageRepository, CrudRepository<Message, String>,
    QuerydslPredicateExecutor<Message> {

    Message findByJurorNumber(String jurorNumber);

    List<Message> findAllByJurorNumber(String jurorNumber);
}

