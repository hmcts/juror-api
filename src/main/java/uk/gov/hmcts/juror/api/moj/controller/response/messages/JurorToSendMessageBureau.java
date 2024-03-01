package uk.gov.hmcts.juror.api.moj.controller.response.messages;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString
public class JurorToSendMessageBureau extends JurorToSendMessageBase {

}
