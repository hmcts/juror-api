package uk.gov.hmcts.juror.api.moj.controller.response;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.api.moj.domain.Juror;

import static uk.gov.hmcts.juror.api.validation.ValidationConstants.NO_PIPES_REGEX;

@Builder
@Data
@EqualsAndHashCode
public class NameDetails {

    @Column(name = "title")
    @Length(max = 10)
    @Pattern(regexp = NO_PIPES_REGEX)
    private String title;

    @Column(name = "first_name")
    @Length(max = 20)
    @Pattern(regexp = NO_PIPES_REGEX)
    @NotBlank
    private String firstName;

    @Column(name = "last_name")
    @Length(max = 20)
    @Pattern(regexp = NO_PIPES_REGEX)
    @NotBlank
    private String lastName;

    public static NameDetails from(Juror juror) {
        return NameDetails.builder()
            .title(juror.getTitle())
            .firstName(juror.getFirstName())
            .lastName(juror.getLastName())
            .build();
    }
}
