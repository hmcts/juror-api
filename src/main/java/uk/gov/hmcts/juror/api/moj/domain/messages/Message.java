package uk.gov.hmcts.juror.api.moj.domain.messages;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.validation.JurorNumber;
import uk.gov.hmcts.juror.api.validation.PoolNumber;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * View of the JUROR Messages Table.
 */
@Entity
@IdClass(MessageKey.class)
@Table(name = "message", schema = "juror_mod")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Slf4j
public class Message implements Serializable {
    private static final String SINGLE_SPACE_CHARACTER = " ";

    @Id
    @Column(name = "juror_number")
    @JurorNumber
    @Length(max = 9)
    @NotNull
    private String jurorNumber;

    @Id
    @Column(name = "file_datetime")
    private LocalDateTime fileDatetime;

    @Id
    @Column(name = "username")
    @Length(max = 30)
    private String userName;

    @Id
    @ManyToOne
    @JoinColumn(name = "loc_code")
    private CourtLocation locationCode;

    @Column(name = "phone")
    @Length(max = 15)
    private String phone;

    @Column(name = "email")
    @Length(max = 254)
    private String email;

    @Column(name = "pool_no")
    @Length(max = 9)
    @PoolNumber
    private String poolNumber;

    @Column(name = "subject")
    @Length(max = 50)
    private String subject;

    @Column(name = "message_text")
    @Length(max = 600)
    private String messageText;

    @Column(name = "message_id")
    private int messageId;

    @Column(name = "message_read")
    @Length(max = 2)
    @Builder.Default
    private String messageRead = "NR";
}
