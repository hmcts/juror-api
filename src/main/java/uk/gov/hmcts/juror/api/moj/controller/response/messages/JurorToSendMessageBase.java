package uk.gov.hmcts.juror.api.moj.controller.response.messages;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
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
@EqualsAndHashCode
@ToString
@JsonSubTypes({
    @JsonSubTypes.Type(value = JurorToSendMessageBureau.class, name = "Bureau"),
    @JsonSubTypes.Type(value = JurorToSendMessageCourt.class, name = "Court")
})
public abstract class JurorToSendMessageBase {

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

    @JsonProperty("date_deferred_to")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateDeferredTo;

    @JsonProperty("welsh_language")
    private boolean welshLanguage;
}
