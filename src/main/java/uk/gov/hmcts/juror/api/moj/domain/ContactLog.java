package uk.gov.hmcts.juror.api.moj.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.juror.api.validation.JurorNumber;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Used to log any communications relating to a specific juror record.
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Getter
@Setter
@Table(name = "contact_log", schema = "juror_mod")
public class ContactLog implements Serializable {


    private static final String GENERATOR_NAME = "contact_log_sequence_gen";

    @Id
    @NotNull
    @Column(name = "id")
    @SequenceGenerator(name = GENERATOR_NAME, schema = "juror_mod", sequenceName = "contact_log_id_seq",
        allocationSize = 1)
    @GeneratedValue(generator = GENERATOR_NAME, strategy = GenerationType.SEQUENCE)
    public long id;

    @NotEmpty
    @Size(max = 30)
    @Column(name = "user_id")
    private String username;

    @NotNull
    @Column(name = "juror_number")
    @JurorNumber
    private String jurorNumber;

    @NotNull
    @Column(name = "start_call")
    private LocalDateTime startCall;

    @Column(name = "END_CALL")
    private LocalDateTime endCall;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "enquiry_type", referencedColumnName = "enquiry_code")
    @Enumerated(EnumType.STRING)
    private ContactCode enquiryType;

    @Size(max = 2000)
    @Column(name = "notes")
    private String notes;

    @Column(name = "last_update")
    private LocalDateTime lastUpdate;

    @Column(name = "repeat_enquiry")
    private Boolean repeatEnquiry;

    @PrePersist
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private void prePersist() {
        preUpdate();
    }

    @PreUpdate
    private void preUpdate() {
        lastUpdate = LocalDateTime.now();
    }

    public ContactLog(String username, String jurorNumber, LocalDateTime startCall, ContactCode enquiryType,
                      String notes, boolean repeatEnquiry) {
        this.username = username;
        this.jurorNumber = jurorNumber;
        this.startCall = startCall;
        this.enquiryType = enquiryType;
        this.notes = notes;
        this.repeatEnquiry = repeatEnquiry;

    }
}
