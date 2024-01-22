package uk.gov.hmcts.juror.api.bureau.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;

@Entity
@Immutable
@Table(name = "T_SPECIAL", schema = "JUROR_DIGITAL_USER")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class TSpecial implements Serializable {
    @Id
    @Column(name = "SPEC_NEED")
    private String code;

    @Column(name = "DESCRIPTION")
    private String description;
}
