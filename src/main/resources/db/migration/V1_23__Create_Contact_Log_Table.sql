-- procedure for creating new Pool_Type table, t_pool_type
CREATE TABLE juror_mod.contact_log (
    id bigserial PRIMARY KEY NOT NULL,
    juror_number VARCHAR(9) NOT NULL,
    user_id varchar(20),
    notes VARCHAR(2000),
    last_update timestamp,
    start_call timestamp,
    end_call timestamp,
    enquiry_type varchar(2),
    repeat_enquiry boolean
);

CREATE INDEX contact_log_part_no ON juror_mod.contact_log USING btree (juror_number, start_call, last_update);
