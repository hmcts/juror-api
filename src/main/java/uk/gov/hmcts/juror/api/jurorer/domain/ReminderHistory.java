package uk.gov.hmcts.juror.api.jurorer.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

import static org.hibernate.id.IdentifierGenerator.GENERATOR_NAME;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Getter
@Setter
@Table(name = "reminder_history", schema = "juror_er")
public class ReminderHistory implements Serializable {

    private static final String GENERATOR_NAME = "reminder_history_sequence_gen";


    @Id
    @NotNull
    @Column(name = "id")
    @SequenceGenerator(name = GENERATOR_NAME, schema = "juror_er", sequenceName = "reminder_history_id_seq",
        allocationSize = 1)

    @GeneratedValue(generator = GENERATOR_NAME, strategy = GenerationType.SEQUENCE)
    public long id;

    @Column(name = "la_code", nullable = false, length = 3)
    private String laCode;

    @Column(name = "sent_by", nullable = false, length = 30)
    private String sentBy;

    @Column(name = "time_sent", nullable = false)
    private LocalDateTime timeSent;

}

