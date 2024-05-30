package uk.gov.hmcts.juror.api.juror.domain;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.QDigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.QPaperResponse;

import java.util.List;

/**
 * QueryDSL queries for {@link JurorResponse}.
 *
 * @since JDB-283
 */
public class JurorResponseQueries {


    private JurorResponseQueries() {
    }

    private static final QDigitalResponse jurorResponse = QDigitalResponse.digitalResponse;
    private static final QPaperResponse paperJurorResponse = QPaperResponse.paperResponse;


    /**
     * Query to match 'urgent' / 'super-urgent' responses which are assigned to any staff member.
     *
     * @return QueryDSL filter
     */
    public static BooleanExpression assignedUrgents(User staffMember) {
        return jurorResponse.staff.isNotNull().and(notClosed()).and(urgent()).and(assignedTo(staffMember));
    }

    public static BooleanExpression assignedTo(User staffMember) {
        return jurorResponse.staff.eq(staffMember);
    }


    private static BooleanExpression urgent() {
        return jurorResponse.urgent.isTrue().or(jurorResponse.superUrgent.isTrue());
    }


    private static BooleanExpression nonUrgent() {
        return urgent().not();
    }


    private static BooleanExpression notClosed() {
        return jurorResponse.processingStatus.ne(ProcessingStatus.CLOSED);
    }

    public static BooleanExpression assignedIncompletes(User staffMember) {
        return jurorResponse.staff.isNotNull().and(notClosed()).and(assignedTo(staffMember));
    }

    public static OrderSpecifier oldestFirst() {
        return jurorResponse.dateReceived.asc();
    }

    public static BooleanExpression byMemberOfStaffAssigned(String staffLogin) {
        return jurorResponse.staff.username.equalsIgnoreCase(staffLogin);
    }

    public static BooleanExpression byStatus(List<ProcessingStatus> statuses) {
        return jurorResponse.processingStatus.in(statuses);
    }


    public static BooleanExpression byStatusNotClosed(List<ProcessingStatus> statuses) {
        return jurorResponse.processingStatus.notIn(statuses);
    }

    public static BooleanExpression byStatusNotClosedPaper(List<ProcessingStatus> statuses) {
        return paperJurorResponse.processingStatus.notIn(statuses);
    }


    public static BooleanExpression byAssignmentAndProcessingStatusAndUrgency(String staffLogin,
                                                                              List<ProcessingStatus> statuses,
                                                                              boolean isUrgentOrSuperUrgent) {
        if (isUrgentOrSuperUrgent) {
            return byMemberOfStaffAssigned(staffLogin)
                .and(byStatus(statuses))
                .and(urgent());
        } else {
            return byMemberOfStaffAssigned(staffLogin)
                .and(byStatus(statuses))
                .and(nonUrgent());
        }
    }


    /**
     * return all  responses.
     *
     * @return all  responses
     */
    public static BooleanExpression byUnassignedTodo() {
        return jurorResponse.staff.isNull()
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
        return jurorResponse.juror.bureauTransferDate.isNull();
    }

    public static Predicate jurorIsNotTransferredPaper() {
        return paperJurorResponse.juror.bureauTransferDate.isNull();
    }

    public static Predicate isDigital() {
        return jurorResponse.replyType.type.eq("Digital");
    }

    public static Predicate isPaper() {
        return paperJurorResponse.replyType.type.eq("Paper");
    }
}
