alter table juror_mod.appearance
    add column appearance_confirmed boolean default false not null;

update juror_mod.appearance
    set appearance_confirmed = true
    where no_show = true or appearance_stage in ('EXPENSE_ENTERED','EXPENSE_AUTHORISED','EXPENSE_EDITED');