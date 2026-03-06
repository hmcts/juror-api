package uk.gov.hmcts.juror.api.jurorer.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity mapping for juror_er.file_uploads
 */
@Entity
@Table(name = "file_uploads", schema = "juror_er")
@Data
@Builder
@AllArgsConstructor
public class FileUploads implements Serializable {

    @Id
    @Column(name = "id", nullable = false)
    @SequenceGenerator(name = "file_uploads_id_seq", schema = "juror_er", sequenceName = "file_uploads_id_seq",
        allocationSize = 1)
    @GeneratedValue(generator = "file_uploads_id_seq", strategy = GenerationType.SEQUENCE)
    public long id;

    /**
     * ManyToOne to LocalAuthority table (references local_authority.la_code).
     * The referenced entity must have laCode (or equivalent) mapped as its primary/key column.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "la_code", referencedColumnName = "la_code", nullable = false)
    private LocalAuthority localAuthority;

    @Column(name = "la_username", length = 200, nullable = false)
    private String username;

    /**
    * ManyToOne to LaUser table (references user.username and user.la_code).
    * The referenced entity must have a composite primary key of (username, la_code).
    */
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

    public FileUploads() {
    }

    // Convenience constructor for required fields
    public FileUploads(LocalAuthority localAuthority, String user, String filename,
                       String fileFormat, LocalDateTime uploadDate) {
        this.localAuthority = Objects.requireNonNull(localAuthority, "localAuthority");
        this.username = Objects.requireNonNull(user, "user");
        this.filename = Objects.requireNonNull(filename, "filename");
        this.fileFormat = Objects.requireNonNull(fileFormat, "fileFormat");
        this.uploadDate = Objects.requireNonNull(uploadDate, "uploadDate");
    }

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

