package uk.gov.hmcts.juror.api.moj.domain.authentication;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.juror.api.moj.domain.QUser;
import uk.gov.hmcts.juror.api.moj.domain.SortMethod;
import uk.gov.hmcts.juror.api.moj.domain.UserType;
import uk.gov.hmcts.juror.api.moj.service.IsPageable;
import uk.gov.hmcts.juror.api.validation.CourtLocationCode;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserSearchDto implements IsPageable {

    private String userName;
    @CourtLocationCode
    private String court;
    private UserType userType;

    private boolean onlyActive;
    private SortMethod sortMethod;
    private SortField sortField;

    @Min(1)
    @JsonProperty("page_number")
    private long pageNumber;

    @Min(1)
    @JsonProperty("page_limit")
    private long pageLimit;

    @Getter
    public enum SortField implements SortMethod.HasComparableExpression {
        NAME(QUser.user.name),
        EMAIL(QUser.user.email),
        USER_TYPE(QUser.user.userType),
        COURT(QUser.user.courts.any().owner),
        LAST_SIGNED_IN(QUser.user.lastLoggedIn),
        ACTIVE(QUser.user.active),
        MANAGER(ExpressionUtils.path(Boolean.class, "is_manager")),
        SENIOR_JUROR_OFFICER(ExpressionUtils.path(Boolean.class, "is_senior_juror_officer"));

        private final Expression<? extends Comparable<?>> comparableExpression;

        SortField(Expression<? extends Comparable<?>> comparableExpression) {
            this.comparableExpression = comparableExpression;
        }
    }
}
