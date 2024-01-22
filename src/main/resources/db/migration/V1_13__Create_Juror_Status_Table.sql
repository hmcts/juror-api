-- create the juror status table based on the existing pool_status table in juror schema
-- updated the status to be an integer and active to be boolean
CREATE TABLE juror_mod.t_juror_status (
	status integer NOT NULL,
	status_desc varchar(30) NOT NULL,
	active boolean,
	CONSTRAINT pool_status_pkey PRIMARY KEY (status)
);

-- insert the values that exist currently
INSERT INTO juror_mod.t_juror_status (status,status_desc,active) VALUES
	 (0,'Pool',true),
	 (1,'Summoned',true),
	 (2,'Responded',true),
	 (3,'Panel',true),
	 (4,'Juror',true),
	 (5,'Excused',true),
	 (6,'Disqualified',true),
	 (7,'Deferred',true),
	 (8,'Reassigned',true),
	 (9,'Undeliverable',true),
	 (10,'Transferred',true),
	 (11,'Awaiting Info',true),
	 (12,'FailedToAttend',true);