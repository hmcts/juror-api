package uk.gov.hmcts.juror.api.moj.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;

@Entity
@Data
@Table(name = "t_excusal_code", schema = "juror_mod")
@NoArgsConstructor
@AllArgsConstructor
public class ExcusalCode implements Serializable {

    @Id
    @Column(name = "exc_code")
    @Length(max = 1)
    @NotNull
    private String excusalCode;

    @NotNull
    @Column(name = "description")
    private String description;

    @Column(name = "by_right")
    private boolean byRight;

    @Column(name = "enabled")
    private boolean enabled;

    @Column(name = "for_excusal")
    private boolean forExcusal;

    @Column(name = "for_deferral")
    private boolean forDeferral;
}
