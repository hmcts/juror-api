package uk.gov.hmcts.juror.api.bureau.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * Entity representing deferral denied entry.
 */
@Entity
@Table(name = "DEF_DENIED", schema = "JUROR_DIGITAL_USER")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DefDenied implements Serializable {
    @Id
    @Column(name = "PART_NO")
    private String partNo;

    @NotNull
    @Column(name = "OWNER")
    private String owner;

    @NotNull
    @Size(min = 1, max = 1)
    @Column(name = "EXC_CODE")
    private String excusalCode;

    @NotNull
    @Column(name = "DATE_DEF")
    private Date dateDef;

    @Size(max = 1)
    @Column(name = "PRINTED")
    private String printed;

    @Column(name = "DATE_PRINTED")
    private Date datePrinted;
}
