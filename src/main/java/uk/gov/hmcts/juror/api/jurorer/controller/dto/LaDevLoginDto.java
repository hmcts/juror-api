package uk.gov.hmcts.juror.api.jurorer.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LaDevLoginDto {

    @NotBlank
    @Email
    @Size(max = 200)
    @Schema(description = "Email address of the LA user")
    private String email;
}
