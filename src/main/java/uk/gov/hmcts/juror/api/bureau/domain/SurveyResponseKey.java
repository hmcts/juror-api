package uk.gov.hmcts.juror.api.bureau.domain;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;


/**
 * Composite key for {@link SurveyResponse}.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@EqualsAndHashCode
@Deprecated(forRemoval = true)
public class SurveyResponseKey implements Serializable {
    private String id;
    private String surveyId;
}
