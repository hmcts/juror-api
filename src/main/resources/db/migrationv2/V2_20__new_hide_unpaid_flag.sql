alter table juror_mod.appearance
    add column hide_on_unpaid_expense_and_reports bool default false NOT NULL;


update juror_mod.appearance
    set hide_on_unpaid_expense_and_reports = true,
    appearance_stage = 'EXPENSE_ENTERED'
    where is_draft_expense = true
    and appearance_stage = 'EXPENSE_AUTHORISED';