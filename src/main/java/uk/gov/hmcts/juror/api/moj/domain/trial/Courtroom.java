package uk.gov.hmcts.juror.api.moj.domain.trial;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;

import java.io.Serializable;

/**
 * juror_mod.courtroom table entity.
 */
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString(exclude = "courtLocation")
@EqualsAndHashCode(exclude = {"courtLocation"})
@Builder
@Table(name = "courtroom", schema = "juror_mod")
public class Courtroom implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "courtroom_gen")
    @SequenceGenerator(name = "courtroom_gen", sequenceName = "juror_mod.courtroom_id_seq",
        allocationSize = 1)
    private long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loc_code", nullable = false, updatable = false)
    private CourtLocation courtLocation;


    @Column(name = "room_number")
    @Length(max = 6)
    private String roomNumber;

    @Column(name = "description")
    @Length(max = 30)
    private String description;

}

