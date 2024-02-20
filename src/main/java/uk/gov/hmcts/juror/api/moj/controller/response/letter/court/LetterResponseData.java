package uk.gov.hmcts.juror.api.moj.controller.response.letter.court;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public abstract class LetterResponseData {

    @JsonProperty("juror_number")
    String jurorNumber;

    @JsonProperty("first_name")
    String firstName;

    @JsonProperty("last_name")
    String lastName;

    @JsonProperty("postcode")
    String postcode;

    @JsonProperty("status")
    String status;

    @JsonProperty("date_printed")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate datePrinted;

    @JsonProperty("pool_number")
    String poolNumber;
}
