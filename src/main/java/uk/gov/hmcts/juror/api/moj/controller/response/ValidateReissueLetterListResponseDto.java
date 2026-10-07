package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@Valid
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ValidateReissueLetterListResponseDto {

    private List<ValidSummonedJurors> validSummonedJurors;

    private List<InvalidSummonedJurors> invalidSummonedJurors;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @SuperBuilder
    public static class ValidSummonedJurors {
        private String jurorNumber;
        private String firstName;
        private String lastName;
        private String postcode;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @SuperBuilder
    public static class InvalidSummonedJurors extends ValidSummonedJurors {
        private String errorMessage;
    }

}
