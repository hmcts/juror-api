package uk.gov.hmcts.juror.api.moj.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import uk.gov.hmcts.juror.api.moj.domain.messages.QMessage;

/**
 * QueryDsl queries for {@Message}.
 */
public class MessageQueries {

    private static final String MESSAGE_NOT_READ = "NR";

    private MessageQueries() {

    }

    private static final QMessage messagesDetail = QMessage.message;

    /**
     * Matches Messages records where message_read is equal 'NR' NOT READ.
     *
     */

    public static BooleanExpression messageReadStatus() {
        return messagesDetail.messageRead.eq(MESSAGE_NOT_READ);
    }
}

