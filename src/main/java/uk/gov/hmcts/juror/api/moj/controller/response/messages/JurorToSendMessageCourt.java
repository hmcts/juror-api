package uk.gov.hmcts.juror.api.moj.controller.response.messages;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@SuperBuilder
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString
public class JurorToSendMessageCourt extends JurorToSendMessageBase {

    @JsonProperty("trial_number")
    private String trialNumber;

    @JsonProperty("on_call")
    private Boolean onCall;

    @JsonProperty("next_due_at_court")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate nextDueAtCourt;

    @JsonProperty("completion_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate completionDate;
}
