package uk.gov.hmcts.juror.api.moj.domain.jurorresponse;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.api.moj.domain.User;

import java.time.LocalDateTime;

import static uk.gov.hmcts.juror.api.validation.ValidationConstants.JUROR_NUMBER;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user_juror_response_audit", schema = "juror_mod")
@Getter
@Setter
@Builder
public class UserJurorResponseAudit {

    private static final String GENERATOR_NAME = "user_juror_response_audit_seq_generator";

    @Id
    @NotNull
    @SequenceGenerator(name = GENERATOR_NAME, schema = "juror_mod", sequenceName = "user_juror_response_audit_seq",
        allocationSize = 1)
    @GeneratedValue(generator = GENERATOR_NAME, strategy = GenerationType.SEQUENCE)
    @Column(name = "id")
    private Long id;

    @Pattern(regexp = JUROR_NUMBER)
    @Length(max = 9)
    @NotEmpty
    @Column(name = "juror_number")
    private String jurorNumber;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by", referencedColumnName = "username")
    private User assignedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to", referencedColumnName = "username")
    private User assignedTo;

    @NotNull
    @Column(name = "assigned_on")
    private LocalDateTime assignedOn;

}
