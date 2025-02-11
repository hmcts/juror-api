-- Add new Delete Attendance code when a jurors appearance record is deleted

INSERT INTO juror_mod.t_history_code (history_code,description,"template") VALUES
	 ('DELA','Attendance Deleted',NULL);
