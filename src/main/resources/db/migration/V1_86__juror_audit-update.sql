ALTER TABLE juror_mod.juror
    add column last_modified_by varchar(20),
    add constraint juror_last_modified_by_fk foreign key (last_modified_by) references juror_mod.users (username);

ALTER TABLE juror_mod.juror_audit
    add column last_modified_by varchar(20),
    add column last_update timestamp,
    add constraint juror_last_modified_by_fk foreign key (last_modified_by) references juror_mod.users (username);

CREATE INDEX juror_audit_juror_number_idx ON juror_mod.juror_audit (juror_number,last_update);
CREATE INDEX juror_audit_last_update_idx ON juror_mod.juror_audit (last_update,juror_number);