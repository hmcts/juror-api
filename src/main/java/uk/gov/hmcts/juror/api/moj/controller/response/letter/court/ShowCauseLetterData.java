package uk.gov.hmcts.juror.api.moj.controller.response.letter.court;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonPropertyOrder({"jurorNumber", "firstName", "lastName", "postcode", "status", "datePrinted", "poolNumber",
    "absentDate"})
public class ShowCauseLetterData extends LetterResponseData {

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate absentDate;

    @Builder
    @SuppressWarnings("java:S107")
    ShowCauseLetterData(String jurorNumber, String firstName, String lastName, String postcode, String status,
                        LocalDate datePrinted, String poolNumber, LocalDate absentDate) {
        super(jurorNumber, firstName, lastName, postcode, status, datePrinted, poolNumber);
        this.absentDate = absentDate;
    }
}