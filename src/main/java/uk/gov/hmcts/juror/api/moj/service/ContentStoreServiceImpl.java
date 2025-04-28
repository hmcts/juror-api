package uk.gov.hmcts.juror.api.moj.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.moj.domain.*;
import uk.gov.hmcts.juror.api.moj.repository.ContentStoreRepository;
import uk.gov.hmcts.juror.api.moj.utils.FileUtils;

import java.io.File;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class ContentStoreServiceImpl implements ContentStoreService {

    private ContentStoreRepository contentStoreRepository;

    @Override
    public void generateFiles() {

        List<ContentStore> contentStoreList = contentStoreRepository.getContentStoreData();

        // generate the files here..
        contentStoreList.forEach(contentStore -> {
            try {

                File file = FileUtils.createFile(
                    "/Users/akhlaqur/Documents/generated"+ '/' + contentStore.getDocumentId());

                FileUtils.writeToFile(file, contentStore.getData());

            } catch (Exception e) {
                log.error("{}: Failed to generate file for: {}", "Error ", contentStore.getDocumentId(), e);

            }
        });
    }

}
