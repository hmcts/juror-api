package uk.gov.hmcts.juror.api.bureau.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;

/**
 * Entity representing deferral entry.
 */
@Entity
@Table(name = "DEFER_DBF", schema = "JUROR_DIGITAL_USER")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeferDBF implements Serializable {
    @Id
    @Column(name = "PART_NO")
    private String partNo;

    @NotNull
    @Column(name = "OWNER")
    private String owner;

    @NotNull
    @Column(name = "DEFER_TO")
    private LocalDate deferTo;

    @Size(max = 1)
    @Column(name = "CHECKED")
    private String checked;

    @NotEmpty
    @Column(name = "LOC_CODE")
    private String locCode;
}
