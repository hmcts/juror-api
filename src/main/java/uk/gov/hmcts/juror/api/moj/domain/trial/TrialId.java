package uk.gov.hmcts.juror.api.moj.domain.trial;

import jakarta.persistence.JoinColumn;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@EqualsAndHashCode
public class TrialId implements Serializable {


    private String trialNumber;

    @JoinColumn(name = "loc_code", nullable = false)
    private CourtLocation courtLocation;

}
