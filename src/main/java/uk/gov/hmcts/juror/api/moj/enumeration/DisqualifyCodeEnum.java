package uk.gov.hmcts.juror.api.moj.enumeration;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Disqualify codes (reasons) specific to modernisation.
 * These will need to be mapped to heritage values when disqualifying a juror to ensure backward compatibility
 * and no impact to downstream systems.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum DisqualifyCodeEnum {
    A("A", "Age", "A", "Less Than Eighteen Years of Age or Over 75"),
    B("B", "Bail", "B", "On Bail"),
    C("C", "Conviction", "C", "Has Been Convicted of an Offence"),
    N("N", "Mental Capacity Act", "M", "Suffering From a Mental Disorder"),
    O("O", "Mental Health Act", "M", "Suffering From a Mental Disorder"),
    R("R", "Residency", "R", "Not Resident for the Appropriate Period");

    private final String code;
    private final String description;
    private final String heritageCode;
    private final String heritageDescription;

}
