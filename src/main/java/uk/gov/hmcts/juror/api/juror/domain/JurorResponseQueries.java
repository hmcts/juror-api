package uk.gov.hmcts.juror.api.juror.domain;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.QDigitalResponse;

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


    /**
     * Query to match 'backlog' responses.
     * (Status is to-do, Neither urgent/super-urgent nor assigned to a bureau officer)
     *
     * @return QueryDSL filter
     */
    public static BooleanExpression backlog() {
        return jurorResponse.urgent.isFalse().and(jurorResponse.superUrgent.isFalse())
            .and(jurorResponse.processingStatus.eq(
                ProcessingStatus.TODO)).and(jurorResponse.staff.isNull());
    }

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
                .and(urgent().not());
        }
    }

    /**
     * Work allocation related methods, kept separate from the original work.
     *
     */

    private static BooleanExpression backlogUrgent() {
        return jurorResponse.urgent.isTrue().and(jurorResponse.superUrgent.isNull()
                                                     .or(jurorResponse.superUrgent.isFalse()));
    }


    /**
     * returns a response - Super Urgent.
     * @returns a response - Super Urgent
     */
    private static BooleanExpression backlogSuperUrgent() {
        return jurorResponse.superUrgent.isTrue().and(jurorResponse.urgent.isFalse());
    }

    /**
     * return all  responses.
     * @return all  responses
     */
    public static BooleanExpression byStatusAll() {
        return jurorResponse.staff.isNull()
            .and(byStatus(List.of(ProcessingStatus.TODO)));
    }


    /**
     * return all urgent responses.
     * @return all urgent responses
     */
    public static BooleanExpression byStatusUrgent() {
        return jurorResponse.staff.isNull()
            .and(byStatus(List.of(ProcessingStatus.TODO))
                .and(backlogUrgent()));
    }

    /**
     * return all super urgent responses.
     * @return all super urgent responses
     */
    public static BooleanExpression byStatusSuperUrgent() {
        return jurorResponse.staff.isNull()
            .and(byStatus(List.of(ProcessingStatus.TODO))
                .and(backlogSuperUrgent()));
    }


    /**
     * return all un assigned responses assigned to staff.
     * @return all un assigned responses assigned to staff
     */
    public static BooleanExpression byAssignedNonUrgent(User staffMember) {
        return jurorResponse.staff.isNotNull()
            .and(notClosed())
            .and(backlogUrgent().not()).and(backlogSuperUrgent().not())
            .and(assignedTo(staffMember));
    }

    /**
     * return all urgent responses assigned to staff.
     * @return all urgent responses assigned to staff
     */
    public static BooleanExpression byAssignedUrgent(User staffMember) {
        return jurorResponse.staff.isNotNull()
            .and(notClosed())
            .and(backlogUrgent()).and(assignedTo(staffMember));
    }

    /**
     * return all super urgent responses assigned to staff.
     * @return all super urgent responses assigned to staff
     */
    public static BooleanExpression byAssignedSuperUrgent(User staffMember) {
        return jurorResponse.staff.isNotNull().and(notClosed())
            .and(backlogSuperUrgent()).and(assignedTo(staffMember));
    }

    /**
     * return all responses assigned to staff.
     * @return all responses assigned to staff
     */
    public static BooleanExpression byAssignedAll(User staffMember) {
        return jurorResponse.staff.isNotNull().and(notClosed())
            .and(assignedTo(staffMember));
    }


}
