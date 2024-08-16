package uk.gov.hmcts.juror.api.moj.enumeration;

import java.util.EnumSet;
import java.util.Set;

public enum AppearanceStage {
    CHECKED_IN,
    CHECKED_OUT,
    EXPENSE_ENTERED,
    EXPENSE_AUTHORISED,
    EXPENSE_EDITED;


    public static Set<AppearanceStage> getConfirmedAppearanceStages() {
        return EnumSet.of(AppearanceStage.EXPENSE_ENTERED,
            AppearanceStage.EXPENSE_AUTHORISED,
            AppearanceStage.EXPENSE_EDITED);
    }
}
