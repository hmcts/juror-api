-- Create the voters table where jurors are selected from during pool creation/summoning.
-- This table replaces the voters table per court in juror schema.
CREATE TABLE juror_mod.voters
   (
    loc_code varchar(3),
    part_no varchar(9),
	register_lett varchar(5),
	poll_number varchar(5),
	new_marker varchar(1),
	title varchar(10),
	lname varchar(20) not null,
	fname varchar(20) not null,
	dob date,
	flags varchar(2),
	address varchar(35) not null,
	address2 varchar(35),
	address3 varchar(35),
	address4 varchar(35),
	address5 varchar(35),
	address6 varchar(35),
	zip varchar(10),
	date_selected1 date,
	date_selected2 date,
	date_selected3 date,
	rec_num integer,
	perm_disqual varchar(1),
	source_id varchar(1),
	CONSTRAINT VOTERS_PK PRIMARY KEY (loc_code, part_no)
   );

-- view of available voters for selecting into a pool
CREATE OR REPLACE VIEW juror_mod.loc_postcode_totals_view (loc_code, zip, total) AS
SELECT loc_code, zip, SUM(CASE WHEN date_selected1 IS NULL
                          AND perm_disqual IS NULL THEN 1 ELSE 0 END) AS total,
                      SUM(CASE WHEN date_selected1 IS NULL
                          AND perm_disqual IS NULL
                          AND flags IS NULL THEN 1 ELSE 0 END) AS total_cor
FROM juror_mod.voters WHERE date_selected1 IS null AND perm_disqual IS null
GROUP BY loc_code, zip;