package uk.gov.hmcts.juror.api.moj.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;

/**
 * Reference table to lookup descriptions related to contact enquiry codes.
 */
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Getter
@Table(name = "CONTACT_ENQUIRY_TYPE", schema = "JUROR_DIGITAL_USER")
@Immutable
public class ContactEnquiryType implements Serializable {
    @Id
    @Column(name = "ENQUIRY_CODE")
    @NotNull
    @Size(min = 2, max = 2)
    @Enumerated(EnumType.STRING)
    private ContactEnquiryCode enquiryCode;

    @Column(name = "DESCRIPTION")
    @NotNull
    @Size(max = 60)
    private String description;

}
