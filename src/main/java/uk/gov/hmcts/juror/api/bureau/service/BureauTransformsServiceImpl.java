package uk.gov.hmcts.juror.api.bureau.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import uk.gov.hmcts.juror.api.bureau.controller.response.BureauResponseSummaryDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.BureauResponseSummaryWrapper;
import uk.gov.hmcts.juror.api.bureau.controller.response.StaffDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.TeamDto;
import uk.gov.hmcts.juror.api.bureau.domain.Team;
import uk.gov.hmcts.juror.api.moj.domain.ModJurorDetail;
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
    public List<BureauResponseSummaryDto> convertToDtos(Iterable<ModJurorDetail> details) {
        return StreamSupport.stream(details.spliterator(), false)
           // .map(urgencyCalculator::flagSlaOverdueForResponse)
            .map(this::detailToDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    public BureauResponseSummaryWrapper prepareOutput(Iterable<ModJurorDetail> details) {
        return BureauResponseSummaryWrapper.builder().responses(convertToDtos(details)).build();
    }

    @Override
    public BureauResponseSummaryDto detailToDto(ModJurorDetail detail) {
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
            .replyMethod(detail.getReplyType())
            .urgent(detail.getUrgent())
            .superUrgent(detail.getSuperUrgent())
            .slaOverdue(detail.getSlaOverdue())
            .dateReceived(detail.getDateReceived())
            .assignedStaffMember(detail.getAssignedStaffMember() != null
                ? toStaffDto(detail.getAssignedStaffMember()) : null)
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
