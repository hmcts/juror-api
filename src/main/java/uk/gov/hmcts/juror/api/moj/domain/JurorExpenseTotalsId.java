package uk.gov.hmcts.juror.api.moj.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@EqualsAndHashCode
@Immutable
public class JurorExpenseTotalsId implements Serializable {

    private String jurorNumber;
    private String poolNumber;

}
