package uk.gov.hmcts.juror.api.juror.domain;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.QDigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.QPaperResponse;

import java.util.List;

/**
 * QueryDSL queries for {@link JurorResponse}.
 *
 * @since JDB-283
 */
@SuppressWarnings({"PMD.TooManyMethods"})
public final class JurorResponseQueries {
    private static final QDigitalResponse JUROR_RESPONSE = QDigitalResponse.digitalResponse;
    private static final QPaperResponse PAPER_JUROR_RESPONSE = QPaperResponse.paperResponse;
    private static final QJurorPool JUROR_POOL = QJurorPool.jurorPool;

    private JurorResponseQueries() {
    }

    /**
     * Query to match 'urgent' / 'super-urgent' responses which are assigned to any staff member.
     *
     * @return QueryDSL filter
     */
    public static BooleanExpression assignedUrgents(User staffMember) {
        return JUROR_RESPONSE.staff.isNotNull().and(notClosed()).and(urgent()).and(assignedTo(staffMember));
    }

    public static BooleanExpression assignedTo(User staffMember) {
        return JUROR_RESPONSE.staff.eq(staffMember);
    }


    private static BooleanExpression urgent() {
        return JUROR_RESPONSE.urgent.isTrue();
    }


    private static BooleanExpression nonUrgent() {
        return urgent().not();
    }


    private static BooleanExpression notClosed() {
        return JUROR_RESPONSE.processingStatus.ne(ProcessingStatus.CLOSED);
    }

    public static BooleanExpression assignedIncompletes(User staffMember) {
        return JUROR_RESPONSE.staff.isNotNull().and(notClosed()).and(assignedTo(staffMember));
    }

    public static OrderSpecifier oldestFirst() {
        return JUROR_RESPONSE.dateReceived.asc();
    }

    public static BooleanExpression byMemberOfStaffAssigned(String staffLogin) {
        return JUROR_RESPONSE.staff.username.equalsIgnoreCase(staffLogin);
    }

    public static BooleanExpression byStatus(List<ProcessingStatus> statuses) {
        return JUROR_RESPONSE.processingStatus.in(statuses);
    }


    public static BooleanExpression byStatusNotClosed(List<ProcessingStatus> statuses) {
        return JUROR_RESPONSE.processingStatus.notIn(statuses);
    }

    public static BooleanExpression byStatusNotClosedPaper(List<ProcessingStatus> statuses) {
        return PAPER_JUROR_RESPONSE.processingStatus.notIn(statuses);
    }

    public static BooleanExpression byStatusAwaitingContact() {
        return JUROR_RESPONSE.processingStatus.eq(ProcessingStatus.AWAITING_CONTACT);

    }

    public static BooleanExpression byStatusAwaitingCourtReply() {
        return JUROR_RESPONSE.processingStatus.eq(ProcessingStatus.AWAITING_COURT_REPLY);
    }

    public static BooleanExpression byStatusAwaitingTranslation() {
        return JUROR_RESPONSE.processingStatus.eq(ProcessingStatus.AWAITING_TRANSLATION);
    }

    public static BooleanExpression byAllAwaitingInfo() {
        return byStatusAwaitingCourtReply()
            .or(byStatusAwaitingContact())
            .or(byStatusAwaitingTranslation());
    }






    public static BooleanExpression byAssignmentAndProcessingStatusAndUrgency(String staffLogin,
                                                                              List<ProcessingStatus> statuses,
                                                                              boolean isUrgent) {
        if (isUrgent) {
            return byMemberOfStaffAssigned(staffLogin)
                .and(byStatus(statuses))
                .and(urgent());
        } else {
            return byMemberOfStaffAssigned(staffLogin)
                .and(byStatus(statuses))
                .and(nonUrgent());
        }
    }

    public static BooleanExpression byOwnerAndJurorTransferredCourt(String owner) {
        return JUROR_POOL.owner.eq(owner).and(poolStatusIsActive())
            .and(jurorTransferredCourt()).and(JUROR_RESPONSE.processingStatus.ne(ProcessingStatus.CLOSED));

    }

    /**
     * return all  responses.
     *
     * @return all  responses
     */
    public static BooleanExpression byUnassignedTodo() {
        return JUROR_RESPONSE.staff.isNull()
            .and(byStatus(List.of(ProcessingStatus.TODO)));
    }

    /**
     * Query to match 'backlog' responses.
     * (Status is to-do, Neither urgent/super-urgent nor assigned to a bureau officer)
     *
     * @return QueryDSL filter
     */
    public static BooleanExpression byUnassignedTodoNonUrgent() {
        return byUnassignedTodo()
            .and(nonUrgent());
    }

    /**
     * return all urgent responses.
     *
     * @return all urgent responses
     */
    public static BooleanExpression byUnassignedTodoUrgent() {
        return byUnassignedTodo()
            .and(urgent());
    }

    public static Predicate jurorIsNotTransferred() {
        return JUROR_RESPONSE.juror.bureauTransferDate.isNull();
    }

    public static Predicate jurorIsNotTransferredPaper() {
        return PAPER_JUROR_RESPONSE.juror.bureauTransferDate.isNull();
    }

    public static BooleanExpression jurorTransferredCourt() {
        return JUROR_RESPONSE.juror.bureauTransferDate.isNotNull();
    }

    public static Predicate jurorTransferredCourtPaper() {
        return PAPER_JUROR_RESPONSE.juror.bureauTransferDate.isNotNull();
    }

    public static  BooleanExpression poolStatusIsActive() {
        return JUROR_POOL.isActive.isTrue();
    }



    @SuppressWarnings({"PMD.LinguisticNaming"})
    public static Predicate isDigital() {
        return JUROR_RESPONSE.replyType.type.eq("Digital");
    }

    @SuppressWarnings({"PMD.LinguisticNaming"})
    public static Predicate isPaper() {
        return PAPER_JUROR_RESPONSE.replyType.type.eq("Paper");
    }
}
