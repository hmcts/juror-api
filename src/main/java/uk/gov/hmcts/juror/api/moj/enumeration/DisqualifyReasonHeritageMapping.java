package uk.gov.hmcts.juror.api.moj.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DisqualifyReasonHeritageMapping {

    //Mapping required for heritage (back) compatibility and to minimise downstream impact
    BAIL(
        "B",
        "On Bail",
        DisqualifyCode.getCode(DisqualifyCode.B),
        DisqualifyCode.getDescription(DisqualifyCode.B)
    );

    private final String heritageCode;
    private final String heritageDescription;
    private final String newCode;
    private final String newDescription;

    //TODO: TO BE IMPLEMENTED
}
