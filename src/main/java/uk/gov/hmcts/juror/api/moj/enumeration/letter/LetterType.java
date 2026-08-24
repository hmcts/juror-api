package uk.gov.hmcts.juror.api.moj.enumeration.letter;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import lombok.Getter;
import uk.gov.hmcts.juror.api.moj.domain.FormCode;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.QBulkPrintData;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.enumeration.ExcusalCodeEnum;
import uk.gov.hmcts.juror.api.moj.service.ReissueLetterService;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

@Getter
public enum LetterType {

    SUMMONS(List.of(FormCode.ENG_SUMMONS, FormCode.BI_SUMMONS, FormCode.ENG_DBD_SUMMONS, FormCode.BI_DBD_SUMMONS),
            List.of(
        ReissueLetterService.DataType.JUROR_NUMBER,
        ReissueLetterService.DataType.SUMMONS_POOL_NUMBER,
        ReissueLetterService.DataType.SUMMONS_DATE,
        ReissueLetterService.DataType.JUROR_FIRST_NAME,
        ReissueLetterService.DataType.JUROR_LAST_NAME,
        ReissueLetterService.DataType.JUROR_POSTCODE,
        ReissueLetterService.DataType.DATE_PRINTED,
        ReissueLetterService.DataType.EXTRACTED_FLAG,
        ReissueLetterService.DataType.FORM_CODE),
        tupleJPAQuery -> tupleJPAQuery.where(QJurorPool.jurorPool.status.status.eq(IJurorStatus.SUMMONED)),
        datePrintedComparator()),

    CONFIRMATION(List.of(FormCode.ENG_CONFIRMATION, FormCode.BI_CONFIRMATION), List.of(
        ReissueLetterService.DataType.JUROR_NUMBER,
        ReissueLetterService.DataType.JUROR_FIRST_NAME,
        ReissueLetterService.DataType.JUROR_LAST_NAME,
        ReissueLetterService.DataType.JUROR_POSTCODE,
        ReissueLetterService.DataType.DATE_PRINTED,
        ReissueLetterService.DataType.EXTRACTED_FLAG,
        ReissueLetterService.DataType.FORM_CODE),
        tupleJPAQuery -> tupleJPAQuery.where(QJurorPool.jurorPool.status.status.eq(IJurorStatus.RESPONDED)),
        datePrintedComparator()),

    WITHDRAWAL(List.of(FormCode.BI_WITHDRAWAL, FormCode.ENG_WITHDRAWAL), List.of(
        ReissueLetterService.DataType.JUROR_NUMBER,
        ReissueLetterService.DataType.JUROR_FIRST_NAME,
        ReissueLetterService.DataType.JUROR_LAST_NAME,
        ReissueLetterService.DataType.JUROR_POSTCODE,
        ReissueLetterService.DataType.JUROR_STATUS,
        ReissueLetterService.DataType.JUROR_WITHDRAWAL_DATE,
        ReissueLetterService.DataType.JUROR_WITHDRAWAL_REASON,
        ReissueLetterService.DataType.DATE_PRINTED,
        ReissueLetterService.DataType.EXTRACTED_FLAG,
        ReissueLetterService.DataType.FORM_CODE),
        tupleJPAQuery -> tupleJPAQuery.where(QJurorPool.jurorPool.status.status.eq(IJurorStatus.DISQUALIFIED)),
        datePrintedComparator()),

    DEFERRAL_GRANTED(List.of(FormCode.ENG_DEFERRAL, FormCode.BI_DEFERRAL), List.of(
        ReissueLetterService.DataType.JUROR_NUMBER,
        ReissueLetterService.DataType.JUROR_FIRST_NAME,
        ReissueLetterService.DataType.JUROR_LAST_NAME,
        ReissueLetterService.DataType.JUROR_POSTCODE,
        ReissueLetterService.DataType.JUROR_STATUS,
        ReissueLetterService.DataType.JUROR_DEFERRED_TO,
        ReissueLetterService.DataType.JUROR_DEFERRED_TO_REASON,
        ReissueLetterService.DataType.DATE_PRINTED,
        ReissueLetterService.DataType.EXTRACTED_FLAG,
        ReissueLetterService.DataType.FORM_CODE,
        ReissueLetterService.DataType.COMMUNICATION_CHANNEL,
        ReissueLetterService.DataType.JUROR_PREFERENCE,
        ReissueLetterService.DataType.EMAIL_STATUS),
        tupleJPAQuery -> tupleJPAQuery
            .where(QJurorPool.jurorPool.status.status.eq(IJurorStatus.DEFERRED)
                .and(QJurorPool.jurorPool.deferralCode.ne(ExcusalCodeEnum.P.getCode()))),
        datePrintedComparator()),

