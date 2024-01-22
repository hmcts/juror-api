package uk.gov.hmcts.juror.api.moj.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.moj.domain.CoronerPool;
import uk.gov.hmcts.juror.api.moj.repository.CoronerPoolRepository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * The Pool number is automatically generated when a new Coroner Pool is being requested.
 * It should be generated as a 9 character numeric String in the following format:
 * <p/>
 *     <ul>
 *     <li>Starting with a Number 9</li>
 *     <li>Pool Request Date year part (2 digits - YY)<li/>
 *     <li>Pool Request Date month part (2 digits - MM)<li/>
 *     <li>Pool Request Sequence Number for requested YY/MM (4 digits - starting at 0001)<li/>
 * </ul>
 * <p/>
 *     For example, a coroner pool requested on 19/09/2022 will have a generated
 * pool number: 922090001 (if it was the first pool to be requested for September 2022)
 * <p/>
 *     If another coroner pool request is raised for the same year and month, e.g. for 26/09/2022
 * then the last 2 digits will be incremented to indicate this is the second pool
 * generated, and the pool number will be: 922090002
 */
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Slf4j
public class GenerateCoronerPoolNumberServiceImpl implements GenerateCoronerPoolNumberService {

    private static final String SEQUENCE_START_POSITION = "0001";
    private static final String CORONER_POOL_STARTING_DIGIT = "9";

    @NonNull
    private final CoronerPoolRepository coronerPoolRepository;


    /**
     * Queries the database to find last sequence number to be used (defaults to '0001' if no records currently exist)
     * for a given year and month.
     *
     * @return Newly generated Pool Number as a numeric String
     */
    @Override
    @Transactional(readOnly = true)
    public String generateCoronerPoolNumber() {
        log.trace("Entered generateCoronerPoolNumber for Coroner pool");
        String newPoolNumber = "";

        // get the latest coroner pool record by ID (Pool number)
        Optional<CoronerPool> coronerPoolOpt = coronerPoolRepository.findFirstByOrderByPoolNumberDesc();

        if (coronerPoolOpt.isPresent()) {
            log.debug("Found latest coroner pool record");
            CoronerPool coronerPool = coronerPoolOpt.get();
            newPoolNumber = calculateNewPoolNumber(coronerPool);
        } else {
            log.debug("No Coroner pool records found, generating new pool number");
            newPoolNumber = generateNewSequenceNumber();
        }

        log.info(String.format("New Coroner Pool number generated: %s", newPoolNumber));
        return newPoolNumber;
    }

    private String calculateNewPoolNumber(CoronerPool coronerPool) {

        LocalDate currentDate = LocalDate.now();

        String latestPoolNumber = coronerPool.getPoolNumber();
        log.debug(String.format("Latest Coroner Pool Number found: %s", latestPoolNumber));

        // format is 9YYMMNNNN e.g. 923020123
        String latestYear = latestPoolNumber.substring(1, 3);
        String latestMonth = latestPoolNumber.substring(3, 5);
        String currentYear = String.valueOf(currentDate.getYear()).substring(2);
        String currentMonth = leftPadIntegerMonth(currentDate.getMonthValue());

        // check if the current pool number is within the current year and month
        if (latestYear.equals(currentYear) && latestMonth.equals(currentMonth)) {
            String latestSequenceNumber = latestPoolNumber.substring(latestPoolNumber.length() - 4);
            log.debug(String.format("Current Latest Sequence Number part: %s", latestSequenceNumber));

            // Increment the previous sequence number by one to get the new sequence number
            int newSequenceNumber = Integer.parseInt(latestSequenceNumber) + 1;

            String newPoolNumber = CORONER_POOL_STARTING_DIGIT + latestYear
                + latestMonth
                + leftPadInteger(newSequenceNumber);
            return newPoolNumber;
        }

        log.debug("Latest Coroner Pool number Year/Month is in the past, generating a new pool number");
        // generate a new pool number
        return generateNewSequenceNumber();
    }

    private String generateNewSequenceNumber() {
        StringBuilder poolNumber = new StringBuilder();
        LocalDate currentDate = LocalDate.now();

        poolNumber.append(CORONER_POOL_STARTING_DIGIT);
        // only want last two digits of year
        poolNumber.append(String.valueOf(currentDate.getYear()).substring(2));
        poolNumber.append(leftPadIntegerMonth(currentDate.getMonthValue()));
        poolNumber.append(SEQUENCE_START_POSITION);

        return poolNumber.toString();
    }

    private String leftPadIntegerMonth(int intValue) {
        if (intValue < 1 || intValue > 31) {
            throw new IllegalArgumentException("Integer value to be converted must be between 1 and 31 (inclusive)");
        }
        // Convert the value from an int to a numeric String
        String stringValue = String.valueOf(intValue);

        if (stringValue.length() == 1) {
            // for values less than 0 - 9, pad with a preceding '0'
            stringValue = "0" + stringValue;
        }

        return stringValue;
    }

    /**
     * Convert an int to a string, left padded with a '0' to ensure the length of the returned value is always 4.
     *
     * @param intValue value  to be converted to a string and padded up to 4 characters
     * @return int value
     */
    private String leftPadInteger(int intValue) {
        if (intValue < 1 || intValue > 9999) {
            throw new IllegalArgumentException("Integer value to be converted must be between 1 and 9999 (inclusive)");
        }
        // Convert the value from an int to a numeric String
        String stringValue = String.valueOf(intValue);

        if (stringValue.length() == 1) {
            // for values less than 0 - 9, pad with a preceding '000'
            stringValue = "000" + stringValue;
        } else if (stringValue.length() == 2) {
            // for values between 10 - 99, pad with a preceding '00'
            stringValue = "00" + stringValue;
        } else if (stringValue.length() == 3) {
            // for values between 100 - 999, pad with a preceding '0'
            stringValue = "0" + stringValue;
        }

        return stringValue;
    }
}
