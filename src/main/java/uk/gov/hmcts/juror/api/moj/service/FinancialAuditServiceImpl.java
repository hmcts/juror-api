package uk.gov.hmcts.juror.api.moj.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.history.Revision;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.domain.FinancialAuditDetails;
import uk.gov.hmcts.juror.api.moj.domain.FinancialAuditDetailsAppearances;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.repository.AppearanceRepository;
import uk.gov.hmcts.juror.api.moj.repository.FinancialAuditDetailsAppearancesRepository;
import uk.gov.hmcts.juror.api.moj.repository.FinancialAuditDetailsRepository;
import uk.gov.hmcts.juror.api.moj.repository.UserRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class FinancialAuditServiceImpl implements FinancialAuditService {

    private final FinancialAuditDetailsRepository financialAuditDetailsRepository;
    private final FinancialAuditDetailsAppearancesRepository financialAuditDetailsAppearancesRepository;
    private final AppearanceRepository appearanceRepository;
    private final UserRepository userRepository;
    private final RevisionService revisionService;
    private final Clock clock;

    @Override
    public FinancialAuditDetails createFinancialAuditDetail(String jurorNumber,
                                                            String courtLocationCode,
                                                            FinancialAuditDetails.Type type,
                                                            List<Appearance> appearances) {
        Revision<Long, Juror> jurorRevision = revisionService.getLatestJurorRevision(jurorNumber);
        Revision<Long, CourtLocation> courtRevision = revisionService.getLatestCourtRevision(courtLocationCode);

        // create a single financial audit details object with a new audit number generated for this batch of expenses
        FinancialAuditDetails auditDetails = FinancialAuditDetails.builder()
            .createdBy(userRepository.findByUsername(SecurityUtil.getActiveLogin()))
            .createdOn(LocalDateTime.now(clock))
            .type(type)
            .jurorRevision(jurorRevision.getRequiredRevisionNumber())
            .courtLocationRevision(courtRevision.getRequiredRevisionNumber())
            .build();

        FinancialAuditDetails financialAuditDetails = financialAuditDetailsRepository.save(auditDetails);

        List<FinancialAuditDetailsAppearances> financialAuditDetailsAppearances = new ArrayList<>(appearances.size());

        appearances.forEach(appearance -> {
            appearance.setFinancialAuditDetails(financialAuditDetails);
            Appearance savedAppearance = appearanceRepository.saveAndFlush(appearance);
            financialAuditDetailsAppearances.add(
                new FinancialAuditDetailsAppearances(
                    financialAuditDetails.getId(),
                    savedAppearance.getJurorNumber(),
                    savedAppearance.getAttendanceDate(),
                    savedAppearance.getCourtLocation(),
                    savedAppearance.getVersion()
                )
            );
        });
        financialAuditDetailsAppearancesRepository.saveAll(financialAuditDetailsAppearances);
        appearanceRepository.saveAll(appearances);
        return financialAuditDetails;
    }

    @Override
    public List<FinancialAuditDetails> getFinancialAuditDetails(Appearance appearance) {
        return financialAuditDetailsRepository.findAllByAppearance(appearance);
    }
}