    DEFERRAL_REFUSED(List.of(FormCode.ENG_DEFERRALDENIED, FormCode.BI_DEFERRALDENIED), List.of(
        ReissueLetterService.DataType.JUROR_NUMBER,
        ReissueLetterService.DataType.JUROR_FIRST_NAME,
        ReissueLetterService.DataType.JUROR_LAST_NAME,
        ReissueLetterService.DataType.JUROR_POSTCODE,
        ReissueLetterService.DataType.JUROR_STATUS,
        ReissueLetterService.DataType.JUROR_DEFERRAL_DATE_REFUSED,
        ReissueLetterService.DataType.JUROR_DEFERRAL_REJECTED_REASON,
        ReissueLetterService.DataType.DATE_PRINTED,
        ReissueLetterService.DataType.EXTRACTED_FLAG,
        ReissueLetterService.DataType.FORM_CODE),
        tupleJPAQuery -> tupleJPAQuery
            .where(QJurorPool.jurorPool.status.status.eq(IJurorStatus.RESPONDED)
                .and(QJurorPool.jurorPool.deferralCode.ne(ExcusalCodeEnum.P.getCode()))),
        datePrintedComparator()),

    EXCUSAL_GRANTED(List.of(FormCode.ENG_EXCUSAL, FormCode.BI_EXCUSAL), List.of(
        ReissueLetterService.DataType.JUROR_NUMBER,
        ReissueLetterService.DataType.JUROR_FIRST_NAME,
        ReissueLetterService.DataType.JUROR_LAST_NAME,
        ReissueLetterService.DataType.JUROR_POSTCODE,
        ReissueLetterService.DataType.JUROR_STATUS,
        ReissueLetterService.DataType.JUROR_EXCUSAL_DATE,
        ReissueLetterService.DataType.JUROR_EXCUSAL_REASON,
        ReissueLetterService.DataType.DATE_PRINTED,
        ReissueLetterService.DataType.EXTRACTED_FLAG,
        ReissueLetterService.DataType.FORM_CODE),
        tupleJPAQuery -> tupleJPAQuery.where(QJurorPool.jurorPool.status.status.eq(IJurorStatus.EXCUSED)),
        datePrintedComparator()),

    EXCUSAL_REFUSED(List.of(FormCode.ENG_EXCUSALDENIED, FormCode.BI_EXCUSALDENIED), List.of(
        ReissueLetterService.DataType.JUROR_NUMBER,
        ReissueLetterService.DataType.JUROR_FIRST_NAME,
        ReissueLetterService.DataType.JUROR_LAST_NAME,
        ReissueLetterService.DataType.JUROR_POSTCODE,
        ReissueLetterService.DataType.JUROR_STATUS,
        ReissueLetterService.DataType.JUROR_EXCUSAL_DENIED_DATE,
        ReissueLetterService.DataType.JUROR_EXCUSAL_REASON,
        ReissueLetterService.DataType.DATE_PRINTED,
        ReissueLetterService.DataType.EXTRACTED_FLAG,
        ReissueLetterService.DataType.FORM_CODE),
        tupleJPAQuery -> tupleJPAQuery
            .where(QJurorPool.jurorPool.status.status.in(IJurorStatus.SUMMONED, IJurorStatus.RESPONDED))
            .where(QJuror.juror.excusalRejected.eq("Y")),
        datePrintedComparator()),

