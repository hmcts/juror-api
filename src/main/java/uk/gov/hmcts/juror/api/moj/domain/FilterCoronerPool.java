package uk.gov.hmcts.juror.api.moj.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import uk.gov.hmcts.juror.api.validation.ValidationConstants;

import java.io.Serializable;
import java.time.LocalDate;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class FilterCoronerPool implements Serializable {

    @Id
    @NotNull
    @JsonProperty("pool_number")
    private String poolNumber;

    @JsonProperty("court_name")
    private String courtName;

    @JsonProperty("requested_date")
    @JsonFormat(pattern = ValidationConstants.DATE_FORMAT)
    private LocalDate requestedDate;

    @JsonProperty("requested_by")
    private String requestedBy;
}
