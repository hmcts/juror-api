package uk.gov.hmcts.juror.api.moj.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.juror.api.moj.controller.response.messages.JurorToSendMessage;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.SortMethod;
import uk.gov.hmcts.juror.api.moj.domain.messages.MessagePlaceholders;
import uk.gov.hmcts.juror.api.moj.domain.messages.MessageSearch;
import uk.gov.hmcts.juror.api.moj.domain.trial.QPanel;
import uk.gov.hmcts.juror.api.moj.domain.trial.QTrial;
import uk.gov.hmcts.juror.api.moj.enumeration.trial.PanelResult;
import uk.gov.hmcts.juror.api.moj.utils.PaginationUtil;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("PMD.LawOfDemeter")
@NoArgsConstructor
public class MessageTemplateRepositoryImpl implements IMessageTemplateRepository {

    @PersistenceContext
    EntityManager entityManager;
    private static final QJurorPool JUROR_POOL = QJurorPool.jurorPool;
    private static final QJuror JUROR = QJuror.juror;
    private static final QPanel JUROR_TRIAL = QPanel.panel;

    private static final QTrial TRIAL = QTrial.trial;

    public MessageTemplateRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }


    JPAQueryFactory getQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }

    @Override
    public PaginatedList<JurorToSendMessage> messageSearch(MessageSearch search, String locCode,
                                                           boolean simpleResponse, Long maxItems) {

        List<Expression<?>> returnFields = new ArrayList<>();
        returnFields.add(JUROR.jurorNumber);
        returnFields.add(JUROR_POOL.pool.poolNumber);
        returnFields.add(JUROR.email);
        returnFields.add(JUROR.phoneNumber);
        returnFields.add(JUROR.welsh);

        if (!simpleResponse) {
            returnFields.add(JUROR.firstName);
            returnFields.add(JUROR.lastName);
            returnFields.add(JUROR_POOL.status);
            returnFields.add(TRIAL.trialNumber);
            returnFields.add(JUROR_POOL.onCall);
            returnFields.add(JUROR_POOL.nextDate);
            returnFields.add(JUROR_POOL.deferralDate);
            returnFields.add(JUROR.completionDate);
        }
        JPAQueryFactory queryFactory = getQueryFactory();
        JPQLQuery<Tuple> query = queryFactory.select(returnFields.toArray(new Expression<?>[0]))
            .from(JUROR)
            .join(JUROR_POOL)
            .on(JUROR.jurorNumber.eq(JUROR_POOL.juror.jurorNumber))
            .where(JUROR_POOL.pool.courtLocation.locCode.eq(locCode));

        if (search.getTrialNumber() != null || !simpleResponse) {
            query.leftJoin(JUROR_TRIAL).on(
                JUROR_POOL.eq(JUROR_TRIAL.jurorPool),
                JUROR_TRIAL.result.in(PanelResult.JUROR),
                TRIAL.courtLocation.locCode.eq(locCode),
                JUROR_POOL.status.status.in(IJurorStatus.PANEL, IJurorStatus.JUROR)
            );
        }

        search.apply(query);

        return PaginationUtil.toPaginatedList(query, search,
            MessageSearch.SortField.JUROR_NUMBER,
            SortMethod.ASC,
            tuple ->
                JurorToSendMessage.builder()
                    .jurorNumber(tuple.get(JUROR.jurorNumber))
                    .poolNumber(tuple.get(JUROR_POOL.pool.poolNumber))
                    .welshLanguage(Boolean.TRUE.equals(tuple.get(JUROR.welsh)))
                    .email(tuple.get(JUROR.email))
                    .phone(tuple.get(JUROR.phoneNumber))
                    .firstName(tuple.get(JUROR.firstName))
                    .lastName(tuple.get(JUROR.lastName))
                    .status(tuple.get(JUROR_POOL.status) == null ? null : tuple.get(JUROR_POOL.status).getStatusDesc())
                    .trialNumber(tuple.get(TRIAL.trialNumber))
                    .onCall(tuple.get(JUROR_POOL.onCall))
                    .nextDueAtCourt(tuple.get(JUROR_POOL.nextDate))
                    .dateDeferredTo(tuple.get(JUROR_POOL.deferralDate))
                    .completionDate(tuple.get(JUROR.completionDate))
                    .build(),
            maxItems);
    }

    @Override
    public String getDefaultValue(MessagePlaceholders messagePlaceholder, String locCode) {

        Query query =
            entityManager.createNativeQuery(
                "SELECT " + messagePlaceholder.getSourceColumnName()
                    + " from juror_mod." + messagePlaceholder.getSourceTableName()
                    + " where loc_code=?");
        query.setParameter(1, locCode);
        return String.valueOf(query.getSingleResult());
    }
}
