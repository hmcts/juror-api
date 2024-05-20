package uk.gov.hmcts.juror.api.moj.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;
import uk.gov.hmcts.juror.api.moj.audit.AuditorRevisionListener;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "rev_info", schema = "juror_mod")
@RevisionEntity(AuditorRevisionListener.class)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RevisionInfo implements Serializable {

    private static final String GENERATOR_NAME = "rev_info_revision_gen";

    @Id
    @GeneratedValue(generator = GENERATOR_NAME, strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = GENERATOR_NAME, schema = "juror_mod", sequenceName = "rev_info_seq",
        allocationSize = 1)
    @RevisionNumber
    @Column(name = "revision_number")
    private Long revision;

    @RevisionTimestamp
    @Column(name = "revision_timestamp")
    private Long timestamp;


    @Column(name = "changed_by")
    private String changedBy;

    @Transient
    public LocalDateTime getRevisionDate() {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    }

}
