package uk.gov.hmcts.juror.api.jurorer.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.jurorer.domain.FileUploads;

import java.util.List;
import java.util.Optional;

import java.time.LocalDateTime;


/**
 * Repository for FileUploads entity.
 */
@Repository
public interface FileUploadsRepository extends JpaRepository<FileUploads, Long> {

    /**
     * Get the most recent upload for an LA.
     * Uses Spring Data's Top/First keyword to limit results to 1.
     */
    Optional<FileUploads> findTopByLocalAuthority_LaCodeOrderByUploadDateDesc(String laCode);


    /**
     * Find all uploads for a specific LA code, ordered by upload date descending.
     *
     * @param laCode LA code
     * @param pageable Pagination parameters
     * @return List of file uploads
     */
    @Query("SELECT f FROM FileUploads f "
        + "WHERE f.localAuthority.laCode = :laCode "
        + "ORDER BY f.uploadDate DESC")
    List<FileUploads> findByLaCodeOrderByUploadDateDesc(
        @Param("laCode") String laCode,
        Pageable pageable
    );

    /**
     * Count total uploads for a specific LA.
     *
     * @param laCode LA code
     * @return Total count of uploads
     */
    @Query("SELECT COUNT(f) FROM FileUploads f "
        + "WHERE f.localAuthority.laCode = :laCode")
    Long countByLaCode(@Param("laCode") String laCode);

    /**
     * Find all uploads by a specific user.
     *
     * @param username User's username
     * @return List of file uploads by this user
     */
    @Query("SELECT f FROM FileUploads f "
        + "WHERE f.user.username = :username "
        + "ORDER BY f.uploadDate DESC")
    List<FileUploads> findByUsernameOrderByUploadDateDesc(@Param("username") String username);

    /**
     * Find uploads within a date range for a specific LA.
     *
     * @param laCode LA code
     * @param startDate Start of date range
     * @param endDate End of date range
     * @return List of file uploads in range
     */
    @Query("SELECT f FROM FileUploads f "
        + "WHERE f.localAuthority.laCode = :laCode "
        + "AND f.uploadDate BETWEEN :startDate AND :endDate "
        + "ORDER BY f.uploadDate DESC")
    List<FileUploads> findByLaCodeAndDateRange(
        @Param("laCode") String laCode,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Get the most recent upload for a specific LA.
     *
     * @param laCode LA code
     * @param pageable Pagination (use PageRequest.of(0, 1) for single result)
     * @return List containing most recent upload
     */
    @Query("SELECT f FROM FileUploads f "
        + "WHERE f.localAuthority.laCode = :laCode "
        + "ORDER BY f.uploadDate DESC")
    List<FileUploads> findMostRecentByLaCode(@Param("laCode") String laCode, Pageable pageable);

    /**
     * Check if LA has any uploads.
     *
     * @param laCode LA code
     * @return true if LA has at least one upload
     */
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END "
        + "FROM FileUploads f "
        + "WHERE f.localAuthority.laCode = :laCode")
    boolean existsByLaCode(@Param("laCode") String laCode);

    // get the latest upload for each LA (one per LA)
    @Query(value = "SELECT la_code, la_username, upload_date  "
        + "        FROM ( "
        + "          SELECT la_code, la_username, upload_date,  "
        + "                 ROW_NUMBER() OVER (PARTITION BY la_code ORDER BY upload_date DESC, id DESC) AS rownum  "
        + "          FROM juror_er.file_uploads  "
        + "        ) t "
        + "        WHERE rownum = 1", nativeQuery = true)
    List<String> getLatestUploadForEachLa();
}
