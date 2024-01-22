package uk.gov.hmcts.juror.api.moj.enumeration.trial;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PanelResult {

    NOT_USED("NU", "Not used"),
    CHALLENGED("CD", "Challenged"),
    JUROR("J", "Empanelled on a Jury"),
    RETURNED("R", "Returned to in waiting");

    private final String code;
    private final String description;


}
