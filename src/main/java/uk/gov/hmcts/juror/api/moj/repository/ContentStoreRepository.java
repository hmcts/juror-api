package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uk.gov.hmcts.juror.api.moj.domain.ContentStore;

import java.util.List;

public interface ContentStoreRepository extends JpaRepository<ContentStore, Long> {

    @Query("SELECT cs FROM ContentStore cs")
    List<ContentStore> getContentStoreData();
}
