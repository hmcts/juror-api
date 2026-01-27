package uk.gov.hmcts.juror.api.jurorer.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "local_authority", schema = "juror_er")
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
}
