package uk.gov.hmcts.juror.api.moj.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * View of active pools with the Court.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ActivePoolsCourt implements Serializable {

    @Id
    @NotNull
    @Column(name = "POOL_NO")
    @Length(max = 9)
    private String poolNumber;

    @Column(name = "POOL_CAPACITY")
    private int poolCapacity;

    @Column(name = "JURORS_IN_POOL")
    private long jurorsInPool;

    @Column(name = "COURT_NAME")
    private String courtName;

    @Column(name = "POOL_TYPE")
    private String poolType;

    @Column(name = "SERVICE_START_DATE")
    private LocalDate serviceStartDate;

}
