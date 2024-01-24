package uk.gov.hmcts.juror.api.moj.enumeration.jurorresponse;

import lombok.Getter;

@Getter
public enum ReasonableAdjustmentsEnum {
    
    CHILDCARE_PROBLEMS("C", "Childcare Problems"),
    DIET("D", "Diet"),
    HEARING_IMPAIRMENT("H", "Hearing Impairment"),
    DIABETIC("I", "Diabetic"),
    LIMITED_MOBILITY("L", "Limited mobility"),
    MULTIPLE("M", "Multiple"),
    OTHER("O", "Other"),
    PREGNANCY("P", "Pregnancy"),
    READING("R", "Reading"),
    DRUG_DEPENDENT("U", "Drug dependent"),
    VISUAL_IMPAIRMENT("V", "Visual Impairment"),
    WHEELCHAIR_ACCESS("W", "Wheelchair Access");

    private final String code;
    private final String description;

    ReasonableAdjustmentsEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
