package uk.gov.hmcts.juror.api.moj.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

import java.io.Serializable;

@Entity
@Table(name = "rev_info", schema = "juror_mod")
@RevisionEntity
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

}
