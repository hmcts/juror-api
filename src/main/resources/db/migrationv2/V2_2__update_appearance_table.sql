alter table juror_mod.appearance
    add column appearance_confirmed boolean default false not null;

update juror_mod.appearance a
    set a.appearance_confirmed = true
    where a.no_show = true or a.appearance_stage in ('EXPENSE_ENTERED','EXPENSE_AUTHORISED','EXPENSE_EDITED');