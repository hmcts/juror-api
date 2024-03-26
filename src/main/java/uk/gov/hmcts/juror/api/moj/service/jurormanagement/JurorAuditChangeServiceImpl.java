package uk.gov.hmcts.juror.api.moj.service.jurormanagement;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorNameDetailsDto;
import uk.gov.hmcts.juror.api.moj.domain.ContactLog;
import uk.gov.hmcts.juror.api.moj.domain.IContactCode;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.AbstractJurorResponse;
import uk.gov.hmcts.juror.api.moj.enumeration.ApprovalDecision;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;
import uk.gov.hmcts.juror.api.moj.repository.ContactCodeRepository;
import uk.gov.hmcts.juror.api.moj.repository.ContactLogRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.utils.RepositoryUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class JurorAuditChangeServiceImpl implements JurorAuditChangeService {
    private final ContactCodeRepository contactCodeRepository;

    private static final String EMPTY_STRING = "";
    private static final String TITLE = "title";
    private static final String FIRST_NAME = "first Name";
    private static final String LAST_NAME = "last Name";
    private static final String DATE_OF_BIRTH = "date Of Birth";
    private static final String ADDRESS = "address";
    private static final String POSTCODE = "postcode";

    @NonNull
    private final JurorHistoryRepository jurorHistoryRepository;
    @NonNull
    private final ContactLogRepository contactLogRepository;

    /**
     * Create a Map containing property names as keys and a boolean result indicating whether the value provided in the
     * summons reply is different to the value currently stored in the juror record.
     * <p/>
     * Property names are intentionally written in camelCase but with spaces separating individual words, for example,
     * the lastName property is referenced by the key "last Name" - this makes it easier to utilise a single key for
     * both property reference in code (remove space between words) and text descriptions in a more readable format
     * for audit/history records (capitalise the first letter and maintain the spaces)
     *
     * @param juror               the original juror record, to reference the existing juror details
     * @param jurorNameDetailsDto the request DTO containing the changed/updated data values
     * @return a Map    containing property names as keys and a Boolean result indicating whether the
     *                  property values differ between the original juror record and the newly provided juror
     *                  name details, true means there is a difference, false means there is no difference
     */
    @Override
    public Map<String, Boolean> initChangedPropertyMap(Juror juror,
                                                       JurorNameDetailsDto jurorNameDetailsDto) {
        // check for changes between the new/updated values and the juror record values
        Map<String, Boolean> changedPropertiesMap = new HashMap<>();

        // new title value CAN be null
        changedPropertiesMap.put(TITLE, hasTitleChanged(juror.getTitle(), jurorNameDetailsDto.getTitle()));

        changedPropertiesMap.put(FIRST_NAME, hasPropertyChanged(jurorNameDetailsDto.getFirstName(),
            juror.getFirstName()));
        changedPropertiesMap.put(LAST_NAME, hasPropertyChanged(jurorNameDetailsDto.getLastName(),
            juror.getLastName()));

        return changedPropertiesMap;
    }

    /**
     * Create a Map containing property names as keys and a boolean result indicating whether the value provided in the
     * summons reply is different to the value currently stored in the juror record.
     * <p/>
     * Property names are intentionally written in camelCase but with spaces separating individual words, for example,
     * the dateOfBirth property is referenced by the key "date Of Birth" - this makes it easier to utilise a single key
     * for both property reference in code (remove space between words) and text descriptions in a more readable format
     * for audit/history records (capitalise the first letter and maintain the spaces)
     *
     * @param juror         the original juror record, to reference the existing juror details
     * @param jurorResponse the juror response (Paper/Digital), to reference the newly provided juror
     *                      details from a summons reply
     * @return a Map    containing juror response property names as keys and a Boolean result indicating whether the
     *                  property values differ between the original juror record and the new juror summons reply, true
     *                  means there is a difference, false means there is no difference
     */
    @Override
    public Map<String, Boolean> initChangedPropertyMap(Juror juror,
                                                       AbstractJurorResponse jurorResponse) {
        // check for changes between the new/updated values and the juror record values
        Map<String, Boolean> changedPropertiesMap = new HashMap<>();

        // new title value CAN be null
        changedPropertiesMap.put(TITLE, hasTitleChanged(juror.getTitle(), jurorResponse.getTitle())
            && !hasNameChanged(jurorResponse.getFirstName(), juror.getFirstName(),
            jurorResponse.getLastName(), juror.getLastName()));

        LocalDate originalDate = setOriginalDateOfBirth(juror.getDateOfBirth());
        changedPropertiesMap.put(DATE_OF_BIRTH, hasPropertyChanged(jurorResponse.getDateOfBirth(),
            originalDate));

        changedPropertiesMap.put(ADDRESS, hasAddressChanged(jurorResponse, juror));
        changedPropertiesMap.put(POSTCODE, hasPropertyChanged(jurorResponse.getPostcode(),
            juror.getPostcode()));

        return changedPropertiesMap;
    }

    /**
     * Detect changes between the existing juror record Title value and the newly requested Title value
     * New Title values can be null.
     *
     * @param updatedTitle  newly provided value to check
     * @param originalTitle existing value on the juror record
     * @return true if the title value has changed, false if it is the same
     */
    @Override
    public boolean hasTitleChanged(String updatedTitle, String originalTitle) {
        return (originalTitle != null && updatedTitle == null)
            || hasPropertyChanged(updatedTitle, originalTitle);
    }

    /**
     * Detect changes between the existing juror record first name/last name and the newly requested first name/last
     * name.
     *
     * @param updatedFirstName  newly provided first name value to check
     * @param originalFirstname existing first name value on the juror record
     * @param updatedLastName   newly provided last name value to check
     * @param originalLastname  existing last name value on the juror record
     * @return true if either part of the juror's name has changed, false if it is the same
     */
    @Override
    public boolean hasNameChanged(String updatedFirstName, String originalFirstname,
                                  String updatedLastName, String originalLastname) {
        return hasPropertyChanged(updatedFirstName, originalFirstname)
            || hasPropertyChanged(updatedLastName, originalLastname);
    }

    @Override
    @Transactional
    public void recordApprovalHistoryEvent(String jurorNumber, ApprovalDecision approvalDecision,
                                           String auditorUsername, String poolNumber) {
        log.trace("Juror: {}. Enter recordApprovalHistoryEvent", jurorNumber);

        JurorHistory jurorHistory = JurorHistory.builder()
            .jurorNumber(jurorNumber)
            .poolNumber(poolNumber)
            .historyCode(HistoryCodeMod.CHANGE_PERSONAL_DETAILS)
            .otherInformation("Name change " + approvalDecision.getDescription())
            .createdBy(auditorUsername)
            .build();

        jurorHistoryRepository.save(jurorHistory);
        log.trace("Juror: {}. Exit recordApprovalHistoryEvent", jurorNumber);
    }

    @Override
    @Transactional
    public void recordContactLog(Juror juror, String auditorUsername,
                                 String contactEnquiryCode, String notes) {
        log.trace("Juror: {}. Enter recordContactLog", juror.getJurorNumber());

        ContactLog contactLog = ContactLog.builder()
            .username(auditorUsername)
            .jurorNumber(juror.getJurorNumber())
            .startCall(LocalDateTime.now())
            .enquiryType(RepositoryUtils.retrieveFromDatabase(
                IContactCode.fromCode(contactEnquiryCode).getCode(),
                contactCodeRepository))
            .notes(notes)
            .repeatEnquiry(false)
            .build();

        contactLogRepository.saveAndFlush(contactLog);
        log.trace("Juror: {}. Exit recordContactLog", juror.getJurorNumber());
    }

    /**
     * Create a history event for the change of personal details (name, date of birth or address).
     *
     * @param propertyName    Description of the juror record property that is being updated, e.g. first name
     * @param juror           the juror record that is being updated
     * @param poolNumber      the pool number the juror is currently associated with, when the change is being recorded
     * @param auditorUsername the username of the current agent/officer actioning the change of juror details
     */
    @Override
    public void recordPersonalDetailsHistory(String propertyName, Juror juror, String poolNumber,
                                             String auditorUsername) {
        JurorHistory jurorHistory = JurorHistory.builder()
            .jurorNumber(juror.getJurorNumber())
            .poolNumber(poolNumber)
            .dateCreated(LocalDateTime.now())
            .historyCode(HistoryCodeMod.CHANGE_PERSONAL_DETAILS)
            .otherInformation(StringUtils.capitalize(propertyName) + " Changed")
            .createdBy(auditorUsername)
            .build();

        jurorHistoryRepository.save(jurorHistory);
    }

    /**
     * Detect changes between the existing juror record's address values and the newly requested address details.
     *
     * @param updatedDetails Newly requested juror details supplied via digital or paper summons replies
     * @param juror          existing pool member record to check changes against
     * @return true if any part of the juror's address has changed, false if it is the same
     */

    private boolean hasAddressChanged(AbstractJurorResponse updatedDetails, Juror juror) {
        String newAddress = formatForConcat(updatedDetails.getAddressLine1())
            + formatForConcat(updatedDetails.getAddressLine2())
            + formatForConcat(updatedDetails.getAddressLine3())
            + formatForConcat(updatedDetails.getAddressLine4())
            + formatForConcat(updatedDetails.getAddressLine5());

        String oldAddress = formatForConcat(juror.getAddressLine1())
            + formatForConcat(juror.getAddressLine2())
            + formatForConcat(juror.getAddressLine3())
            + formatForConcat(juror.getAddressLine4())
            + formatForConcat(juror.getAddressLine5());

        return oldAddress.compareToIgnoreCase(newAddress) != 0;
    }

    /**
     * In most cases the date of birth field will be empty (null) when the Juror record was initially created from
     * the voters table - the juror record will create a new revision when the date of birth is changed, in the
     * event of the initial assignment (from null to a date value) a default date is used 1901-01-01 (YYYY-MM-DD) for
     * comparison (to avoid a null pointer exception).
     *
     * @return LocalDate    object with either the Juror's date of birth or a default date value to use for change
     *                      comparison
     */
    private LocalDate setOriginalDateOfBirth(LocalDate jurorDob) {
        final LocalDate defaultNullReplacementDob = LocalDate.of(1901, 1, 1);
        return jurorDob != null ? jurorDob : defaultNullReplacementDob;
    }

    private boolean hasPropertyChanged(String updatedValue, String originalValue) {
        if (updatedValue != null) {
            if (originalValue != null) {
                return updatedValue.compareTo(originalValue) != 0;
            } else {
                return true;
            }
        }
        return false;
    }

    private boolean hasPropertyChanged(LocalDate updatedValue, LocalDate originalValue) {
        if (updatedValue != null) {
            if (originalValue != null) {
                return !updatedValue.isEqual(originalValue);
            } else {
                return true;
            }
        }
        return false;
    }

    private String formatForConcat(String property) {
        return Objects.toString(property, EMPTY_STRING).trim();
    }

}
