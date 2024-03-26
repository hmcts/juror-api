package uk.gov.hmcts.juror.api.moj.service;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.EntityPathBase;
import lombok.Getter;
import uk.gov.hmcts.juror.api.moj.controller.request.ReissueLetterListRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.ReissueLetterRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.ReissueLetterListResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.QBulkPrintData;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.enumeration.DisqualifyCode;
import uk.gov.hmcts.juror.api.moj.enumeration.ExcusalCodeEnum;
import uk.gov.hmcts.juror.api.moj.exception.MojException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.UnaryOperator;

@SuppressWarnings("unchecked")
public interface ReissueLetterService {

    String REASON = "Reason";

    ReissueLetterListResponseDto reissueLetterList(ReissueLetterListRequestDto request);

    void reissueLetter(ReissueLetterRequestDto request);

    void deletePendingLetter(ReissueLetterRequestDto request);

    @Getter
    enum DataType {
        JUROR_NUMBER(String.class, "Juror number", QJuror.juror.jurorNumber
            .as("juror_number"), List.of(QJuror.class)),
        JUROR_FIRST_NAME(String.class, "First name", QJuror.juror.firstName
            .as("first_name"), List.of(QJuror.class)),
        JUROR_LAST_NAME(String.class, "Last name", QJuror.juror.lastName
            .as("last_name"), List.of(QJuror.class)),
        JUROR_POSTCODE(String.class, "Postcode", QJuror.juror.postcode
            .as("postcode"), List.of(QJuror.class)),
        JUROR_STATUS(String.class, "Status", QJurorPool.jurorPool.status.statusDesc
            .as("status"), List.of(QJurorPool.class)),
        JUROR_DEFERRED_TO(LocalDate.class, "Deferred to", QJurorPool.jurorPool.deferralDate
            .as("deferral_date"), List.of(QJurorPool.class), Object::toString),
        JUROR_POSTPONED_TO(LocalDate.class, "Postponed to", QJurorPool.jurorPool.deferralDate
            .as("postponed_date"), List.of(QJurorPool.class), Object::toString),
        JUROR_DEFERRED_TO_REASON(String.class, REASON, QJurorPool.jurorPool.deferralCode
            .as("deferral_code"),
            List.of(QJurorPool.class), deferralCode -> ExcusalCodeEnum.valueOf((String)deferralCode).getDescription()),
        JUROR_DEFERRAL_REJECTED_REASON(String.class, REASON, QJurorPool.jurorPool.deferralCode.as(
            "deferral_code"),
            List.of(QJurorPool.class), deferralCode -> ExcusalCodeEnum.valueOf((String)deferralCode).getDescription()),
        JUROR_DEFERRAL_DATE_REFUSED(LocalDateTime.class, "Date refused", QJurorHistory.jurorHistory.dateCreated.as(
            "date_refused"), List.of(QJurorHistory.class), dateTime -> {
            if (dateTime == null) {
                return null;
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            return formatter.format((LocalDateTime)dateTime);
        }),
        JUROR_EXCUSAL_REASON(String.class, REASON, QJuror.juror.excusalCode
            .as("excusal_code"),
            List.of(QJuror.class), excusalCode -> ExcusalCodeEnum.valueOf((String) excusalCode).getDescription()),
        JUROR_EXCUSAL_DATE(LocalDate.class, "Date excused", QJuror.juror.excusalDate
            .as("date_excused"), List.of(QJuror.class), Object::toString),
        JUROR_EXCUSAL_DENIED_DATE(LocalDate.class, "Date refused", QJuror.juror.excusalDate
            .as("date_refused"), List.of(QJuror.class), Object::toString),
        JUROR_WITHDRAWAL_DATE(LocalDate.class, "Date disqualified", QJuror.juror.disqualifyDate
            .as("disqualify_date"), List.of(QJurorPool.class), Object::toString),
        JUROR_WITHDRAWAL_REASON(String.class, "Reason", QJuror.juror.disqualifyCode
            .as("disqualify_reason"),
            List.of(QJuror.class), disqualifyCode -> DisqualifyCode.valueOf((String)disqualifyCode).getDescription()),
        DATE_PRINTED(LocalDate.class, "Date printed", QBulkPrintData.bulkPrintData.creationDate
            .as("date_printed"), List.of(QBulkPrintData.class), Object::toString),
        BULK_PRINT_ID(Long.class, "hidden_print_id", QBulkPrintData.bulkPrintData.id
            .as("id"), List.of(QBulkPrintData.class)),
        FORM_CODE(String.class, "hidden_form_code", QBulkPrintData.bulkPrintData.formAttribute.formType
            .as("form_code"), List.of(QBulkPrintData.class)),
        EXTRACTED_FLAG(Boolean.class, "hidden_extracted_flag", QBulkPrintData.bulkPrintData.extractedFlag
            .as("extracted_flag"),
            List.of(QBulkPrintData.class), flag -> {
                if (flag == null) {
                    return false;
                }
                return flag;
            });

        private final Class<?> classType;
        private final String displayText;
        private final Expression<?> expression;
        private final List<Class<? extends EntityPathBase<?>>> entityPaths;
        private final UnaryOperator<Object> transformer;

        <T> DataType(Class<T> classType, String displayText, Expression<T> expression,
                     List<Class<? extends EntityPathBase<?>>> entityPaths) {
            this(classType, displayText, expression, entityPaths, null);
        }

        <T> DataType(Class<T> classType, String displayText, Expression<T> expression,
                     List<Class<? extends EntityPathBase<?>>> entityPaths,
                     UnaryOperator<Object> transformer) {
            this.classType = classType;
            this.displayText = displayText;
            this.expression = expression;
            this.entityPaths = entityPaths;
            this.transformer = transformer;
        }

        public Expression<Object> getExpression() {
            return (Expression<Object>) this.expression;
        }

        public Object transform(Object data) {
            if (this.transformer == null) {
                return data;
            } else if (data == null && this.classType == LocalDate.class) {
                return null;
            }
            return this.transformer.apply(data);
        }

        public String getDataType() {
            if (String.class.equals(classType)) {
                return "string";
            }

            if (LocalDate.class.equals(classType)) {
                return "date";
            }

            if (Boolean.class.equals(classType)) {
                return "boolean";
            }
            if (LocalDateTime.class.equals(classType)) {
                return "date"; // no use case for time so keeping date but can revisit this later
            }
            throw new MojException.InternalServerError("Unknown data type: " + classType, null);
        }
    }
}
