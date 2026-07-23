-- Create Digital by Default response stats table

CREATE TABLE juror_dashboard.dbd_response_stats (
    id              bigserial PRIMARY KEY,
    summons_date    timestamp(0) NOT NULL,
    response_date   timestamp(0),                    -- nullable: null = not-responded juror
    response_period varchar(15) NOT NULL,
    loc_code        varchar(3) NOT NULL,
    response_method varchar(13) NOT NULL,
    age_group       varchar(12) NOT NULL,
    juror_count     int4 DEFAULT 0 NOT NULL,

    CONSTRAINT dbd_response_stats_uq
        UNIQUE (summons_date, response_date, response_period, loc_code, response_method, age_group),

    CONSTRAINT dbd_response_stats_loc_code_fk
        FOREIGN KEY (loc_code) REFERENCES juror_mod.court_location(loc_code)
);
