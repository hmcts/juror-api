package uk.gov.hmcts.juror.api.moj.controller.response.messages;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
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
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class JurorToSendMessageCourt extends JurorToSendMessageBase {

    private String trialNumber;

    private Boolean onCall;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate nextDueAtCourt;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate completionDate;
}
