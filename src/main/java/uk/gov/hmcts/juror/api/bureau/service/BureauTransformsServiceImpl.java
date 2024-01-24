package uk.gov.hmcts.juror.api.bureau.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import uk.gov.hmcts.juror.api.bureau.controller.response.BureauResponseSummaryDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.BureauResponseSummaryWrapper;
import uk.gov.hmcts.juror.api.bureau.controller.response.StaffDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.TeamDto;
import uk.gov.hmcts.juror.api.bureau.domain.BureauJurorDetail;
import uk.gov.hmcts.juror.api.bureau.domain.Team;
import uk.gov.hmcts.juror.api.moj.domain.User;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


/**
 * Service to perform data transform operations needed by multiple service classes.
 */
@Service
public class BureauTransformsServiceImpl implements BureauTransformsService {

    private final UrgencyService urgencyCalculator;

    @Autowired
    public BureauTransformsServiceImpl(UrgencyService urgencyCalculator) {
        Assert.notNull(urgencyCalculator, "UrgencyService must not be null");
        this.urgencyCalculator = urgencyCalculator;
    }

    @Override
    public List<BureauResponseSummaryDto> convertToDtos(Iterable<BureauJurorDetail> details) {
        return StreamSupport.stream(details.spliterator(), false)
            .map(urgencyCalculator::flagSlaOverdueForResponse)
            .map(this::detailToDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    public BureauResponseSummaryWrapper prepareOutput(Iterable<BureauJurorDetail> details) {
        return BureauResponseSummaryWrapper.builder().responses(convertToDtos(details)).build();
    }

    @Override
    public BureauResponseSummaryDto detailToDto(BureauJurorDetail detail) {
        return BureauResponseSummaryDto.builder()
            .jurorNumber(detail.getJurorNumber())
            .title(detail.getNewTitle())
            .firstName(detail.getNewFirstName())
            .lastName(detail.getNewLastName())
            .courtCode(detail.getCourtCode())
            .courtName(detail.getCourtName())
            .postcode(detail.getNewJurorPostcode())
            .processingStatus(detail.getProcessingStatus())
            .residency(detail.getResidency())
            .mentalHealthAct(detail.getMentalHealthAct())
            .bail(detail.getBail())
            .convictions(detail.getConvictions())
            .deferralDate(detail.getDeferralDate())
            .excusalReason(detail.getExcusalReason())
            .poolNumber(detail.getPoolNumber())
            .urgent(detail.getUrgent())
            .superUrgent(detail.getSuperUrgent())
            .slaOverdue(detail.getSlaOverdue())
            .dateReceived(detail.getDateReceived())
            .assignedStaffMember(detail.getAssignedStaffMember() != null
                ?
                toStaffDto(detail.getAssignedStaffMember())
                :
                null)
            .completedAt(detail.getCompletedAt())
            .version(detail.getVersion())
            .build();
    }

    @Override
    public StaffDto toStaffDto(User staffMember) {

        if (staffMember == null) {
            return null;
        }

        return StaffDto.builder().login(staffMember.getUsername())
            .name(staffMember.getName())
            .team(toTeamDto(staffMember.getTeam()))
            .isActive(staffMember.isActive())
            .isTeamLeader(staffMember.isTeamLeader())
            .version(staffMember.getVersion())
            .court1(staffMember.getCourtAtIndex(0, null))
            .court2(staffMember.getCourtAtIndex(1, null))
            .court3(staffMember.getCourtAtIndex(2, null))
            .court4(staffMember.getCourtAtIndex(3, null))
            .court5(staffMember.getCourtAtIndex(4, null))
            .court6(staffMember.getCourtAtIndex(5, null))
            .court7(staffMember.getCourtAtIndex(6, null))
            .court8(staffMember.getCourtAtIndex(7, null))
            .court9(staffMember.getCourtAtIndex(8, null))
            .court10(staffMember.getCourtAtIndex(9, null))
            .build();
    }

    @Override
    public TeamDto toTeamDto(Team teamEntity) {

        if (teamEntity == null) {
            return null;
        }
        return TeamDto.builder()
            .id(teamEntity.getId())
            .name(teamEntity.getTeamName())
            .version(teamEntity.getVersion())
            .build();
    }
}