    SUMMONED_REMINDER(List.of(FormCode.ENG_SUMMONS_REMINDER, FormCode.BI_SUMMONS_REMINDER,
                              FormCode.ENG_DBD_SUMMONS_REM, FormCode.BI_DBD_SUMMONS_REM),
        List.of(
            ReissueLetterService.DataType.JUROR_NUMBER,
            ReissueLetterService.DataType.JUROR_FIRST_NAME,
            ReissueLetterService.DataType.JUROR_LAST_NAME,
            ReissueLetterService.DataType.JUROR_POSTCODE,
            ReissueLetterService.DataType.DATE_PRINTED,
            ReissueLetterService.DataType.EXTRACTED_FLAG,
            ReissueLetterService.DataType.FORM_CODE
        ),
        tupleJPAQuery -> tupleJPAQuery.where(QJurorPool.jurorPool.status.status.eq(IJurorStatus.SUMMONED)
            .and(QJuror.juror.responded.eq(false))
            .and(QJuror.juror.digitalByDefault.isFalse().or(hasReceivedSummonsLetter()))
        ),
        datePrintedComparator()),
    POSTPONED(List.of(FormCode.ENG_POSTPONE, FormCode.BI_POSTPONE), List.of(
        ReissueLetterService.DataType.JUROR_NUMBER,
        ReissueLetterService.DataType.JUROR_FIRST_NAME,
        ReissueLetterService.DataType.JUROR_LAST_NAME,
        ReissueLetterService.DataType.JUROR_POSTCODE,
        ReissueLetterService.DataType.JUROR_STATUS,
        ReissueLetterService.DataType.JUROR_POSTPONED_TO,
        ReissueLetterService.DataType.JUROR_DEFERRED_TO_REASON,
        ReissueLetterService.DataType.DATE_PRINTED,
        ReissueLetterService.DataType.EXTRACTED_FLAG,
        ReissueLetterService.DataType.FORM_CODE),
        tupleJPAQuery -> tupleJPAQuery
            .where(QJurorPool.jurorPool.status.status.eq(IJurorStatus.DEFERRED)
                .and(QJurorPool.jurorPool.deferralCode.eq("P"))),
        datePrintedComparator()
    ),
    INFORMATION(List.of(FormCode.ENG_REQUESTINFO, FormCode.BI_REQUESTINFO), List.of(
        ReissueLetterService.DataType.JUROR_NUMBER,
        ReissueLetterService.DataType.JUROR_FIRST_NAME,
        ReissueLetterService.DataType.JUROR_LAST_NAME,
        ReissueLetterService.DataType.JUROR_POSTCODE,
        ReissueLetterService.DataType.DATE_PRINTED,
        ReissueLetterService.DataType.EXTRACTED_FLAG,
        ReissueLetterService.DataType.FORM_CODE),
        tupleJPAQuery -> tupleJPAQuery
            .where(QJurorPool.jurorPool.status.status.eq(IJurorStatus.SUMMONED)),
        datePrintedComparator()
    );

    private final List<FormCode> formCodes;
    private final List<ReissueLetterService.DataType> reissueDataTypes;
    private final Consumer<JPAQuery<Tuple>> letterQueryConsumer;
    private final Comparator<Tuple> tupleComparator;


    private static Comparator<Tuple> datePrintedComparator() {
        return (o1, o2) -> {
            LocalDate o1Date = o1.get(QBulkPrintData.bulkPrintData.creationDate);
            if (o1Date == null) {
                return 1;
            }
            LocalDate o2Date = o2.get(QBulkPrintData.bulkPrintData.creationDate);
            if (o2Date == null) {
                return -1;
            }
            return o1Date.compareTo(o2Date);
        };
    }

    private static BooleanExpression hasReceivedSummonsLetter() {
        QBulkPrintData summons = new QBulkPrintData("summons");
        return JPAExpressions.selectOne()
            .from(summons)
            .where(summons.jurorNo.eq(QJuror.juror.jurorNumber)
                .and(summons.formAttribute.formType.in(
                    FormCode.ENG_SUMMONS.getCode(),
                    FormCode.BI_SUMMONS.getCode(),
                    FormCode.ENG_DBD_SUMMONS.getCode(),
                    FormCode.BI_DBD_SUMMONS.getCode())))
            .exists();
    }

    LetterType(List<FormCode> formCodes,
               List<ReissueLetterService.DataType> reissueDataTypes,
               Consumer<JPAQuery<Tuple>> letterQueryConsumer,
               Comparator<Tuple> tupleComparator) {
        this.formCodes = formCodes;
        this.reissueDataTypes = reissueDataTypes;
        this.letterQueryConsumer = letterQueryConsumer;
        this.tupleComparator = tupleComparator;
    }

}
