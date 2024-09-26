package uk.gov.hmcts.juror.api.moj.utils;

import uk.gov.service.notify.NotificationClientException;

public final class NotifyUtil {

    private NotifyUtil() {

    }

    public static boolean isInvalidPhoneNumberError(Throwable e) {
        return doesMessageContain(e, "phone_number is a required property")
            || doesMessageContain(e, "InvalidPhoneError");
    }

    public static boolean isInvalidEmailAddressError(Throwable e) {
        return doesMessageContain(e, "email_address is a required property")
            || doesMessageContain(e, "InvalidEmailAddressError");
    }

    public static boolean doesMessageContain(Throwable e, String message) {
        if (e instanceof NotificationClientException notificationClientException) {
            return notificationClientException.getMessage() != null
                && notificationClientException.getMessage().contains(message);
        }
        return false;
    }
}
