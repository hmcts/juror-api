package uk.gov.hmcts.juror.api.moj.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.juror.api.moj.controller.request.messages.ExportContactDetailsRequest;
import uk.gov.hmcts.juror.api.moj.controller.response.messages.JurorToSendMessageBase;
import uk.gov.hmcts.juror.api.moj.controller.response.messages.JurorToSendMessageBureau;
import uk.gov.hmcts.juror.api.moj.controller.response.messages.JurorToSendMessageCourt;
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
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.utils.PaginationUtil;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("PMD.LawOfDemeter")
@NoArgsConstructor
public class MessageTemplateRepositoryImpl implements IMessageTemplateRepository {

    @PersistenceContext
    EntityManager entityManager;
    private static final QJurorPool JUROR_POOL = QJurorPool.jurorPool;
    private static final QJuror JUROR = QJuror.juror;
    private static final QPanel PANEL = QPanel.panel;

    private static final QTrial TRIAL = QTrial.trial;

    public MessageTemplateRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }


    JPAQueryFactory getQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }

    @Override
    public PaginatedList<? extends JurorToSendMessageBase> messageSearch(MessageSearch search, String locCode,
                                                                         boolean simpleResponse, Long maxItems) {
        final boolean isCourt = SecurityUtil.isCourt();
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
            returnFields.add(JUROR_POOL.deferralDate);

            if (isCourt) {
                returnFields.add(TRIAL.trialNumber);
                returnFields.add(JUROR.completionDate);
                returnFields.add(JUROR_POOL.onCall);
                returnFields.add(JUROR_POOL.nextDate);
            }
        }
        JPAQueryFactory queryFactory = getQueryFactory();

        JPAQuery<Tuple> query = queryFactory.select(returnFields.toArray(new Expression<?>[0]))
            .from(JUROR)
            .join(JUROR_POOL)
            .on(JUROR.eq(JUROR_POOL.juror))
            .where(JUROR_POOL.isActive.isTrue());
        if (isCourt || !locCode.equals("400")) {
            query.where(JUROR_POOL.pool.courtLocation.locCode.eq(locCode));
            if (isCourt) {
                query.where(JUROR_POOL.pool.owner.ne("400"));
            }
        }

        if (search.getTrialNumber() != null || !simpleResponse) {
            query.leftJoin(PANEL).on(
                JUROR.eq(PANEL.juror)
                    .and(PANEL.result.notIn(PanelResult.RETURNED, PanelResult.NOT_USED, PanelResult.CHALLENGED))
                    .and(PANEL.trial.courtLocation.locCode.eq(JUROR_POOL.pool.courtLocation.locCode))
                    .and(JUROR_POOL.status.status.in(IJurorStatus.PANEL, IJurorStatus.JUROR))
            );
        }

        search.apply(query);

        return PaginationUtil.toPaginatedList(query, search,
            MessageSearch.SortField.JUROR_NUMBER,
            SortMethod.ASC,
            tuple -> {
                JurorToSendMessageBase.JurorToSendMessageBaseBuilder<?, ?> builder;

                if (isCourt) {
                    builder = JurorToSendMessageCourt.builder()
                        .trialNumber(tuple.get(TRIAL.trialNumber))
                        .onCall(tuple.get(JUROR_POOL.onCall))
                        .nextDueAtCourt(tuple.get(JUROR_POOL.nextDate))
                        .completionDate(tuple.get(JUROR.completionDate));
                } else {
                    builder = JurorToSendMessageBureau.builder();
                }
                return builder
                    .jurorNumber(tuple.get(JUROR.jurorNumber))
                    .poolNumber(tuple.get(JUROR_POOL.pool.poolNumber))
                    .welshLanguage(Boolean.TRUE.equals(tuple.get(JUROR.welsh)))
                    .email(tuple.get(JUROR.email))
                    .phone(tuple.get(JUROR.phoneNumber))
                    .firstName(tuple.get(JUROR.firstName))
                    .lastName(tuple.get(JUROR.lastName))
                    .status(tuple.get(JUROR_POOL.status) == null ? null : tuple.get(JUROR_POOL.status).getStatusDesc())
                    .dateDeferredTo(tuple.get(JUROR_POOL.deferralDate))
                    .build();
            },
            maxItems);
    }

    @Override
    public List<List<String>> exportDetails(ExportContactDetailsRequest exportContactDetailsRequest, String locCode) {
        List<? extends Expression<?>> returnFields = exportContactDetailsRequest.getExportItems()
            .stream()
            .map(ExportContactDetailsRequest.ExportItems::getExpression)
            .toList();
        JPAQueryFactory queryFactory = getQueryFactory();

        List<BooleanExpression> or = exportContactDetailsRequest.getJurors().stream()
            .map(jurorAndPoolRequest -> JUROR.jurorNumber.eq(jurorAndPoolRequest.getJurorNumber())
                .and(JUROR_POOL.pool.poolNumber.eq(jurorAndPoolRequest.getPoolNumber()))).toList();


        List<List<String>> rows = new ArrayList<>();
        JPQLQuery<Tuple> query = queryFactory.select(returnFields.toArray(new Expression<?>[0]))
            .from(JUROR)
            .join(JUROR_POOL)
            .on(JUROR.eq(JUROR_POOL.juror));

        if (SecurityUtil.isCourt()) {
            query.where(JUROR_POOL.pool.courtLocation.locCode.eq(locCode));
        }

        query.where(or.stream().reduce(BooleanExpression::or).get());

        query.fetch().stream().map(tuple -> {
            List<String> row = new ArrayList<>();
            exportContactDetailsRequest.getExportItems()
                .forEach(exportItems -> row.add(exportItems.getAsString(tuple)));
            return row;
        }).forEach(rows::add);

        if (rows.size() != exportContactDetailsRequest.getJurors().size()) {
            throw new MojException.NotFound("One or more jurors not found", null);
        }
        return rows;
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
