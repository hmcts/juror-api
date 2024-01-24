package uk.gov.hmcts.juror.api.moj.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.annotation.CreatedDate;
import uk.gov.hmcts.juror.api.validation.LocalDateOfBirth;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static uk.gov.hmcts.juror.api.validation.ValidationConstants.JUROR_NUMBER;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.NO_PIPES_REGEX;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.POOL_NUMBER;

@Entity
@Table(name = "pending_juror", schema = "juror_mod")
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("PMD.TooManyFields")
public class PendingJuror extends Address implements Serializable {

    @Id
    @NotBlank
    @Column(name = "juror_number")
    @Pattern(regexp = JUROR_NUMBER)
    private String jurorNumber;

    @NotBlank
    @Column(name = "pool_number")
    @Pattern(regexp = POOL_NUMBER)
    private String poolNumber;

    @Column(name = "title")
    @Length(max = 10)
    @Pattern(regexp = NO_PIPES_REGEX)
    private String title;

    @Column(name = "first_name")
    @Length(max = 20)
    @Pattern(regexp = NO_PIPES_REGEX)
    @NotBlank
    private String firstName;

    @Column(name = "last_name")
    @Length(max = 20)
    @Pattern(regexp = NO_PIPES_REGEX)
    @NotBlank
    private String lastName;

    @Column(name = "dob")
    @NotNull
    @LocalDateOfBirth
    private LocalDate dateOfBirth;

    @Column(name = "h_phone")
    @Length(max = 15)
    private String phoneNumber;

    @Column(name = "w_phone")
    @Length(max = 15)
    private String workPhone;

    @Column(name = "w_ph_local")
    @Length(max = 4)
    private String workPhoneExtension;

    @Column(name = "m_phone")
    @Length(max = 15)
    private String altPhoneNumber;

    @Length(max = 254)
    @Column(name = "h_email")
    private String email;

    @NotNull
    @Column(name = "responded")
    private boolean responded;

    @Column(name = "next_date")
    private LocalDate nextDate;

    @Column(name = "added_by")
    @Length(max = 20)
    private String addedBy;

    @Length(max = 2000)
    @Column(name = "notes")
    private String notes;

    @Column(name = "contact_preference")
    private Integer contactPreference;

    @Column(name = "mileage")
    private Integer mileage;

    @Length(max = 4)
    @Column(name = "pool_seq")
    private String poolSequence;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "status")
    private PendingJurorStatus status;

    @CreatedDate
    @Column(name = "date_added", updatable = false)
    private LocalDate dateAdded;

    @CreatedDate
    @NotNull
    @Column(name = "date_created", updatable = false)
    private LocalDateTime dateCreated;

    @PrePersist
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private void prePersist() {
        dateAdded = LocalDate.now();
        preUpdate();
    }

    @PreUpdate
    private void preUpdate() {
        dateCreated = LocalDateTime.now();
    }

}
