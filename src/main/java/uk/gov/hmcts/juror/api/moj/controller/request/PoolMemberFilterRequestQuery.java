package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.Expressions;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.QJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.SortMethod;
import uk.gov.hmcts.juror.api.moj.service.IsPageable;
import uk.gov.hmcts.juror.api.validation.PoolNumber;

import java.util.List;

@Data
@Builder
@Schema(description = "Pool member filter request")
public class PoolMemberFilterRequestQuery implements IsPageable {

    @JsonProperty("pool_number")
    @NotNull
    @Schema(name = "Pool number", description = "Number of pool to fetch members of")
    @PoolNumber
    private String poolNumber;

    @JsonProperty("juror_number")
    @Schema(name = "Juror number", description = "Partial juror number to search for")
    @Length(max = 9)
    @Pattern(regexp = "[0-9]*")
    private String jurorNumber;

    @JsonProperty("first_name")
    @Schema(name = "First name", description = "Partial first name to search for")
    @Length(max = 20)
    private String firstName;

    @JsonProperty("last_name")
    @Schema(name = "Last name", description = "Partial last name to search for")
    @Length(max = 20)
    private String lastName;

    @JsonProperty("attendance")
    @Schema(name = "Attendance", description = "Attendance values to search for")
    private List<AttendanceEnum> attendance;

    @JsonProperty("checked_in")
    @Schema(name = "Checked in", description = "Member was checked in today")
    private Boolean checkedIn;

    @JsonProperty("next_due")
    @Schema(name = "Next due in court", description =
        "If present, searches members who have a date set for true, or don't have date set for false")
    private List<String> nextDue;

    @JsonProperty("status")
    @Schema(name = "Statuses", description = "List of statuses to filter pool members by")
    private List<String> statuses;

    @JsonProperty("sort_method")
    private SortMethod sortMethod;

    @JsonProperty("sort_field")
    private PoolMemberFilterRequestQuery.SortField sortField;

    @Min(1)
    @JsonProperty("page_limit")
    @Schema(name = "Page limit", description = "Number of items per page")
    private long pageLimit;

    @Min(1)
    @JsonProperty("page_number")
    @Schema(name = "Page number", description = "Page number to fetch (1-indexed)")
    private long pageNumber;

    @Getter
    public enum SortField implements SortMethod.HasComparableExpression {
        JUROR_NUMBER(QJurorPool.jurorPool.juror.jurorNumber),
        FIRST_NAME(QJurorPool.jurorPool.juror.firstName),
        LAST_NAME(QJurorPool.jurorPool.juror.lastName),
        NEXT_DATE(QJurorPool.jurorPool.nextDate),
        POSTCODE(QJurorPool.jurorPool.juror.postcode),
        ATTENDANCE(Expressions.stringPath("attendance")),
        CHECKED_IN(Expressions.booleanPath("checked_in_today")),
        STATUS(QJurorStatus.jurorStatus.statusDesc);

        private final Expression<? extends Comparable<?>> comparableExpression;

        SortField(Expression<? extends Comparable<?>> comparableExpression) {
            this.comparableExpression = comparableExpression;
        }
    }

    @Getter
    public enum AttendanceEnum {
        ON_CALL("on call"),
        ON_A_TRIAL("on a trial"),
        IN_ATTENDANCE("in attendance"),
        OTHER("other");

        private final String keyString;

        AttendanceEnum(String keyString) {
            this.keyString = keyString;
        }
    }
}
