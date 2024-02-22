package uk.gov.hmcts.juror.api.juror.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;

/**
 * Entity representing settings used by the application.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "app_settings", schema = "juror_mod")
@Immutable
@Builder
public class ApplicationSettings implements Serializable {

    @Id
    @Length(max = 80)
    @Column(name = "setting")
    @Enumerated(EnumType.STRING)
    private Setting setting;

    @NotEmpty
    @Length(max = 200)
    @Column(name = "value")
    private String value;


    public enum Setting {
        SEARCH_RESULT_LIMIT_BUREAU_OFFICER,
        SEARCH_RESULT_LIMIT_TEAM_LEADER,
        AUTO_ASSIGNMENT_DEFAULT_CAPACITY,
        WELSH_LANGUAGE_ENABLED,
        TOTAL_NUMBER_SUMMONSES_SENT,
        TOTAL_NUMBER_ONLINE_REPLIES,
        SEND_EMAIL_OR_SMS,
        TRIGGERED_COMMS_EXCUSAL_DAYS,
        TRIGGERED_COMMS_SERVICE_COMPLETED_DAYS,
        WELSH_TRANSLATION,
        SMART_SURVEY_SUMMONS_RESPONSE_SURVEY_ID,
        SMART_SURVEY_SUMMONS_RESPONSE_DAYS,
        SMART_SURVEY_SUMMONS_RESPONSE_EXPORT_NAME,
        PAYMENT_AUTH_CODE;
    }
}
