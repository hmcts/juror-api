package uk.gov.hmcts.juror.api.moj.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Immutable;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;

@Entity
@Immutable
@IdClass(VotersLocPostcodeTotals.VotersLocPostcodeTotalsId.class)
@Table(name = "loc_postcode_totals_view", schema = "juror_mod")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Slf4j
public class VotersLocPostcodeTotals implements Serializable {

    @Id
    @Column(name = "LOC_CODE")
    @Length(max = 3)
    private String locCode;

    @Id
    @Column(name = "POSTCODE_START")
    @Length(max = 10)
    private String postcode;

    @Column(name = "TOTAL")
    private Integer total;

    @Column(name = "TOTAL_COR")
    private Integer totalCor;


    @AllArgsConstructor
    @Getter
    @Setter
    public static class CourtCatchmentItem {

        private String postCode;
        private Integer total;
    }

    @AllArgsConstructor
    @EqualsAndHashCode
    @Setter
    @Getter
    public static class CourtCatchmentSummaryItem implements Serializable {
        private String postCodePart;
        private Integer total;
    }

    @EqualsAndHashCode
    @Setter
    @Getter
    public static class VotersLocPostcodeTotalsId implements Serializable {

        private String locCode;
        private String postcode;
    }

}
