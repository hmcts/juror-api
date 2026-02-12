package uk.gov.hmcts.juror.api.jurorer.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;

import java.time.LocalDateTime;

@Entity
@Table(name = "local_authority", schema = "juror_er")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Audited
@AuditTable(value = "local_authority_audit", schema = "juror_er")
public class LocalAuthority {

    @Id
    @Column(name = "la_code", length = 3, nullable = false)
    private String laCode;

    @Column(name = "la_name", length = 100)
    private String laName;

    @Column(name = "is_active")
    private Boolean active;

    @Enumerated(EnumType.STRING)
    @Column(name = "upload_status", length = 40)
    private UploadStatus uploadStatus;

    @Column(name = "notes", length = 2000)
    private String notes;

    @Column(name = "inactive_reason", length = 2000)
    private String inactiveReason;

    @Column(name = "updated_by", length = 30)
    private String updatedBy;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(name = "email")
    private String email;


    /**
     * Check if LA has uploaded file for current period.
     *
     * @return true if upload_status is UPLOADED
     */
    public boolean hasUploaded() {
        return uploadStatus == UploadStatus.UPLOADED;
    }

    /**
     * Check if LA can upload (active and not already uploaded).
     *
     * @return true if LA is active and status is NOT_UPLOADED
     */
    public boolean canUpload() {
        return Boolean.TRUE.equals(active)
                &&
            uploadStatus == UploadStatus.NOT_UPLOADED;
    }

    /**
     * Get upload status as string for compatibility.
     *
     * @return Upload status string value
     */
    public String getUploadStatusString() {
        return uploadStatus != null ? uploadStatus.name() : null;
    }
}
