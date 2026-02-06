package uk.gov.hmcts.juror.api.jurorer.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.jurorer.domain.FileUploads;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileUploadsRepository extends JpaRepository<FileUploads, Long> {

    /**
     * Count total uploads for a specific LA.
     */
    @Query("SELECT COUNT(f) FROM FileUploads f WHERE f.localAuthority.laCode = :laCode")
    Long countByLaCode(@Param("laCode") String laCode);

    /**
     * Get recent uploads for an LA with pagination.
     */
    @Query("SELECT f FROM FileUploads f WHERE f.localAuthority.laCode = :laCode ORDER BY f.uploadDate DESC")
    List<FileUploads> findByLaCodeOrderByUploadDateDesc(@Param("laCode") String laCode, Pageable pageable);

    /**
     * Get the most recent upload for an LA.
     */
    @Query("SELECT f FROM FileUploads f WHERE f.localAuthority.laCode = :laCode ORDER BY f.uploadDate DESC")
    Optional<FileUploads> findFirstByLaCodeOrderByUploadDateDesc(@Param("laCode") String laCode);
}
