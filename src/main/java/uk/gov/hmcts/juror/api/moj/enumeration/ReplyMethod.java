package uk.gov.hmcts.juror.api.moj.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ReplyMethod {
    PAPER("Paper"),
    DIGITAL("Digital");

    private String description;

}
