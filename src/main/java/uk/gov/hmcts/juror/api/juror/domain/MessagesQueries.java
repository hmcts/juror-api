package uk.gov.hmcts.juror.api.juror.domain;


import com.querydsl.core.types.dsl.BooleanExpression;

/**
 * QueryDSL queries for {@Link Messages}.
*/


public class MessagesQueries {

    private static final String MESSAGE_NOT_READ = "NR";
    private static final String MESSAGE_READ = "MR";

    private MessagesQueries() {

    }

    private static final QMessages messagesDetail = QMessages.messages;

    /**
     * Matches Messages records where message_read is equal 'NR' NOT READ.
     *
     * @return
     */

    public static BooleanExpression messageReadStatus() {
        return messagesDetail.messageRead.eq(MESSAGE_NOT_READ);
    }


}




