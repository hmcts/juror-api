package uk.gov.hmcts.juror.api.bureau.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Java entity for a Team which bureau officers are associated with.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "TEAM", schema = "JUROR_DIGITAL")
@Deprecated(forRemoval = true)
public class Team implements Serializable {
    @Id
    @Column(name = "ID", nullable = false, unique = true)
    //TODO: should add a sequence to this if a CRUD interface for Team is exposed.
    private Long id;

    @NotEmpty
    @Size(min = 1, max = 1000)
    @Column(name = "TEAM_NAME", nullable = false, unique = true)
    private String teamName;

    @Version
    private Integer version;
}
