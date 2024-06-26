package uk.gov.hmcts.juror.api.juror.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;

import static uk.gov.hmcts.juror.api.validation.ValidationConstants.JUROR_NUMBER;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.NO_PIPES_REGEX;


/**
 * View of the JUROR Messages Table.
 */
@Entity
@IdClass(MessagesKey.class)
@Table(name = "MESSAGES", schema = "JUROR_DIGITAL_USER")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Slf4j
@Deprecated(forRemoval = true)
public class Messages implements Serializable {
    /**
     * Default value for varchar fields if null.
     *
     * @see #prePersist()
     */

    private static final String SINGLE_SPACE_CHARACTER = " ";

    @Id
    @Column(name = "PART_NO")
    @Pattern.List({
        @Pattern(regexp = JUROR_NUMBER),
        @Pattern(regexp = NO_PIPES_REGEX)
    })
    @Length(max = 9)
    @NotNull
    private String jurorNumber;

    @Id
    @Column(name = "FILE_DATETIME")
    @Length(max = 15)
    private String fileDatetime;

    @Id
    @Column(name = "USERNAME")
    @Length(max = 20)
    private String userName;

    @Id
    @ManyToOne
    @JoinColumn(name = "LOC_CODE")
    private CourtLocation locationCode;

    @Column(name = "PHONE")
    @Length(max = 15)
    private String phone;

    @Column(name = "EMAIL")
    @Length(max = 254)
    private String email;

    @Column(name = "LOC_NAME")
    @Length(max = 100)
    private String locationName;

    @Column(name = "POOL_NO")
    @Length(max = 9)
    private String poolNumber;

    @Column(name = "SUBJECT")
    @Length(max = 50)
    private String subject;

    @Column(name = "MESSAGE_TEXT")
    @Length(max = 600)
    private String messageText;

    @Column(name = "MESSAGE_ID")
    private int messageId;

    @Column(name = "MESSAGE_READ")
    @Length(max = 2)
    @Builder.Default
    private String messageRead = "NR";
}
