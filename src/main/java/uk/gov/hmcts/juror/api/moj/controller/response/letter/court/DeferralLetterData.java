package uk.gov.hmcts.juror.api.moj.controller.response.letter.court;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@JsonPropertyOrder({"jurorNumber", "firstName", "lastName", "postcode", "status", "deferredTo", "reason",
    "datePrinted", "poolNumber"})
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DeferralLetterData extends LetterResponseData {

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate deferredTo;

    String reason;

    @Builder
    @SuppressWarnings("java:S107")
    DeferralLetterData(String jurorNumber, String firstName, String lastName, String postcode, String status,
                       LocalDate datePrinted, String poolNumber, LocalDate deferredTo, String reason) {
        super(jurorNumber, firstName, lastName, postcode, status, datePrinted, poolNumber);
        this.deferredTo = deferredTo;
        this.reason = reason;
    }

}
