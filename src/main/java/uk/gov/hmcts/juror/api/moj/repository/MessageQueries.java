package uk.gov.hmcts.juror.api.moj.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import uk.gov.hmcts.juror.api.moj.domain.messages.QMessage;

/**
 * QueryDsl queries for {@Message}.
 */
public final class MessageQueries {

    private static final String MESSAGE_NOT_READ = "NR";
    private static final QMessage MESSAGES_DETAIL = QMessage.message;

    private MessageQueries() {

    }

    /**
     * Matches Messages records where message_read is equal 'NR' NOT READ.
     *
     */

    public static BooleanExpression messageReadStatus() {
        return MESSAGES_DETAIL.messageRead.eq(MESSAGE_NOT_READ);
    }
}

