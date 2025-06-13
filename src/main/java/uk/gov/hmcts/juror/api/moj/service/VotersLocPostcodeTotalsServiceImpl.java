package uk.gov.hmcts.juror.api.moj.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.moj.domain.VotersLocPostcodeTotals;
import uk.gov.hmcts.juror.api.moj.repository.VotersLocPostcodeTotalsRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class VotersLocPostcodeTotalsServiceImpl implements VotersLocPostcodeTotalsService {

    @NonNull
    private final VotersLocPostcodeTotalsRepository votersLocPostcodeTotalsRepository;

    @Override
    public List<VotersLocPostcodeTotals.CourtCatchmentSummaryItem> getCourtCatchmentSummaryItems(
        String locationCode, boolean isCoronersPool) {

        List<VotersLocPostcodeTotals.CourtCatchmentSummaryItem> courtCatchmentItemsList = new ArrayList<>();

        List<VotersLocPostcodeTotals> votersLocPostcodeTotalsList = votersLocPostcodeTotalsRepository.findByLocCode(
            locationCode);

        if (votersLocPostcodeTotalsList.isEmpty()) {
            log.error("No Court Catchment records found for given Location code {}", locationCode);
            return courtCatchmentItemsList;
        }

        Map<String, Integer> postcodeTotals = new HashMap<>();
        final List<Integer> totalList = new ArrayList<>(List.of(0));

        for (VotersLocPostcodeTotals locPostcodeTotals : votersLocPostcodeTotalsList) {

            String postcode = locPostcodeTotals.getPostcode(); //get first part of postcode

            if (!isCoronersPool) {
                totalList.set(0, locPostcodeTotals.getTotal());

            } else {
                totalList.set(0, locPostcodeTotals.getTotalCor());
            }
            postcodeTotals.computeIfPresent(postcode, (key, val) -> val + totalList.get(0));
            postcodeTotals.putIfAbsent(postcode, totalList.get(0));
        }

        postcodeTotals.forEach((key, val) -> courtCatchmentItemsList.add(
            new VotersLocPostcodeTotals.CourtCatchmentSummaryItem(key, val)));

        return courtCatchmentItemsList;
    }
}
