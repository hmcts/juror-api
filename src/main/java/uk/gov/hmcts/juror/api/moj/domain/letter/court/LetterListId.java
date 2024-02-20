package uk.gov.hmcts.juror.api.moj.domain.letter.court;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
public class LetterListId implements Serializable {

    private String owner;
    private String poolNumber;
    private String jurorNumber;
    private int rowNumber;

}
