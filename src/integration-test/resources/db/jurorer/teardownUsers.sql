
-- delete from juror_er."user"
-- where username like 'test_%';

-- delete from juror_er.local_authority
-- where la_code in ('001','002','003','004','005','006','007');


delete from juror_er.file_uploads
where la_username like 'test_%';

delete from juror_er."user"
where username like 'test_%';

delete from juror_er.local_authority
where la_code in ('001','002','003','004','005','006','007');
