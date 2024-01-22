package uk.gov.hmcts.juror.api.moj.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@EqualsAndHashCode
public class CoronerPoolDetailId implements Serializable {

    private String poolNumber;
    private String jurorNumber;
}
