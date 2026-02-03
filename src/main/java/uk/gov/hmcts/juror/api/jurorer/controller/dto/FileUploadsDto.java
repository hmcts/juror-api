package uk.gov.hmcts.juror.api.jurorer.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Entity mapping for juror_er.file_uploads
 */
@Entity
@Table(name = "file_uploads", schema = "juror_er")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploads implements Serializable {

    @Id
    @Column(name = "id", nullable = false)
    @SequenceGenerator(
        name = "file_uploads_id_seq",
        schema = "juror_er",
        sequenceName = "file_uploads_id_seq",
        allocationSize = 1
    )
    @GeneratedValue(generator = "file_uploads_id_seq", strategy = GenerationType.SEQUENCE)
    private Long id;

    /**
     * ManyToOne to LocalAuthority table (references local_authority.la_code).
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "la_code", referencedColumnName = "la_code", nullable = false)
    private LocalAuthority localAuthority;

    /**
     * ManyToOne to User table (references "user".username).
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "la_username", referencedColumnName = "username", nullable = false)
    private LaUser user;

    @Column(name = "filename", length = 200, nullable = false)
    private String filename;

    @Column(name = "file_format", length = 20, nullable = false)
    private String fileFormat;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "other_information", length = 1000)
    private String otherInformation;

    @Column(name = "upload_date", nullable = false)
    private LocalDateTime uploadDate;

    @PrePersist
    protected void onCreate() {
        if (this.uploadDate == null) {
            this.uploadDate = LocalDateTime.now();
        }
    }

    /**
     * Get file size in human-readable format.
     */
    public String getFileSizeFormatted() {
        if (fileSizeBytes == null) {
            return "Unknown";
        }

        if (fileSizeBytes < 1024) {
            return fileSizeBytes + " B";
        } else if (fileSizeBytes < 1024 * 1024) {
            return String.format("%.2f KB", fileSizeBytes / 1024.0);
        } else if (fileSizeBytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", fileSizeBytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", fileSizeBytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
}
