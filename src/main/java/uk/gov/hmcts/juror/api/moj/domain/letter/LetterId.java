package uk.gov.hmcts.juror.api.moj.domain.letter;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
public class LetterId implements Serializable {

    private String owner;
    private String jurorNumber;

}
