package uk.gov.hmcts.juror.api.moj.domain.trial;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;

/**
 * juror_mod.judge table entity.
 */
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "judge", schema = "juror_mod")
public class Judge implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "judge_gen")
    @SequenceGenerator(name = "judge_gen", sequenceName = "juror_mod.judge_id_seq", allocationSize = 1)
    private long id;

    @Column(name = "owner")
    @Length(min = 3, max = 3)
    private String owner;

    @Column(name = "code")
    @Length(max = 4)
    private String code;

    @Column(name = "description")
    @Length(max = 30)
    private String description;

}
