package uk.gov.hmcts.juror.api.moj.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class FilterPoolMember implements Serializable {

    @Id
    @NotNull
    @JsonProperty("juror_number")
    private String jurorNumber;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("postcode")
    private String postcode;

    @JsonProperty("attendance")
    private String attendance;

    @JsonProperty("checked_in")
    private LocalTime checkedIn;

    @JsonProperty("checked_in_today")
    private Boolean checkedInToday;

    @JsonProperty("next_date")
    private LocalDate nextDate;

    @JsonProperty("status")
    private String status;
}
