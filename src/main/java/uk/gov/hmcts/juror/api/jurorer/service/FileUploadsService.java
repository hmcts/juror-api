package uk.gov.hmcts.juror.api.jurorer.service;

import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.juror.api.jurorer.domain.FileUploads;

import java.time.LocalDateTime;
import java.util.List;

@SuppressWarnings("PMD.ShortMethodName")
public interface FileUploadsService {

    List<FileUploadStatus> getLatestUploadForEachLa();

    FileUploads getLatestUploadForLa(String localAuthorityCode);

    @Builder
    @Getter
    class FileUploadStatus {
        private String localAuthorityCode;
        private String laUsername;
        private LocalDateTime lastUploadDate;

        public FileUploadStatus of(String localAuthorityCode, String laUsername, LocalDateTime lastUploadDate) {
            return FileUploadStatus.builder()
                .localAuthorityCode(localAuthorityCode)
                .laUsername(laUsername)
                .lastUploadDate(lastUploadDate)
                .build();
        }
    }
}
