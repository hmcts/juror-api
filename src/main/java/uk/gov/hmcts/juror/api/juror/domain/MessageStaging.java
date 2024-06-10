package uk.gov.hmcts.juror.api.juror.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
 * JUROR_DIGITAL.MESSAGE_STAGING Table.
 */

@Entity
@Table(name = "MESSAGE_STAGING", schema = "JUROR_DIGITAL")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Slf4j
@Deprecated(forRemoval = true)
public class MessageStaging implements Serializable {
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

    @Column(name = "FILE_DATETIME")
    @Length(max = 15)
    private String fileDatetime;

    @Column(name = "USERNAME")
    @Length(max = 20)
    private String userName;

    @Column(name = "LOC_CODE")
    private String locationCode;

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
    private Integer messageId;

    @Column(name = "NOTIFY_TEMPLATE_ID")
    @Length(max = 100)
    private String notifyTemplateId;

    @Column(name = "NOTIFY_ACCOUNT_KEY")
    @Length(max = 100)
    private String notifyAccountKey;

    @Column(name = "REGION_ID")
    @Length(max = 5)
    private String regionId;

}
