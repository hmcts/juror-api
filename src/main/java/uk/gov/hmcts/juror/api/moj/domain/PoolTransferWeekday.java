package uk.gov.hmcts.juror.api.moj.domain;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pool_transfer_weekday",schema = "juror_mod")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PoolTransferWeekday {

    @Id
    @NotNull
    @Column(name = "transfer_day")
    @Size(max = 3)
    private String transferDay;

    @Column(name = "run_day")
    @Size(max = 3)
    private String runDay;

    @Column(name = "adjustment")
    private int adjustment;
}
