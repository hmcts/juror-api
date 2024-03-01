package uk.gov.hmcts.juror.api.moj.enumeration.jurorresponse;

import lombok.Getter;
import uk.gov.hmcts.juror.api.moj.domain.system.HasCodeAndDescription;

@Getter
public enum ReasonableAdjustmentsEnum implements HasCodeAndDescription<String> {
    NONE(" ", "None"),
    
    CARING_RESPONSIBILITIES("C", "Caring responsibilities"),
    ALLERGIES("D", "Allergies"),
    HEARING_LOSS("H", "Hearing Loss"),
    DIABETIC("I", "Diabetic"),
    LIMITED_MOBILITY("L", "Limited mobility"),
    MULTIPLE("M", "Multiple"),
    OTHER("O", "Other"),
    PREGNANCY("P", "Pregnancy/Breastfeeding"),
    READING("R", "Reading"),
    MEDICATION("U", "Medication"),
    VISUAL_IMPAIRMENT("V", "Visual Impairment"),
    WHEELCHAIR_ACCESS("W", "Wheelchair Access"),

    CJS_EMPLOYEE("J","CJS Employee"),
    EPILEPSY("E","Epilepsy"),
    RELIGIOUS_REASONS("A","Religious Reasons"),
    TRAVELLING_DIFFICULTIES("T","Travelling Difficulties");

    private final String code;
    private final String description;

    ReasonableAdjustmentsEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
