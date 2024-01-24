package uk.gov.hmcts.juror.api.moj.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeConverter;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;

import java.io.Serializable;
import java.time.LocalDateTime;


@Entity
@Table(name = "juror_history", schema = "juror_mod")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JurorHistory implements Serializable {

    // Constants for History other info text
    public static final String RESPONDED = "Responded";
    public static final String ADDED = "Added to New Pool";

    @Id
    @NotNull
    @Column(name = "id")
    @SequenceGenerator(name = "juror_history_gen", schema = "juror_mod", sequenceName = "juror_history_id_seq",
        allocationSize = 1)
    @GeneratedValue(generator = "juror_history_gen", strategy = GenerationType.SEQUENCE)
    public long id;

    @NotNull
    @Column(name = "juror_number")
    private String jurorNumber;

    @NotNull
    @Column(name = "history_code")
    @Convert(converter = HistoryCodeConverter.class)
    private HistoryCodeMod historyCode;

    @NotNull
    @Column(name = "date_created")
    private LocalDateTime dateCreated;

    @NotNull
    @Column(name = "user_id")
    private String createdBy;

    @Column(name = "other_information")
    private String otherInformation;

    @Column(name = "pool_number")
    private String poolNumber;

    @PrePersist
    public void prePersist() {
        this.dateCreated = LocalDateTime.now();
    }

    public JurorHistory(String jurorNumber, HistoryCodeMod historyCode, LocalDateTime dateCreated, String createdBy,
                        String otherInformation, String poolNumber) {
        this.jurorNumber = jurorNumber;
        this.historyCode = historyCode;
        this.dateCreated = dateCreated;
        this.createdBy = createdBy;
        this.otherInformation = otherInformation;
        this.poolNumber = poolNumber;
    }

}
