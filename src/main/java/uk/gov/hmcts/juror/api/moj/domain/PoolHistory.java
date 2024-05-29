package uk.gov.hmcts.juror.api.moj.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Pool_History table for the juror_mod schema.
 */
@Entity
@Table(name = "pool_history", schema = "juror_mod")
@Getter
@Setter
@NoArgsConstructor
public class PoolHistory implements Serializable {

    public static final String NEW_POOL_REQUEST_SUFFIX = " (New Pool Request)";
    public static final String ADD_POOL_REQUEST_SUFFIX = " (Add Pool Request)";
    public static final String ADD_POOL_MEMBERS_SUFFIX = " (Add Pool Members)";


    @Id
    @NotNull
    @Column(name = "id")
    @SequenceGenerator(name = "pool_history_gen", schema = "juror_mod", sequenceName = "pool_history_id_seq",
        allocationSize = 1)
    @GeneratedValue(generator = "pool_history_gen", strategy = GenerationType.SEQUENCE)
    public long id;

    @NotNull
    @Column(name = "HISTORY_CODE")
    @Enumerated(EnumType.STRING)
    private HistoryCode historyCode;

    @NotNull
    @Column(name = "POOL_NO")
    private String poolNumber;

    @NotNull
    @Column(name = "HISTORY_DATE")
    private LocalDateTime historyDate;

    @NotNull
    @Column(name = "USER_ID")
    private String userId;


    @NotNull
    @Column(name = "OTHER_INFORMATION")
    private String otherInformation;

    public PoolHistory(String poolNumber, LocalDateTime historyDate, HistoryCode historyCode, String userId,
                       String otherInformation) {
        this.poolNumber = poolNumber;
        this.historyDate = historyDate;
        this.historyCode = historyCode;
        this.userId = userId;
        this.otherInformation = otherInformation;
    }
}
