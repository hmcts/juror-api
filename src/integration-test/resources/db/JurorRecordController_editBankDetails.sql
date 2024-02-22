-- create a pool record owned by 415

--add jurors
INSERT INTO juror_mod.juror (juror_number,poll_number,last_name,first_name,dob,address_line_1,address_line_2,postcode,responded,user_edtq,no_def_pos,notifications,notes) VALUES
	 ('123456789','21112','Lname','Fname','1984-07-24 00:00:00','4 Knutson Trail','Scotland','AB3 9RY','N',NULL,NULL,
	 0,NULL);