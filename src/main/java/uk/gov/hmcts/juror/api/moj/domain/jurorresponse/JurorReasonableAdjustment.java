package uk.gov.hmcts.juror.api.moj.domain.jurorresponse;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;

@Entity
@Data
@Table(name = "juror_reasonable_adjustment", schema = "juror_mod")
public class JurorReasonableAdjustment implements Serializable {
    @Column(name = "juror_number")
    @Length(max = 9)
    private String jurorNumber;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "reasonable_adjustment")
    private ReasonableAdjustments reasonableAdjustment;

    @Column(name = "reasonable_adjustment_detail")
    private String reasonableAdjustmentDetail;

    @Id
    @GeneratedValue(generator = "juror_reasonable_adjustment_gen", strategy = GenerationType.SEQUENCE)
    @Column(name = "id")
    @SequenceGenerator(name = "juror_reasonable_adjustment_gen", schema = "juror_mod",
        sequenceName = "juror_reasonable_adjustment_id_seq", allocationSize = 1)
    private Long id;
}
