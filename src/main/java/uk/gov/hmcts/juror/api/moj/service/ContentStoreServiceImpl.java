package uk.gov.hmcts.juror.api.moj.service;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.moj.domain.*;
import uk.gov.hmcts.juror.api.moj.repository.ContentStoreRepository;

import java.util.List;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class ContentStoreServiceImpl implements ContentStoreService {

    private ContentStoreRepository contentStoreRepository;

    public void generateFiles(String poolNumber) {

        List<ContentStore> contentStoreList = contentStoreRepository.getContentStoreData();
    }

}
