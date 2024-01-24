package uk.gov.hmcts.juror.api.moj.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@NoArgsConstructor
@AllArgsConstructor
@IdClass(CourtCatchmentAreaId.class)
@Getter
@Entity
@Table(name = "court_catchment_area", schema = "juror_mod")
@Immutable
public class CourtCatchmentArea {
    @Id
    @Column(name = "postcode")
    private String postcode;

    @Id
    @Column(name = "loc_code")
    private String locCode;

}
