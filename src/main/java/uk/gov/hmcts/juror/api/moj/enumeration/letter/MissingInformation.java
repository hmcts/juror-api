package uk.gov.hmcts.juror.api.moj.enumeration.letter;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public enum MissingInformation {

    DATE_OF_BIRTH(
        "Date of birth",
        LetterCategory.JUROR_DETAILS.getCategoryName(),
        "Part 1 Date of Birth",
        "Rhan 1 Dyddiad Geni"
    ),
    TELEPHONE_NO(
        "Telephone number",
        LetterCategory.JUROR_DETAILS.getCategoryName(),
        "Part 1 Telephone No.",
        "Rhan 1 Rhif FFon."
    ),
    BAIL(
        "On bail for a criminal offence",
        LetterCategory.ELIGIBILITY.getCategoryName(),
        "Part 2 Section A",
        "Rhan 2 Adran A"
    ),
    CONVICTIONS(
        "Found guilty and given prison sentence, community order or suspended sentence",
        LetterCategory.ELIGIBILITY.getCategoryName(),
        "Part 2 Section B",
        "Rhan 2 Adran B"
    ),
    MENTAL_HEALTH_ACT(
        "Mental Health Act",
        LetterCategory.ELIGIBILITY.getCategoryName(),
        "Part 2 Section C",
        "Rhan 2 Adran C"
    ),
    MENTAL_CAPACITY_ACT(
        "Mental Capacity Act",
        LetterCategory.ELIGIBILITY.getCategoryName(),
        "Part 2 Section C",
        "Rhan 2 Adran C"
    ),
    CJS_EMPLOYMENT_5_YEARS(
        "Juror worked in CJS in last 5 years",
        LetterCategory.CJS_EMPLOYMENT.getCategoryName(),
        "Part 2 Section D",
        "Rhan 2 Adran D"
    ),
    RESIDENCY(
        "Residency",
        LetterCategory.ELIGIBILITY.getCategoryName(),
        "Part 2 Section E",
        "Rhan 2 Adran E"
    ),
    SERVE_ON_DATE(
        "Serve on the date shown in summons",
        LetterCategory.REPLY_TYPE.getCategoryName(),
        "Part 3 Section A/B/C",
        "Rhan 3 Adran A/B/C"
    ),
    DISABILITY_OR_IMPAIRMENT(
        "Juror have disability or impairment and require extra support",
        LetterCategory.REASONABLE_ADJUSTMENTS.getCategoryName(),
        "Part 4",
        "Rhan 4"
    ),

    SIGNATURE("", "", "", "");

    private final String description;
    private String category;
    private final String englishTranslation;
    private final String welshTranslation;

    public static String buildMissingInformationString(List<MissingInformation> missingInformation,
                                                       boolean welshTranslation) {

        StringBuilder missingInformationString = new StringBuilder();
        String separator = "";

        for (MissingInformation information : missingInformation) {
            if (!welshTranslation) {
                missingInformationString.append(separator).append(information.getEnglishTranslation());
            } else {
                missingInformationString.append(separator).append(information.getWelshTranslation());
            }
            separator = ", ";
        }
        return missingInformationString.toString();
    }
}
