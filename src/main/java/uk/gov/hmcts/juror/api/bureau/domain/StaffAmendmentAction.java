package uk.gov.hmcts.juror.api.bureau.domain;

/**
 * Actions performed on {@link Staff} record.
 *
 * @implNote        See JUROR_DIGITAL.STAFF_AUDIT.action column for maximum length when mapped with
 *                  {@link jakarta.persistence.EnumType#STRING}!
 */
public enum StaffAmendmentAction {
    CREATE,
    EDIT
}
