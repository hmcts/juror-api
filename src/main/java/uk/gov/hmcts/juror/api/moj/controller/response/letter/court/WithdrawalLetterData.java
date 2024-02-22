package uk.gov.hmcts.juror.api.moj.controller.response.letter.court;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@JsonPropertyOrder({"jurorNumber", "firstName", "lastName", "postcode", "status", "dateDisqualified", "reason",
    "datePrinted", "poolNumber"})
public class WithdrawalLetterData extends LetterResponseData {

    @JsonProperty("date_disqualified")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate dateDisqualified;

    @JsonProperty("reason")
    String reason;

    @Builder
    @SuppressWarnings("java:S107")
    WithdrawalLetterData(String jurorNumber, String firstName, String lastName, String postcode, String status,
                         LocalDate datePrinted, String poolNumber, LocalDate dateDisqualified, String reason) {
        super(jurorNumber, firstName, lastName, postcode, status, datePrinted, poolNumber);
        this.dateDisqualified = dateDisqualified;
        this.reason = reason;
    }

}
