package uk.gov.hmcts.juror.api.jurorer.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.jurorer.domain.FileUploads;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for FileUploads entity.
 */
@Repository
public interface FileUploadsRepository extends JpaRepository<FileUploads, Long> {

    /**
     * Find all uploads for a specific LA code, ordered by upload date descending.
     */
    @Query("SELECT f FROM FileUploads f " +
        "WHERE f.localAuthority.laCode = :laCode " +
        "ORDER BY f.uploadDate DESC")
    List<FileUploads> findByLaCodeOrderByUploadDateDesc(
        @Param("laCode") String laCode,
        Pageable pageable
    );

    /**
     * Count total uploads for a specific LA.
     */
    @Query("SELECT COUNT(f) FROM FileUploads f " +
        "WHERE f.localAuthority.laCode = :laCode")
    Long countByLaCode(@Param("laCode") String laCode);

    /**
     * Find all uploads by a specific user.
     */
    @Query("SELECT f FROM FileUploads f " +
        "WHERE f.user.username = :username " +
        "ORDER BY f.uploadDate DESC")
    List<FileUploads> findByUsernameOrderByUploadDateDesc(@Param("username") String username);

    /**
     * Find uploads within a date range for a specific LA.
     */
    @Query("SELECT f FROM FileUploads f " +
        "WHERE f.localAuthority.laCode = :laCode " +
        "AND f.uploadDate BETWEEN :startDate AND :endDate " +
        "ORDER BY f.uploadDate DESC")
    List<FileUploads> findByLaCodeAndDateRange(
        @Param("laCode") String laCode,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Get the most recent upload for a specific LA.
     */
    @Query("SELECT f FROM FileUploads f " +
        "WHERE f.localAuthority.laCode = :laCode " +
        "ORDER BY f.uploadDate DESC")
    List<FileUploads> findMostRecentByLaCode(@Param("laCode") String laCode, Pageable pageable);

    /**
     * Check if LA has any uploads.
     */
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END " +
        "FROM FileUploads f " +
        "WHERE f.localAuthority.laCode = :laCode")
    boolean existsByLaCode(@Param("laCode") String laCode);
}
