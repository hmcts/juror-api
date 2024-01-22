package uk.gov.hmcts.juror.api.bureau.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "COURT_CATCHMENT_VIEW", schema = "JUROR_DIGITAL_USER")
@Immutable
public class CourtCatchmentEntity implements Serializable {
    @Id
    @Column(name = "POSTCODE")
    private String postCode;

    @NotEmpty
    @Column(name = "LOC_CODE")
    private String courtCode;

}
