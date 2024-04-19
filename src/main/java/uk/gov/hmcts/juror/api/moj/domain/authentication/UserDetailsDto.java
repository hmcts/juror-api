package uk.gov.hmcts.juror.api.moj.domain.authentication;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.juror.api.moj.domain.Role;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.domain.UserType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserDetailsDto extends UserDetailsSimpleDto {
    @NotNull
    private Boolean isActive;
    private LocalDateTime lastSignIn;
    private UserType userType;

    private Set<Role> roles;
    private List<UserCourtDto> courts;

    // use this constructor to create a UserDetailsDto object from a User object when returning
    // Paper or Digital response or there will be an infinite recursion
    public UserDetailsDto(User user) {
        this(user, null);
    }

    public UserDetailsDto(User user, List<UserCourtDto> courts) {
        super(user);
        this.isActive = user.isActive();
        this.lastSignIn = user.getLastLoggedIn();
        this.userType = user.getUserType();
        this.roles = user.getRoles();
        this.courts = courts;
    }
}
