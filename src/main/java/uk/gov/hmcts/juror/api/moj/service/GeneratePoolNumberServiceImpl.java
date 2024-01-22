package uk.gov.hmcts.juror.api.moj.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;

import java.time.LocalDate;

/**
 * The Pool number is automatically generated when a new Pool is being requested (it can only be edited by the
 * Bureau when they raise a pool request on behalf of a court). It should be generated as a 9 character numeric
 * String in the following format:
 * <p/><ul>
 *     <li>Court Location Code (3 digits)<li/>
 *     <li>Pool Request Date year part (2 digits - YY)<li/>
 *     <li>Pool Request Date month part (2 digits - MM)<li/>
 *     <li>Pool Request Sequence Number for requested YY/MM (2 digits - starting at 01)<li/>
 * </ul>
 * <p/>For example, a pool requested for Redditch County Court (location code 797) for 19/09/2022 will have a generated
 * pool number: 797220901 (if it was the first pool to be requested at Redditch for September 2022)
 * <p/>If another pool request is raised for the same court for the same year and month, e.g. Redditch County Court
 * (location code 797) for 26/09/2022 then the last 2 digits will be incremented to indicate this is the second pool
 * generated, and the pool number will be: 797220902
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class GeneratePoolNumberServiceImpl implements GeneratePoolNumberService {

    private static final String SEQUENCE_START_POSITION = "01";

    @NonNull
    private final PoolRequestRepository poolRequestRepository;

    /**
     * Queries the database to find existing records for a given court location and attendance date - specifically the
     * Year and Month parts of the date. Then, generate a new Pool Number based on location code, attendance date year,
     * attendance date month and the last sequence number to be used (defaults to "01" if no records currently exist)
     *
     * @param locationCode   3-digit numeric string to uniquely identify an individual court location
     * @param attendanceDate the date this pool has been requested for and when the jurors are expected to
     *                       first attend court
     * @return Newly generated Pool Number as a numeric String
     */
    @Override
    @Transactional(readOnly = true)
    public String generatePoolNumber(String locationCode, LocalDate attendanceDate) {
        log.trace(String.format("Enter generatePoolNumber for Location Code %s and Attendance Date %s",
            locationCode, attendanceDate
        ));
        String newPoolNumber;
        try {
            String poolNumberPrefix = buildPoolNumberPrefix(locationCode, attendanceDate);
            PoolRequest poolRequest = findMatchingPoolRequests(poolNumberPrefix);

            if (poolRequest != null) {
                log.debug(String.format("Records exist matching the prefix: %s", poolNumberPrefix));
                String newSequenceNumber = calculateNewSequenceNumber(poolRequest);
                newPoolNumber = poolNumberPrefix + newSequenceNumber;
            } else {
                log.debug(String.format("No existing records found matching the prefix: %s", poolNumberPrefix));
                newPoolNumber = poolNumberPrefix + SEQUENCE_START_POSITION;
            }

            log.info(String.format("New Pool number generated: %s", newPoolNumber));
            return newPoolNumber;
        } catch (IllegalArgumentException ex) {
            log.error(String.format(
                "An exception was thrown whilst trying to generate a new Pool Number: %s",
                ex.getMessage()
            ));
            // Return an empty string to indicate the system failed to generate a pool number
            return "";
        }
    }

    private String buildPoolNumberPrefix(String locationCode, LocalDate attendanceDate) {
        String attendanceMonth = leftPadInteger(attendanceDate.getMonthValue());
        String attendanceYear = String.valueOf(attendanceDate.getYear());
        attendanceYear = attendanceYear.substring(attendanceYear.length() - 2);
        return locationCode + attendanceYear + attendanceMonth;
    }

    /**
     * Query the database to find existing pools created for the same court, attendance month and year.
     *
     * @param poolNumberPrefix The first 7 characters of a Pool Number containing the Court Location Code,
     *                         Attendance Date Year (YY) and Attendance Date Month (MM)
     * @return A Pool Request with the current highest sequence number for a given court location and attendance date.
     *      If no Pool Requests exist matching the prefix, the returned object will be null.
     */
    private PoolRequest findMatchingPoolRequests(String poolNumberPrefix) {
        log.debug(String.format("Searching for existing Pool Numbers matching the prefix: %s", poolNumberPrefix));
        return poolRequestRepository.findLatestPoolRequestByPoolNumberPrefix(poolNumberPrefix);
    }

    private String calculateNewSequenceNumber(PoolRequest poolRequest) {
        String latestPoolNumber = poolRequest.getPoolNumber();
        log.debug(String.format("Latest Pool Number found: %s", latestPoolNumber));

        String latestSequenceNumber = latestPoolNumber.substring(latestPoolNumber.length() - 2);
        log.debug(String.format("Latest Sequence Number part: %s", latestSequenceNumber));

        // Increment the previous sequence number by one to get the new sequence number
        int newSequenceNumber = Integer.parseInt(latestSequenceNumber) + 1;

        return leftPadInteger(newSequenceNumber);
    }

    /**
     * Convert an int to a string, left padded with a '0' to ensure the length of the returned value is always 2.
     *
     * @param intValue value  to be converted to a string and padded up to 2 characters
     * @return int value
     */
    private String leftPadInteger(int intValue) {
        if (intValue < 1 || intValue > 99) {
            throw new IllegalArgumentException("Integer value to be converted must be between 1 and 99 (inclusive)");
        }
        // Convert the value from an int to a numeric String
        String stringValue = String.valueOf(intValue);

        // for values less than 10, pad with a preceding '0'
        if (stringValue.length() < 2) {
            stringValue = "0" + stringValue;
        }

        return stringValue;
    }
}
