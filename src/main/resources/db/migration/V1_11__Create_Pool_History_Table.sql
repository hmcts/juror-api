-- procedure for creating new Pool_Type table, t_pool_type
CREATE TABLE juror_mod.pool_history (
    id bigserial PRIMARY KEY NOT NULL,
    history_code VARCHAR(4) NOT NULL,
    pool_no VARCHAR(9) NOT NULL,
    history_date TIMESTAMP,
    user_id VARCHAR(20),
    other_information VARCHAR(50)
);

CREATE INDEX pool_history_pool_idx ON juror_mod.pool_history USING btree (pool_no);

ALTER TABLE juror_mod.pool_history ADD CONSTRAINT pool_history_fk FOREIGN KEY (history_code) REFERENCES juror_mod.t_history_code;

