package uk.gov.hmcts.juror.api.moj.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class FilterJurorRecord implements Serializable {

    @NotNull
    @JsonProperty("juror_number")
    private String jurorNumber;

    @JsonProperty("juror_name")
    private String jurorName;

    @JsonProperty("postcode")
    private String postcode;

    @JsonProperty("pool_number")
    private String poolNumber;

    @JsonProperty("court_name")
    private String courtName;

    @JsonProperty("status")
    private String status;

}
