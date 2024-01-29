package uk.gov.hmcts.juror.api.moj.enumeration.letter;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import lombok.Getter;
import uk.gov.hmcts.juror.api.moj.domain.FormCode;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.service.ReissueLetterService;

import java.util.List;
import java.util.function.Consumer;

@Getter
public enum LetterType {

    SUMMONS(List.of(FormCode.ENG_SUMMONS,FormCode.BI_SUMMONS), List.of()),  // ToDo: implement in future story

    CONFIRMATION(List.of(FormCode.ENG_CONFIRMATION, FormCode.BI_CONFIRMATION), List.of()), // ToDo: implement in future story

    DEFERRAL(List.of(FormCode.ENG_DEFERRAL, FormCode.BI_DEFERRAL), List.of(
        ReissueLetterService.DataType.JUROR_NUMBER,
        ReissueLetterService.DataType.JUROR_FIRST_NAME,
        ReissueLetterService.DataType.JUROR_LAST_NAME,
        ReissueLetterService.DataType.JUROR_POSTCODE,
        ReissueLetterService.DataType.JUROR_STATUS,
        ReissueLetterService.DataType.JUROR_DEFERRED_TO,
        ReissueLetterService.DataType.JUROR_DEFERRED_TO_REASON,
        ReissueLetterService.DataType.DATE_PRINTED,
        ReissueLetterService.DataType.FORM_CODE),
        tupleJPAQuery -> tupleJPAQuery.where(QJurorPool.jurorPool.status.status.eq(IJurorStatus.DEFERRED)));

    List<FormCode> formCodes;

    private final List<ReissueLetterService.DataType> reissueDataTypes;
    private final Consumer<JPAQuery<Tuple>> letterQueryConsumer;

    LetterType(List<FormCode> formCodes,
               List<ReissueLetterService.DataType> reissueDataTypes) {
        this(formCodes, reissueDataTypes, null);
    }

    LetterType(List<FormCode> formCodes,
               List<ReissueLetterService.DataType> reissueDataTypes,
               Consumer<JPAQuery<Tuple>> letterQueryConsumer) {
        this.formCodes = formCodes;
        this.reissueDataTypes = reissueDataTypes;
        this.letterQueryConsumer = letterQueryConsumer;
    }

}
