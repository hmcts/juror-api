delete from juror_mod.t_history_code where history_code = 'RFTA' ;

INSERT INTO juror_mod.t_history_code (history_code,description) VALUES
	 ('RFTA','Failed To Attend Letter'),
	 ('RSHC','Show Cause Letter');