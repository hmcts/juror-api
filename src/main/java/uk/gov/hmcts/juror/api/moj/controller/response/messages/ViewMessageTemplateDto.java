package uk.gov.hmcts.juror.api.moj.controller.response.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.juror.api.moj.domain.messages.DataType;
import uk.gov.hmcts.juror.api.moj.domain.messages.MessageType;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ViewMessageTemplateDto {
    @JsonProperty("message_template_english")
    private String messageTemplateEnglish;

    @JsonProperty("message_template_welsh")
    private String messageTemplateWelsh;

    @JsonProperty("placeholders")
    private List<Placeholder> placeholders;

    @JsonProperty("send_type")
    private MessageType.SendType sendType;

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @EqualsAndHashCode
    @Getter
    public static class Placeholder {
        @JsonProperty("display_name")
        private String displayName;

        @JsonProperty("placeholder_name")
        private String placeholderName;

        @JsonProperty("data_type")
        private DataType dataType;

        @JsonProperty("editable")
        private boolean editable;

        @JsonProperty("default_value")
        private String defaultValue;
    }
}
