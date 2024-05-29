package uk.gov.hmcts.juror.api.moj.domain.trial;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.enumeration.trial.TrialType;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * juror_mod.trial table entity.
 */
@Entity
@NoArgsConstructor
@AllArgsConstructor
@IdClass(TrialId.class)
@Builder
@Getter
@Setter
@Table(name = "trial", schema = "juror_mod")
public class Trial implements Serializable {

    @Id
    @Column(name = "trial_number")
    @Length(max = 16)
    @NotBlank
    private String trialNumber;

    @Id
    @NotNull
    @ManyToOne
    @JoinColumn(name = "loc_code", nullable = false)
    private CourtLocation courtLocation;

    @Column(name = "description")
    @Length(max = 50)
    @NotBlank
    private String description;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "courtroom", nullable = false)
    private Courtroom courtroom;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "judge", nullable = false)
    private Judge judge;

    @Column(name = "trial_type")
    @NotNull
    @Enumerated(EnumType.STRING)
    private TrialType trialType;

    @Column(name = "trial_start_date")
    private LocalDate trialStartDate;

    @Column(name = "trial_end_date")
    private LocalDate trialEndDate;

    @Column(name = "juror_requested")
    @Deprecated(since = "Old Heritage column")
    private Integer jurorRequested;

    @Column(name = "jurors_sent")
    @Deprecated(since = "Old Heritage column")
    private Integer jurorsSent;

    @Column(name = "anonymous")
    private Boolean anonymous;

}
