package uk.gov.hmcts.juror.api.moj.client.contracts;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import static uk.gov.hmcts.juror.api.validation.ValidationConstants.JUROR_NUMBER;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.POSTCODE_REGEX;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.SHORT_DATE_STRING_REGEX;

public interface PncCheckServiceClient extends Client {

    void checkJuror(String jurorNumber);

    @Data
    @Builder
    class NameDetails {
        @JsonProperty("first_name")
        @NotBlank
        private String firstName;

        @JsonProperty("middle_name")
        private String middleName;

        @JsonProperty("last_name")
        @NotBlank
        private String lastName;
    }

    @Builder
    @Data
    @ToString
    class JurorCheckRequest {

        @JsonProperty("juror_number")
        @NotBlank
        @Pattern(regexp = JUROR_NUMBER)
        private String jurorNumber;

        @JsonProperty("date_of_birth")
        @Pattern(regexp = SHORT_DATE_STRING_REGEX)
        @NotNull
        private String dateOfBirth;

        @JsonProperty("post_code")
        @NotBlank
        @Pattern(regexp = POSTCODE_REGEX)
        private String postCode;

        @NotNull
        @JsonProperty("name")
        private NameDetails name;
    }
}
