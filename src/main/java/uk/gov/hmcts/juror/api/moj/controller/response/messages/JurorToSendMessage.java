package uk.gov.hmcts.juror.api.moj.controller.response.messages;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class JurorToSendMessage {

    @JsonProperty("juror_number")
    private String jurorNumber;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("email")
    private String email;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("pool_number")
    private String poolNumber;

    @JsonProperty("status")
    private String status;

    @JsonProperty("trial_number")
    private String trialNumber;

    @JsonProperty("on_call")
    private Boolean onCall;

    @JsonProperty("next_due_at_court")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate nextDueAtCourt;

    @JsonProperty("date_deferred_to")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateDeferredTo;

    @JsonProperty("completion_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate completionDate;

    @JsonProperty("welsh_language")
    private boolean welshLanguage;
}
