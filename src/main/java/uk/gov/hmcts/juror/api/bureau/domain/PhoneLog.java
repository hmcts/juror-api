package uk.gov.hmcts.juror.api.bureau.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.juror.api.validation.JurorNumber;

import java.io.Serializable;
import java.util.Date;

/**
 * Entry in a juror response phone log.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@IdClass(PhoneLogKey.class)
@Table(name = "PHONE_LOG", schema = "JUROR_DIGITAL_USER")
@Deprecated(forRemoval = true)
public class PhoneLog implements Serializable {
    @Id
    @Column(name = "OWNER")
    @NotNull
    @Size(min = 3, max = 3)
    private String owner;

    @Id
    @Column(name = "PART_NO")
    @JurorNumber
    private String jurorNumber;

    @Size(max = 2000)
    @Column(name = "NOTES")
    private String notes;

    @NotEmpty
    @Size(max = 20)
    @Column(name = "USER_ID")
    private String username;

    @NotEmpty
    @Size(min = 2, max = 2)
    @Column(name = "PHONE_CODE")
    private String phoneCode;

    @Column(name = "LAST_UPDATE")
    private Date lastUpdate;

    @NotNull
    @Id
    @Column(name = "START_CALL")
    private Date startCall;

    @Column(name = "END_CALL")
    private Date endCall;
}
