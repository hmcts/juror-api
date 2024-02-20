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
 * juror_mod.courtroom table entity.
 */
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "courtroom", schema = "juror_mod")
public class Courtroom implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "courtroom_gen")
    @SequenceGenerator(name = "courtroom_gen", sequenceName = "juror_mod.courtroom_id_seq",
        allocationSize = 1)
    private long id;

    @Column(name = "owner")
    @Length(min = 3, max = 3)
    private String owner;

    @Column(name = "room_number")
    @Length(max = 6)
    private String roomNumber;

    @Column(name = "description")
    @Length(max = 30)
    private String description;

}

