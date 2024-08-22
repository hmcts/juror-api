package uk.gov.hmcts.juror.api.moj.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.domain.FinancialAuditDetails;
import uk.gov.hmcts.juror.api.moj.domain.FinancialAuditDetailsAppearances;
import uk.gov.hmcts.juror.api.moj.domain.SortMethod;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.AppearanceRepository;
import uk.gov.hmcts.juror.api.moj.repository.FinancialAuditDetailsAppearancesRepository;
import uk.gov.hmcts.juror.api.moj.repository.FinancialAuditDetailsRepository;
import uk.gov.hmcts.juror.api.moj.repository.UserRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
        // create a single financial audit details object with a new audit number generated for this batch of expenses
        FinancialAuditDetails auditDetails = FinancialAuditDetails.builder()
            .createdBy(userRepository.findByUsername(SecurityUtil.getActiveLogin()))
            .createdOn(LocalDateTime.now(clock))
            .type(type)
            .jurorNumber(jurorNumber)
            .jurorRevision(revisionService.getLatestJurorRevisionNumber(jurorNumber))
            .locCode(courtLocationCode)
            .courtLocationRevision(revisionService.getLatestCourtRevisionNumber(courtLocationCode))
            .build();

        FinancialAuditDetails financialAuditDetails = financialAuditDetailsRepository.save(auditDetails);

        List<FinancialAuditDetailsAppearances> financialAuditDetailsAppearances = new ArrayList<>(appearances.size());

        appearances.forEach(appearance -> {
            Long lastApprovedAuditNumber = null;
            if (Set.of(FinancialAuditDetails.Type.REAPPROVED_BACS, FinancialAuditDetails.Type.REAPPROVED_CASH)
                .contains(financialAuditDetails.getType())) {
                lastApprovedAuditNumber = getPreviousApprovedValue(
                    findFromAppearance(appearance),
                    appearance
                ).getFinancialAudit();
            }
            appearance.setFinancialAudit(financialAuditDetails.getId());
            Appearance savedAppearance = appearanceRepository.saveAndFlush(appearance);
            financialAuditDetailsAppearances.add(
                new FinancialAuditDetailsAppearances(
                    financialAuditDetails.getId(),
                    savedAppearance.getAttendanceDate(),
                    savedAppearance.getVersion(),
                    savedAppearance.getLocCode(),
                    lastApprovedAuditNumber
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

    @Override
    public FinancialAuditDetails getFinancialAuditDetails(long financialAuditNumber, String locCode) {
        return financialAuditDetailsRepository.findById(new FinancialAuditDetails.IdClass(
                financialAuditNumber,
                locCode))
            .orElseThrow(() -> new MojException.NotFound("Financial Audit Details not found: "
                + financialAuditNumber, null));
    }

    @Override
    public List<Appearance> getAppearances(FinancialAuditDetails financialAuditDetails) {
        List<FinancialAuditDetailsAppearances> financialAuditDetailsAppearances =
            financialAuditDetails.getFinancialAuditDetailsAppearances();
        return financialAuditDetailsAppearances.stream()
            .map(
                financialAuditDetailsAppearances1 ->
                    getAppearanceFromFinancialAuditDetailsAppearances(
                        financialAuditDetails.getLocCode(),
                        financialAuditDetails.getJurorNumber(),
                        financialAuditDetailsAppearances1))
            .toList();
    }


    @Override
    public FinancialAuditDetails getLastFinancialAuditDetailsFromTypes(FinancialAuditDetails financialAuditDetails,
                                                                       Set<FinancialAuditDetails.Type> types) {
        return financialAuditDetailsRepository.findLastFinancialAuditDetailsWithAnyTypeWithin(
            financialAuditDetails,
            types,
            SortMethod.DESC);
    }

    @Override
    public Optional<FinancialAuditDetails> getLastFinancialAuditDetailsFromAppearanceAndGenericType(
        Appearance appearance,
        FinancialAuditDetails.Type.GenericType genericType) {
        return financialAuditDetailsRepository
            .findLastFinancialAuditDetailsByType(
                appearance.getJurorNumber(),
                appearance.getAttendanceDate(),
                appearance.getLocCode(),
                genericType.getTypes())
            ;
    }


    @Override
    public Appearance getPreviousAppearance(FinancialAuditDetails financialAuditDetails, Appearance appearance) {
        return getAppearanceFromFinancialAuditDetailsAppearances(
            financialAuditDetails.getLocCode(),
            financialAuditDetails.getJurorNumber(),
            getPreviousFinancialAuditDetailsAppearances(financialAuditDetails, appearance));
    }

    @Override
    public Appearance getPreviousApprovedValue(FinancialAuditDetails financialAuditDetails, Appearance appearance) {
        return getAppearanceFromFinancialAuditDetailsAppearances(
            financialAuditDetails.getLocCode(),
            financialAuditDetails.getJurorNumber(),
            getPreviousFinancialAuditDetailsAppearancesWithGenericTypeExcludingAuditNumber(
                FinancialAuditDetails.Type.GenericType.APPROVED, financialAuditDetails, appearance));
    }

    @Override
    public FinancialAuditDetails findFromAppearance(Appearance appearance) {
        return getFinancialAuditDetails(appearance.getFinancialAudit(), appearance.getLocCode());
    }


    FinancialAuditDetailsAppearances getPreviousFinancialAuditDetailsAppearances(
        FinancialAuditDetails financialAuditDetails, Appearance appearance) {
        return financialAuditDetailsAppearancesRepository
            .findPreviousFinancialAuditDetailsAppearances(
                financialAuditDetails, appearance)
            .orElseThrow(
                () -> new MojException.NotFound("No previous appearance found for appearance: "
                    + appearance.getFinancialAudit()
                    + " locCode: " + appearance.getLocCode(),
                    null));
    }

    private FinancialAuditDetailsAppearances getFinancialAuditDetailsAppearancesWithGenericTypeExcludingAuditNumber(
        FinancialAuditDetails.Type.GenericType genericType,
        FinancialAuditDetails financialAuditDetails,
        Appearance appearance) {
        return financialAuditDetailsAppearancesRepository
            .findPreviousFinancialAuditDetailsAppearancesWithGenericTypeExcludingProvidedAuditDetails(
                genericType,
                financialAuditDetails,
                appearance)
            .orElseThrow(
                () -> new MojException.NotFound("No previous appearance found for appearance", null));
    }


    @SuppressWarnings("LineLength")
    private FinancialAuditDetailsAppearances getPreviousFinancialAuditDetailsAppearancesWithGenericTypeExcludingAuditNumber(
        FinancialAuditDetails.Type.GenericType genericType,
        FinancialAuditDetails financialAuditDetails,
        Appearance appearance) {
        return financialAuditDetailsAppearancesRepository
            .findPreviousFinancialAuditDetailsAppearancesWithGenericTypeExcludingProvidedAuditDetails(
                genericType,
                financialAuditDetails,
                appearance)
            .orElseThrow(
                () -> new MojException.NotFound("No previous appearance found for appearance", null));
    }


    private Appearance getAppearanceFromFinancialAuditDetailsAppearances(
        String locCode,
        String jurorNumber,
        FinancialAuditDetailsAppearances financialAuditDetailsAppearances) {

        return appearanceRepository.findByJurorNumberAndLocCodeAndAttendanceDateAndVersion(
            jurorNumber,
            locCode,
            financialAuditDetailsAppearances.getAttendanceDate(),
            financialAuditDetailsAppearances.getAppearanceVersion()
        ).orElseThrow(
            () -> new MojException.NotFound(
                "Appearance not found for financial audit details appearances "
                    + " juror number: " + jurorNumber
                    + " loc code: " + locCode
                    + " attendance date: " + financialAuditDetailsAppearances.getAttendanceDate()
                    + " appearance version: " + financialAuditDetailsAppearances.getAppearanceVersion(),
                null));
    }
}
