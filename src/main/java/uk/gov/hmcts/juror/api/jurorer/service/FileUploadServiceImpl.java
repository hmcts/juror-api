package uk.gov.hmcts.juror.api.jurorer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.jurorer.domain.FileUploads;
import uk.gov.hmcts.juror.api.jurorer.repository.FileUploadsRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class FileUploadServiceImpl implements FileUploadsService {

    private final FileUploadsRepository fileUploadsRepository;

    @Override
    public List<FileUploadStatus> getLatestUploadForEachLa() {
        List<String> fileUploads = fileUploadsRepository.getLatestUploadForEachLa();

        List<FileUploadStatus> fileUploadStatuses = new ArrayList<>();

        fileUploads.forEach(upload -> {
            String[] parts = upload.split(",");
            String localAuthorityCode = parts[0];
            String laUsername = parts[1];
            String lastUploadDateStr = parts[2];

            LocalDateTime lastUploadDate = LocalDateTime.parse(
                lastUploadDateStr.substring(0,19), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            FileUploadStatus status = FileUploadStatus.builder()
                .localAuthorityCode(localAuthorityCode)
                .laUsername(laUsername)
                .lastUploadDate(lastUploadDate)
                .build();

            fileUploadStatuses.add(status);
        });

        return fileUploadStatuses;

    }

    @Override
    public FileUploads getLatestUploadForLa(String localAuthorityCode) {
        Optional<FileUploads> fileUploadsOptional = fileUploadsRepository
                                .findTopByLocalAuthority_LaCodeOrderByUploadDateDesc(localAuthorityCode);

        return fileUploadsOptional.orElse(null);
    }

}
