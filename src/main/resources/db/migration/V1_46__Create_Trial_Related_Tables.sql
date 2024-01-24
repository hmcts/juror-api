-- juror_mod.judge definition
CREATE TABLE juror_mod.judge (
	id bigserial NOT NULL,
	"owner" varchar(3) NOT NULL,
	code varchar(4) NOT NULL,
	description varchar(30) NOT NULL,
	telephone_number varchar(16) NULL,
	CONSTRAINT judge_pk PRIMARY KEY (id)
);

-- juror_mod.courtroom definition
CREATE TABLE juror_mod.courtroom (
	id bigserial NOT NULL,
	"owner" varchar(3) NOT NULL,
	room_number varchar(6) NOT NULL,
	description varchar(30) NOT NULL,
	CONSTRAINT courtroom_pk PRIMARY KEY (id)
);

-- juror_mod.trial definition
CREATE TABLE juror_mod.trial (
	trial_number varchar(16) NOT NULL,
	loc_code varchar(3) NOT NULL,
	description varchar(50) NOT NULL,
	courtroom bigint NOT NULL,
	judge bigint NOT NULL,
	trial_type varchar(3) NOT NULL,
	trial_start_date date NULL,
	trial_end_date date NULL,
	anonymous bool NULL,
	CONSTRAINT trial_pkey PRIMARY KEY (trial_number, loc_code),
	CONSTRAINT trial_court_loc_fk FOREIGN KEY (loc_code) REFERENCES juror_mod.court_location(loc_code),
	CONSTRAINT trial_courtroom_fk FOREIGN KEY (courtroom) REFERENCES juror_mod.courtroom(id),
	CONSTRAINT trial_judge_fk FOREIGN KEY (judge) REFERENCES juror_mod.judge(id),
    CONSTRAINT trial_type_val CHECK ((trial_type) = ANY ((ARRAY['CIV', 'CRI'])))
);