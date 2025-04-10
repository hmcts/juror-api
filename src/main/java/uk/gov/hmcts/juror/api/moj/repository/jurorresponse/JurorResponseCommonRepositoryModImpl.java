package uk.gov.hmcts.juror.api.moj.repository.jurorresponse;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.controller.request.summonsmanagement.JurorResponseRetrieveRequestDto;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.AbstractJurorResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.JurorResponseCommon;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.QDigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.QJurorResponseCommon;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.QPaperResponse;

import java.util.List;

import static java.lang.Boolean.TRUE;
import static java.time.LocalDate.now;
import static uk.gov.hmcts.juror.api.moj.utils.DataUtils.isNotEmptyOrNull;

/**
 * Custom Repository implementation for juror responses (paper or digital).
 */

@Repository
public class JurorResponseCommonRepositoryModImpl implements JurorResponseCommonRepositoryMod {

    private static final QJurorResponseCommon JUROR_RESPONSE_COMMON = QJurorResponseCommon.jurorResponseCommon;
    private static final QDigitalResponse DIGITAL_RESPONSE = QDigitalResponse.digitalResponse;
    private static final QPaperResponse PAPER_RESPONSE = QPaperResponse.paperResponse;
    private static final QJurorPool JUROR_POOL = QJurorPool.jurorPool;
    private static final QJuror JUROR = QJuror.juror;


    @PersistenceContext
    EntityManager entityManager;

    @Override
    public QueryResults<Tuple> retrieveJurorResponseDetails(JurorResponseRetrieveRequestDto request,
                                                            boolean isTeamLeader, int resultsLimit) {
        // build sql query
        JPAQuery<Tuple> query = sqlRetrieveJurorResponseDetails(request, isTeamLeader);

        if (request.getProcessingStatus() != null
            && request.getProcessingStatus().size() == 1
            && request.getProcessingStatus().contains(ProcessingStatus.CLOSED)) {
            query.orderBy(JUROR_RESPONSE_COMMON.dateReceived.desc());
        } else {
            query.orderBy(JUROR_RESPONSE_COMMON.dateReceived.asc());
        }

        // fetch the data
        return fetchQueryResults(query, resultsLimit);
    }

    @Override
    public AbstractJurorResponse findByJurorNumber(String jurorNumber) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        AbstractJurorResponse response = queryFactory.select(DIGITAL_RESPONSE).from(DIGITAL_RESPONSE)
            .where(DIGITAL_RESPONSE.jurorNumber.eq(jurorNumber).and(DIGITAL_RESPONSE.replyType.type.eq("Digital")))
            .fetchOne();
        if (response == null) {
            return queryFactory.select(PAPER_RESPONSE).from(PAPER_RESPONSE)
                .where(PAPER_RESPONSE.jurorNumber.eq(jurorNumber).and(PAPER_RESPONSE.replyType.type.eq("Paper")))
                .fetchOne();
        }
        return response;
    }

    @Override
    public List<JurorResponseCommon> findByJurorNumberIn(List<String> jurorNumbers) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        return queryFactory.select(JUROR_RESPONSE_COMMON).from(JUROR_RESPONSE_COMMON)
            .where(JUROR_RESPONSE_COMMON.jurorNumber.in(jurorNumbers))
            .fetch();
    }

    private QueryResults<Tuple> fetchQueryResults(JPAQuery<Tuple> query, int resultsLimit) {
        return query
            .limit(resultsLimit)
            .fetchResults();
    }

    private JPAQuery<Tuple> sqlRetrieveJurorResponseDetails(JurorResponseRetrieveRequestDto request,
                                                            boolean isTeamLeader) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        JPAQuery<Tuple> query = queryFactory
            .select(
                JUROR_RESPONSE_COMMON.jurorNumber.as("juror_number"),
                JUROR_RESPONSE_COMMON.firstName.as("first_name"),
                JUROR_RESPONSE_COMMON.lastName.as("last_name"),
                JUROR_RESPONSE_COMMON.postcode.as("postcode"),
                JUROR_RESPONSE_COMMON.processingStatus.as("processing_status"),
                JUROR_RESPONSE_COMMON.dateReceived.as("date_received"),
                JUROR_RESPONSE_COMMON.staff.username.as("username"),
                JUROR_POOL.pool.poolNumber.as("pool_number"),
                JUROR_POOL.pool.courtLocation.name.as("court_name"),
                JUROR_RESPONSE_COMMON.replyType.type.as("reply_type"))
            .from(JUROR_RESPONSE_COMMON)
            .join(JUROR_POOL)
            .on(JUROR_RESPONSE_COMMON.jurorNumber
                .eq(JUROR_POOL.juror.jurorNumber)
                .and(JUROR_POOL.isActive.eq(true)))
            .join(JUROR)
            .on(JUROR_RESPONSE_COMMON.jurorNumber.eq(JUROR.jurorNumber))
            .where(JUROR.bureauTransferDate.isNull());

        // add filters to query
        addCommonFilters(request, query);

        if (isTeamLeader) {
            addTeamLeaderFilters(request, query);
        }

        return query;
    }

    // the following filters apply to bureau officers and team leaders
    private void addCommonFilters(JurorResponseRetrieveRequestDto request, JPAQuery<Tuple> query) {
        if (request.getJurorNumber() != null && !request.getJurorNumber().isBlank()) {
            query.where(JUROR_RESPONSE_COMMON.jurorNumber.eq(request.getJurorNumber()));
        }

        if (request.getLastName() != null && !request.getLastName().isBlank()) {
            query.where(JUROR_RESPONSE_COMMON.lastName.equalsIgnoreCase(request.getLastName()));
        }

        if (request.getPoolNumber() != null && !request.getPoolNumber().isBlank()) {
            query.where(JUROR_POOL.pool.poolNumber.eq(request.getPoolNumber()));
        }
    }

    // the following filters apply to team leaders only
    private void addTeamLeaderFilters(JurorResponseRetrieveRequestDto request, JPAQuery<Tuple> query) {
        if (request.getIsUrgent() != null && TRUE.equals(request.getIsUrgent())) {
            query
                .where(JUROR_RESPONSE_COMMON.urgent.eq(TRUE)
                    .or(JUROR_POOL.pool.returnDate.lt(now())));
        }

        if (request.getOfficerAssigned() != null && !request.getOfficerAssigned().isBlank()) {
            query.where(JUROR_RESPONSE_COMMON.staff.username.eq(request.getOfficerAssigned()));
        }

        if (isNotEmptyOrNull(request.getProcessingStatus())) {
            query.where(JUROR_RESPONSE_COMMON.processingStatus.in(request.getProcessingStatus()));
        }
    }
}
