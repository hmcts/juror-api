package uk.gov.hmcts.juror.api.moj.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

import java.io.Serializable;

@Entity
@Table(name = "rev_info", schema = "juror_mod")
@RevisionEntity
public class RevisionInfo implements Serializable {

    @Id
    @GeneratedValue
    @RevisionNumber
    @Column(name = "revision_number")
    private Long revision;

    @RevisionTimestamp
    @Column(name = "revision_timestamp")
    private Long timestamp;

}
