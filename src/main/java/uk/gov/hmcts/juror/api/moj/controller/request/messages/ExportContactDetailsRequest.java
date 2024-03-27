package uk.gov.hmcts.juror.api.moj.controller.request.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.dsl.DatePath;
import com.querydsl.core.types.dsl.StringPath;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.hibernate.validator.constraints.UniqueElements;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorAndPoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;

@Data
@Builder
public class ExportContactDetailsRequest {

    @JsonProperty("export_items")
    @UniqueElements
    @NotEmpty
    @Valid
    private List<@NotNull ExportItems> exportItems;

    @JsonProperty("jurors")
    @NotEmpty
    @Valid
    private List<@NotNull JurorAndPoolRequest> jurors;


    @Getter
    public enum ExportItems {
        JUROR_NUMBER("Juror Number", QJuror.juror.jurorNumber),
        TITLE("Title", QJuror.juror.title),
        FIRST_NAME("First Name", QJuror.juror.firstName),
        LAST_NAME("Last Name", QJuror.juror.lastName),
        EMAIL("Email", QJuror.juror.email),
        MAIN_PHONE("Main Phone", QJuror.juror.phoneNumber),
        OTHER_PHONE("Other Phone", QJuror.juror.altPhoneNumber),
        WORK_PHONE("Work Phone", QJuror.juror.workPhone),

        ADDRESS_LINE_1("Address Line 1", QJuror.juror.addressLine1),
        ADDRESS_LINE_2("Address Line 2", QJuror.juror.addressLine2),
        ADDRESS_LINE_3("Address Line 3", QJuror.juror.addressLine3),
        ADDRESS_LINE_4("Address Line 4", QJuror.juror.addressLine4),
        ADDRESS_LINE_5("Address Line 5", QJuror.juror.addressLine5),

        POSTCODE("Postcode", QJuror.juror.postcode),
        WELSH_LANGUAGE("Welsh language", QJuror.juror.welsh),
        STATUS("Status", QJurorPool.jurorPool.status, tuple -> {
            JurorStatus jurorStatus = tuple.get(QJurorPool.jurorPool.status);
            if (jurorStatus == null) {
                return null;
            } else {
                return String.valueOf(jurorStatus.getStatus());
            }
        }),
        POOL_NUMBER("Pool Number", QJurorPool.jurorPool.pool.poolNumber),
        NEXT_DUE_AT_COURT_DATE("Next due at court date", QJurorPool.jurorPool.nextDate),
        DATE_DEFERRED_TO("Date deferred to", QJurorPool.jurorPool.deferralDate),
        COMPLETION_DATE("Completion date", QJuror.juror.completionDate);

        private final String title;

        private final Expression<?> expression;
        private final Function<Tuple, String> asStringFunction;

        ExportItems(String title, Expression<?> expression, Function<Tuple, String> asStringFunction) {
            this.expression = expression;
            this.title = title;
            this.asStringFunction = asStringFunction;
        }

        ExportItems(String title, DatePath<LocalDate> expression) {
            this(title, expression, tuple -> {
                LocalDate localDate = tuple.get(expression);
                if (localDate == null) {
                    return null;
                } else {
                    return localDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                }
            });
        }

        ExportItems(String title, StringPath expression) {
            this(title, expression, tuple -> tuple.get(expression));
        }

        ExportItems(String title, BooleanPath expression) {
            this(title, expression, tuple -> String.valueOf(tuple.get(expression)));
        }


        public String getAsString(Tuple tuple) {
            return getAsStringFunction().apply(tuple);
        }
    }
}