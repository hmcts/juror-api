package uk.gov.hmcts.juror.api.moj.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;


@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class PoolMemberSequenceServiceImpl implements PoolMemberSequenceService {

    private static final int SEQUENCE_START_POSITION = 1;

    @NonNull
    private final JurorPoolRepository jurorPoolRepository;

    @Override
    @Transactional(readOnly = true)
    public int getPoolMemberSequenceNumber(String poolNumber) {

        int newPoolMemberSequenceNumber;
        try {
            log.debug(String.format("Searching for existing Jurors from pool number: %s", poolNumber));
            String latestPoolSequenceNumber = jurorPoolRepository.findLatestPoolSequence(poolNumber);

            if (latestPoolSequenceNumber != null) {
                log.debug(String.format("Records exist matching the pool number: %s", poolNumber));
                newPoolMemberSequenceNumber = getNewSequenceNumber(latestPoolSequenceNumber);
            } else {
                log.debug(String.format("No existing juror records found matching the pool number: %s", poolNumber));
                newPoolMemberSequenceNumber = SEQUENCE_START_POSITION;
            }

            log.info(String.format("New Pool member sequence number generated: %s", newPoolMemberSequenceNumber));
            return newPoolMemberSequenceNumber;

        } catch (IllegalArgumentException ex) {
            log.error(String.format(
                "An exception was thrown whilst trying to generate a new Pool member sequence number: %s",
                ex.getMessage()
            ));
            // Return the default start position to indicate the system failed to generate a pool member sequence number
            return SEQUENCE_START_POSITION;
        }
    }

    private int getNewSequenceNumber(String latestPoolSequenceNumber) {
        log.debug(String.format("Latest Pool Sequence number found: %s", latestPoolSequenceNumber));

        // Increment the previous sequence number by one to get the new sequence number
        return Integer.parseInt(latestPoolSequenceNumber) + 1;
    }

    /**
     * Convert an int to a string, left padded with a '0' to ensure the length of the returned value is always 4.
     *
     * @param intValue value  to be converted to a string and padded up to 4 characters
     * @return int value
     */
    @Override
    public String leftPadInteger(int intValue) {
        int maxLength = 4;
        String paddingChar = "0";
        if (intValue < 1 || intValue > 9999) {
            throw new IllegalArgumentException("Integer value to be converted must be between 1 and 9999 (inclusive)");
        }
        // Convert the value from an int to a numeric String, left padded with 0's
        String stringValue = String.valueOf(intValue);
        return StringUtils.leftPad(stringValue, maxLength, paddingChar);
    }

}
