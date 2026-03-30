package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Schema(description = "Coroner Pool add citizens request")
public class CoronerPoolAddCitizenRequestDto implements Serializable {

    @JsonProperty("poolNumber")
    @Size(min = 9, max = 9)
    @NotEmpty
    @Schema(name = "Pool number", description = "Pool Request number")
    private String poolNumber;

    @JsonProperty("locCode")
    @Size(min = 3, max = 3)
    @NotEmpty
    @Schema(name = "Location Code ", description = "Pool Request Location Code")
    private String locCode;

    @JsonProperty("postcodeAndNumbers")
    @NotEmpty
    @Schema(name = "Postcodes and numbers", description = "Postcode and number of citizens to summon")
    private List<PostCodeAndNumbers> postcodeAndNumbers;

    @Getter
    @AllArgsConstructor
    public static class PostCodeAndNumbers {
        @JsonProperty("postcode")
        @NotEmpty
        @Schema(description = "Postcode")
        private String postcode;

        @JsonProperty("numberToAdd")
        @NotNull
        @Schema(description = "Number of citizens to add")
        private Integer numberToAdd;
    }

}