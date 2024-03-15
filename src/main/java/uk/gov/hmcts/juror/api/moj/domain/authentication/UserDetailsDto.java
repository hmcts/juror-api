package uk.gov.hmcts.juror.api.moj.domain.authentication;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.juror.api.moj.domain.Role;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.domain.UserType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserDetailsDto {
    @NotBlank
    private String username;
    @NotBlank
    private String email;
    @NotBlank
    private String name;
    @NotNull
    private Boolean isActive;
    private LocalDateTime lastSignIn;
    private UserType userType;

    private Set<Role> roles;
    private List<UserCourtDto> courts;

    public UserDetailsDto(User user, List<UserCourtDto> courts) {
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.name = user.getName();
        this.isActive = user.isActive();
        this.lastSignIn = user.getLastLoggedIn();
        this.userType = user.getUserType();
        this.roles = user.getRoles();
        this.courts = courts;
    }
}
