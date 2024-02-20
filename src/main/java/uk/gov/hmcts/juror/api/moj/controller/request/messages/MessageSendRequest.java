package uk.gov.hmcts.juror.api.moj.controller.request.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.juror.api.moj.domain.messages.MessageType;
import uk.gov.hmcts.juror.api.validation.JurorNumber;
import uk.gov.hmcts.juror.api.validation.PoolNumber;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageSendRequest {
    @JsonProperty("jurors")
    @NotEmpty
    private List<@NotNull @Valid JurorAndSendType> jurors;

    @JsonProperty("placeholder_values")
    private Map<@NotNull String, @NotNull String> placeholderValues;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class JurorAndSendType {

        @JurorNumber
        @NotBlank
        private String jurorNumber;

        @PoolNumber
        @NotBlank
        private String poolNumber;

        @JsonProperty("type")
        @NotNull
        private MessageType.SendType type;

    }
}
