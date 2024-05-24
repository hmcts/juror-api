CREATE TABLE juror_mod.hk_run_log (
	seq_id BIGSERIAL NOT NULL,
	start_time timestamp(0) NULL,
	end_time timestamp(0) NULL,
	jurors_deleted numeric(38) NULL,
	jurors_error numeric(38) NULL,
	CONSTRAINT pk_hk_un_log PRIMARY KEY (seq_id)
);


CREATE TABLE juror_mod.hk_audit (
	seq_id BIGSERIAL NOT NULL,
	juror_number varchar(9) not null, 
	selected_date timestamp null,
	deletion_date date null,
	deletion_summary text null,
	CONSTRAINT pk_hk_audit PRIMARY KEY (seq_id)
);


CREATE TABLE juror_mod.hk_params (
	"key" numeric(38) NOT NULL,
	value varchar(20) NULL,
	description varchar(60) NULL,
	last_updated timestamp(0) NOT NULL,
	CONSTRAINT pk_hk_params_key PRIMARY KEY (key)
);

-- defaults taken from hk.hk_params
INSERT INTO juror_mod.hk_params("key",value,description,last_updated)
VALUES (1,2557,'Court data age threshold in days',NOW());
INSERT INTO juror_mod.hk_params("key",value,description,last_updated)
VALUES (2,2557,'Bureau data age threshold in days',NOW());
INSERT INTO juror_mod.hk_params("key",value,description,last_updated)
VALUES (3,60000,'Maximum Juror deletions allowed in days',NOW());
INSERT INTO juror_mod.hk_params("key",value,description,last_updated)
VALUES (4,3652,'Audit log age threshold',NOW());
INSERT INTO juror_mod.hk_params("key",value,description,last_updated)
VALUES (5,365,'Digital data age threshold in days',NOW());

