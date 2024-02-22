package uk.gov.hmcts.juror.api.moj.controller.response.letter.court;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@JsonPropertyOrder({"jurorNumber", "firstName", "lastName", "postcode", "status", "postponedTo", "reason",
    "datePrinted", "poolNumber"})
public class PostponeLetterData extends LetterResponseData {

    @JsonProperty("postponed_to")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate postponedTo;

    @JsonProperty("reason")
    String reason;

    @Builder
    @SuppressWarnings("java:S107")
    PostponeLetterData(String jurorNumber, String firstName, String lastName, String postcode, String status,
                       LocalDate datePrinted, String poolNumber, LocalDate postponedTo, String reason) {
        super(jurorNumber, firstName, lastName, postcode, status, datePrinted, poolNumber);
        this.postponedTo = postponedTo;
        this.reason = reason;
    }

}