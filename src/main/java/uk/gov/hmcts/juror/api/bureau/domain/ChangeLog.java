package uk.gov.hmcts.juror.api.bureau.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.validation.JurorNumber;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a change log entry in the Bureau application.
 */
@Entity
@Table(name = "CHANGE_LOG", schema = "JUROR_DIGITAL")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"staff", "changeLogItems"})
@Deprecated(forRemoval = true)
public class ChangeLog implements Serializable {
    private static final String GENERATOR_NAME = "CHANGE_LOG_SEQ_GENERATOR";

    @Id
    @SequenceGenerator(name = GENERATOR_NAME, sequenceName = "JUROR_DIGITAL.CHANGE_LOG_SEQ", allocationSize = 1)
    @GeneratedValue(generator = GENERATOR_NAME)
    @Column(name = "ID")
    private Long id;

    @JurorNumber
    @Column(name = "JUROR_NUMBER")
    private String jurorNumber;

    @NotNull
    @Column(name = "TIMESTAMP")
    @Builder.Default
    private Date timestamp = Date.from(Instant.now().atZone(ZoneId.systemDefault()).toInstant());

    @ManyToOne
    @JoinColumn(name = "STAFF")
    private User staff;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE")
    private ChangeLogType type;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "changeLog", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<ChangeLogItem> changeLogItems = new HashSet<>();

    @NotEmpty
    @Size(max = 2000)
    @Column(name = "NOTES")
    private String notes;

    @Version
    @Column(name = "VERSION", updatable = false)
    private Integer version;

    public void addChangeLogItem(ChangeLogItem changeLogItem) {
        if (null == this.changeLogItems) {
            this.changeLogItems = new HashSet<>();
        }
        changeLogItem.setChangeLog(this);
        changeLogItems.add(changeLogItem);
    }

    @PrePersist
    private void prePersist() {
        this.timestamp = Date.from(Instant.now().atZone(ZoneId.systemDefault()).toInstant());

    }
}
