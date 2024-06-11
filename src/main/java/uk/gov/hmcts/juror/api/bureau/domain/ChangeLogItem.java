package uk.gov.hmcts.juror.api.bureau.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.Objects;

/**
 * Entity representing a change log line item associated with a parent {@link ChangeLog}.
 */
@Entity
@Table(name = "CHANGE_LOG_ITEM", schema = "JUROR_DIGITAL")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "changeLog")
@ToString(exclude = "changeLog")
@Deprecated(forRemoval = true)
public class ChangeLogItem implements Serializable {
    private static final String GENERATOR_NAME = "CHANGE_LOG_ITEM_SEQ_GENERATOR";

    @Id
    @Column(name = "ID")
    @SequenceGenerator(name = GENERATOR_NAME, sequenceName = "JUROR_DIGITAL.CHANGE_LOG_ITEM_SEQ", allocationSize = 1)
    @GeneratedValue(generator = GENERATOR_NAME)
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "CHANGE_LOG")
    private ChangeLog changeLog;

    @Size(max = 128)
    @Column(name = "OLD_KEY")
    private String oldKeyName;

    @Size(max = 2048)
    @Column(name = "OLD_VALUE")
    private String oldValue;

    @Size(max = 128)
    @Column(name = "NEW_KEY")
    private String newKeyName;

    @Size(max = 2048)
    @Column(name = "NEW_VALUE")
    private String newValue;

    @Version
    @Column(name = "VERSION", updatable = false)
    private Integer version;

    public ChangeLogItem(String oldKeyName, Object oldValue, String newKeyName, Object newValue) {
        this.oldKeyName = oldKeyName;
        this.oldValue = Objects.toString(oldValue, null);
        this.newKeyName = newKeyName;
        this.newValue = Objects.toString(newValue, null);
    }
}
