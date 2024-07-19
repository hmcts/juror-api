package uk.gov.hmcts.juror.api.moj.service;

import com.querydsl.core.util.ArrayUtils;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.controller.response.CourtRates;
import uk.gov.hmcts.juror.api.moj.controller.response.administration.CodeDescriptionResponse;
import uk.gov.hmcts.juror.api.moj.controller.response.administration.CourtDetailsReduced;
import uk.gov.hmcts.juror.api.moj.domain.Address;
import uk.gov.hmcts.juror.api.moj.domain.CodeType;
import uk.gov.hmcts.juror.api.moj.domain.CourtDetailsDto;
import uk.gov.hmcts.juror.api.moj.domain.UpdateCourtDetailsDto;
import uk.gov.hmcts.juror.api.moj.domain.system.HasCodeAndDescription;
import uk.gov.hmcts.juror.api.moj.domain.system.HasEnabled;
import uk.gov.hmcts.juror.api.moj.domain.trial.Courtroom;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.trial.CourtroomRepository;
import uk.gov.hmcts.juror.api.moj.utils.DataUtils;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AdministrationServiceImpl implements AdministrationService {

    private final EntityManager entityManager;

    private final CourtLocationRepository courtLocationRepository;
    private final WelshCourtLocationRepository welshCourtLocationRepository;
    private final CourtroomRepository courtroomRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CodeDescriptionResponse> viewCodeAndDescriptions(CodeType codeType) {
        final Collection<? extends HasCodeAndDescription<?>> values;
        if (ArrayUtils.isEmpty(codeType.getValues())) {
            values =
                getJpaQueryFactory()
                    .select(codeType.getEntityPathBase())
                    .from(codeType.getEntityPathBase())
                    .fetch();
        } else {
            values = List.of(codeType.getValues());
        }
        return values.stream()
            .sorted(Comparator.comparing(HasCodeAndDescription::getCode))
            .filter(hasCodeAndDescription -> {
                if (hasCodeAndDescription instanceof HasEnabled hasEnabled) {
                    return hasEnabled.isEnabled();
                }
                return true;
            })
            .map(CodeDescriptionResponse::new)
            .toList();
    }

    @Override
    public List<CourtDetailsReduced> viewCourts() {
        return courtLocationRepository.findAll().stream()
            .map(courtLocation -> CourtDetailsReduced.builder()
                .locCode(courtLocation.getLocCode())
                .courtName(courtLocation.getName())
                .courtType(courtLocation.getType())
                .build())
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CourtDetailsDto viewCourt(String locCode) {
        CourtLocation courtLocation = getCourtLocation(locCode);
        Courtroom courtroom = courtLocation.getAssemblyRoom();
        CourtDetailsDto courtDetailsDto = CourtDetailsDto.builder()
            .courtCode(locCode)
            .englishCourtName(courtLocation.getName())
            .englishAddress(
                Address.builder()
                    .addressLine1(courtLocation.getAddress1())
                    .addressLine2(courtLocation.getAddress2())
                    .addressLine3(courtLocation.getAddress3())
                    .addressLine4(courtLocation.getAddress4())
                    .addressLine5(courtLocation.getAddress5())
                    .postcode(DataUtils.toUppercase(courtLocation.getPostcode()))
                    .build()
            )
            .mainPhone(courtLocation.getLocPhone())
            .attendanceTime(courtLocation.getCourtAttendTime())
            .costCentre(courtLocation.getCostCentre())
            .signature(courtLocation.getSignatory())
            .assemblyRoom(courtroom == null ? null : courtroom.getRoomNumber())
            .build();


        Optional<WelshCourtLocation> welshCourtLocationOptional =
            welshCourtLocationRepository.findById(locCode);
        if (welshCourtLocationOptional.isPresent()) {
            WelshCourtLocation welshCourtLocation = welshCourtLocationOptional.get();
            courtDetailsDto.setWelsh(true);
            courtDetailsDto.setWelshCourtName(welshCourtLocation.getLocCourtName());
            courtDetailsDto.setWelshAddress(
                Address.builder()
                    .addressLine1(welshCourtLocation.getAddress1())
                    .addressLine2(welshCourtLocation.getAddress2())
                    .addressLine3(welshCourtLocation.getAddress3())
                    .addressLine4(welshCourtLocation.getAddress4())
                    .addressLine5(welshCourtLocation.getAddress5())
                    .build()
            );

        }
        return courtDetailsDto;
    }

    @Override
    @Transactional
    public void updateCourt(String locCode, UpdateCourtDetailsDto updateCourtDetailsDto) {
        CourtLocation courtLocation = getCourtLocation(locCode);
        courtLocation.setLocPhone(updateCourtDetailsDto.getMainPhoneNumber());
        courtLocation.setCourtAttendTime(updateCourtDetailsDto.getDefaultAttendanceTime());
        courtLocation.setCostCentre(updateCourtDetailsDto.getCostCentre());
        courtLocation.setSignatory(updateCourtDetailsDto.getSignature());
        courtLocation.setAssemblyRoom(getCourtRoom(updateCourtDetailsDto.getAssemblyRoomId()));
        courtLocationRepository.save(courtLocation);
    }

    Courtroom getCourtRoom(Long assemblyRoomId) {
        return courtroomRepository.findById(assemblyRoomId)
            .orElseThrow(() -> new MojException.NotFound("Courtroom not found", null));
    }

    @Override
    @Transactional
    public void updateCourtRates(String locCode, CourtRates courtRates) {
        CourtLocation courtLocation = getCourtLocation(locCode);
        courtLocation.setTaxiSoftLimit(courtRates.getTaxiSoftLimit());
        courtLocation.setPublicTransportSoftLimit(courtRates.getPublicTransportSoftLimit());
        courtLocationRepository.save(courtLocation);
    }

    CourtLocation getCourtLocation(String locCode) {
        return courtLocationRepository.findById(locCode)
            .orElseThrow(() -> new MojException.NotFound("Court not found", null));
    }

    JPAQueryFactory getJpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }
}
