package uk.gov.hmcts.juror.api.moj.controller.response.letter.court;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Valid
@SuperBuilder
@NoArgsConstructor
public class LetterListResponseDto {

    @JsonProperty("headings")
    private List<String> headings;

    @JsonProperty("data_types")
    private List<String> dataTypes;

    @JsonProperty("data")
    @JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
    @JsonSubTypes({
        @JsonSubTypes.Type(value = DeferralLetterData.class, name = "Deferral Letter Data"),
        @JsonSubTypes.Type(value = NonDeferralLetterData.class, name = "Deferral Denied Letter Data")
    })
    private List<? extends LetterResponseData> data;

}
