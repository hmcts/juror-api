package uk.gov.hmcts.juror.api.moj.controller.response.letter.court;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@JsonPropertyOrder({"jurorNumber", "firstName", "lastName", "poolNumber", "startDate", "completionDate", "datePrinted"})
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CertificateOfAttendanceLetterData extends LetterResponseData {

    @JsonProperty("start_date")
    LocalDate startDate;
    @JsonProperty("completion_date")
    LocalDate completionDate;


    @Builder
    CertificateOfAttendanceLetterData(String jurorNumber, String firstName, String lastName,
                                      LocalDate datePrinted, String poolNumber, LocalDate startDate,
                                      LocalDate completionDate) {
        super(jurorNumber, firstName, lastName, null, null, datePrinted, poolNumber);
        this.startDate = startDate;
        this.completionDate = completionDate;
    }

}
