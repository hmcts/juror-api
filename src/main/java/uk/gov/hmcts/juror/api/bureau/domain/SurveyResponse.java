package uk.gov.hmcts.juror.api.bureau.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;
import java.util.Date;

/**
 * Entity representing survey response data.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Immutable
@IdClass(SurveyResponseKey.class)
@Table(name = "survey_response", schema = "juror_dashboard")
@Builder
@Deprecated(forRemoval = true)
public class SurveyResponse implements Serializable {

    @Id
    @NotNull
    @Column(name = "ID")
    private String id;

    @Id
    @NotNull
    @Column(name = "SURVEY_ID")
    private String surveyId;

    @Column(name = "USER_NO")
    private Integer userNo;

    @Column(name = "SURVEY_RESPONSE_DATE")
    private Date surveyResponseDate;

    @Column(name = "SATISFACTION_DESC")
    private String satisfactionDesc;

    @Override
    public String toString() {
        return "SurveyResponse [id=" + id + ", surveyId=" + surveyId + ", userNo=" + userNo + ", surveyResponseDate="
            + surveyResponseDate + ", satisfactionDesc=" + satisfactionDesc + "]";
    }

}
