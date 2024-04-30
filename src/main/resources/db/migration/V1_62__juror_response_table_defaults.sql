update juror_mod.juror_response
set urgent = false
where urgent is null;

update juror_mod.juror_response
set super_urgent = false
where super_urgent is null;

alter table juror_mod.juror_response
    alter column urgent set default false,
    alter column urgent set not null,
    alter column super_urgent set default false,
    alter column super_urgent set not null
;