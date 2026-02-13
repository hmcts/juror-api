-- Only clean up juror_er schema data for upload tests

DELETE FROM juror_er.file_uploads WHERE la_username LIKE 'test_%';
DELETE FROM juror_er."user" WHERE username LIKE 'test_%';
DELETE FROM juror_er.local_authority WHERE la_code IN ('001','002','003','004','005','006','007');
DELETE FROM juror_er.deadline WHERE id = 1;
