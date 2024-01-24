CREATE TABLE juror_trial (
    loc_code varchar(3),
    juror_number varchar(9) NOT NULL,
    trial_number varchar(16),
    pool_number varchar(9) NOT NULL,
    rand_number bigint,
    date_selected timestamp NOT NULL, -- date created
    result varchar(2), -- enum
    completed boolean
);