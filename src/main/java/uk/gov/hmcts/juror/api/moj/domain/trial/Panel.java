package uk.gov.hmcts.juror.api.moj.domain.trial;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.enumeration.trial.PanelResult;
import uk.gov.hmcts.juror.api.moj.enumeration.trial.PanelResultConverter;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.annotation.Nullable;

/**
 * juror_mod.panel table entity.
 */
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@IdClass(PanelId.class)
@Table(name = "juror_trial", schema = "juror_mod")
public class Panel implements Serializable {

    @JoinColumn(name = "loc_code")
    @JoinColumn(name = "trial_number")
    @ManyToOne
    @Id
    private Trial trial;

    @JoinColumn(name = "juror_number")
    @NotNull
    @ManyToOne
    @Id
    private Juror juror;

    @Column(name = "rand_number")
    @Deprecated(since = "Old Heritage column")
    private Long randomNumber;

    @Column(name = "date_selected")
    @NotNull
    private LocalDateTime dateSelected;

    /**
     /* The date the juror started sitting on the trial.
     */
    @Column(name = "empanelled_date")
    private LocalDate empanelledDate;

    /**
     /* The date the juror was returned from the trial and no longer sitting.
     */
    @Column(name = "return_date")
    private LocalDate returnDate;

    @Column(name = "result")
    @Convert(converter = PanelResultConverter.class)
    private PanelResult result;

    /**
    /* boolean flag to indicate whether empanelment has taken place (true = empanelment completed).
    */
    @Column(name = "completed")
    private boolean completed;

    public String getJurorNumber() {
        return this.getJuror().getJurorNumber();
    }

}

