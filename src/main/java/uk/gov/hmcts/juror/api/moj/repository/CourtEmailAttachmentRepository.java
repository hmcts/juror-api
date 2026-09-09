package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.hmcts.juror.api.moj.domain.CourtEmailAttachment;

public interface CourtEmailAttachmentRepository extends JpaRepository <CourtEmailAttachment,String> {}
