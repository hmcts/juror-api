package uk.gov.hmcts.juror.api.moj.domain.authentication;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.juror.api.moj.domain.User;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserDetailsSimpleDto {
    @NotBlank
    private String username;
    @NotBlank
    private String email;
    @NotBlank
    private String name;

    public UserDetailsSimpleDto(User user) {
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.name = user.getName();
    }
}
