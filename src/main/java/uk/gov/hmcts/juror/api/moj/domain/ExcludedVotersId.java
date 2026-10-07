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
public class ExcludedVotersId implements Serializable {

    private String firstName;
    private String lastName;
    private String address1;
    private String postcode;
}
